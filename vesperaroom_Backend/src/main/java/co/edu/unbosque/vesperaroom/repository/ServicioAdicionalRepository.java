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

import co.edu.unbosque.vesperaroom.entity.ServicioAdicional;

/**
 * Repositorio para la gestión de servicios adicionales en la tabla {@code servicio_adicional}, implementado
 * con JDBC puro: cada método obtiene una {@link Connection} del
 * {@link DataSource}, prepara la sentencia SQL con {@link PreparedStatement},
 * asigna los parámetros, la ejecuta y recorre el {@link ResultSet}. Los
 * recursos se cierran con try-with-resources.
 */
@Repository
public class ServicioAdicionalRepository {

	private static final String SELECT_BASE = "SELECT s.id_servicio, s.id_motel, s.nombre, "
			+ "s.descripcion, s.precio, s.activo FROM servicio_adicional s ";

	private static final String SELECT_TODOS = SELECT_BASE + "ORDER BY s.nombre";

	private static final String SELECT_POR_ID = SELECT_BASE + "WHERE s.id_servicio = ?";

	private static final String SELECT_POR_MOTEL = SELECT_BASE + "WHERE s.id_motel = ? ORDER BY "
			+ "s.nombre";

	private static final String SELECT_ACTIVOS_POR_MOTEL = SELECT_BASE + "WHERE s.id_motel = ? AND "
			+ "s.activo = TRUE ORDER BY s.nombre";

	private static final String INSERT = "INSERT INTO servicio_adicional (id_motel, nombre, "
			+ "descripcion, precio, activo) VALUES (?, ?, ?, ?, ?)";

	private static final String UPDATE = "UPDATE servicio_adicional SET id_motel = ?, nombre = ?, "
			+ "descripcion = ?, precio = ?, activo = ? WHERE id_servicio = ?";

	private static final String DELETE = "DELETE FROM servicio_adicional WHERE id_servicio = ?";

	private final DataSource dataSource;

	public ServicioAdicionalRepository(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * Retorna todos los servicios adicionales.
	 *
	 * @return lista con todos los servicios adicionales
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public List<ServicioAdicional> findAll() throws SQLException {
		return listar(SELECT_TODOS);
	}

	/**
	 * Busca un servicio adicional por su identificador.
	 *
	 * @param id identificador del servicio adicional
	 * @return {@link Optional} con el servicio adicional encontrado, o vacío si no existe
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public Optional<ServicioAdicional> findById(Long id) throws SQLException {
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
	 * Retorna todos los servicios de un motel, activos o no.
	 *
	 * @param idMotel identificador del motel
	 * @return lista de servicios del motel
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public List<ServicioAdicional> findByMotel(Long idMotel) throws SQLException {
		return listar(SELECT_POR_MOTEL, idMotel);
	}

	/**
	 * Retorna los servicios activos de un motel.
	 *
	 * @param idMotel identificador del motel
	 * @return lista de servicios activos del motel
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public List<ServicioAdicional> findActivosByMotel(Long idMotel) throws SQLException {
		return listar(SELECT_ACTIVOS_POR_MOTEL, idMotel);
	}

	/**
	 * Verifica si el motel ya tiene un servicio con el nombre indicado.
	 *
	 * @param idMotel identificador del motel
	 * @param nombre nombre a verificar
	 * @return {@code true} si existe
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public boolean existsByMotelAndNombre(Long idMotel, String nombre) throws SQLException {
		return contar("SELECT COUNT(*) FROM servicio_adicional WHERE id_motel = ? AND nombre = ?", idMotel, nombre) > 0;
	}

	/**
	 * Verifica si el nombre ya lo usa otro registro del mismo motel, excluyendo el indicado.
	 *
	 * @param idMotel identificador del motel
	 * @param nombre nombre a verificar
	 * @param id identificador que se excluye de la búsqueda
	 * @return {@code true} si existe
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public boolean existsByMotelAndNombreAndIdNot(Long idMotel, String nombre, Long id) throws SQLException {
		return contar("SELECT COUNT(*) FROM servicio_adicional WHERE id_motel = ? AND nombre = ? AND id_servicio <> ?", idMotel, nombre, id) > 0;
	}

	/**
	 * Verifica si existe un servicio adicional con el identificador indicado.
	 *
	 * @param id identificador a verificar
	 * @return {@code true} si existe, {@code false} en caso contrario
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public boolean existsById(Long id) throws SQLException {
		return contar("SELECT COUNT(*) FROM servicio_adicional WHERE id_servicio = ?", id) > 0;
	}

	/**
	 * Retorna el total de servicios adicionales registrados.
	 *
	 * @return cantidad total de servicios adicionales
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public long count() throws SQLException {
		return contar("SELECT COUNT(*) FROM servicio_adicional");
	}

	/**
	 * Inserta un nuevo servicio adicional y retorna el identificador generado por
	 * {@code AUTO_INCREMENT}.
	 *
	 * @param s servicio adicional a insertar
	 * @return identificador generado para el nuevo registro
	 * @throws SQLException si ocurre un error de acceso a la base de datos o se
	 *                      viola una restricción de la tabla
	 */
	public Long insertar(ServicioAdicional s) throws SQLException {
		try (Connection con = dataSource.getConnection();
				PreparedStatement ps = con.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
			ps.setLong(1, s.getIdMotel());
			ps.setString(2, s.getNombre());
			ps.setString(3, s.getDescripcion());
			ps.setBigDecimal(4, s.getPrecio());
			ps.setBoolean(5, s.getActivo());
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
	 * Actualiza los campos editables de un servicio adicional existente.
	 *
	 * @param s servicio adicional con los nuevos datos y su identificador
	 * @return cantidad de filas afectadas
	 * @throws SQLException si ocurre un error de acceso a la base de datos o se
	 *                      viola una restricción de la tabla
	 */
	public int actualizar(ServicioAdicional s) throws SQLException {
		try (Connection con = dataSource.getConnection(); PreparedStatement ps = con.prepareStatement(UPDATE)) {
			ps.setLong(1, s.getIdMotel());
			ps.setString(2, s.getNombre());
			ps.setString(3, s.getDescripcion());
			ps.setBigDecimal(4, s.getPrecio());
			ps.setBoolean(5, s.getActivo());
			ps.setLong(6, s.getIdServicio());
			return ps.executeUpdate();
		}
	}

	/**
	 * Elimina un servicio adicional según su identificador.
	 *
	 * @param id identificador del servicio adicional a eliminar
	 * @return cantidad de filas afectadas
	 * @throws SQLException si ocurre un error de acceso a la base de datos o
	 *                      el servicio adicional tiene registros que dependen de él
	 */
	public int eliminar(Long id) throws SQLException {
		try (Connection con = dataSource.getConnection(); PreparedStatement ps = con.prepareStatement(DELETE)) {
			ps.setLong(1, id);
			return ps.executeUpdate();
		}
	}

	/**
	 * Convierte la fila actual del {@link ResultSet} en una entidad
	 * {@link ServicioAdicional}, leyendo cada columna por su nombre.
	 *
	 * @param rs resultado posicionado en la fila a convertir
	 * @return servicio adicional con los datos de la fila
	 * @throws SQLException si alguna columna no existe o no puede leerse
	 */
	private ServicioAdicional mapear(ResultSet rs) throws SQLException {
		ServicioAdicional s = new ServicioAdicional();
		s.setIdServicio(rs.getLong("id_servicio"));
		s.setIdMotel(rs.getLong("id_motel"));
		s.setNombre(rs.getString("nombre"));
		s.setDescripcion(rs.getString("descripcion"));
		s.setPrecio(rs.getBigDecimal("precio"));
		s.setActivo(rs.getBoolean("activo"));
		return s;
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
	private List<ServicioAdicional> listar(String sql, Object... parametros) throws SQLException {
		try (Connection con = dataSource.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
			for (int i = 0; i < parametros.length; i++) {
				ps.setObject(i + 1, parametros[i]);
			}
			try (ResultSet rs = ps.executeQuery()) {
				List<ServicioAdicional> lista = new ArrayList<>();
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
