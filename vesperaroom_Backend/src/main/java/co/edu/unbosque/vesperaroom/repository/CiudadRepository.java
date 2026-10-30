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

import co.edu.unbosque.vesperaroom.entity.Ciudad;

/**
 * Repositorio para la gestión de ciudades en la tabla {@code ciudad}, implementado
 * con JDBC puro: cada método obtiene una {@link Connection} del
 * {@link DataSource}, prepara la sentencia SQL con {@link PreparedStatement},
 * asigna los parámetros, la ejecuta y recorre el {@link ResultSet}. Los
 * recursos se cierran con try-with-resources.
 */
@Repository
public class CiudadRepository {

	private static final String SELECT_BASE = "SELECT c.id_ciudad, c.id_departamento, c.nombre FROM "
			+ "ciudad c ";

	private static final String SELECT_TODOS = SELECT_BASE + "ORDER BY c.nombre";

	private static final String SELECT_POR_ID = SELECT_BASE + "WHERE c.id_ciudad = ?";

	private static final String SELECT_POR_DEPARTAMENTO = SELECT_BASE + "WHERE c.id_departamento = "
			+ "? ORDER BY c.nombre";

	private static final String INSERT = "INSERT INTO ciudad (id_departamento, nombre) VALUES (?, "
			+ "?)";

	private static final String UPDATE = "UPDATE ciudad SET id_departamento = ?, nombre = ? WHERE "
			+ "id_ciudad = ?";

	private static final String DELETE = "DELETE FROM ciudad WHERE id_ciudad = ?";

	private final DataSource dataSource;

	public CiudadRepository(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * Retorna todas las ciudades.
	 *
	 * @return lista con todas las ciudades
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public List<Ciudad> findAll() throws SQLException {
		return listar(SELECT_TODOS);
	}

	/**
	 * Busca una ciudad por su identificador.
	 *
	 * @param id identificador de la ciudad
	 * @return {@link Optional} con la ciudad encontrada, o vacío si no existe
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public Optional<Ciudad> findById(Long id) throws SQLException {
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
	 * Retorna las ciudades de un departamento ordenadas por nombre.
	 *
	 * @param idDepartamento identificador del departamento
	 * @return lista de ciudades del departamento
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public List<Ciudad> findByDepartamento(Long idDepartamento) throws SQLException {
		return listar(SELECT_POR_DEPARTAMENTO, idDepartamento);
	}

	/**
	 * Verifica si el departamento ya tiene una ciudad con el nombre indicado.
	 *
	 * @param idDepartamento identificador del departamento
	 * @param nombre nombre a verificar
	 * @return {@code true} si existe
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public boolean existsByDepartamentoAndNombre(Long idDepartamento, String nombre) throws SQLException {
		return contar("SELECT COUNT(*) FROM ciudad WHERE id_departamento = ? AND nombre = ?", idDepartamento, nombre) > 0;
	}

	/**
	 * Verifica si el nombre ya lo usa otra ciudad del mismo departamento, excluyendo la indicada.
	 *
	 * @param idDepartamento identificador del departamento
	 * @param nombre nombre a verificar
	 * @param id identificador que se excluye de la búsqueda
	 * @return {@code true} si existe
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public boolean existsByDepartamentoAndNombreAndIdNot(Long idDepartamento, String nombre, Long id) throws SQLException {
		return contar("SELECT COUNT(*) FROM ciudad WHERE id_departamento = ? AND nombre = ? AND id_ciudad <> ?", idDepartamento, nombre, id) > 0;
	}

	/**
	 * Verifica si existe una ciudad con el identificador indicado.
	 *
	 * @param id identificador a verificar
	 * @return {@code true} si existe, {@code false} en caso contrario
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public boolean existsById(Long id) throws SQLException {
		return contar("SELECT COUNT(*) FROM ciudad WHERE id_ciudad = ?", id) > 0;
	}

	/**
	 * Retorna el total de ciudades registradas.
	 *
	 * @return cantidad total de ciudades
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public long count() throws SQLException {
		return contar("SELECT COUNT(*) FROM ciudad");
	}

	/**
	 * Inserta una nueva ciudad y retorna el identificador generado por
	 * {@code AUTO_INCREMENT}.
	 *
	 * @param c ciudad a insertar
	 * @return identificador generado para el nuevo registro
	 * @throws SQLException si ocurre un error de acceso a la base de datos o se
	 *                      viola una restricción de la tabla
	 */
	public Long insertar(Ciudad c) throws SQLException {
		try (Connection con = dataSource.getConnection();
				PreparedStatement ps = con.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
			ps.setLong(1, c.getIdDepartamento());
			ps.setString(2, c.getNombre());
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
	 * Actualiza los campos editables de una ciudad existente.
	 *
	 * @param c ciudad con los nuevos datos y su identificador
	 * @return cantidad de filas afectadas
	 * @throws SQLException si ocurre un error de acceso a la base de datos o se
	 *                      viola una restricción de la tabla
	 */
	public int actualizar(Ciudad c) throws SQLException {
		try (Connection con = dataSource.getConnection(); PreparedStatement ps = con.prepareStatement(UPDATE)) {
			ps.setLong(1, c.getIdDepartamento());
			ps.setString(2, c.getNombre());
			ps.setLong(3, c.getIdCiudad());
			return ps.executeUpdate();
		}
	}

	/**
	 * Elimina una ciudad según su identificador.
	 *
	 * @param id identificador de la ciudad a eliminar
	 * @return cantidad de filas afectadas
	 * @throws SQLException si ocurre un error de acceso a la base de datos o
	 *                      la ciudad tiene registros que dependen de ella
	 */
	public int eliminar(Long id) throws SQLException {
		try (Connection con = dataSource.getConnection(); PreparedStatement ps = con.prepareStatement(DELETE)) {
			ps.setLong(1, id);
			return ps.executeUpdate();
		}
	}

	/**
	 * Convierte la fila actual del {@link ResultSet} en una entidad
	 * {@link Ciudad}, leyendo cada columna por su nombre.
	 *
	 * @param rs resultado posicionado en la fila a convertir
	 * @return ciudad con los datos de la fila
	 * @throws SQLException si alguna columna no existe o no puede leerse
	 */
	private Ciudad mapear(ResultSet rs) throws SQLException {
		Ciudad c = new Ciudad();
		c.setIdCiudad(rs.getLong("id_ciudad"));
		c.setIdDepartamento(rs.getLong("id_departamento"));
		c.setNombre(rs.getString("nombre"));
		return c;
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
	private List<Ciudad> listar(String sql, Object... parametros) throws SQLException {
		try (Connection con = dataSource.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
			for (int i = 0; i < parametros.length; i++) {
				ps.setObject(i + 1, parametros[i]);
			}
			try (ResultSet rs = ps.executeQuery()) {
				List<Ciudad> lista = new ArrayList<>();
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
