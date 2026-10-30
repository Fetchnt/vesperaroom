package co.edu.unbosque.vesperaroom.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import org.springframework.stereotype.Repository;

import co.edu.unbosque.vesperaroom.entity.Usuario;
import co.edu.unbosque.vesperaroom.entity.Usuario.Estado;
import co.edu.unbosque.vesperaroom.entity.Usuario.Rol;

/**
 * Repositorio para la gestión de usuarios en la tabla {@code usuario},
 * implementado con JDBC puro: cada método obtiene una {@link Connection} del
 * {@link DataSource}, prepara la sentencia SQL con {@link PreparedStatement},
 * asigna los parámetros, la ejecuta y recorre el {@link ResultSet}. Los
 * recursos se cierran con try-with-resources, lo que devuelve la conexión al
 * pool aunque ocurra un error. Los errores de base de datos se propagan como
 * {@link SQLException}.
 */
@Repository
public class UsuarioRepository {

	private static final String SELECT_BASE = "SELECT id_usuario, nombre, apellido, correo, telefono, "
			+ "contrasena, rol, estado, fecha_registro FROM usuario ";

	private static final String SELECT_TODOS = SELECT_BASE + "ORDER BY id_usuario";

	private static final String SELECT_POR_ROL = SELECT_BASE + "WHERE rol = ? ORDER BY apellido, nombre";

	private static final String SELECT_POR_ID = SELECT_BASE + "WHERE id_usuario = ?";

	private static final String SELECT_POR_CORREO = SELECT_BASE + "WHERE correo = ?";

	private static final String INSERT = "INSERT INTO usuario (nombre, apellido, correo, telefono, "
			+ "contrasena, rol, estado, fecha_registro) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

	private static final String UPDATE = "UPDATE usuario SET nombre = ?, apellido = ?, correo = ?, telefono = ?, "
			+ "contrasena = ?, rol = ?, estado = ? WHERE id_usuario = ?";

	private static final String UPDATE_ESTADO = "UPDATE usuario SET estado = ? WHERE id_usuario = ?";

	private static final String DELETE = "DELETE FROM usuario WHERE id_usuario = ?";

	private final DataSource dataSource;

	public UsuarioRepository(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * Retorna todos los usuarios ordenados por identificador.
	 *
	 * @return lista con todos los usuarios
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public List<Usuario> findAll() throws SQLException {
		try (Connection con = dataSource.getConnection();
				PreparedStatement ps = con.prepareStatement(SELECT_TODOS);
				ResultSet rs = ps.executeQuery()) {
			List<Usuario> lista = new ArrayList<>();
			while (rs.next()) {
				lista.add(mapear(rs));
			}
			return lista;
		}
	}

	/**
	 * Retorna los usuarios que tienen el rol indicado.
	 *
	 * @param rol rol por el que se filtra
	 * @return lista de usuarios con ese rol, ordenados por apellido y nombre
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public List<Usuario> findByRol(Rol rol) throws SQLException {
		try (Connection con = dataSource.getConnection();
				PreparedStatement ps = con.prepareStatement(SELECT_POR_ROL)) {
			ps.setString(1, rol.name());
			try (ResultSet rs = ps.executeQuery()) {
				List<Usuario> lista = new ArrayList<>();
				while (rs.next()) {
					lista.add(mapear(rs));
				}
				return lista;
			}
		}
	}

	/**
	 * Busca un usuario por su identificador.
	 *
	 * @param id identificador del usuario
	 * @return {@link Optional} con el usuario encontrado, o vacío si no existe
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public Optional<Usuario> findById(Long id) throws SQLException {
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
	 * Busca un usuario por su correo electrónico.
	 *
	 * @param correo correo electrónico a buscar
	 * @return {@link Optional} con el usuario encontrado, o vacío si no existe
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public Optional<Usuario> findByCorreo(String correo) throws SQLException {
		try (Connection con = dataSource.getConnection();
				PreparedStatement ps = con.prepareStatement(SELECT_POR_CORREO)) {
			ps.setString(1, correo);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return Optional.of(mapear(rs));
				}
				return Optional.empty();
			}
		}
	}

	/**
	 * Verifica si existe un usuario con el identificador indicado.
	 *
	 * @param id identificador a verificar
	 * @return {@code true} si existe, {@code false} en caso contrario
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public boolean existsById(Long id) throws SQLException {
		return contar("SELECT COUNT(*) FROM usuario WHERE id_usuario = ?", id) > 0;
	}

	/**
	 * Verifica si un correo ya está registrado.
	 *
	 * @param correo correo a verificar
	 * @return {@code true} si ya existe, {@code false} en caso contrario
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public boolean existsByCorreo(String correo) throws SQLException {
		return contar("SELECT COUNT(*) FROM usuario WHERE correo = ?", correo) > 0;
	}

	/**
	 * Verifica si un teléfono ya está registrado.
	 *
	 * @param telefono teléfono a verificar
	 * @return {@code true} si ya existe, {@code false} en caso contrario
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public boolean existsByTelefono(String telefono) throws SQLException {
		return contar("SELECT COUNT(*) FROM usuario WHERE telefono = ?", telefono) > 0;
	}

	/**
	 * Verifica si un correo pertenece a un usuario distinto al indicado. Se usa al
	 * actualizar para permitir que el usuario conserve su propio correo.
	 *
	 * @param correo correo a verificar
	 * @param id     identificador del usuario que se excluye de la búsqueda
	 * @return {@code true} si otro usuario ya usa ese correo
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public boolean existsByCorreoAndIdNot(String correo, Long id) throws SQLException {
		return contar("SELECT COUNT(*) FROM usuario WHERE correo = ? AND id_usuario <> ?", correo, id) > 0;
	}

	/**
	 * Verifica si un teléfono pertenece a un usuario distinto al indicado.
	 *
	 * @param telefono teléfono a verificar
	 * @param id       identificador del usuario que se excluye de la búsqueda
	 * @return {@code true} si otro usuario ya usa ese teléfono
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public boolean existsByTelefonoAndIdNot(String telefono, Long id) throws SQLException {
		return contar("SELECT COUNT(*) FROM usuario WHERE telefono = ? AND id_usuario <> ?", telefono, id) > 0;
	}

	/**
	 * Retorna el total de usuarios registrados.
	 *
	 * @return cantidad total de usuarios
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public long count() throws SQLException {
		return contar("SELECT COUNT(*) FROM usuario");
	}

	/**
	 * Inserta un nuevo usuario y retorna el identificador generado por
	 * {@code AUTO_INCREMENT}.
	 *
	 * @param u usuario a insertar, con la contraseña ya cifrada
	 * @return identificador generado para el nuevo registro
	 * @throws SQLException si ocurre un error de acceso a la base de datos o se
	 *                      viola una restricción de la tabla
	 */
	public Long insertar(Usuario u) throws SQLException {
		try (Connection con = dataSource.getConnection();
				PreparedStatement ps = con.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
			ps.setString(1, u.getNombre());
			ps.setString(2, u.getApellido());
			ps.setString(3, u.getCorreo());
			ps.setString(4, u.getTelefono());
			ps.setString(5, u.getContrasena());
			ps.setString(6, u.getRol().name());
			ps.setString(7, u.getEstado().name());
			ps.setTimestamp(8, Timestamp.valueOf(u.getFechaRegistro()));
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
	 * Actualiza todos los campos editables de un usuario existente.
	 *
	 * @param u usuario con los nuevos datos y su identificador
	 * @return cantidad de filas afectadas
	 * @throws SQLException si ocurre un error de acceso a la base de datos o se
	 *                      viola una restricción de la tabla
	 */
	public int actualizar(Usuario u) throws SQLException {
		try (Connection con = dataSource.getConnection(); PreparedStatement ps = con.prepareStatement(UPDATE)) {
			ps.setString(1, u.getNombre());
			ps.setString(2, u.getApellido());
			ps.setString(3, u.getCorreo());
			ps.setString(4, u.getTelefono());
			ps.setString(5, u.getContrasena());
			ps.setString(6, u.getRol().name());
			ps.setString(7, u.getEstado().name());
			ps.setLong(8, u.getIdUsuario());
			return ps.executeUpdate();
		}
	}

	/**
	 * Actualiza únicamente el estado de un usuario.
	 *
	 * @param id     identificador del usuario
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
	 * Elimina un usuario según su identificador.
	 *
	 * @param id identificador del usuario a eliminar
	 * @return cantidad de filas afectadas
	 * @throws SQLException si ocurre un error de acceso a la base de datos o el
	 *                      usuario tiene registros que dependen de él
	 */
	public int eliminar(Long id) throws SQLException {
		try (Connection con = dataSource.getConnection(); PreparedStatement ps = con.prepareStatement(DELETE)) {
			ps.setLong(1, id);
			return ps.executeUpdate();
		}
	}

	/**
	 * Convierte la fila actual del {@link ResultSet} en una entidad
	 * {@link Usuario}, leyendo cada columna por su nombre.
	 *
	 * @param rs resultado posicionado en la fila a convertir
	 * @return usuario con los datos de la fila
	 * @throws SQLException si alguna columna no existe o no puede leerse
	 */
	private Usuario mapear(ResultSet rs) throws SQLException {
		Usuario u = new Usuario();
		u.setIdUsuario(rs.getLong("id_usuario"));
		u.setNombre(rs.getString("nombre"));
		u.setApellido(rs.getString("apellido"));
		u.setCorreo(rs.getString("correo"));
		u.setTelefono(rs.getString("telefono"));
		u.setContrasena(rs.getString("contrasena"));
		u.setRol(Rol.valueOf(rs.getString("rol")));
		u.setEstado(Estado.valueOf(rs.getString("estado")));
		u.setFechaRegistro(rs.getTimestamp("fecha_registro").toLocalDateTime());
		return u;
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
