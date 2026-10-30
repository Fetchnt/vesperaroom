package co.edu.unbosque.vesperaroom.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import org.springframework.stereotype.Repository;

import co.edu.unbosque.vesperaroom.entity.Motel;
import co.edu.unbosque.vesperaroom.entity.Motel.Estado;

/**
 * Repositorio para la gestión de moteles en la tabla {@code motel}, implementado
 * con JDBC puro: cada método obtiene una {@link Connection} del
 * {@link DataSource}, prepara la sentencia SQL con {@link PreparedStatement},
 * asigna los parámetros, la ejecuta y recorre el {@link ResultSet}. Los
 * recursos se cierran con try-with-resources.
 */
@Repository
public class MotelRepository {

	private static final String SELECT_BASE = "SELECT m.id_motel, m.id_propietario, m.id_ciudad, "
			+ "m.nombre, m.descripcion, m.calle, m.carrera, m.latitud, m.longitud, m.estado FROM "
			+ "motel m ";

	private static final String SELECT_TODOS = SELECT_BASE + "ORDER BY m.nombre";

	private static final String SELECT_POR_ID = SELECT_BASE + "WHERE m.id_motel = ?";

	private static final String SELECT_ACTIVOS = SELECT_BASE + "WHERE m.estado = 'ACTIVO' ORDER BY "
			+ "m.nombre";

	private static final String SELECT_ACTIVOS_POR_CIUDAD = SELECT_BASE + "WHERE m.estado = "
			+ "'ACTIVO' AND m.id_ciudad = ? ORDER BY m.nombre";

	private static final String SELECT_POR_PROPIETARIO = SELECT_BASE + "WHERE m.id_propietario = ? "
			+ "ORDER BY m.nombre";

	private static final String UPDATE_ESTADO = "UPDATE motel SET estado = ? WHERE id_motel = ?";

	private static final String INSERT = "INSERT INTO motel (id_propietario, id_ciudad, nombre, "
			+ "descripcion, calle, carrera, latitud, longitud, estado) VALUES (?, ?, ?, ?, ?, ?, ?, "
			+ "?, ?)";

	private static final String UPDATE = "UPDATE motel SET id_propietario = ?, id_ciudad = ?, "
			+ "nombre = ?, descripcion = ?, calle = ?, carrera = ?, latitud = ?, longitud = ?, "
			+ "estado = ? WHERE id_motel = ?";

	private static final String DELETE = "DELETE FROM motel WHERE id_motel = ?";

	private final DataSource dataSource;

	public MotelRepository(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * Retorna todos los moteles.
	 *
	 * @return lista con todos los moteles
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public List<Motel> findAll() throws SQLException {
		return listar(SELECT_TODOS);
	}

	/**
	 * Busca un motel por su identificador.
	 *
	 * @param id identificador del motel
	 * @return {@link Optional} con el motel encontrado, o vacío si no existe
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public Optional<Motel> findById(Long id) throws SQLException {
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
	 * Retorna los moteles activos, que son los visibles en el catálogo público.
	 *
	 * @return lista de moteles activos ordenados por nombre
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public List<Motel> findActivos() throws SQLException {
		return listar(SELECT_ACTIVOS);
	}

	/**
	 * Retorna los moteles activos de una ciudad.
	 *
	 * @param idCiudad identificador de la ciudad
	 * @return lista de moteles activos de la ciudad
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public List<Motel> findActivosByCiudad(Long idCiudad) throws SQLException {
		return listar(SELECT_ACTIVOS_POR_CIUDAD, idCiudad);
	}

	/**
	 * Retorna todos los moteles de un propietario, activos o no.
	 *
	 * @param idPropietario identificador del propietario
	 * @return lista de moteles del propietario
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public List<Motel> findByPropietario(Long idPropietario) throws SQLException {
		return listar(SELECT_POR_PROPIETARIO, idPropietario);
	}

	/**
	 * Actualiza únicamente el estado de un motel.
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
	 * Verifica si existe un motel con el identificador indicado.
	 *
	 * @param id identificador a verificar
	 * @return {@code true} si existe, {@code false} en caso contrario
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public boolean existsById(Long id) throws SQLException {
		return contar("SELECT COUNT(*) FROM motel WHERE id_motel = ?", id) > 0;
	}

	/**
	 * Retorna el total de moteles registrados.
	 *
	 * @return cantidad total de moteles
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public long count() throws SQLException {
		return contar("SELECT COUNT(*) FROM motel");
	}

	/**
	 * Inserta un nuevo motel y retorna el identificador generado por
	 * {@code AUTO_INCREMENT}.
	 *
	 * @param m motel a insertar
	 * @return identificador generado para el nuevo registro
	 * @throws SQLException si ocurre un error de acceso a la base de datos o se
	 *                      viola una restricción de la tabla
	 */
	public Long insertar(Motel m) throws SQLException {
		try (Connection con = dataSource.getConnection();
				PreparedStatement ps = con.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
			ps.setLong(1, m.getIdPropietario());
			ps.setLong(2, m.getIdCiudad());
			ps.setString(3, m.getNombre());
			ps.setString(4, m.getDescripcion());
			ps.setString(5, m.getCalle());
			ps.setString(6, m.getCarrera());
			ps.setBigDecimal(7, m.getLatitud());
			ps.setBigDecimal(8, m.getLongitud());
			ps.setString(9, m.getEstado().name());
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
	 * Actualiza los campos editables de un motel existente.
	 *
	 * @param m motel con los nuevos datos y su identificador
	 * @return cantidad de filas afectadas
	 * @throws SQLException si ocurre un error de acceso a la base de datos o se
	 *                      viola una restricción de la tabla
	 */
	public int actualizar(Motel m) throws SQLException {
		try (Connection con = dataSource.getConnection(); PreparedStatement ps = con.prepareStatement(UPDATE)) {
			ps.setLong(1, m.getIdPropietario());
			ps.setLong(2, m.getIdCiudad());
			ps.setString(3, m.getNombre());
			ps.setString(4, m.getDescripcion());
			ps.setString(5, m.getCalle());
			ps.setString(6, m.getCarrera());
			ps.setBigDecimal(7, m.getLatitud());
			ps.setBigDecimal(8, m.getLongitud());
			ps.setString(9, m.getEstado().name());
			ps.setLong(10, m.getIdMotel());
			return ps.executeUpdate();
		}
	}

	/**
	 * Elimina un motel según su identificador.
	 *
	 * @param id identificador del motel a eliminar
	 * @return cantidad de filas afectadas
	 * @throws SQLException si ocurre un error de acceso a la base de datos o
	 *                      el motel tiene registros que dependen de él
	 */
	public int eliminar(Long id) throws SQLException {
		try (Connection con = dataSource.getConnection(); PreparedStatement ps = con.prepareStatement(DELETE)) {
			ps.setLong(1, id);
			return ps.executeUpdate();
		}
	}

	/**
	 * Convierte la fila actual del {@link ResultSet} en una entidad
	 * {@link Motel}, leyendo cada columna por su nombre.
	 *
	 * @param rs resultado posicionado en la fila a convertir
	 * @return motel con los datos de la fila
	 * @throws SQLException si alguna columna no existe o no puede leerse
	 */
	private Motel mapear(ResultSet rs) throws SQLException {
		Motel m = new Motel();
		m.setIdMotel(rs.getLong("id_motel"));
		m.setIdPropietario(rs.getLong("id_propietario"));
		m.setIdCiudad(rs.getLong("id_ciudad"));
		m.setNombre(rs.getString("nombre"));
		m.setDescripcion(rs.getString("descripcion"));
		m.setCalle(rs.getString("calle"));
		m.setCarrera(rs.getString("carrera"));
		m.setLatitud(rs.getBigDecimal("latitud"));
		m.setLongitud(rs.getBigDecimal("longitud"));
		m.setEstado(Estado.valueOf(rs.getString("estado")));
		return m;
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
	private List<Motel> listar(String sql, Object... parametros) throws SQLException {
		try (Connection con = dataSource.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
			for (int i = 0; i < parametros.length; i++) {
				ps.setObject(i + 1, parametros[i]);
			}
			try (ResultSet rs = ps.executeQuery()) {
				List<Motel> lista = new ArrayList<>();
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
