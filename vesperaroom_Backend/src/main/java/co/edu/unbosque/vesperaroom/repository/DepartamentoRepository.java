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

import co.edu.unbosque.vesperaroom.entity.Departamento;

/**
 * Repositorio para la gestión de departamentos en la tabla {@code departamento}, implementado
 * con JDBC puro: cada método obtiene una {@link Connection} del
 * {@link DataSource}, prepara la sentencia SQL con {@link PreparedStatement},
 * asigna los parámetros, la ejecuta y recorre el {@link ResultSet}. Los
 * recursos se cierran con try-with-resources.
 */
@Repository
public class DepartamentoRepository {

	private static final String SELECT_BASE = "SELECT d.id_departamento, d.nombre FROM departamento "
			+ "d ";

	private static final String SELECT_TODOS = SELECT_BASE + "ORDER BY d.nombre";

	private static final String SELECT_POR_ID = SELECT_BASE + "WHERE d.id_departamento = ?";

	private static final String INSERT = "INSERT INTO departamento (nombre) VALUES (?)";

	private static final String UPDATE = "UPDATE departamento SET nombre = ? WHERE id_departamento "
			+ "= ?";

	private static final String DELETE = "DELETE FROM departamento WHERE id_departamento = ?";

	private final DataSource dataSource;

	public DepartamentoRepository(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * Retorna todos los departamentos.
	 *
	 * @return lista con todos los departamentos
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public List<Departamento> findAll() throws SQLException {
		return listar(SELECT_TODOS);
	}

	/**
	 * Busca un departamento por su identificador.
	 *
	 * @param id identificador del departamento
	 * @return {@link Optional} con el departamento encontrado, o vacío si no existe
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public Optional<Departamento> findById(Long id) throws SQLException {
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
	 * Verifica si ya existe un departamento con el nombre indicado.
	 *
	 * @param nombre nombre a verificar
	 * @return {@code true} si existe
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public boolean existsByNombre(String nombre) throws SQLException {
		return contar("SELECT COUNT(*) FROM departamento WHERE nombre = ?", nombre) > 0;
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
		return contar("SELECT COUNT(*) FROM departamento WHERE nombre = ? AND id_departamento <> ?", nombre, id) > 0;
	}

	/**
	 * Verifica si existe un departamento con el identificador indicado.
	 *
	 * @param id identificador a verificar
	 * @return {@code true} si existe, {@code false} en caso contrario
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public boolean existsById(Long id) throws SQLException {
		return contar("SELECT COUNT(*) FROM departamento WHERE id_departamento = ?", id) > 0;
	}

	/**
	 * Retorna el total de departamentos registrados.
	 *
	 * @return cantidad total de departamentos
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public long count() throws SQLException {
		return contar("SELECT COUNT(*) FROM departamento");
	}

	/**
	 * Inserta un nuevo departamento y retorna el identificador generado por
	 * {@code AUTO_INCREMENT}.
	 *
	 * @param d departamento a insertar
	 * @return identificador generado para el nuevo registro
	 * @throws SQLException si ocurre un error de acceso a la base de datos o se
	 *                      viola una restricción de la tabla
	 */
	public Long insertar(Departamento d) throws SQLException {
		try (Connection con = dataSource.getConnection();
				PreparedStatement ps = con.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
			ps.setString(1, d.getNombre());
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
	 * Actualiza los campos editables de un departamento existente.
	 *
	 * @param d departamento con los nuevos datos y su identificador
	 * @return cantidad de filas afectadas
	 * @throws SQLException si ocurre un error de acceso a la base de datos o se
	 *                      viola una restricción de la tabla
	 */
	public int actualizar(Departamento d) throws SQLException {
		try (Connection con = dataSource.getConnection(); PreparedStatement ps = con.prepareStatement(UPDATE)) {
			ps.setString(1, d.getNombre());
			ps.setLong(2, d.getIdDepartamento());
			return ps.executeUpdate();
		}
	}

	/**
	 * Elimina un departamento según su identificador.
	 *
	 * @param id identificador del departamento a eliminar
	 * @return cantidad de filas afectadas
	 * @throws SQLException si ocurre un error de acceso a la base de datos o
	 *                      el departamento tiene registros que dependen de él
	 */
	public int eliminar(Long id) throws SQLException {
		try (Connection con = dataSource.getConnection(); PreparedStatement ps = con.prepareStatement(DELETE)) {
			ps.setLong(1, id);
			return ps.executeUpdate();
		}
	}

	/**
	 * Convierte la fila actual del {@link ResultSet} en una entidad
	 * {@link Departamento}, leyendo cada columna por su nombre.
	 *
	 * @param rs resultado posicionado en la fila a convertir
	 * @return departamento con los datos de la fila
	 * @throws SQLException si alguna columna no existe o no puede leerse
	 */
	private Departamento mapear(ResultSet rs) throws SQLException {
		Departamento d = new Departamento();
		d.setIdDepartamento(rs.getLong("id_departamento"));
		d.setNombre(rs.getString("nombre"));
		return d;
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
	private List<Departamento> listar(String sql, Object... parametros) throws SQLException {
		try (Connection con = dataSource.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
			for (int i = 0; i < parametros.length; i++) {
				ps.setObject(i + 1, parametros[i]);
			}
			try (ResultSet rs = ps.executeQuery()) {
				List<Departamento> lista = new ArrayList<>();
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
