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

/**
 * Repositorio para la gestión de características en la tabla {@code caracteristica}, implementado
 * con JDBC puro: cada método obtiene una {@link Connection} del
 * {@link DataSource}, prepara la sentencia SQL con {@link PreparedStatement},
 * asigna los parámetros, la ejecuta y recorre el {@link ResultSet}. Los
 * recursos se cierran con try-with-resources.
 */
@Repository
public class CaracteristicaRepository {

	private static final String SELECT_BASE = "SELECT c.id_caracteristica, c.nombre, c.icono FROM "
			+ "caracteristica c ";

	private static final String SELECT_TODOS = SELECT_BASE + "ORDER BY c.nombre";

	private static final String SELECT_POR_ID = SELECT_BASE + "WHERE c.id_caracteristica = ?";

	private static final String INSERT = "INSERT INTO caracteristica (nombre, icono) VALUES (?, ?)";

	private static final String UPDATE = "UPDATE caracteristica SET nombre = ?, icono = ? WHERE "
			+ "id_caracteristica = ?";

	private static final String DELETE = "DELETE FROM caracteristica WHERE id_caracteristica = ?";

	private final DataSource dataSource;

	public CaracteristicaRepository(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * Retorna todas las características.
	 *
	 * @return lista con todas las características
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public List<Caracteristica> findAll() throws SQLException {
		return listar(SELECT_TODOS);
	}

	/**
	 * Busca una característica por su identificador.
	 *
	 * @param id identificador de la característica
	 * @return {@link Optional} con la característica encontrada, o vacío si no existe
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public Optional<Caracteristica> findById(Long id) throws SQLException {
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
	 * Verifica si ya existe una característica con el nombre indicado.
	 *
	 * @param nombre nombre a verificar
	 * @return {@code true} si existe
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public boolean existsByNombre(String nombre) throws SQLException {
		return contar("SELECT COUNT(*) FROM caracteristica WHERE nombre = ?", nombre) > 0;
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
		return contar("SELECT COUNT(*) FROM caracteristica WHERE nombre = ? AND id_caracteristica <> ?", nombre, id) > 0;
	}

	/**
	 * Verifica si existe una característica con el identificador indicado.
	 *
	 * @param id identificador a verificar
	 * @return {@code true} si existe, {@code false} en caso contrario
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public boolean existsById(Long id) throws SQLException {
		return contar("SELECT COUNT(*) FROM caracteristica WHERE id_caracteristica = ?", id) > 0;
	}

	/**
	 * Retorna el total de características registradas.
	 *
	 * @return cantidad total de características
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public long count() throws SQLException {
		return contar("SELECT COUNT(*) FROM caracteristica");
	}

	/**
	 * Inserta una nueva característica y retorna el identificador generado por
	 * {@code AUTO_INCREMENT}.
	 *
	 * @param c característica a insertar
	 * @return identificador generado para el nuevo registro
	 * @throws SQLException si ocurre un error de acceso a la base de datos o se
	 *                      viola una restricción de la tabla
	 */
	public Long insertar(Caracteristica c) throws SQLException {
		try (Connection con = dataSource.getConnection();
				PreparedStatement ps = con.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
			ps.setString(1, c.getNombre());
			ps.setString(2, c.getIcono());
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
	 * Actualiza los campos editables de una característica existente.
	 *
	 * @param c característica con los nuevos datos y su identificador
	 * @return cantidad de filas afectadas
	 * @throws SQLException si ocurre un error de acceso a la base de datos o se
	 *                      viola una restricción de la tabla
	 */
	public int actualizar(Caracteristica c) throws SQLException {
		try (Connection con = dataSource.getConnection(); PreparedStatement ps = con.prepareStatement(UPDATE)) {
			ps.setString(1, c.getNombre());
			ps.setString(2, c.getIcono());
			ps.setLong(3, c.getIdCaracteristica());
			return ps.executeUpdate();
		}
	}

	/**
	 * Elimina una característica según su identificador.
	 *
	 * @param id identificador de la característica a eliminar
	 * @return cantidad de filas afectadas
	 * @throws SQLException si ocurre un error de acceso a la base de datos o
	 *                      la característica tiene registros que dependen de ella
	 */
	public int eliminar(Long id) throws SQLException {
		try (Connection con = dataSource.getConnection(); PreparedStatement ps = con.prepareStatement(DELETE)) {
			ps.setLong(1, id);
			return ps.executeUpdate();
		}
	}

	/**
	 * Convierte la fila actual del {@link ResultSet} en una entidad
	 * {@link Caracteristica}, leyendo cada columna por su nombre.
	 *
	 * @param rs resultado posicionado en la fila a convertir
	 * @return característica con los datos de la fila
	 * @throws SQLException si alguna columna no existe o no puede leerse
	 */
	private Caracteristica mapear(ResultSet rs) throws SQLException {
		Caracteristica c = new Caracteristica();
		c.setIdCaracteristica(rs.getLong("id_caracteristica"));
		c.setNombre(rs.getString("nombre"));
		c.setIcono(rs.getString("icono"));
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
	private List<Caracteristica> listar(String sql, Object... parametros) throws SQLException {
		try (Connection con = dataSource.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
			for (int i = 0; i < parametros.length; i++) {
				ps.setObject(i + 1, parametros[i]);
			}
			try (ResultSet rs = ps.executeQuery()) {
				List<Caracteristica> lista = new ArrayList<>();
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
