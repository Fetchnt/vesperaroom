package co.edu.unbosque.vesperaroom.repository;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import org.springframework.stereotype.Repository;

import co.edu.unbosque.vesperaroom.entity.Reserva;
import co.edu.unbosque.vesperaroom.entity.Reserva.Estado;
import co.edu.unbosque.vesperaroom.entity.ReservaServicio;

/**
 * Repositorio para la gestión de reservas en la tabla {@code reserva}, implementado
 * con JDBC puro: cada método obtiene una {@link Connection} del
 * {@link DataSource}, prepara la sentencia SQL con {@link PreparedStatement},
 * asigna los parámetros, la ejecuta y recorre el {@link ResultSet}. Los
 * recursos se cierran con try-with-resources. También
 * administra la tabla intermedia {@code reserva_servicio}, que se inserta en la
 * misma transacción que la reserva.
 */
@Repository
public class ReservaRepository {

	private static final String SELECT_BASE = "SELECT r.id_reserva, r.codigo_reserva, r.id_usuario, "
			+ "r.id_habitacion, r.id_tarifa, r.cantidad_personas, r.fecha_creacion, "
			+ "r.fecha_hora_inicio, r.fecha_hora_fin, r.estado, r.total, r.fecha_cancelacion, "
			+ "r.monto_reembolso, r.fecha_limite_reembolso FROM reserva r ";

	private static final String SELECT_TODOS = SELECT_BASE + "ORDER BY r.fecha_hora_inicio DESC";

	private static final String SELECT_POR_ID = SELECT_BASE + "WHERE r.id_reserva = ?";

	private static final String SELECT_POR_USUARIO = SELECT_BASE + "WHERE r.id_usuario = ? ORDER BY "
			+ "r.fecha_hora_inicio DESC";

	private static final String SELECT_POR_MOTEL = SELECT_BASE + "JOIN habitacion h ON "
			+ "h.id_habitacion = r.id_habitacion WHERE h.id_motel = ? ORDER BY r.fecha_hora_inicio "
			+ "DESC";

	private static final String SELECT_SERVICIOS = "SELECT id_reserva, id_servicio, cantidad, "
			+ "precio_unitario FROM reserva_servicio WHERE id_reserva = ?";

	private static final String CONTAR_SOLAPAMIENTOS = "SELECT COUNT(*) FROM reserva WHERE "
			+ "id_habitacion = ? AND estado IN ('PENDIENTE', 'CONFIRMADA', 'EN_CURSO') AND "
			+ "fecha_hora_inicio < ? AND fecha_hora_fin > ?";

	private static final String BLOQUEAR_HABITACION = "SELECT id_habitacion FROM habitacion WHERE "
			+ "id_habitacion = ? FOR UPDATE";

	private static final String INSERT = "INSERT INTO reserva (codigo_reserva, id_usuario, "
			+ "id_habitacion, id_tarifa, cantidad_personas, fecha_creacion, fecha_hora_inicio, "
			+ "fecha_hora_fin, estado, total) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

	private static final String INSERT_SERVICIO = "INSERT INTO reserva_servicio (id_reserva, "
			+ "id_servicio, cantidad, precio_unitario) VALUES (?, ?, ?, ?)";

	private static final String UPDATE_ESTADO = "UPDATE reserva SET estado = ? WHERE id_reserva = ?";

	private static final String CANCELAR = "UPDATE reserva SET estado = 'CANCELADA', fecha_cancelacion = ?, "
			+ "monto_reembolso = ?, fecha_limite_reembolso = ? WHERE id_reserva = ? AND estado IN "
			+ "('PENDIENTE', 'CONFIRMADA')";

	private static final String RECHAZAR_PAGOS_PENDIENTES = "UPDATE pago SET estado = 'RECHAZADO' "
			+ "WHERE id_reserva = ? AND estado = 'PENDIENTE'";

	private static final String SELECT_PENDIENTES_VENCIDAS = "SELECT id_reserva FROM reserva WHERE "
			+ "estado = 'PENDIENTE' AND fecha_hora_inicio <= ?";

	private static final String MARCAR_NO_SHOW = "UPDATE reserva r JOIN habitacion h ON "
			+ "h.id_habitacion = r.id_habitacion LEFT JOIN politica_reserva p ON p.id_motel = h.id_motel "
			+ "SET r.estado = 'NO_SHOW' WHERE r.estado = 'CONFIRMADA' AND DATE_ADD(r.fecha_hora_inicio, "
			+ "INTERVAL COALESCE(p.tiempo_gracia_no_show_min, 0) MINUTE) <= ?";

	private static final String FINALIZAR_VENCIDAS = "UPDATE reserva SET estado = 'FINALIZADA' WHERE "
			+ "estado = 'EN_CURSO' AND fecha_hora_fin <= ?";

	private static final String DELETE = "DELETE FROM reserva WHERE id_reserva = ?";

	private final DataSource dataSource;

	public ReservaRepository(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * Retorna todas las reservas.
	 *
	 * @return lista con todas las reservas
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public List<Reserva> findAll() throws SQLException {
		return listar(SELECT_TODOS);
	}

	/**
	 * Busca una reserva por su identificador.
	 *
	 * @param id identificador de la reserva
	 * @return {@link Optional} con la reserva encontrada, o vacío si no existe
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public Optional<Reserva> findById(Long id) throws SQLException {
		try (Connection con = dataSource.getConnection();
				PreparedStatement ps = con.prepareStatement(SELECT_POR_ID)) {
			ps.setLong(1, id);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return Optional.of(mapear(rs));
				}
				return Optional.empty();
			}
		}
	}

	/**
	 * Retorna las reservas de un cliente, de la más reciente a la más antigua.
	 *
	 * @param idUsuario identificador del cliente
	 * @return lista de reservas del cliente
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public List<Reserva> findByUsuario(Long idUsuario) throws SQLException {
		return listar(SELECT_POR_USUARIO, idUsuario);
	}

	/**
	 * Retorna las reservas de todas las habitaciones de un motel.
	 *
	 * @param idMotel identificador del motel
	 * @return lista de reservas del motel
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public List<Reserva> findByMotel(Long idMotel) throws SQLException {
		return listar(SELECT_POR_MOTEL, idMotel);
	}

	/**
	 * Retorna el detalle de servicios adicionales de una reserva.
	 *
	 * @param idReserva identificador de la reserva
	 * @return lista con los servicios incluidos en la reserva
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public List<ReservaServicio> findServicios(Long idReserva) throws SQLException {
		try (Connection con = dataSource.getConnection();
				PreparedStatement ps = con.prepareStatement(SELECT_SERVICIOS)) {
			ps.setLong(1, idReserva);
			try (ResultSet rs = ps.executeQuery()) {
				List<ReservaServicio> lista = new ArrayList<>();
				while (rs.next()) {
					ReservaServicio s = new ReservaServicio();
					s.setIdReserva(rs.getLong("id_reserva"));
					s.setIdServicio(rs.getLong("id_servicio"));
					s.setCantidad(rs.getInt("cantidad"));
					s.setPrecioUnitario(rs.getBigDecimal("precio_unitario"));
					lista.add(s);
				}
				return lista;
			}
		}
	}

	/**
	 * Verifica si la habitación tiene alguna reserva vigente que se cruce con el
	 * rango indicado. Es una comprobación previa; la definitiva se repite dentro de
	 * la transacción de {@link #insertarConServicios(Reserva, List)}.
	 *
	 * @param idHabitacion identificador de la habitación
	 * @param inicio       fecha y hora de inicio del rango
	 * @param fin          fecha y hora de fin del rango
	 * @return {@code true} si la habitación está ocupada en ese rango
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public boolean existsSolapamiento(Long idHabitacion, LocalDateTime inicio, LocalDateTime fin)
			throws SQLException {
		return contar(CONTAR_SOLAPAMIENTOS, idHabitacion, fin, inicio) > 0;
	}

	/**
	 * Inserta una reserva y su detalle de servicios en una sola transacción. Antes
	 * de insertar bloquea la fila de la habitación con {@code SELECT ... FOR UPDATE}
	 * y vuelve a comprobar que no haya otra reserva vigente en el mismo horario,
	 * para que dos clientes no puedan reservar la misma habitación a la vez. Si
	 * alguna sentencia falla se hace {@code rollback} y no queda nada guardado.
	 *
	 * @param r         reserva a insertar, con código, fechas y total calculados
	 * @param servicios detalle de servicios adicionales (puede estar vacío)
	 * @return identificador generado para la reserva, o {@code null} si la
	 *         habitación ya estaba ocupada en ese horario
	 * @throws SQLException si ocurre un error de acceso a la base de datos o se
	 *                      viola una restricción de las tablas
	 */
	public Long insertarConServicios(Reserva r, List<ReservaServicio> servicios) throws SQLException {
		try (Connection con = dataSource.getConnection()) {
			con.setAutoCommit(false);
			try {
				try (PreparedStatement ps = con.prepareStatement(BLOQUEAR_HABITACION)) {
					ps.setLong(1, r.getIdHabitacion());
					try (ResultSet rs = ps.executeQuery()) {
						rs.next();
					}
				}

				if (haySolapamiento(con, r.getIdHabitacion(), r.getFechaHoraInicio(), r.getFechaHoraFin())) {
					con.rollback();
					return null;
				}

				Long idReserva;
				try (PreparedStatement ps = con.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
					ps.setString(1, r.getCodigoReserva());
					ps.setLong(2, r.getIdUsuario());
					ps.setLong(3, r.getIdHabitacion());
					ps.setLong(4, r.getIdTarifa());
					ps.setInt(5, r.getCantidadPersonas());
					ps.setTimestamp(6, Timestamp.valueOf(r.getFechaCreacion()));
					ps.setTimestamp(7, Timestamp.valueOf(r.getFechaHoraInicio()));
					ps.setTimestamp(8, Timestamp.valueOf(r.getFechaHoraFin()));
					ps.setString(9, r.getEstado().name());
					ps.setBigDecimal(10, r.getTotal());
					ps.executeUpdate();

					try (ResultSet keys = ps.getGeneratedKeys()) {
						if (!keys.next()) {
							throw new SQLException("La base de datos no devolvió el id generado");
						}
						idReserva = keys.getLong(1);
					}
				}

				try (PreparedStatement ps = con.prepareStatement(INSERT_SERVICIO)) {
					for (ReservaServicio s : servicios) {
						ps.setLong(1, idReserva);
						ps.setLong(2, s.getIdServicio());
						ps.setInt(3, s.getCantidad());
						ps.setBigDecimal(4, s.getPrecioUnitario());
						ps.addBatch();
					}
					ps.executeBatch();
				}

				con.commit();
				return idReserva;
			} catch (SQLException e) {
				con.rollback();
				throw e;
			} finally {
				con.setAutoCommit(true);
			}
		}
	}

	/**
	 * Actualiza únicamente el estado de una reserva.
	 *
	 * @param id     identificador del registro
	 * @param estado nuevo estado
	 * @return cantidad de filas afectadas
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public int actualizarEstado(Long id, Estado estado) throws SQLException {
		try (Connection con = dataSource.getConnection();
				PreparedStatement ps = con.prepareStatement(UPDATE_ESTADO)) {
			ps.setString(1, estado.name());
			ps.setLong(2, id);
			return ps.executeUpdate();
		}
	}

	/**
	 * Cancela una reserva y rechaza sus pagos pendientes en una sola transacción.
	 * El {@code UPDATE} de la reserva solo afecta filas que sigan
	 * {@code PENDIENTE} o {@code CONFIRMADA}; si otra petición la cambió entre la
	 * lectura del servicio y este momento, no se modifica nada y se hace
	 * {@code rollback}. Los pagos aprobados no se tocan: el dinero sí se recibió
	 * y lo que se devuelve queda registrado en {@code monto_reembolso}.
	 *
	 * @param id                   identificador de la reserva
	 * @param fechaCancelacion     momento de la cancelación
	 * @param montoReembolso       monto que se le devolverá al cliente
	 * @param fechaLimiteReembolso fecha máxima de devolución, o {@code null} si
	 *                             no hay nada que devolver
	 * @return {@code true} si se canceló, {@code false} si la reserva ya no estaba
	 *         en un estado cancelable
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public boolean cancelar(Long id, LocalDateTime fechaCancelacion, BigDecimal montoReembolso,
			LocalDate fechaLimiteReembolso) throws SQLException {
		try (Connection con = dataSource.getConnection()) {
			con.setAutoCommit(false);
			try {
				int filas;
				try (PreparedStatement ps = con.prepareStatement(CANCELAR)) {
					ps.setTimestamp(1, Timestamp.valueOf(fechaCancelacion));
					ps.setBigDecimal(2, montoReembolso);
					ps.setDate(3, fechaLimiteReembolso == null ? null : Date.valueOf(fechaLimiteReembolso));
					ps.setLong(4, id);
					filas = ps.executeUpdate();
				}

				if (filas == 0) {
					con.rollback();
					return false;
				}

				try (PreparedStatement ps = con.prepareStatement(RECHAZAR_PAGOS_PENDIENTES)) {
					ps.setLong(1, id);
					ps.executeUpdate();
				}

				con.commit();
				return true;
			} catch (SQLException e) {
				con.rollback();
				throw e;
			} finally {
				con.setAutoCommit(true);
			}
		}
	}

	/**
	 * Retorna los identificadores de las reservas que siguen {@code PENDIENTE}
	 * aunque su hora de inicio ya llegó (nadie las confirmó a tiempo).
	 *
	 * @param ahora momento de referencia
	 * @return lista de identificadores
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public List<Long> findPendientesVencidas(LocalDateTime ahora) throws SQLException {
		try (Connection con = dataSource.getConnection();
				PreparedStatement ps = con.prepareStatement(SELECT_PENDIENTES_VENCIDAS)) {
			ps.setTimestamp(1, Timestamp.valueOf(ahora));
			try (ResultSet rs = ps.executeQuery()) {
				List<Long> ids = new ArrayList<>();
				while (rs.next()) {
					ids.add(rs.getLong("id_reserva"));
				}
				return ids;
			}
		}
	}

	/**
	 * Pasa a {@code NO_SHOW} las reservas {@code CONFIRMADA} cuyo cliente no
	 * llegó: la hora de inicio más el tiempo de gracia de la política del motel
	 * ya pasó. Si el motel no tiene política, el tiempo de gracia es 0. Usa un
	 * {@code UPDATE} con {@code JOIN} para leer la política en la misma sentencia.
	 *
	 * @param ahora momento de referencia
	 * @return cantidad de reservas actualizadas
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public int marcarNoShow(LocalDateTime ahora) throws SQLException {
		return actualizarPorFecha(MARCAR_NO_SHOW, ahora);
	}

	/**
	 * Pasa a {@code FINALIZADA} las reservas {@code EN_CURSO} cuya hora de fin ya
	 * pasó.
	 *
	 * @param ahora momento de referencia
	 * @return cantidad de reservas actualizadas
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public int finalizarVencidas(LocalDateTime ahora) throws SQLException {
		return actualizarPorFecha(FINALIZAR_VENCIDAS, ahora);
	}

	/**
	 * Ejecuta un {@code UPDATE} cuyo único parámetro es una fecha y hora.
	 *
	 * @param sql   sentencia a ejecutar
	 * @param fecha valor para el marcador {@code ?}
	 * @return cantidad de filas afectadas
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	private int actualizarPorFecha(String sql, LocalDateTime fecha) throws SQLException {
		try (Connection con = dataSource.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
			ps.setTimestamp(1, Timestamp.valueOf(fecha));
			return ps.executeUpdate();
		}
	}

	/**
	 * Cuenta, usando la conexión de la transacción en curso, las reservas vigentes
	 * de la habitación que se cruzan con el rango indicado.
	 *
	 * @param con          conexión de la transacción
	 * @param idHabitacion identificador de la habitación
	 * @param inicio       fecha y hora de inicio del rango
	 * @param fin          fecha y hora de fin del rango
	 * @return {@code true} si hay al menos un cruce
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	private boolean haySolapamiento(Connection con, Long idHabitacion, LocalDateTime inicio, LocalDateTime fin)
			throws SQLException {
		try (PreparedStatement ps = con.prepareStatement(CONTAR_SOLAPAMIENTOS)) {
			ps.setLong(1, idHabitacion);
			ps.setTimestamp(2, Timestamp.valueOf(fin));
			ps.setTimestamp(3, Timestamp.valueOf(inicio));
			try (ResultSet rs = ps.executeQuery()) {
				rs.next();
				return rs.getLong(1) > 0;
			}
		}
	}

	/**
	 * Verifica si existe una reserva con el identificador indicado.
	 *
	 * @param id identificador a verificar
	 * @return {@code true} si existe, {@code false} en caso contrario
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public boolean existsById(Long id) throws SQLException {
		return contar("SELECT COUNT(*) FROM reserva WHERE id_reserva = ?", id) > 0;
	}

	/**
	 * Retorna el total de reservas registradas.
	 *
	 * @return cantidad total de reservas
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public long count() throws SQLException {
		return contar("SELECT COUNT(*) FROM reserva");
	}

	/**
	 * Elimina una reserva según su identificador.
	 *
	 * @param id identificador de la reserva a eliminar
	 * @return cantidad de filas afectadas
	 * @throws SQLException si ocurre un error de acceso a la base de datos o
	 *                      la reserva tiene registros que dependen de ella
	 */
	public int eliminar(Long id) throws SQLException {
		try (Connection con = dataSource.getConnection(); PreparedStatement ps = con.prepareStatement(DELETE)) {
			ps.setLong(1, id);
			return ps.executeUpdate();
		}
	}

	/**
	 * Convierte la fila actual del {@link ResultSet} en una entidad
	 * {@link Reserva}, leyendo cada columna por su nombre.
	 *
	 * @param rs resultado posicionado en la fila a convertir
	 * @return reserva con los datos de la fila
	 * @throws SQLException si alguna columna no existe o no puede leerse
	 */
	private Reserva mapear(ResultSet rs) throws SQLException {
		Reserva r = new Reserva();
		r.setIdReserva(rs.getLong("id_reserva"));
		r.setCodigoReserva(rs.getString("codigo_reserva"));
		r.setIdUsuario(rs.getLong("id_usuario"));
		r.setIdHabitacion(rs.getLong("id_habitacion"));
		r.setIdTarifa(rs.getLong("id_tarifa"));
		r.setCantidadPersonas(rs.getInt("cantidad_personas"));
		r.setFechaCreacion(rs.getTimestamp("fecha_creacion").toLocalDateTime());
		r.setFechaHoraInicio(rs.getTimestamp("fecha_hora_inicio").toLocalDateTime());
		r.setFechaHoraFin(rs.getTimestamp("fecha_hora_fin").toLocalDateTime());
		r.setEstado(Estado.valueOf(rs.getString("estado")));
		r.setTotal(rs.getBigDecimal("total"));
		Timestamp cancelacion = rs.getTimestamp("fecha_cancelacion");
		r.setFechaCancelacion(cancelacion == null ? null : cancelacion.toLocalDateTime());
		r.setMontoReembolso(rs.getBigDecimal("monto_reembolso"));
		Date limite = rs.getDate("fecha_limite_reembolso");
		r.setFechaLimiteReembolso(limite == null ? null : limite.toLocalDate());
		return r;
	}

	/**
	 * Ejecuta una consulta que devuelve varias filas y convierte cada una en una
	 * entidad con {@link #mapear(ResultSet)}. Los parámetros se asignan en orden a
	 * los marcadores {@code ?}.
	 *
	 * @param sql        sentencia de consulta
	 * @param parametros valores para los marcadores {@code ?}
	 * @return lista con las entidades encontradas
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	private List<Reserva> listar(String sql, Object... parametros) throws SQLException {
		try (Connection con = dataSource.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
			for (int i = 0; i < parametros.length; i++) {
				ps.setObject(i + 1, parametros[i]);
			}
			try (ResultSet rs = ps.executeQuery()) {
				List<Reserva> lista = new ArrayList<>();
				while (rs.next()) {
					lista.add(mapear(rs));
				}
				return lista;
			}
		}
	}

	/**
	 * Ejecuta una consulta {@code COUNT(*)} y retorna su resultado. Los parámetros
	 * se asignan en orden a los marcadores {@code ?}.
	 *
	 * @param sql        sentencia de conteo
	 * @param parametros valores para los marcadores {@code ?}
	 * @return resultado del conteo
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	private long contar(String sql, Object... parametros) throws SQLException {
		try (Connection con = dataSource.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
			for (int i = 0; i < parametros.length; i++) {
				ps.setObject(i + 1, parametros[i]);
			}
			try (ResultSet rs = ps.executeQuery()) {
				rs.next();
				return rs.getLong(1);
			}
		}
	}
}
