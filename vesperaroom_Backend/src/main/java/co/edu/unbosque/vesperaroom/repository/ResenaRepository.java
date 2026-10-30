package co.edu.unbosque.vesperaroom.repository;

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

import co.edu.unbosque.vesperaroom.entity.Resena;

/**
 * Repositorio para la gestión de reseñas en la tabla {@code resena}, implementado
 * con JDBC puro: cada método obtiene una {@link Connection} del
 * {@link DataSource}, prepara la sentencia SQL con {@link PreparedStatement},
 * asigna los parámetros, la ejecuta y recorre el {@link ResultSet}. Los
 * recursos se cierran con try-with-resources.
 */
@Repository
public class ResenaRepository {

	private static final String SELECT_BASE = "SELECT re.id_resena, re.id_reserva, re.calificacion, "
			+ "re.comentario, re.fecha FROM resena re ";

	private static final String SELECT_TODOS = SELECT_BASE + "ORDER BY re.fecha DESC";

	private static final String SELECT_POR_ID = SELECT_BASE + "WHERE re.id_resena = ?";

	private static final String SELECT_POR_MOTEL = SELECT_BASE + "JOIN reserva r ON r.id_reserva = "
			+ "re.id_reserva JOIN habitacion h ON h.id_habitacion = r.id_habitacion WHERE "
			+ "h.id_motel = ? ORDER BY re.fecha DESC";

	private static final String SELECT_POR_USUARIO = SELECT_BASE + "JOIN reserva r ON r.id_reserva "
			+ "= re.id_reserva WHERE r.id_usuario = ? ORDER BY re.fecha DESC";

	private static final String PROMEDIO_POR_MOTEL = "SELECT AVG(re.calificacion) FROM resena re "
			+ "JOIN reserva r ON r.id_reserva = re.id_reserva JOIN habitacion h ON h.id_habitacion "
			+ "= r.id_habitacion WHERE h.id_motel = ?";

	private static final String INSERT = "INSERT INTO resena (id_reserva, calificacion, comentario, "
			+ "fecha) VALUES (?, ?, ?, ?)";

	private static final String UPDATE = "UPDATE resena SET calificacion = ?, comentario = ? WHERE "
			+ "id_resena = ?";

	private static final String DELETE = "DELETE FROM resena WHERE id_resena = ?";

	private final DataSource dataSource;

	public ResenaRepository(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * Retorna todas las reseñas.
	 *
	 * @return lista con todas las reseñas
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public List<Resena> findAll() throws SQLException {
		return listar(SELECT_TODOS);
	}

	/**
	 * Busca una reseña por su identificador.
	 *
	 * @param id identificador de la reseña
	 * @return {@link Optional} con la reseña encontrada, o vacío si no existe
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public Optional<Resena> findById(Long id) throws SQLException {
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
	 * Retorna las reseñas de un motel, llegando a él por la reserva y la habitación.
	 *
	 * @param idMotel identificador del motel
	 * @return lista de reseñas del motel
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public List<Resena> findByMotel(Long idMotel) throws SQLException {
		return listar(SELECT_POR_MOTEL, idMotel);
	}

	/**
	 * Retorna las reseñas escritas por un cliente.
	 *
	 * @param idUsuario identificador del cliente
	 * @return lista de reseñas del cliente
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public List<Resena> findByUsuario(Long idUsuario) throws SQLException {
		return listar(SELECT_POR_USUARIO, idUsuario);
	}

	/**
	 * Verifica si la reserva ya tiene una reseña.
	 *
	 * @param idReserva identificador de la reserva
	 * @return {@code true} si existe
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public boolean existsByReserva(Long idReserva) throws SQLException {
		return contar("SELECT COUNT(*) FROM resena WHERE id_reserva = ?", idReserva) > 0;
	}

	/**
	 * Calcula la calificación promedio de un motel con la función de agregación
	 * {@code AVG}.
	 *
	 * @param idMotel identificador del motel
	 * @return promedio de calificaciones, o {@code null} si el motel no tiene
	 *         reseñas
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public Double promedioByMotel(Long idMotel) throws SQLException {
		try (Connection con = dataSource.getConnection();
				PreparedStatement ps = con.prepareStatement(PROMEDIO_POR_MOTEL)) {
			ps.setLong(1, idMotel);
			try (ResultSet rs = ps.executeQuery()) {
				rs.next();
				double promedio = rs.getDouble(1);
				return rs.wasNull() ? null : promedio;
			}
		}
	}

	/**
	 * Verifica si existe una reseña con el identificador indicado.
	 *
	 * @param id identificador a verificar
	 * @return {@code true} si existe, {@code false} en caso contrario
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public boolean existsById(Long id) throws SQLException {
		return contar("SELECT COUNT(*) FROM resena WHERE id_resena = ?", id) > 0;
	}

	/**
	 * Retorna el total de reseñas registradas.
	 *
	 * @return cantidad total de reseñas
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public long count() throws SQLException {
		return contar("SELECT COUNT(*) FROM resena");
	}

	/**
	 * Inserta una nueva reseña y retorna el identificador generado por
	 * {@code AUTO_INCREMENT}.
	 *
	 * @param r reseña a insertar
	 * @return identificador generado para el nuevo registro
	 * @throws SQLException si ocurre un error de acceso a la base de datos o se
	 *                      viola una restricción de la tabla
	 */
	public Long insertar(Resena r) throws SQLException {
		try (Connection con = dataSource.getConnection();
				PreparedStatement ps = con.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
			ps.setLong(1, r.getIdReserva());
			ps.setInt(2, r.getCalificacion());
			ps.setString(3, r.getComentario());
			ps.setTimestamp(4, Timestamp.valueOf(r.getFecha()));
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
	 * Actualiza los campos editables de una reseña existente.
	 *
	 * @param r reseña con los nuevos datos y su identificador
	 * @return cantidad de filas afectadas
	 * @throws SQLException si ocurre un error de acceso a la base de datos o se
	 *                      viola una restricción de la tabla
	 */
	public int actualizar(Resena r) throws SQLException {
		try (Connection con = dataSource.getConnection(); PreparedStatement ps = con.prepareStatement(UPDATE)) {
			ps.setInt(1, r.getCalificacion());
			ps.setString(2, r.getComentario());
			ps.setLong(3, r.getIdResena());
			return ps.executeUpdate();
		}
	}

	/**
	 * Elimina una reseña según su identificador.
	 *
	 * @param id identificador de la reseña a eliminar
	 * @return cantidad de filas afectadas
	 * @throws SQLException si ocurre un error de acceso a la base de datos o
	 *                      la reseña tiene registros que dependen de ella
	 */
	public int eliminar(Long id) throws SQLException {
		try (Connection con = dataSource.getConnection(); PreparedStatement ps = con.prepareStatement(DELETE)) {
			ps.setLong(1, id);
			return ps.executeUpdate();
		}
	}

	/**
	 * Convierte la fila actual del {@link ResultSet} en una entidad
	 * {@link Resena}, leyendo cada columna por su nombre.
	 *
	 * @param rs resultado posicionado en la fila a convertir
	 * @return reseña con los datos de la fila
	 * @throws SQLException si alguna columna no existe o no puede leerse
	 */
	private Resena mapear(ResultSet rs) throws SQLException {
		Resena r = new Resena();
		r.setIdResena(rs.getLong("id_resena"));
		r.setIdReserva(rs.getLong("id_reserva"));
		r.setCalificacion(rs.getInt("calificacion"));
		r.setComentario(rs.getString("comentario"));
		r.setFecha(rs.getTimestamp("fecha").toLocalDateTime());
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
	private List<Resena> listar(String sql, Object... parametros) throws SQLException {
		try (Connection con = dataSource.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
			for (int i = 0; i < parametros.length; i++) {
				ps.setObject(i + 1, parametros[i]);
			}
			try (ResultSet rs = ps.executeQuery()) {
				List<Resena> lista = new ArrayList<>();
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
