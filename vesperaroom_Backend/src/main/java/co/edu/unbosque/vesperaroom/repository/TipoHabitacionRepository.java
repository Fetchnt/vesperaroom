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

import co.edu.unbosque.vesperaroom.entity.Caracteristica;
import co.edu.unbosque.vesperaroom.entity.TipoHabitacion;

/**
 * Repositorio para la gestión de tipos de habitación en la tabla {@code tipo_habitacion}, implementado
 * con JDBC puro: cada método obtiene una {@link Connection} del
 * {@link DataSource}, prepara la sentencia SQL con {@link PreparedStatement},
 * asigna los parámetros, la ejecuta y recorre el {@link ResultSet}. Los
 * recursos se cierran con try-with-resources. También administra la tabla intermedia
 * {@code tipo_habitacion_caracteristica}, que resuelve la relación N:M con
 * {@link Caracteristica}.
 */
@Repository
public class TipoHabitacionRepository {

	private static final String SELECT_BASE = "SELECT t.id_tipo, t.id_motel, t.nombre, "
			+ "t.capacidad_personas, t.descripcion FROM tipo_habitacion t ";

	private static final String SELECT_TODOS = SELECT_BASE + "ORDER BY t.nombre";

	private static final String SELECT_POR_ID = SELECT_BASE + "WHERE t.id_tipo = ?";

	private static final String SELECT_POR_MOTEL = SELECT_BASE + "WHERE t.id_motel = ? ORDER BY "
			+ "t.nombre";

	private static final String SELECT_CARACTERISTICAS = "SELECT c.id_caracteristica, c.nombre, "
			+ "c.icono FROM caracteristica c JOIN tipo_habitacion_caracteristica thc ON "
			+ "thc.id_caracteristica = c.id_caracteristica WHERE thc.id_tipo = ? ORDER BY c.nombre";

	private static final String INSERT_CARACTERISTICA = "INSERT INTO tipo_habitacion_caracteristica "
			+ "(id_tipo, id_caracteristica) VALUES (?, ?)";

	private static final String DELETE_CARACTERISTICA = "DELETE FROM tipo_habitacion_caracteristica "
			+ "WHERE id_tipo = ? AND id_caracteristica = ?";

	private static final String INSERT = "INSERT INTO tipo_habitacion (id_motel, nombre, "
			+ "capacidad_personas, descripcion) VALUES (?, ?, ?, ?)";

	private static final String UPDATE = "UPDATE tipo_habitacion SET nombre = ?, capacidad_personas "
			+ "= ?, descripcion = ? WHERE id_tipo = ?";

	private static final String DELETE = "DELETE FROM tipo_habitacion WHERE id_tipo = ?";

	private final DataSource dataSource;

	public TipoHabitacionRepository(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * Retorna todos los tipos de habitación.
	 *
	 * @return lista con todos los tipos de habitación
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public List<TipoHabitacion> findAll() throws SQLException {
		return listar(SELECT_TODOS);
	}

	/**
	 * Busca un tipo de habitación por su identificador.
	 *
	 * @param id identificador del tipo de habitación
	 * @return {@link Optional} con el tipo de habitación encontrado, o vacío si no existe
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public Optional<TipoHabitacion> findById(Long id) throws SQLException {
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
	 * Retorna los tipos de habitación de un motel.
	 *
	 * @param idMotel identificador del motel
	 * @return lista de tipos del motel
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public List<TipoHabitacion> findByMotel(Long idMotel) throws SQLException {
		return listar(SELECT_POR_MOTEL, idMotel);
	}

	/**
	 * Verifica si el motel ya tiene un tipo con el nombre indicado.
	 *
	 * @param idMotel identificador del motel
	 * @param nombre nombre a verificar
	 * @return {@code true} si existe
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public boolean existsByMotelAndNombre(Long idMotel, String nombre) throws SQLException {
		return contar("SELECT COUNT(*) FROM tipo_habitacion WHERE id_motel = ? AND nombre = ?", idMotel, nombre) > 0;
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
		return contar("SELECT COUNT(*) FROM tipo_habitacion WHERE id_motel = ? AND nombre = ? AND id_tipo <> ?", idMotel, nombre, id) > 0;
	}

	/**
	 * Retorna las características asignadas a un tipo de habitación, uniendo la
	 * tabla intermedia con el catálogo de características.
	 *
	 * @param idTipo identificador del tipo de habitación
	 * @return lista de características del tipo ordenadas por nombre
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public List<Caracteristica> findCaracteristicas(Long idTipo) throws SQLException {
		try (Connection con = dataSource.getConnection();
				PreparedStatement ps = con.prepareStatement(SELECT_CARACTERISTICAS)) {
			ps.setLong(1, idTipo);
			try (ResultSet rs = ps.executeQuery()) {
				List<Caracteristica> lista = new ArrayList<>();
				while (rs.next()) {
					Caracteristica c = new Caracteristica();
					c.setIdCaracteristica(rs.getLong("id_caracteristica"));
					c.setNombre(rs.getString("nombre"));
					c.setIcono(rs.getString("icono"));
					lista.add(c);
				}
				return lista;
			}
		}
	}

	/**
	 * Verifica si una característica ya está asignada al tipo de habitación.
	 *
	 * @param idTipo identificador del tipo de habitación
	 * @param idCaracteristica identificador de la característica
	 * @return {@code true} si existe
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public boolean existsCaracteristica(Long idTipo, Long idCaracteristica) throws SQLException {
		return contar("SELECT COUNT(*) FROM tipo_habitacion_caracteristica WHERE id_tipo = ? AND id_caracteristica = ?", idTipo, idCaracteristica) > 0;
	}

	/**
	 * Asigna una característica a un tipo de habitación insertando la pareja en la
	 * tabla intermedia.
	 *
	 * @param idTipo           identificador del tipo de habitación
	 * @param idCaracteristica identificador de la característica
	 * @return cantidad de filas afectadas
	 * @throws SQLException si ocurre un error de acceso a la base de datos o se
	 *                      viola una restricción de la tabla
	 */
	public int asignarCaracteristica(Long idTipo, Long idCaracteristica) throws SQLException {
		try (Connection con = dataSource.getConnection();
				PreparedStatement ps = con.prepareStatement(INSERT_CARACTERISTICA)) {
			ps.setLong(1, idTipo);
			ps.setLong(2, idCaracteristica);
			return ps.executeUpdate();
		}
	}

	/**
	 * Quita una característica de un tipo de habitación.
	 *
	 * @param idTipo           identificador del tipo de habitación
	 * @param idCaracteristica identificador de la característica
	 * @return cantidad de filas afectadas (0 si no estaba asignada)
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public int quitarCaracteristica(Long idTipo, Long idCaracteristica) throws SQLException {
		try (Connection con = dataSource.getConnection();
				PreparedStatement ps = con.prepareStatement(DELETE_CARACTERISTICA)) {
			ps.setLong(1, idTipo);
			ps.setLong(2, idCaracteristica);
			return ps.executeUpdate();
		}
	}

	/**
	 * Verifica si existe un tipo de habitación con el identificador indicado.
	 *
	 * @param id identificador a verificar
	 * @return {@code true} si existe, {@code false} en caso contrario
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public boolean existsById(Long id) throws SQLException {
		return contar("SELECT COUNT(*) FROM tipo_habitacion WHERE id_tipo = ?", id) > 0;
	}

	/**
	 * Retorna el total de tipos de habitación registrados.
	 *
	 * @return cantidad total de tipos de habitación
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public long count() throws SQLException {
		return contar("SELECT COUNT(*) FROM tipo_habitacion");
	}

	/**
	 * Inserta un nuevo tipo de habitación y retorna el identificador generado por
	 * {@code AUTO_INCREMENT}.
	 *
	 * @param t tipo de habitación a insertar
	 * @return identificador generado para el nuevo registro
	 * @throws SQLException si ocurre un error de acceso a la base de datos o se
	 *                      viola una restricción de la tabla
	 */
	public Long insertar(TipoHabitacion t) throws SQLException {
		try (Connection con = dataSource.getConnection();
				PreparedStatement ps = con.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
			ps.setLong(1, t.getIdMotel());
			ps.setString(2, t.getNombre());
			ps.setInt(3, t.getCapacidadPersonas());
			ps.setString(4, t.getDescripcion());
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
	 * Actualiza los campos editables de un tipo de habitación existente.
	 *
	 * @param t tipo de habitación con los nuevos datos y su identificador
	 * @return cantidad de filas afectadas
	 * @throws SQLException si ocurre un error de acceso a la base de datos o se
	 *                      viola una restricción de la tabla
	 */
	public int actualizar(TipoHabitacion t) throws SQLException {
		try (Connection con = dataSource.getConnection(); PreparedStatement ps = con.prepareStatement(UPDATE)) {
			ps.setString(1, t.getNombre());
			ps.setInt(2, t.getCapacidadPersonas());
			ps.setString(3, t.getDescripcion());
			ps.setLong(4, t.getIdTipo());
			return ps.executeUpdate();
		}
	}

	/**
	 * Elimina un tipo de habitación según su identificador.
	 *
	 * @param id identificador del tipo de habitación a eliminar
	 * @return cantidad de filas afectadas
	 * @throws SQLException si ocurre un error de acceso a la base de datos o
	 *                      el tipo de habitación tiene registros que dependen de él
	 */
	public int eliminar(Long id) throws SQLException {
		try (Connection con = dataSource.getConnection(); PreparedStatement ps = con.prepareStatement(DELETE)) {
			ps.setLong(1, id);
			return ps.executeUpdate();
		}
	}

	/**
	 * Convierte la fila actual del {@link ResultSet} en una entidad
	 * {@link TipoHabitacion}, leyendo cada columna por su nombre.
	 *
	 * @param rs resultado posicionado en la fila a convertir
	 * @return tipo de habitación con los datos de la fila
	 * @throws SQLException si alguna columna no existe o no puede leerse
	 */
	private TipoHabitacion mapear(ResultSet rs) throws SQLException {
		TipoHabitacion t = new TipoHabitacion();
		t.setIdTipo(rs.getLong("id_tipo"));
		t.setIdMotel(rs.getLong("id_motel"));
		t.setNombre(rs.getString("nombre"));
		t.setCapacidadPersonas(rs.getInt("capacidad_personas"));
		t.setDescripcion(rs.getString("descripcion"));
		return t;
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
	private List<TipoHabitacion> listar(String sql, Object... parametros) throws SQLException {
		try (Connection con = dataSource.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
			for (int i = 0; i < parametros.length; i++) {
				ps.setObject(i + 1, parametros[i]);
			}
			try (ResultSet rs = ps.executeQuery()) {
				List<TipoHabitacion> lista = new ArrayList<>();
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
