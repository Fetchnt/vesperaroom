package co.edu.unbosque.vesperaroom.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import org.springframework.stereotype.Repository;

import co.edu.unbosque.vesperaroom.entity.PerfilPropietario;
import co.edu.unbosque.vesperaroom.entity.PerfilPropietario.TipoCuenta;

/**
 * Repositorio para la gestión de perfiles de propietario en la tabla {@code perfil_propietario}, implementado
 * con JDBC puro: cada método obtiene una {@link Connection} del
 * {@link DataSource}, prepara la sentencia SQL con {@link PreparedStatement},
 * asigna los parámetros, la ejecuta y recorre el {@link ResultSet}. Los
 * recursos se cierran con try-with-resources.
 */
@Repository
public class PerfilPropietarioRepository {

	private static final String SELECT_BASE = "SELECT p.id_usuario, p.nit, p.razon_social, p.banco, "
			+ "p.tipo_cuenta, p.numero_cuenta, p.fecha_verificacion FROM perfil_propietario p ";

	private static final String SELECT_TODOS = SELECT_BASE + "ORDER BY p.razon_social";

	private static final String SELECT_POR_ID = SELECT_BASE + "WHERE p.id_usuario = ?";

	private static final String UPDATE_VERIFICACION = "UPDATE perfil_propietario SET "
			+ "fecha_verificacion = ? WHERE id_usuario = ?";

	private static final String INSERT = "INSERT INTO perfil_propietario (id_usuario, nit, "
			+ "razon_social, banco, tipo_cuenta, numero_cuenta, fecha_verificacion) VALUES (?, ?, "
			+ "?, ?, ?, ?, ?)";

	private static final String UPDATE = "UPDATE perfil_propietario SET nit = ?, razon_social = ?, "
			+ "banco = ?, tipo_cuenta = ?, numero_cuenta = ? WHERE id_usuario = ?";

	private static final String DELETE = "DELETE FROM perfil_propietario WHERE id_usuario = ?";

	private final DataSource dataSource;

	public PerfilPropietarioRepository(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * Retorna todos los perfiles de propietario.
	 *
	 * @return lista con todos los perfiles de propietario
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public List<PerfilPropietario> findAll() throws SQLException {
		return listar(SELECT_TODOS);
	}

	/**
	 * Busca un perfil de propietario por su identificador.
	 *
	 * @param id identificador del perfil de propietario
	 * @return {@link Optional} con el perfil de propietario encontrado, o vacío si no existe
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public Optional<PerfilPropietario> findById(Long id) throws SQLException {
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
	 * Verifica si un NIT ya está registrado.
	 *
	 * @param nit NIT a verificar
	 * @return {@code true} si existe
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public boolean existsByNit(String nit) throws SQLException {
		return contar("SELECT COUNT(*) FROM perfil_propietario WHERE nit = ?", nit) > 0;
	}

	/**
	 * Verifica si un NIT pertenece a un perfil distinto al indicado.
	 *
	 * @param nit NIT a verificar
	 * @param id identificador que se excluye de la búsqueda
	 * @return {@code true} si existe
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public boolean existsByNitAndIdNot(String nit, Long id) throws SQLException {
		return contar("SELECT COUNT(*) FROM perfil_propietario WHERE nit = ? AND id_usuario <> ?", nit, id) > 0;
	}

	/**
	 * Verifica si una cuenta bancaria (banco y número) ya está registrada.
	 *
	 * @param banco nombre del banco
	 * @param numeroCuenta número de la cuenta
	 * @return {@code true} si existe
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public boolean existsByCuenta(String banco, String numeroCuenta) throws SQLException {
		return contar("SELECT COUNT(*) FROM perfil_propietario WHERE banco = ? AND numero_cuenta = ?", banco, numeroCuenta) > 0;
	}

	/**
	 * Verifica si una cuenta bancaria pertenece a un perfil distinto al indicado.
	 *
	 * @param banco nombre del banco
	 * @param numeroCuenta número de la cuenta
	 * @param id identificador que se excluye de la búsqueda
	 * @return {@code true} si existe
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public boolean existsByCuentaAndIdNot(String banco, String numeroCuenta, Long id) throws SQLException {
		return contar("SELECT COUNT(*) FROM perfil_propietario WHERE banco = ? AND numero_cuenta = ? AND id_usuario <> ?", banco, numeroCuenta, id) > 0;
	}

	/**
	 * Registra la fecha en que un administrador verificó el perfil.
	 *
	 * @param id    identificador del perfil (id del usuario propietario)
	 * @param fecha fecha y hora de la verificación
	 * @return cantidad de filas afectadas
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public int actualizarVerificacion(Long id, LocalDateTime fecha) throws SQLException {
		try (Connection con = dataSource.getConnection();
				PreparedStatement ps = con.prepareStatement(UPDATE_VERIFICACION)) {
			ps.setTimestamp(1, Timestamp.valueOf(fecha));
			ps.setLong(2, id);
			return ps.executeUpdate();
		}
	}

	/**
	 * Verifica si existe un perfil de propietario con el identificador indicado.
	 *
	 * @param id identificador a verificar
	 * @return {@code true} si existe, {@code false} en caso contrario
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public boolean existsById(Long id) throws SQLException {
		return contar("SELECT COUNT(*) FROM perfil_propietario WHERE id_usuario = ?", id) > 0;
	}

	/**
	 * Retorna el total de perfiles de propietario registrados.
	 *
	 * @return cantidad total de perfiles de propietario
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public long count() throws SQLException {
		return contar("SELECT COUNT(*) FROM perfil_propietario");
	}

	/**
	 * Inserta un nuevo perfil de propietario. Su identificador es la llave del registro
	 * del que depende, por lo que no se genera con {@code AUTO_INCREMENT}.
	 *
	 * @param p perfil de propietario a insertar
	 * @return cantidad de filas afectadas
	 * @throws SQLException si ocurre un error de acceso a la base de datos o se
	 *                      viola una restricción de la tabla
	 */
	public int insertar(PerfilPropietario p) throws SQLException {
		try (Connection con = dataSource.getConnection(); PreparedStatement ps = con.prepareStatement(INSERT)) {
			ps.setLong(1, p.getIdUsuario());
			ps.setString(2, p.getNit());
			ps.setString(3, p.getRazonSocial());
			ps.setString(4, p.getBanco());
			ps.setString(5, p.getTipoCuenta().name());
			ps.setString(6, p.getNumeroCuenta());
			ps.setObject(7, p.getFechaVerificacion(), Types.TIMESTAMP);
			return ps.executeUpdate();
		}
	}

	/**
	 * Actualiza los campos editables de un perfil de propietario existente.
	 *
	 * @param p perfil de propietario con los nuevos datos y su identificador
	 * @return cantidad de filas afectadas
	 * @throws SQLException si ocurre un error de acceso a la base de datos o se
	 *                      viola una restricción de la tabla
	 */
	public int actualizar(PerfilPropietario p) throws SQLException {
		try (Connection con = dataSource.getConnection(); PreparedStatement ps = con.prepareStatement(UPDATE)) {
			ps.setString(1, p.getNit());
			ps.setString(2, p.getRazonSocial());
			ps.setString(3, p.getBanco());
			ps.setString(4, p.getTipoCuenta().name());
			ps.setString(5, p.getNumeroCuenta());
			ps.setLong(6, p.getIdUsuario());
			return ps.executeUpdate();
		}
	}

	/**
	 * Elimina un perfil de propietario según su identificador.
	 *
	 * @param id identificador del perfil de propietario a eliminar
	 * @return cantidad de filas afectadas
	 * @throws SQLException si ocurre un error de acceso a la base de datos o
	 *                      el perfil de propietario tiene registros que dependen de él
	 */
	public int eliminar(Long id) throws SQLException {
		try (Connection con = dataSource.getConnection(); PreparedStatement ps = con.prepareStatement(DELETE)) {
			ps.setLong(1, id);
			return ps.executeUpdate();
		}
	}

	/**
	 * Convierte la fila actual del {@link ResultSet} en una entidad
	 * {@link PerfilPropietario}, leyendo cada columna por su nombre.
	 *
	 * @param rs resultado posicionado en la fila a convertir
	 * @return perfil de propietario con los datos de la fila
	 * @throws SQLException si alguna columna no existe o no puede leerse
	 */
	private PerfilPropietario mapear(ResultSet rs) throws SQLException {
		PerfilPropietario p = new PerfilPropietario();
		p.setIdUsuario(rs.getLong("id_usuario"));
		p.setNit(rs.getString("nit"));
		p.setRazonSocial(rs.getString("razon_social"));
		p.setBanco(rs.getString("banco"));
		p.setTipoCuenta(TipoCuenta.valueOf(rs.getString("tipo_cuenta")));
		p.setNumeroCuenta(rs.getString("numero_cuenta"));
		p.setFechaVerificacion(rs.getObject("fecha_verificacion", LocalDateTime.class));
		return p;
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
	private List<PerfilPropietario> listar(String sql, Object... parametros) throws SQLException {
		try (Connection con = dataSource.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
			for (int i = 0; i < parametros.length; i++) {
				ps.setObject(i + 1, parametros[i]);
			}
			try (ResultSet rs = ps.executeQuery()) {
				List<PerfilPropietario> lista = new ArrayList<>();
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
