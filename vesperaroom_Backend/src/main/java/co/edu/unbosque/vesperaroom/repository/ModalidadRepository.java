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

import co.edu.unbosque.vesperaroom.entity.Modalidad;

/**
 * Repositorio para la gestión de modalidades en la tabla {@code modalidad}, implementado
 * con JDBC puro: cada método obtiene una {@link Connection} del
 * {@link DataSource}, prepara la sentencia SQL con {@link PreparedStatement},
 * asigna los parámetros, la ejecuta y recorre el {@link ResultSet}. Los
 * recursos se cierran con try-with-resources.
 */
@Repository
public class ModalidadRepository {

	private static final String SELECT_BASE = "SELECT mo.id_modalidad, mo.nombre, mo.duracion_horas "
			+ "FROM modalidad mo ";

	private static final String SELECT_TODOS = SELECT_BASE + "ORDER BY mo.duracion_horas";

	private static final String SELECT_POR_ID = SELECT_BASE + "WHERE mo.id_modalidad = ?";

	private static final String INSERT = "INSERT INTO modalidad (nombre, duracion_horas) VALUES (?, "
			+ "?)";

	private static final String UPDATE = "UPDATE modalidad SET nombre = ?, duracion_horas = ? WHERE "
			+ "id_modalidad = ?";

	private static final String DELETE = "DELETE FROM modalidad WHERE id_modalidad = ?";

	private final DataSource dataSource;

	public ModalidadRepository(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * Retorna todas las modalidades.
	 *
	 * @return lista con todas las modalidades
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public List<Modalidad> findAll() throws SQLException {
		return listar(SELECT_TODOS);
	}

	/**
	 * Busca una modalidad por su identificador.
	 *
	 * @param id identificador de la modalidad
	 * @return {@link Optional} con la modalidad encontrada, o vacío si no existe
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public Optional<Modalidad> findById(Long id) throws SQLException {
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
	 * Verifica si ya existe una modalidad con el nombre indicado.
	 *
	 * @param nombre nombre a verificar
	 * @return {@code true} si existe
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public boolean existsByNombre(String nombre) throws SQLException {
		return contar("SELECT COUNT(*) FROM modalidad WHERE nombre = ?", nombre) > 0;
	}

	/**
	 * Verifica si el nombre pertenece a un registro distinto al indicado. Se usa al actualizar para
	 * permitir que el registro conserve su propio nombre.
	 *
	 * @param nombre nombre a verificar
	 * @param id identificador que se excluye de la búsqueda
	 * @return {@code true} si existe
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public boolean existsByNombreAndIdNot(String nombre, Long id) throws SQLException {
		return contar("SELECT COUNT(*) FROM modalidad WHERE nombre = ? AND id_modalidad <> ?", nombre, id) > 0;
	}

	/**
	 * Verifica si existe una modalidad con el identificador indicado.
	 *
	 * @param id identificador a verificar
	 * @return {@code true} si existe, {@code false} en caso contrario
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public boolean existsById(Long id) throws SQLException {
		return contar("SELECT COUNT(*) FROM modalidad WHERE id_modalidad = ?", id) > 0;
	}

	/**
	 * Retorna el total de modalidades registradas.
	 *
	 * @return cantidad total de modalidades
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public long count() throws SQLException {
		return contar("SELECT COUNT(*) FROM modalidad");
	}

	/**
	 * Inserta una nueva modalidad y retorna el identificador generado por
	 * {@code AUTO_INCREMENT}.
	 *
	 * @param m modalidad a insertar
	 * @return identificador generado para el nuevo registro
	 * @throws SQLException si ocurre un error de acceso a la base de datos o se
	 *                      viola una restricción de la tabla
	 */
	public Long insertar(Modalidad m) throws SQLException {
		try (Connection con = dataSource.getConnection();
				PreparedStatement ps = con.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
			ps.setString(1, m.getNombre());
			ps.setInt(2, m.getDuracionHoras());
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
	 * Actualiza los campos editables de una modalidad existente.
	 *
	 * @param m modalidad con los nuevos datos y su identificador
	 * @return cantidad de filas afectadas
	 * @throws SQLException si ocurre un error de acceso a la base de datos o se
	 *                      viola una restricción de la tabla
	 */
	public int actualizar(Modalidad m) throws SQLException {
		try (Connection con = dataSource.getConnection(); PreparedStatement ps = con.prepareStatement(UPDATE)) {
			ps.setString(1, m.getNombre());
			ps.setInt(2, m.getDuracionHoras());
			ps.setLong(3, m.getIdModalidad());
			return ps.executeUpdate();
		}
	}

	/**
	 * Elimina una modalidad según su identificador.
	 *
	 * @param id identificador de la modalidad a eliminar
	 * @return cantidad de filas afectadas
	 * @throws SQLException si ocurre un error de acceso a la base de datos o
	 *                      la modalidad tiene registros que dependen de ella
	 */
	public int eliminar(Long id) throws SQLException {
		try (Connection con = dataSource.getConnection(); PreparedStatement ps = con.prepareStatement(DELETE)) {
			ps.setLong(1, id);
			return ps.executeUpdate();
		}
	}

	/**
	 * Convierte la fila actual del {@link ResultSet} en una entidad
	 * {@link Modalidad}, leyendo cada columna por su nombre.
	 *
	 * @param rs resultado posicionado en la fila a convertir
	 * @return modalidad con los datos de la fila
	 * @throws SQLException si alguna columna no existe o no puede leerse
	 */
	private Modalidad mapear(ResultSet rs) throws SQLException {
		Modalidad m = new Modalidad();
		m.setIdModalidad(rs.getLong("id_modalidad"));
		m.setNombre(rs.getString("nombre"));
		m.setDuracionHoras(rs.getInt("duracion_horas"));
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
	private List<Modalidad> listar(String sql, Object... parametros) throws SQLException {
		try (Connection con = dataSource.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
			for (int i = 0; i < parametros.length; i++) {
				ps.setObject(i + 1, parametros[i]);
			}
			try (ResultSet rs = ps.executeQuery()) {
				List<Modalidad> lista = new ArrayList<>();
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
