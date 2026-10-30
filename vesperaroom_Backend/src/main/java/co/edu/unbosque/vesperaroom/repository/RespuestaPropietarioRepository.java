package co.edu.unbosque.vesperaroom.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import org.springframework.stereotype.Repository;

import co.edu.unbosque.vesperaroom.entity.RespuestaPropietario;

/**
 * Repositorio para la gestión de respuestas del propietario en la tabla {@code respuesta_propietario}, implementado
 * con JDBC puro: cada método obtiene una {@link Connection} del
 * {@link DataSource}, prepara la sentencia SQL con {@link PreparedStatement},
 * asigna los parámetros, la ejecuta y recorre el {@link ResultSet}. Los
 * recursos se cierran con try-with-resources.
 */
@Repository
public class RespuestaPropietarioRepository {

	private static final String SELECT_BASE = "SELECT rp.id_resena, rp.comentario, rp.fecha FROM "
			+ "respuesta_propietario rp ";

	private static final String SELECT_TODOS = SELECT_BASE + "ORDER BY rp.fecha DESC";

	private static final String SELECT_POR_ID = SELECT_BASE + "WHERE rp.id_resena = ?";

	private static final String INSERT = "INSERT INTO respuesta_propietario (id_resena, comentario, "
			+ "fecha) VALUES (?, ?, ?)";

	private static final String UPDATE = "UPDATE respuesta_propietario SET comentario = ? WHERE "
			+ "id_resena = ?";

	private static final String DELETE = "DELETE FROM respuesta_propietario WHERE id_resena = ?";

	private final DataSource dataSource;

	public RespuestaPropietarioRepository(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * Retorna todas las respuestas del propietario.
	 *
	 * @return lista con todas las respuestas del propietario
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public List<RespuestaPropietario> findAll() throws SQLException {
		return listar(SELECT_TODOS);
	}

	/**
	 * Busca una respuesta del propietario por su identificador.
	 *
	 * @param id identificador de la respuesta del propietario
	 * @return {@link Optional} con la respuesta del propietario encontrada, o vacío si no existe
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public Optional<RespuestaPropietario> findById(Long id) throws SQLException {
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
	 * Verifica si existe una respuesta del propietario con el identificador indicado.
	 *
	 * @param id identificador a verificar
	 * @return {@code true} si existe, {@code false} en caso contrario
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public boolean existsById(Long id) throws SQLException {
		return contar("SELECT COUNT(*) FROM respuesta_propietario WHERE id_resena = ?", id) > 0;
	}

	/**
	 * Retorna el total de respuestas del propietario registradas.
	 *
	 * @return cantidad total de respuestas del propietario
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public long count() throws SQLException {
		return contar("SELECT COUNT(*) FROM respuesta_propietario");
	}

	/**
	 * Inserta una nueva respuesta del propietario. Su identificador es la llave del registro
	 * del que depende, por lo que no se genera con {@code AUTO_INCREMENT}.
	 *
	 * @param r respuesta del propietario a insertar
	 * @return cantidad de filas afectadas
	 * @throws SQLException si ocurre un error de acceso a la base de datos o se
	 *                      viola una restricción de la tabla
	 */
	public int insertar(RespuestaPropietario r) throws SQLException {
		try (Connection con = dataSource.getConnection(); PreparedStatement ps = con.prepareStatement(INSERT)) {
			ps.setLong(1, r.getIdResena());
			ps.setString(2, r.getComentario());
			ps.setTimestamp(3, Timestamp.valueOf(r.getFecha()));
			return ps.executeUpdate();
		}
	}

	/**
	 * Actualiza los campos editables de una respuesta del propietario existente.
	 *
	 * @param r respuesta del propietario con los nuevos datos y su identificador
	 * @return cantidad de filas afectadas
	 * @throws SQLException si ocurre un error de acceso a la base de datos o se
	 *                      viola una restricción de la tabla
	 */
	public int actualizar(RespuestaPropietario r) throws SQLException {
		try (Connection con = dataSource.getConnection(); PreparedStatement ps = con.prepareStatement(UPDATE)) {
			ps.setString(1, r.getComentario());
			ps.setLong(2, r.getIdResena());
			return ps.executeUpdate();
		}
	}

	/**
	 * Elimina una respuesta del propietario según su identificador.
	 *
	 * @param id identificador de la respuesta del propietario a eliminar
	 * @return cantidad de filas afectadas
	 * @throws SQLException si ocurre un error de acceso a la base de datos o
	 *                      la respuesta del propietario tiene registros que dependen de ella
	 */
	public int eliminar(Long id) throws SQLException {
		try (Connection con = dataSource.getConnection(); PreparedStatement ps = con.prepareStatement(DELETE)) {
			ps.setLong(1, id);
			return ps.executeUpdate();
		}
	}

	/**
	 * Convierte la fila actual del {@link ResultSet} en una entidad
	 * {@link RespuestaPropietario}, leyendo cada columna por su nombre.
	 *
	 * @param rs resultado posicionado en la fila a convertir
	 * @return respuesta del propietario con los datos de la fila
	 * @throws SQLException si alguna columna no existe o no puede leerse
	 */
	private RespuestaPropietario mapear(ResultSet rs) throws SQLException {
		RespuestaPropietario r = new RespuestaPropietario();
		r.setIdResena(rs.getLong("id_resena"));
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
	private List<RespuestaPropietario> listar(String sql, Object... parametros) throws SQLException {
		try (Connection con = dataSource.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
			for (int i = 0; i < parametros.length; i++) {
				ps.setObject(i + 1, parametros[i]);
			}
			try (ResultSet rs = ps.executeQuery()) {
				List<RespuestaPropietario> lista = new ArrayList<>();
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
