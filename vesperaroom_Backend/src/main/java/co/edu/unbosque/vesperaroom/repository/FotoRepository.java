package co.edu.unbosque.vesperaroom.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import org.springframework.stereotype.Repository;

import co.edu.unbosque.vesperaroom.entity.Foto;

/**
 * Repositorio para la gestión de fotos en la tabla {@code foto}, implementado
 * con JDBC puro: cada método obtiene una {@link Connection} del
 * {@link DataSource}, prepara la sentencia SQL con {@link PreparedStatement},
 * asigna los parámetros, la ejecuta y recorre el {@link ResultSet}. Los
 * recursos se cierran con try-with-resources.
 */
@Repository
public class FotoRepository {

	private static final String SELECT_BASE = "SELECT f.id_foto, f.id_motel, f.id_habitacion, "
			+ "f.uri, f.orden FROM foto f ";

	private static final String SELECT_TODOS = SELECT_BASE + "ORDER BY f.id_foto";

	private static final String SELECT_POR_ID = SELECT_BASE + "WHERE f.id_foto = ?";

	private static final String SELECT_POR_MOTEL = SELECT_BASE + "WHERE f.id_motel = ? ORDER BY "
			+ "f.orden";

	private static final String SELECT_POR_HABITACION = SELECT_BASE + "WHERE f.id_habitacion = ? "
			+ "ORDER BY f.orden";

	private static final String INSERT = "INSERT INTO foto (id_motel, id_habitacion, uri, orden) "
			+ "VALUES (?, ?, ?, ?)";

	private static final String UPDATE = "UPDATE foto SET id_motel = ?, id_habitacion = ?, uri = ?, "
			+ "orden = ? WHERE id_foto = ?";

	private static final String DELETE = "DELETE FROM foto WHERE id_foto = ?";

	private final DataSource dataSource;

	public FotoRepository(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * Retorna todas las fotos.
	 *
	 * @return lista con todas las fotos
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public List<Foto> findAll() throws SQLException {
		return listar(SELECT_TODOS);
	}

	/**
	 * Busca una foto por su identificador.
	 *
	 * @param id identificador de la foto
	 * @return {@link Optional} con la foto encontrada, o vacío si no existe
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public Optional<Foto> findById(Long id) throws SQLException {
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
	 * Retorna las fotos generales de un motel ordenadas por su posición.
	 *
	 * @param idMotel identificador del motel
	 * @return lista de fotos del motel
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public List<Foto> findByMotel(Long idMotel) throws SQLException {
		return listar(SELECT_POR_MOTEL, idMotel);
	}

	/**
	 * Retorna las fotos de una habitación ordenadas por su posición.
	 *
	 * @param idHabitacion identificador de la habitación
	 * @return lista de fotos de la habitación
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public List<Foto> findByHabitacion(Long idHabitacion) throws SQLException {
		return listar(SELECT_POR_HABITACION, idHabitacion);
	}

	/**
	 * Verifica si otra foto del motel ya ocupa la posición indicada. Para una foto
	 * nueva se envía {@code id = 0}.
	 *
	 * @param idMotel identificador del motel
	 * @param orden posición a verificar
	 * @param id identificador que se excluye de la búsqueda
	 * @return {@code true} si existe
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public boolean existsByMotelAndOrdenAndIdNot(Long idMotel, Integer orden, Long id) throws SQLException {
		return contar("SELECT COUNT(*) FROM foto WHERE id_motel = ? AND orden = ? AND id_foto <> ?", idMotel, orden, id) > 0;
	}

	/**
	 * Verifica si otra foto de la habitación ya ocupa la posición indicada. Para
	 * una foto nueva se envía {@code id = 0}.
	 *
	 * @param idHabitacion identificador de la habitación
	 * @param orden posición a verificar
	 * @param id identificador que se excluye de la búsqueda
	 * @return {@code true} si existe
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public boolean existsByHabitacionAndOrdenAndIdNot(Long idHabitacion, Integer orden, Long id) throws SQLException {
		return contar("SELECT COUNT(*) FROM foto WHERE id_habitacion = ? AND orden = ? AND id_foto <> ?", idHabitacion, orden, id) > 0;
	}

	/**
	 * Calcula la primera posición libre al final de las fotos de un dueño.
	 *
	 * @param idMotel      motel dueño (o nulo)
	 * @param idHabitacion habitación dueña (o nula)
	 * @return la posición mayor más uno, o 1 si aún no tiene fotos
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public int siguienteOrden(Long idMotel, Long idHabitacion) throws SQLException {
		return idMotel != null
				? (int) contar("SELECT COALESCE(MAX(orden), 0) + 1 FROM foto WHERE id_motel = ?", idMotel)
				: (int) contar("SELECT COALESCE(MAX(orden), 0) + 1 FROM foto WHERE id_habitacion = ?", idHabitacion);
	}

	/**
	 * Verifica si existe una foto con el identificador indicado.
	 *
	 * @param id identificador a verificar
	 * @return {@code true} si existe, {@code false} en caso contrario
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public boolean existsById(Long id) throws SQLException {
		return contar("SELECT COUNT(*) FROM foto WHERE id_foto = ?", id) > 0;
	}

	/**
	 * Retorna el total de fotos registradas.
	 *
	 * @return cantidad total de fotos
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public long count() throws SQLException {
		return contar("SELECT COUNT(*) FROM foto");
	}

	/**
	 * Inserta una nueva foto y retorna el identificador generado por
	 * {@code AUTO_INCREMENT}.
	 *
	 * @param f foto a insertar
	 * @return identificador generado para el nuevo registro
	 * @throws SQLException si ocurre un error de acceso a la base de datos o se
	 *                      viola una restricción de la tabla
	 */
	public Long insertar(Foto f) throws SQLException {
		try (Connection con = dataSource.getConnection();
				PreparedStatement ps = con.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
			ps.setObject(1, f.getIdMotel(), Types.BIGINT);
			ps.setObject(2, f.getIdHabitacion(), Types.BIGINT);
			ps.setString(3, f.getUri());
			ps.setInt(4, f.getOrden());
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
	 * Actualiza los campos editables de una foto existente.
	 *
	 * @param f foto con los nuevos datos y su identificador
	 * @return cantidad de filas afectadas
	 * @throws SQLException si ocurre un error de acceso a la base de datos o se
	 *                      viola una restricción de la tabla
	 */
	public int actualizar(Foto f) throws SQLException {
		try (Connection con = dataSource.getConnection(); PreparedStatement ps = con.prepareStatement(UPDATE)) {
			ps.setObject(1, f.getIdMotel(), Types.BIGINT);
			ps.setObject(2, f.getIdHabitacion(), Types.BIGINT);
			ps.setString(3, f.getUri());
			ps.setInt(4, f.getOrden());
			ps.setLong(5, f.getIdFoto());
			return ps.executeUpdate();
		}
	}

	/**
	 * Elimina una foto según su identificador.
	 *
	 * @param id identificador de la foto a eliminar
	 * @return cantidad de filas afectadas
	 * @throws SQLException si ocurre un error de acceso a la base de datos o
	 *                      la foto tiene registros que dependen de ella
	 */
	public int eliminar(Long id) throws SQLException {
		try (Connection con = dataSource.getConnection(); PreparedStatement ps = con.prepareStatement(DELETE)) {
			ps.setLong(1, id);
			return ps.executeUpdate();
		}
	}

	/**
	 * Convierte la fila actual del {@link ResultSet} en una entidad
	 * {@link Foto}, leyendo cada columna por su nombre.
	 *
	 * @param rs resultado posicionado en la fila a convertir
	 * @return foto con los datos de la fila
	 * @throws SQLException si alguna columna no existe o no puede leerse
	 */
	private Foto mapear(ResultSet rs) throws SQLException {
		Foto f = new Foto();
		f.setIdFoto(rs.getLong("id_foto"));
		f.setIdMotel(rs.getObject("id_motel", Long.class));
		f.setIdHabitacion(rs.getObject("id_habitacion", Long.class));
		f.setUri(rs.getString("uri"));
		f.setOrden(rs.getInt("orden"));
		return f;
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
	private List<Foto> listar(String sql, Object... parametros) throws SQLException {
		try (Connection con = dataSource.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
			for (int i = 0; i < parametros.length; i++) {
				ps.setObject(i + 1, parametros[i]);
			}
			try (ResultSet rs = ps.executeQuery()) {
				List<Foto> lista = new ArrayList<>();
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
