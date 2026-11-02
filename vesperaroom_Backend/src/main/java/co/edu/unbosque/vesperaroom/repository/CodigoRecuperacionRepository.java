package co.edu.unbosque.vesperaroom.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import javax.sql.DataSource;

import org.springframework.stereotype.Repository;

/**
 * Repositorio de la tabla {@code codigo_recuperacion}, con JDBC puro como los
 * demás. Las fechas se comparan con {@code NOW()} de la base de datos, no con
 * el reloj de Java, para que el vencimiento no dependa de la zona del servidor
 * de aplicaciones.
 */
@Repository
public class CodigoRecuperacionRepository {

	/** Un código vence a los 30 minutos y admite hasta 5 intentos. */
	private static final String GUARDAR = "INSERT INTO codigo_recuperacion (id_usuario, codigo_hash, "
			+ "fecha_creacion, fecha_expiracion, intentos) VALUES (?, ?, NOW(), NOW() + INTERVAL 30 MINUTE, 0) "
			+ "ON DUPLICATE KEY UPDATE codigo_hash = VALUES(codigo_hash), fecha_creacion = NOW(), "
			+ "fecha_expiracion = NOW() + INTERVAL 30 MINUTE, intentos = 0";

	private static final String ENVIADO_HACE_POCO = "SELECT COUNT(*) FROM codigo_recuperacion "
			+ "WHERE id_usuario = ? AND fecha_creacion > NOW() - INTERVAL 60 SECOND";

	private static final String GASTAR_INTENTO = "UPDATE codigo_recuperacion SET intentos = intentos + 1 "
			+ "WHERE id_usuario = ? AND intentos < 5 AND fecha_expiracion > NOW()";

	private static final String SELECT_HASH = "SELECT codigo_hash FROM codigo_recuperacion WHERE id_usuario = ?";

	private static final String UPDATE_CONTRASENA = "UPDATE usuario SET contrasena = ? WHERE id_usuario = ?";

	private static final String DELETE = "DELETE FROM codigo_recuperacion WHERE id_usuario = ?";

	private final DataSource dataSource;

	public CodigoRecuperacionRepository(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * Guarda el código de un usuario, reemplazando el anterior si lo había.
	 *
	 * @param idUsuario  identificador del usuario
	 * @param codigoHash hash BCrypt del código enviado
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public void guardar(Long idUsuario, String codigoHash) throws SQLException {
		try (Connection con = dataSource.getConnection(); PreparedStatement ps = con.prepareStatement(GUARDAR)) {
			ps.setLong(1, idUsuario);
			ps.setString(2, codigoHash);
			ps.executeUpdate();
		}
	}

	/**
	 * @param idUsuario identificador del usuario
	 * @return {@code true} si al usuario se le envió un código hace menos de 60 s
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public boolean enviadoHacePoco(Long idUsuario) throws SQLException {
		try (Connection con = dataSource.getConnection();
				PreparedStatement ps = con.prepareStatement(ENVIADO_HACE_POCO)) {
			ps.setLong(1, idUsuario);
			try (ResultSet rs = ps.executeQuery()) {
				rs.next();
				return rs.getLong(1) > 0;
			}
		}
	}

	/**
	 * Cuenta un intento sobre el código vigente y retorna su hash. El intento se
	 * cuenta antes de comparar y en una sola sentencia, así varias peticiones a
	 * la vez no pueden pasar del límite.
	 *
	 * @param idUsuario identificador del usuario
	 * @return hash del código, o vacío si no hay código, venció o ya agotó sus
	 *         intentos
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public Optional<String> gastarIntento(Long idUsuario) throws SQLException {
		try (Connection con = dataSource.getConnection()) {
			try (PreparedStatement ps = con.prepareStatement(GASTAR_INTENTO)) {
				ps.setLong(1, idUsuario);
				if (ps.executeUpdate() == 0) {
					return Optional.empty();
				}
			}
			try (PreparedStatement ps = con.prepareStatement(SELECT_HASH)) {
				ps.setLong(1, idUsuario);
				try (ResultSet rs = ps.executeQuery()) {
					return rs.next() ? Optional.of(rs.getString(1)) : Optional.empty();
				}
			}
		}
	}

	/**
	 * Cambia la contraseña y borra el código en una sola transacción: o pasan
	 * las dos cosas o ninguna, así un código no sirve dos veces.
	 *
	 * @param idUsuario      identificador del usuario
	 * @param contrasenaHash hash BCrypt de la nueva contraseña
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public void restablecer(Long idUsuario, String contrasenaHash) throws SQLException {
		try (Connection con = dataSource.getConnection()) {
			con.setAutoCommit(false);
			try (PreparedStatement contrasena = con.prepareStatement(UPDATE_CONTRASENA);
					PreparedStatement borrar = con.prepareStatement(DELETE)) {
				contrasena.setString(1, contrasenaHash);
				contrasena.setLong(2, idUsuario);
				contrasena.executeUpdate();
				borrar.setLong(1, idUsuario);
				borrar.executeUpdate();
				con.commit();
			} catch (SQLException e) {
				con.rollback();
				throw e;
			} finally {
				con.setAutoCommit(true);
			}
		}
	}
}
