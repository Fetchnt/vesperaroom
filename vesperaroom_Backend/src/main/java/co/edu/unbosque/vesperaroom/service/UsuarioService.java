package co.edu.unbosque.vesperaroom.service;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import co.edu.unbosque.vesperaroom.dto.UsuarioDTO;
import co.edu.unbosque.vesperaroom.entity.Usuario;
import co.edu.unbosque.vesperaroom.entity.Usuario.Estado;
import co.edu.unbosque.vesperaroom.entity.Usuario.Rol;
import co.edu.unbosque.vesperaroom.exception.AccesoDatosException;
import co.edu.unbosque.vesperaroom.exception.ContrasenaException;
import co.edu.unbosque.vesperaroom.exception.CorreoDuplicadoException;
import co.edu.unbosque.vesperaroom.exception.CorreoException;
import co.edu.unbosque.vesperaroom.exception.EstadoException;
import co.edu.unbosque.vesperaroom.exception.InvalidCharException;
import co.edu.unbosque.vesperaroom.exception.LanzadorExcepciones;
import co.edu.unbosque.vesperaroom.exception.PasswordSizeException;
import co.edu.unbosque.vesperaroom.exception.RegistroNoEncontradoException;
import co.edu.unbosque.vesperaroom.exception.RolException;
import co.edu.unbosque.vesperaroom.exception.RolNoPermitidoException;
import co.edu.unbosque.vesperaroom.exception.TelefonoDuplicadoException;
import co.edu.unbosque.vesperaroom.exception.TelefonoException;
import co.edu.unbosque.vesperaroom.repository.UsuarioRepository;

/**
 * Servicio que gestiona las operaciones sobre usuarios en el sistema,
 * incluyendo creación, registro público, actualización, cambio de estado,
 * eliminación y consulta. Las validaciones se delegan a
 * {@link LanzadorExcepciones} y cada excepción se traduce a un código de
 * resultado. Cada escritura es una única sentencia SQL en modo autocommit; las
 * restricciones UNIQUE de la tabla protegen contra duplicados concurrentes. Las
 * consultas de lectura envuelven la {@link SQLException} en
 * {@link AccesoDatosException}.
 *
 * <p>
 * Códigos de resultado compartidos por {@link #create(UsuarioDTO)},
 * {@link #registrar(UsuarioDTO)} y {@link #updateById(Long, UsuarioDTO)}:
 * </p>
 * <ul>
 * <li>0: operación exitosa</li>
 * <li>1: nombre o apellido con caracteres inválidos</li>
 * <li>2: correo con formato inválido</li>
 * <li>3: teléfono con formato inválido</li>
 * <li>4: contraseña con menos de 8 caracteres</li>
 * <li>5: contraseña sin mayúscula, minúscula o número</li>
 * <li>6: rol no indicado</li>
 * <li>7: rol no permitido en el registro público</li>
 * <li>8: correo ya registrado</li>
 * <li>9: teléfono ya registrado</li>
 * <li>10: usuario no encontrado</li>
 * <li>11: error inesperado</li>
 * </ul>
 */
@Service
public class UsuarioService implements CRUDOperation<UsuarioDTO> {

	private static final String ENTIDAD = "usuario";

	/** Código de error de MySQL para una violación de llave única. */
	private static final int ERROR_LLAVE_DUPLICADA = 1062;

	private final UsuarioRepository usuarioRepository;
	private final ModelMapper mapper;
	private final PasswordEncoder passwordEncoder;

	public UsuarioService(UsuarioRepository usuarioRepository, ModelMapper mapper, PasswordEncoder passwordEncoder) {
		this.usuarioRepository = usuarioRepository;
		this.mapper = mapper;
		this.passwordEncoder = passwordEncoder;
	}

	/**
	 * Crea un nuevo usuario con cualquier rol, incluido {@code ADMINISTRADOR}. Se
	 * usa desde la administración del sistema.
	 *
	 * @param data objeto con los datos del usuario a crear
	 * @return código de resultado según la tabla de la clase (0 a 9 u 11)
	 */
	@Override
	public int create(UsuarioDTO data) {
		return guardarNuevo(data, false);
	}

	/**
	 * Registra un nuevo usuario desde el endpoint público. Solo permite los roles
	 * {@code CLIENTE} y {@code PROPIETARIO}, y la cuenta siempre queda
	 * {@code ACTIVO}.
	 *
	 * @param data objeto con los datos del usuario a registrar
	 * @return código de resultado según la tabla de la clase (0 a 9 u 11)
	 */
	public int registrar(UsuarioDTO data) {
		return guardarNuevo(data, true);
	}

	/**
	 * Actualiza los datos de un usuario existente. Si la contraseña llega vacía se
	 * conserva la actual, y si el estado llega nulo se conserva el actual.
	 *
	 * @param id   identificador del usuario a actualizar
	 * @param data objeto con los nuevos datos del usuario
	 * @return código de resultado según la tabla de la clase (0 a 6 u 8 a 11)
	 */
	@Override
	public int updateById(Long id, UsuarioDTO data) {
		try {
			Optional<Usuario> found = usuarioRepository.findById(id);
			LanzadorExcepciones.verificarExistencia(found.isPresent(), ENTIDAD, id);

			normalizar(data);
			boolean cambiaContrasena = data.getContrasena() != null && !data.getContrasena().isBlank();
			validarDatos(data, cambiaContrasena);
			LanzadorExcepciones.verificarCorreoDuplicado(usuarioRepository.existsByCorreoAndIdNot(data.getCorreo(), id));
			LanzadorExcepciones
					.verificarTelefonoDuplicado(usuarioRepository.existsByTelefonoAndIdNot(data.getTelefono(), id));

			Usuario temp = found.get();
			temp.setNombre(data.getNombre());
			temp.setApellido(data.getApellido());
			temp.setCorreo(data.getCorreo());
			temp.setTelefono(data.getTelefono());
			temp.setRol(data.getRol());

			if (cambiaContrasena) {
				temp.setContrasena(passwordEncoder.encode(data.getContrasena()));
			}
			if (data.getEstado() != null) {
				temp.setEstado(data.getEstado());
			}

			usuarioRepository.actualizar(temp);
			return 0;

		} catch (InvalidCharException e) {
			return 1;
		} catch (CorreoException e) {
			return 2;
		} catch (TelefonoException e) {
			return 3;
		} catch (PasswordSizeException e) {
			return 4;
		} catch (ContrasenaException e) {
			return 5;
		} catch (RolException e) {
			return 6;
		} catch (CorreoDuplicadoException e) {
			return 8;
		} catch (TelefonoDuplicadoException e) {
			return 9;
		} catch (SQLIntegrityConstraintViolationException e) {
			return codigoDuplicado(e);
		} catch (RegistroNoEncontradoException e) {
			return 10;
		} catch (Exception e) {
			e.printStackTrace();
			return 11;
		}
	}

	/**
	 * Cambia el estado de un usuario (activar o inactivar la cuenta).
	 *
	 * @param id     identificador del usuario
	 * @param estado nuevo estado de la cuenta
	 * @return 0 si es exitoso, 1 si el estado es nulo, 2 si no se encuentra el
	 *         usuario, 3 si ocurre un error inesperado
	 */
	public int updateEstado(Long id, Estado estado) {
		try {
			LanzadorExcepciones.verificarEstado(estado);
			LanzadorExcepciones.verificarExistencia(usuarioRepository.existsById(id), ENTIDAD, id);
			usuarioRepository.actualizarEstado(id, estado);
			return 0;
		} catch (EstadoException e) {
			return 1;
		} catch (RegistroNoEncontradoException e) {
			return 2;
		} catch (Exception e) {
			e.printStackTrace();
			return 3;
		}
	}

	/**
	 * Elimina un usuario del sistema según su ID.
	 *
	 * @param id identificador del usuario a eliminar
	 * @return 0 si se eliminó exitosamente, 1 si no se encontró el usuario, 2 si
	 *         tiene registros asociados que impiden eliminarlo, 3 si ocurre un
	 *         error inesperado
	 */
	@Override
	public int deleteById(Long id) {
		try {
			LanzadorExcepciones.verificarExistencia(usuarioRepository.existsById(id), ENTIDAD, id);
			usuarioRepository.eliminar(id);
			return 0;
		} catch (RegistroNoEncontradoException e) {
			return 1;
		} catch (SQLIntegrityConstraintViolationException e) {
			return 2;
		} catch (Exception e) {
			e.printStackTrace();
			return 3;
		}
	}

	/**
	 * Retorna la lista de todos los usuarios registrados en el sistema.
	 *
	 * @return lista de {@link UsuarioDTO} con todos los usuarios
	 * @throws AccesoDatosException si falla la consulta a la base de datos
	 */
	@Override
	public List<UsuarioDTO> getAll() {
		try {
			return toDtoList(usuarioRepository.findAll());
		} catch (SQLException e) {
			throw new AccesoDatosException(e);
		}
	}

	/**
	 * Retorna la lista de usuarios que tienen el rol indicado.
	 *
	 * @param rol rol por el que se filtra
	 * @return lista de {@link UsuarioDTO} con ese rol
	 * @throws AccesoDatosException si falla la consulta a la base de datos
	 */
	public List<UsuarioDTO> getAllByRol(Rol rol) {
		try {
			return toDtoList(usuarioRepository.findByRol(rol));
		} catch (SQLException e) {
			throw new AccesoDatosException(e);
		}
	}

	@Override
	public boolean exist(Long id) {
		try {
			return usuarioRepository.existsById(id);
		} catch (SQLException e) {
			throw new AccesoDatosException(e);
		}
	}

	@Override
	public long count() {
		try {
			return usuarioRepository.count();
		} catch (SQLException e) {
			throw new AccesoDatosException(e);
		}
	}

	/**
	 * Busca y retorna un usuario según su ID.
	 *
	 * @param id identificador del usuario a buscar
	 * @return {@link UsuarioDTO} con los datos del usuario, o {@code null} si no
	 *         existe
	 * @throws AccesoDatosException si falla la consulta a la base de datos
	 */
	public UsuarioDTO getById(Long id) {
		try {
			Optional<Usuario> found = usuarioRepository.findById(id);

			if (found.isPresent()) {
				return toDto(found.get());
			}

			return null;
		} catch (SQLException e) {
			throw new AccesoDatosException(e);
		}
	}

	/**
	 * Busca y retorna un usuario según su correo electrónico.
	 *
	 * @param correo correo electrónico del usuario a buscar
	 * @return {@link UsuarioDTO} con los datos del usuario, o {@code null} si no
	 *         existe
	 * @throws AccesoDatosException si falla la consulta a la base de datos
	 */
	public UsuarioDTO getByCorreo(String correo) {
		if (correo == null) {
			return null;
		}

		try {
			Optional<Usuario> found = usuarioRepository.findByCorreo(correo.trim().toLowerCase());

			if (found.isPresent()) {
				return toDto(found.get());
			}

			return null;
		} catch (SQLException e) {
			throw new AccesoDatosException(e);
		}
	}

	/**
	 * Valida e inserta un nuevo usuario, cifrando la contraseña y asignando la
	 * fecha de registro.
	 *
	 * @param data            objeto con los datos del usuario
	 * @param registroPublico {@code true} si viene del registro público, en cuyo
	 *                        caso no se permite el rol {@code ADMINISTRADOR} y el
	 *                        estado se fuerza a {@code ACTIVO}
	 * @return código de resultado según la tabla de la clase
	 */
	private int guardarNuevo(UsuarioDTO data, boolean registroPublico) {
		try {
			normalizar(data);
			validarDatos(data, true);
			if (registroPublico) {
				LanzadorExcepciones.verificarRolPublico(data.getRol());
			}
			LanzadorExcepciones.verificarCorreoDuplicado(usuarioRepository.existsByCorreo(data.getCorreo()));
			LanzadorExcepciones.verificarTelefonoDuplicado(usuarioRepository.existsByTelefono(data.getTelefono()));

			Usuario entity = mapper.map(data, Usuario.class);
			entity.setIdUsuario(null);
			entity.setContrasena(passwordEncoder.encode(data.getContrasena()));
			entity.setFechaRegistro(LocalDateTime.now());

			if (registroPublico || data.getEstado() == null) {
				entity.setEstado(Estado.ACTIVO);
			}

			usuarioRepository.insertar(entity);
			return 0;

		} catch (InvalidCharException e) {
			return 1;
		} catch (CorreoException e) {
			return 2;
		} catch (TelefonoException e) {
			return 3;
		} catch (PasswordSizeException e) {
			return 4;
		} catch (ContrasenaException e) {
			return 5;
		} catch (RolException e) {
			return 6;
		} catch (RolNoPermitidoException e) {
			return 7;
		} catch (CorreoDuplicadoException e) {
			return 8;
		} catch (TelefonoDuplicadoException e) {
			return 9;
		} catch (SQLIntegrityConstraintViolationException e) {
			return codigoDuplicado(e);
		} catch (Exception e) {
			e.printStackTrace();
			return 11;
		}
	}

	/**
	 * Aplica todas las validaciones de formato sobre los datos de un usuario.
	 *
	 * @param data               datos a validar
	 * @param validarContrasena  indica si se debe validar la contraseña
	 * @throws InvalidCharException  si el nombre o el apellido son inválidos
	 * @throws CorreoException       si el correo es inválido
	 * @throws TelefonoException     si el teléfono es inválido
	 * @throws PasswordSizeException si la contraseña es demasiado corta
	 * @throws ContrasenaException   si la contraseña no cumple la composición
	 * @throws RolException          si no se indica el rol
	 */
	private void validarDatos(UsuarioDTO data, boolean validarContrasena) throws InvalidCharException,
			CorreoException, TelefonoException, PasswordSizeException, ContrasenaException, RolException {
		LanzadorExcepciones.verificarCaracteres(data.getNombre());
		LanzadorExcepciones.verificarCaracteres(data.getApellido());
		LanzadorExcepciones.verificarCorreo(data.getCorreo());
		LanzadorExcepciones.verificarTelefono(data.getTelefono());
		if (validarContrasena) {
			LanzadorExcepciones.verificarTamanoContrasena(data.getContrasena());
			LanzadorExcepciones.verificarContrasena(data.getContrasena());
		}
		LanzadorExcepciones.verificarRol(data.getRol());
	}

	/**
	 * Elimina espacios sobrantes de los campos de texto y pasa el correo a
	 * minúsculas para evitar duplicados por diferencias de formato.
	 *
	 * @param data datos a normalizar
	 */
	private void normalizar(UsuarioDTO data) {
		if (data.getNombre() != null) {
			data.setNombre(data.getNombre().trim());
		}
		if (data.getApellido() != null) {
			data.setApellido(data.getApellido().trim());
		}
		if (data.getCorreo() != null) {
			data.setCorreo(data.getCorreo().trim().toLowerCase());
		}
		if (data.getTelefono() != null) {
			data.setTelefono(data.getTelefono().trim());
		}
	}

	/**
	 * Traduce una violación de llave única detectada por la base de datos (por
	 * ejemplo, en inserciones concurrentes) al código de duplicado
	 * correspondiente.
	 *
	 * @param e excepción lanzada por el driver JDBC de MySQL
	 * @return 9 si la restricción violada es la del teléfono, 8 si es la del
	 *         correo, 11 si no es una llave duplicada (código MySQL distinto de
	 *         1062)
	 */
	private int codigoDuplicado(SQLIntegrityConstraintViolationException e) {
		if (e.getErrorCode() != ERROR_LLAVE_DUPLICADA) {
			e.printStackTrace();
			return 11;
		}
		String mensaje = e.getMessage();
		return mensaje != null && mensaje.contains("uq_usuario_telefono") ? 9 : 8;
	}

	/**
	 * Convierte una entidad en DTO sin copiar la contraseña cifrada.
	 *
	 * @param entity entidad a convertir
	 * @return {@link UsuarioDTO} sin contraseña
	 */
	private UsuarioDTO toDto(Usuario entity) {
		UsuarioDTO dto = mapper.map(entity, UsuarioDTO.class);
		dto.setContrasena(null);
		return dto;
	}

	/**
	 * Convierte una lista de entidades en una lista de DTOs.
	 *
	 * @param entityList lista de entidades
	 * @return lista de {@link UsuarioDTO}
	 */
	private List<UsuarioDTO> toDtoList(List<Usuario> entityList) {
		List<UsuarioDTO> dtoList = new ArrayList<>();

		entityList.forEach(entity -> {
			dtoList.add(toDto(entity));
		});

		return dtoList;
	}
}
