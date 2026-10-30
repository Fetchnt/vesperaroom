package co.edu.unbosque.vesperaroom.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import org.springframework.stereotype.Repository;

import co.edu.unbosque.vesperaroom.entity.Habitacion;
import co.edu.unbosque.vesperaroom.entity.Habitacion.Estado;

/**
 * Repositorio para la gestión de habitaciones en la tabla {@code habitacion}, implementado
 * con JDBC puro: cada método obtiene una {@link Connection} del
 * {@link DataSource}, prepara la sentencia SQL con {@link PreparedStatement},
 * asigna los parámetros, la ejecuta y recorre el {@link ResultSet}. Los
 * recursos se cierran con try-with-resources.
 */
@Repository
public class HabitacionRepository {

	private static final String SELECT_BASE = "SELECT h.id_habitacion, h.id_motel, h.id_tipo, "
			+ "h.numero, h.piso, h.estado FROM habitacion h ";

	private static final String SELECT_TODOS = SELECT_BASE + "ORDER BY h.id_motel, h.numero";

	private static final String SELECT_POR_ID = SELECT_BASE + "WHERE h.id_habitacion = ?";

	private static final String SELECT_POR_MOTEL = SELECT_BASE + "WHERE h.id_motel = ? ORDER BY "
			+ "h.numero";

	private static final String SELECT_POR_TIPO = SELECT_BASE + "WHERE h.id_tipo = ? ORDER BY "
			+ "h.numero";

	private static final String SELECT_DISPONIBLES = SELECT_BASE + "WHERE h.id_motel = ? AND "
			+ "h.estado = 'ACTIVA' AND NOT EXISTS (SELECT 1 FROM reserva r WHERE r.id_habitacion = "
			+ "h.id_habitacion AND r.estado IN ('PENDIENTE', 'CONFIRMADA', 'EN_CURSO') AND "
			+ "r.fecha_hora_inicio < ? AND r.fecha_hora_fin > ?) ORDER BY h.numero";

	private static final String UPDATE_ESTADO = "UPDATE habitacion SET estado = ? WHERE "
			+ "id_habitacion = ?";

	private static final String INSERT = "INSERT INTO habitacion (id_motel, id_tipo, numero, piso, "
			+ "estado) VALUES (?, ?, ?, ?, ?)";

	private static final String UPDATE = "UPDATE habitacion SET id_motel = ?, id_tipo = ?, numero = "
			+ "?, piso = ?, estado = ? WHERE id_habitacion = ?";

	private static final String DELETE = "DELETE FROM habitacion WHERE id_habitacion = ?";

	private final DataSource dataSource;

	public HabitacionRepository(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * Retorna todas las habitaciones.
	 *
	 * @return lista con todas las habitaciones
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public List<Habitacion> findAll() throws SQLException {
		return listar(SELECT_TODOS);
	}

	/**
	 * Busca una habitación por su identificador.
	 *
	 * @param id identificador de la habitación
	 * @return {@link Optional} con la habitación encontrada, o vacío si no existe
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public Optional<Habitacion> findById(Long id) throws SQLException {
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
	 * Retorna las habitaciones de un motel ordenadas por número.
	 *
	 * @param idMotel identificador del motel
	 * @return lista de habitaciones del motel
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public List<Habitacion> findByMotel(Long idMotel) throws SQLException {
		return listar(SELECT_POR_MOTEL, idMotel);
	}

	/**
	 * Retorna las habitaciones de un tipo ordenadas por número.
	 *
	 * @param idTipo identificador del tipo de habitación
	 * @return lista de habitaciones del tipo
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public List<Habitacion> findByTipo(Long idTipo) throws SQLException {
		return listar(SELECT_POR_TIPO, idTipo);
	}

	/**
	 * Retorna las habitaciones activas de un motel que no tienen reservas vigentes
	 * que se crucen con el rango pedido. Dos rangos se cruzan cuando
	 * {@code inicioReserva < fin} y {@code finReserva > inicio}; la subconsulta
	 * {@code NOT EXISTS} descarta las habitaciones que cumplen esa condición.
	 *
	 * @param idMotel identificador del motel
	 * @param inicio  fecha y hora de inicio del rango
	 * @param fin     fecha y hora de fin del rango
	 * @return lista de habitaciones disponibles
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public List<Habitacion> findDisponibles(Long idMotel, LocalDateTime inicio, LocalDateTime fin)
			throws SQLException {
		return listar(SELECT_DISPONIBLES, idMotel, fin, inicio);
	}

	/**
	 * Verifica si el motel ya tiene una habitación con el número indicado.
	 *
	 * @param idMotel identificador del motel
	 * @param numero número a verificar
	 * @return {@code true} si existe
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public boolean existsByMotelAndNumero(Long idMotel, String numero) throws SQLException {
		return contar("SELECT COUNT(*) FROM habitacion WHERE id_motel = ? AND numero = ?", idMotel, numero) > 0;
	}

	/**
	 * Verifica si el número ya lo usa otra habitación del mismo motel, excluyendo la indicada.
	 *
	 * @param idMotel identificador del motel
	 * @param numero número a verificar
	 * @param id identificador que se excluye de la búsqueda
	 * @return {@code true} si existe
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public boolean existsByMotelAndNumeroAndIdNot(Long idMotel, String numero, Long id) throws SQLException {
		return contar("SELECT COUNT(*) FROM habitacion WHERE id_motel = ? AND numero = ? AND id_habitacion <> ?", idMotel, numero, id) > 0;
	}

	/**
	 * Actualiza únicamente el estado de una habitación.
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
	 * Verifica si existe una habitación con el identificador indicado.
	 *
	 * @param id identificador a verificar
	 * @return {@code true} si existe, {@code false} en caso contrario
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public boolean existsById(Long id) throws SQLException {
		return contar("SELECT COUNT(*) FROM habitacion WHERE id_habitacion = ?", id) > 0;
	}

	/**
	 * Retorna el total de habitaciones registradas.
	 *
	 * @return cantidad total de habitaciones
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public long count() throws SQLException {
		return contar("SELECT COUNT(*) FROM habitacion");
	}

	/**
	 * Inserta una nueva habitación y retorna el identificador generado por
	 * {@code AUTO_INCREMENT}.
	 *
	 * @param h habitación a insertar
	 * @return identificador generado para el nuevo registro
	 * @throws SQLException si ocurre un error de acceso a la base de datos o se
	 *                      viola una restricción de la tabla
	 */
	public Long insertar(Habitacion h) throws SQLException {
		try (Connection con = dataSource.getConnection();
				PreparedStatement ps = con.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
			ps.setLong(1, h.getIdMotel());
			ps.setLong(2, h.getIdTipo());
			ps.setString(3, h.getNumero());
			ps.setInt(4, h.getPiso());
			ps.setString(5, h.getEstado().name());
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
	 * Actualiza los campos editables de una habitación existente.
	 *
	 * @param h habitación con los nuevos datos y su identificador
	 * @return cantidad de filas afectadas
	 * @throws SQLException si ocurre un error de acceso a la base de datos o se
	 *                      viola una restricción de la tabla
	 */
	public int actualizar(Habitacion h) throws SQLException {
		try (Connection con = dataSource.getConnection(); PreparedStatement ps = con.prepareStatement(UPDATE)) {
			ps.setLong(1, h.getIdMotel());
			ps.setLong(2, h.getIdTipo());
			ps.setString(3, h.getNumero());
			ps.setInt(4, h.getPiso());
			ps.setString(5, h.getEstado().name());
			ps.setLong(6, h.getIdHabitacion());
			return ps.executeUpdate();
		}
	}

	/**
	 * Elimina una habitación según su identificador.
	 *
	 * @param id identificador de la habitación a eliminar
	 * @return cantidad de filas afectadas
	 * @throws SQLException si ocurre un error de acceso a la base de datos o
	 *                      la habitación tiene registros que dependen de ella
	 */
	public int eliminar(Long id) throws SQLException {
		try (Connection con = dataSource.getConnection(); PreparedStatement ps = con.prepareStatement(DELETE)) {
			ps.setLong(1, id);
			return ps.executeUpdate();
		}
	}

	/**
	 * Convierte la fila actual del {@link ResultSet} en una entidad
	 * {@link Habitacion}, leyendo cada columna por su nombre.
	 *
	 * @param rs resultado posicionado en la fila a convertir
	 * @return habitación con los datos de la fila
	 * @throws SQLException si alguna columna no existe o no puede leerse
	 */
	private Habitacion mapear(ResultSet rs) throws SQLException {
		Habitacion h = new Habitacion();
		h.setIdHabitacion(rs.getLong("id_habitacion"));
		h.setIdMotel(rs.getLong("id_motel"));
		h.setIdTipo(rs.getLong("id_tipo"));
		h.setNumero(rs.getString("numero"));
		h.setPiso(rs.getInt("piso"));
		h.setEstado(Estado.valueOf(rs.getString("estado")));
		return h;
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
	private List<Habitacion> listar(String sql, Object... parametros) throws SQLException {
		try (Connection con = dataSource.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
			for (int i = 0; i < parametros.length; i++) {
				ps.setObject(i + 1, parametros[i]);
			}
			try (ResultSet rs = ps.executeQuery()) {
				List<Habitacion> lista = new ArrayList<>();
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
