package co.edu.unbosque.vesperaroom.service;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import co.edu.unbosque.vesperaroom.dto.PerfilPropietarioDTO;
import co.edu.unbosque.vesperaroom.entity.PerfilPropietario;
import co.edu.unbosque.vesperaroom.entity.Usuario;
import co.edu.unbosque.vesperaroom.exception.AccesoDatosException;
import co.edu.unbosque.vesperaroom.exception.CampoObligatorioException;
import co.edu.unbosque.vesperaroom.exception.CuentaBancariaException;
import co.edu.unbosque.vesperaroom.exception.LanzadorExcepciones;
import co.edu.unbosque.vesperaroom.exception.NitException;
import co.edu.unbosque.vesperaroom.exception.ReferenciaNoEncontradaException;
import co.edu.unbosque.vesperaroom.exception.RegistroDuplicadoException;
import co.edu.unbosque.vesperaroom.exception.RegistroNoEncontradoException;
import co.edu.unbosque.vesperaroom.exception.RolPropietarioException;
import co.edu.unbosque.vesperaroom.exception.TextoInvalidoException;
import co.edu.unbosque.vesperaroom.repository.PerfilPropietarioRepository;
import co.edu.unbosque.vesperaroom.repository.UsuarioRepository;
import co.edu.unbosque.vesperaroom.util.CodigoMySQL;
import co.edu.unbosque.vesperaroom.util.Normalizador;

/**
 * Servicio que gestiona los perfiles comerciales de los propietarios. El
 * perfil es una relación 1:1 con el usuario: su identificador es el mismo
 * {@code idUsuario}, por lo que no se genera.
 *
 * <p>
 * Códigos de {@link #create(PerfilPropietarioDTO)} y
 * {@link #updateById(Long, PerfilPropietarioDTO)}:
 * </p>
 * <ul>
 * <li>0: operación exitosa</li>
 * <li>1: NIT con formato inválido</li>
 * <li>2: razón social vacía o con más de 120 caracteres</li>
 * <li>3: banco vacío o con más de 60 caracteres</li>
 * <li>4: tipo de cuenta no indicado</li>
 * <li>5: número de cuenta con formato inválido</li>
 * <li>6: usuario no indicado o inexistente</li>
 * <li>7: el usuario no tiene rol PROPIETARIO</li>
 * <li>8: NIT ya registrado</li>
 * <li>9: cuenta bancaria ya registrada</li>
 * <li>10: el usuario ya tiene perfil (solo al crear)</li>
 * <li>11: perfil no encontrado (solo al actualizar)</li>
 * <li>12: error inesperado</li>
 * </ul>
 * <p>
 * Códigos de {@link #deleteById(Long)}: 0 éxito, 1 no encontrado, 2 tiene
 * moteles asociados, 3 error inesperado.
 * </p>
 */
@Service
public class PerfilPropietarioService implements CRUDOperation<PerfilPropietarioDTO> {

	private static final String ENTIDAD = "perfil de propietario";
	private static final String DUPLICADO_NIT = "ese NIT";
	private static final String DUPLICADO_CUENTA = "esa cuenta bancaria";
	private static final String DUPLICADO_PERFIL = "ese usuario";

	private final PerfilPropietarioRepository perfilPropietarioRepository;
	private final UsuarioRepository usuarioRepository;
	private final ModelMapper mapper;

	public PerfilPropietarioService(PerfilPropietarioRepository perfilPropietarioRepository,
			UsuarioRepository usuarioRepository, ModelMapper mapper) {
		this.perfilPropietarioRepository = perfilPropietarioRepository;
		this.usuarioRepository = usuarioRepository;
		this.mapper = mapper;
	}

	/**
	 * Crea el perfil de un usuario con rol PROPIETARIO. El perfil nace sin
	 * verificar; la verificación la hace un administrador con
	 * {@link #verificar(Long)}.
	 *
	 * @param data objeto con el id del usuario y sus datos comerciales
	 * @return código de resultado según la tabla de la clase (0 a 10 o 12)
	 */
	@Override
	public int create(PerfilPropietarioDTO data) {
		try {
			normalizar(data);
			validarDatos(data);

			Optional<Usuario> usuario = data.getIdUsuario() == null ? Optional.empty()
					: usuarioRepository.findById(data.getIdUsuario());
			LanzadorExcepciones.verificarReferencia(usuario.isPresent(), "usuario", data.getIdUsuario());
			LanzadorExcepciones.verificarRolPropietario(usuario.get().getRol());
			LanzadorExcepciones.verificarDuplicado(perfilPropietarioRepository.existsById(data.getIdUsuario()),
					DUPLICADO_PERFIL);
			LanzadorExcepciones.verificarDuplicado(perfilPropietarioRepository.existsByNit(data.getNit()),
					DUPLICADO_NIT);
			LanzadorExcepciones.verificarDuplicado(
					perfilPropietarioRepository.existsByCuenta(data.getBanco(), data.getNumeroCuenta()),
					DUPLICADO_CUENTA);

			PerfilPropietario entity = mapper.map(data, PerfilPropietario.class);
			entity.setFechaVerificacion(null);
			perfilPropietarioRepository.insertar(entity);
			return 0;

		} catch (NitException e) {
			return 1;
		} catch (TextoInvalidoException e) {
			return "banco".equals(e.getCampo()) ? 3 : 2;
		} catch (CampoObligatorioException e) {
			return 4;
		} catch (CuentaBancariaException e) {
			return 5;
		} catch (ReferenciaNoEncontradaException e) {
			return 6;
		} catch (RolPropietarioException e) {
			return 7;
		} catch (RegistroDuplicadoException e) {
			return codigoDuplicado(e.getDetalle());
		} catch (SQLIntegrityConstraintViolationException e) {
			return CodigoMySQL.esLlaveDuplicada(e) ? codigoDuplicado(e.getMessage()) : 12;
		} catch (Exception e) {
			e.printStackTrace();
			return 12;
		}
	}

	/**
	 * Actualiza los datos comerciales de un perfil. La fecha de verificación no
	 * cambia desde aquí.
	 *
	 * @param id   identificador del perfil (id del usuario propietario)
	 * @param data objeto con los nuevos datos
	 * @return código de resultado según la tabla de la clase (0 a 5, 8, 9, 11 o
	 *         12)
	 */
	@Override
	public int updateById(Long id, PerfilPropietarioDTO data) {
		try {
			Optional<PerfilPropietario> found = perfilPropietarioRepository.findById(id);
			LanzadorExcepciones.verificarExistencia(found.isPresent(), ENTIDAD, id);

			normalizar(data);
			validarDatos(data);
			LanzadorExcepciones.verificarDuplicado(perfilPropietarioRepository.existsByNitAndIdNot(data.getNit(), id),
					DUPLICADO_NIT);
			LanzadorExcepciones.verificarDuplicado(perfilPropietarioRepository
					.existsByCuentaAndIdNot(data.getBanco(), data.getNumeroCuenta(), id), DUPLICADO_CUENTA);

			PerfilPropietario temp = found.get();
			temp.setNit(data.getNit());
			temp.setRazonSocial(data.getRazonSocial());
			temp.setBanco(data.getBanco());
			temp.setTipoCuenta(data.getTipoCuenta());
			temp.setNumeroCuenta(data.getNumeroCuenta());
			perfilPropietarioRepository.actualizar(temp);
			return 0;

		} catch (RegistroNoEncontradoException e) {
			return 11;
		} catch (NitException e) {
			return 1;
		} catch (TextoInvalidoException e) {
			return "banco".equals(e.getCampo()) ? 3 : 2;
		} catch (CampoObligatorioException e) {
			return 4;
		} catch (CuentaBancariaException e) {
			return 5;
		} catch (RegistroDuplicadoException e) {
			return codigoDuplicado(e.getDetalle());
		} catch (SQLIntegrityConstraintViolationException e) {
			return CodigoMySQL.esLlaveDuplicada(e) ? codigoDuplicado(e.getMessage()) : 12;
		} catch (Exception e) {
			e.printStackTrace();
			return 12;
		}
	}

	/**
	 * Marca un perfil como verificado registrando la fecha y hora actual.
	 *
	 * @param id identificador del perfil
	 * @return 0 si es exitoso, 1 si el perfil no existe, 2 si ocurre un error
	 *         inesperado
	 */
	public int verificar(Long id) {
		try {
			LanzadorExcepciones.verificarExistencia(perfilPropietarioRepository.existsById(id), ENTIDAD, id);
			perfilPropietarioRepository.actualizarVerificacion(id, LocalDateTime.now());
			return 0;
		} catch (RegistroNoEncontradoException e) {
			return 1;
		} catch (Exception e) {
			e.printStackTrace();
			return 2;
		}
	}

	/**
	 * Elimina un perfil según su ID.
	 *
	 * @param id identificador del perfil
	 * @return 0 si se eliminó, 1 si no existe, 2 si tiene moteles asociados, 3 si
	 *         ocurre un error inesperado
	 */
	@Override
	public int deleteById(Long id) {
		try {
			LanzadorExcepciones.verificarExistencia(perfilPropietarioRepository.existsById(id), ENTIDAD, id);
			perfilPropietarioRepository.eliminar(id);
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
	 * Retorna todos los perfiles ordenados por razón social.
	 *
	 * @return lista de {@link PerfilPropietarioDTO}
	 * @throws AccesoDatosException si falla la consulta a la base de datos
	 */
	@Override
	public List<PerfilPropietarioDTO> getAll() {
		try {
			List<PerfilPropietarioDTO> dtoList = new ArrayList<>();
			perfilPropietarioRepository.findAll().forEach(entity -> {
				dtoList.add(mapper.map(entity, PerfilPropietarioDTO.class));
			});
			return dtoList;
		} catch (SQLException e) {
			throw new AccesoDatosException(e);
		}
	}

	@Override
	public boolean exist(Long id) {
		try {
			return perfilPropietarioRepository.existsById(id);
		} catch (SQLException e) {
			throw new AccesoDatosException(e);
		}
	}

	@Override
	public long count() {
		try {
			return perfilPropietarioRepository.count();
		} catch (SQLException e) {
			throw new AccesoDatosException(e);
		}
	}

	/**
	 * Busca y retorna el perfil de un propietario.
	 *
	 * @param id identificador del perfil (id del usuario propietario)
	 * @return {@link PerfilPropietarioDTO} encontrado, o {@code null} si no existe
	 * @throws AccesoDatosException si falla la consulta a la base de datos
	 */
	public PerfilPropietarioDTO getById(Long id) {
		try {
			Optional<PerfilPropietario> found = perfilPropietarioRepository.findById(id);

			if (found.isPresent()) {
				return mapper.map(found.get(), PerfilPropietarioDTO.class);
			}

			return null;
		} catch (SQLException e) {
			throw new AccesoDatosException(e);
		}
	}

	/**
	 * Aplica las validaciones de formato sobre los datos comerciales.
	 *
	 * @param data datos a validar
	 * @throws NitException             si el NIT es inválido
	 * @throws TextoInvalidoException   si la razón social o el banco son inválidos
	 * @throws CampoObligatorioException si falta el tipo de cuenta
	 * @throws CuentaBancariaException  si el número de cuenta es inválido
	 */
	private void validarDatos(PerfilPropietarioDTO data)
			throws NitException, TextoInvalidoException, CampoObligatorioException, CuentaBancariaException {
		LanzadorExcepciones.verificarNit(data.getNit());
		LanzadorExcepciones.verificarTexto(data.getRazonSocial(), 120, "razón social");
		LanzadorExcepciones.verificarTexto(data.getBanco(), 60, "banco");
		LanzadorExcepciones.verificarObligatorio(data.getTipoCuenta(), "el tipo de cuenta");
		LanzadorExcepciones.verificarNumeroCuenta(data.getNumeroCuenta());
	}

	/**
	 * Elimina espacios sobrantes de los campos de texto.
	 *
	 * @param data datos a normalizar
	 */
	private void normalizar(PerfilPropietarioDTO data) {
		data.setNit(Normalizador.texto(data.getNit()));
		data.setRazonSocial(Normalizador.texto(data.getRazonSocial()));
		data.setBanco(Normalizador.texto(data.getBanco()));
		data.setNumeroCuenta(Normalizador.texto(data.getNumeroCuenta()));
	}

	/**
	 * Traduce el dato repetido al código de la tabla de la clase. Sirve tanto para
	 * las validaciones previas como para la violación de llave única que reporta
	 * MySQL, cuyo mensaje incluye el nombre de la restricción.
	 *
	 * @param detalle descripción del dato repetido o mensaje de MySQL
	 * @return 8 si es el NIT, 9 si es la cuenta, 10 si el usuario ya tiene perfil
	 */
	private int codigoDuplicado(String detalle) {
		if (detalle == null) {
			return 12;
		}
		if (detalle.equals(DUPLICADO_NIT) || detalle.contains("uq_perfil_propietario_nit")) {
			return 8;
		}
		if (detalle.equals(DUPLICADO_CUENTA) || detalle.contains("uq_perfil_propietario_cuenta")) {
			return 9;
		}
		return 10;
	}
}
