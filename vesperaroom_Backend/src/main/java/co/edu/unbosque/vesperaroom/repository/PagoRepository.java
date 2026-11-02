package co.edu.unbosque.vesperaroom.repository;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import org.springframework.stereotype.Repository;

import co.edu.unbosque.vesperaroom.entity.Pago;
import co.edu.unbosque.vesperaroom.entity.Pago.Estado;
import co.edu.unbosque.vesperaroom.entity.Pago.MetodoPago;

/**
 * Repositorio para la gestión de pagos en la tabla {@code pago}, implementado
 * con JDBC puro: cada método obtiene una {@link Connection} del
 * {@link DataSource}, prepara la sentencia SQL con {@link PreparedStatement},
 * asigna los parámetros, la ejecuta y recorre el {@link ResultSet}. Los
 * recursos se cierran con try-with-resources.
 */
@Repository
public class PagoRepository {

	private static final String SELECT_BASE = "SELECT p.id_pago, p.id_reserva, p.monto, "
			+ "p.fecha_pago, p.metodo_pago, p.estado, p.referencia_transaccion FROM pago p ";

	private static final String SELECT_TODOS = SELECT_BASE + "ORDER BY p.fecha_pago DESC";

	private static final String SELECT_POR_ID = SELECT_BASE + "WHERE p.id_pago = ?";

	private static final String SELECT_POR_RESERVA = SELECT_BASE + "WHERE p.id_reserva = ? ORDER BY "
			+ "p.fecha_pago";

	/** Aprobados y pendientes; los rechazados y reembolsados no cuentan. */
	private static final String SUMAR_MONTO_ACTIVO = "SELECT COALESCE(SUM(monto), 0) FROM pago "
			+ "WHERE id_reserva = ? AND estado IN ('APROBADO', 'PENDIENTE')";

	private static final String SUMAR_MONTO_APROBADO = "SELECT COALESCE(SUM(monto), 0) FROM pago "
			+ "WHERE id_reserva = ? AND estado = 'APROBADO'";

	private static final String BLOQUEAR_RESERVA = "SELECT id_reserva FROM reserva WHERE id_reserva = ? "
			+ "FOR UPDATE";

	private static final String UPDATE_ESTADO = "UPDATE pago SET estado = ? WHERE id_pago = ?";

	private static final String INSERT = "INSERT INTO pago (id_reserva, monto, fecha_pago, "
			+ "metodo_pago, estado, referencia_transaccion) VALUES (?, ?, ?, ?, ?, ?)";

	private static final String DELETE = "DELETE FROM pago WHERE id_pago = ?";

	private final DataSource dataSource;

	public PagoRepository(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * Retorna todos los pagos.
	 *
	 * @return lista con todos los pagos
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public List<Pago> findAll() throws SQLException {
		return listar(SELECT_TODOS);
	}

	/**
	 * Busca un pago por su identificador.
	 *
	 * @param id identificador del pago
	 * @return {@link Optional} con el pago encontrado, o vacío si no existe
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public Optional<Pago> findById(Long id) throws SQLException {
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
	 * Retorna los pagos de una reserva en orden cronológico.
	 *
	 * @param idReserva identificador de la reserva
	 * @return lista de pagos de la reserva
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public List<Pago> findByReserva(Long idReserva) throws SQLException {
		return listar(SELECT_POR_RESERVA, idReserva);
	}

	/**
	 * Suma solo los pagos aprobados de una reserva, es decir, el dinero que el
	 * motel ya recibió. Es la base para calcular el reembolso al cancelar.
	 *
	 * @param idReserva identificador de la reserva
	 * @return suma de los montos aprobados, o 0 si no hay
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public BigDecimal sumarMontoAprobado(Long idReserva) throws SQLException {
		try (Connection con = dataSource.getConnection();
				PreparedStatement ps = con.prepareStatement(SUMAR_MONTO_APROBADO)) {
			ps.setLong(1, idReserva);
			try (ResultSet rs = ps.executeQuery()) {
				rs.next();
				return rs.getBigDecimal(1);
			}
		}
	}

	/**
	 * Verifica si una referencia de transacción ya está registrada.
	 *
	 * @param referencia referencia a verificar
	 * @return {@code true} si existe
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public boolean existsByReferencia(String referencia) throws SQLException {
		return contar("SELECT COUNT(*) FROM pago WHERE referencia_transaccion = ?", referencia) > 0;
	}

	/**
	 * Actualiza únicamente el estado de un pago.
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
	 * Verifica si existe un pago con el identificador indicado.
	 *
	 * @param id identificador a verificar
	 * @return {@code true} si existe, {@code false} en caso contrario
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public boolean existsById(Long id) throws SQLException {
		return contar("SELECT COUNT(*) FROM pago WHERE id_pago = ?", id) > 0;
	}

	/**
	 * Retorna el total de pagos registrados.
	 *
	 * @return cantidad total de pagos
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public long count() throws SQLException {
		return contar("SELECT COUNT(*) FROM pago");
	}

	/**
	 * Inserta el pago solo si, sumado a los pagos aprobados y pendientes, no
	 * supera el total de la reserva. La fila de la reserva queda bloqueada con
	 * {@code SELECT ... FOR UPDATE} durante la transacción: si llegan dos pagos a
	 * la vez, el segundo espera y ya ve el primero, así que no pueden pasar los
	 * dos la verificación.
	 *
	 * @param p     pago a insertar
	 * @param total total de la reserva
	 * @return identificador generado, o {@code null} si el pago excede lo que
	 *         falta por pagar
	 * @throws SQLException si ocurre un error de acceso a la base de datos o se
	 *                      viola una restricción de la tabla
	 */
	public Long insertarSiHaySaldo(Pago p, BigDecimal total) throws SQLException {
		try (Connection con = dataSource.getConnection()) {
			con.setAutoCommit(false);
			try {
				try (PreparedStatement ps = con.prepareStatement(BLOQUEAR_RESERVA)) {
					ps.setLong(1, p.getIdReserva());
					try (ResultSet rs = ps.executeQuery()) {
						rs.next();
					}
				}

				BigDecimal pagado;
				try (PreparedStatement ps = con.prepareStatement(SUMAR_MONTO_ACTIVO)) {
					ps.setLong(1, p.getIdReserva());
					try (ResultSet rs = ps.executeQuery()) {
						rs.next();
						pagado = rs.getBigDecimal(1);
					}
				}

				if (pagado.add(p.getMonto()).compareTo(total) > 0) {
					con.rollback();
					return null;
				}

				Long id = insertar(con, p);
				con.commit();
				return id;
			} catch (SQLException e) {
				con.rollback();
				throw e;
			} finally {
				con.setAutoCommit(true);
			}
		}
	}

	private Long insertar(Connection con, Pago p) throws SQLException {
		try (PreparedStatement ps = con.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
			ps.setLong(1, p.getIdReserva());
			ps.setBigDecimal(2, p.getMonto());
			ps.setTimestamp(3, Timestamp.valueOf(p.getFechaPago()));
			ps.setString(4, p.getMetodoPago().name());
			ps.setString(5, p.getEstado().name());
			ps.setString(6, p.getReferenciaTransaccion());
			ps.executeUpdate();

			try (ResultSet keys = ps.getGeneratedKeys()) {
				if (keys.next()) {
					return keys.getLong(1);
				}
				throw new SQLException("La base de datos no devolvió el id generado");
			}
		}
	}

	/**
	 * Elimina un pago según su identificador.
	 *
	 * @param id identificador del pago a eliminar
	 * @return cantidad de filas afectadas
	 * @throws SQLException si ocurre un error de acceso a la base de datos o
	 *                      el pago tiene registros que dependen de él
	 */
	public int eliminar(Long id) throws SQLException {
		try (Connection con = dataSource.getConnection(); PreparedStatement ps = con.prepareStatement(DELETE)) {
			ps.setLong(1, id);
			return ps.executeUpdate();
		}
	}

	/**
	 * Convierte la fila actual del {@link ResultSet} en una entidad
	 * {@link Pago}, leyendo cada columna por su nombre.
	 *
	 * @param rs resultado posicionado en la fila a convertir
	 * @return pago con los datos de la fila
	 * @throws SQLException si alguna columna no existe o no puede leerse
	 */
	private Pago mapear(ResultSet rs) throws SQLException {
		Pago p = new Pago();
		p.setIdPago(rs.getLong("id_pago"));
		p.setIdReserva(rs.getLong("id_reserva"));
		p.setMonto(rs.getBigDecimal("monto"));
		p.setFechaPago(rs.getTimestamp("fecha_pago").toLocalDateTime());
		p.setMetodoPago(MetodoPago.valueOf(rs.getString("metodo_pago")));
		p.setEstado(Estado.valueOf(rs.getString("estado")));
		p.setReferenciaTransaccion(rs.getString("referencia_transaccion"));
		return p;
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
	private List<Pago> listar(String sql, Object... parametros) throws SQLException {
		try (Connection con = dataSource.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
			for (int i = 0; i < parametros.length; i++) {
				ps.setObject(i + 1, parametros[i]);
			}
			try (ResultSet rs = ps.executeQuery()) {
				List<Pago> lista = new ArrayList<>();
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
