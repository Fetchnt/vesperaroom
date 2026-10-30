package co.edu.unbosque.vesperaroom.service;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import co.edu.unbosque.vesperaroom.dto.ServicioAdicionalDTO;
import co.edu.unbosque.vesperaroom.entity.ServicioAdicional;
import co.edu.unbosque.vesperaroom.exception.AccesoDatosException;
import co.edu.unbosque.vesperaroom.exception.LanzadorExcepciones;
import co.edu.unbosque.vesperaroom.exception.ReferenciaNoEncontradaException;
import co.edu.unbosque.vesperaroom.exception.RegistroDuplicadoException;
import co.edu.unbosque.vesperaroom.exception.RegistroNoEncontradoException;
import co.edu.unbosque.vesperaroom.exception.TextoInvalidoException;
import co.edu.unbosque.vesperaroom.exception.ValorFueraDeRangoException;
import co.edu.unbosque.vesperaroom.repository.MotelRepository;
import co.edu.unbosque.vesperaroom.repository.ServicioAdicionalRepository;
import co.edu.unbosque.vesperaroom.util.CodigoMySQL;
import co.edu.unbosque.vesperaroom.util.Normalizador;

/**
 * Servicio que gestiona los servicios adicionales que cada motel vende con la
 * reserva. Desactivar un servicio ({@code activo = false}) lo saca del
 * catálogo sin borrar el historial de reservas que lo incluyeron.
 *
 * <p>
 * Códigos de {@link #create(ServicioAdicionalDTO)} y
 * {@link #updateById(Long, ServicioAdicionalDTO)}:
 * </p>
 * <ul>
 * <li>0: operación exitosa</li>
 * <li>1: nombre vacío o con más de 80 caracteres</li>
 * <li>2: descripción con más de 300 caracteres</li>
 * <li>3: precio nulo o negativo</li>
 * <li>4: motel no indicado o inexistente (solo al crear)</li>
 * <li>5: el motel ya tiene un servicio con ese nombre</li>
 * <li>6: servicio no encontrado (solo al actualizar)</li>
 * <li>7: error inesperado</li>
 * </ul>
 * <p>
 * Códigos de {@link #deleteById(Long)}: 0 éxito, 1 no encontrado, 2 está
 * incluido en reservas, 3 error inesperado.
 * </p>
 */
@Service
public class ServicioAdicionalService implements CRUDOperation<ServicioAdicionalDTO> {

	private static final String ENTIDAD = "servicio adicional";

	private final ServicioAdicionalRepository servicioAdicionalRepository;
	private final MotelRepository motelRepository;
	private final ModelMapper mapper;

	public ServicioAdicionalService(ServicioAdicionalRepository servicioAdicionalRepository,
			MotelRepository motelRepository, ModelMapper mapper) {
		this.servicioAdicionalRepository = servicioAdicionalRepository;
		this.motelRepository = motelRepository;
		this.mapper = mapper;
	}

	/**
	 * Crea un nuevo servicio adicional en un motel. Si no se indica, queda activo.
	 *
	 * @param data objeto con el motel, el nombre, la descripción y el precio
	 * @return código de resultado según la tabla de la clase (0 a 5 o 7)
	 */
	@Override
	public int create(ServicioAdicionalDTO data) {
		try {
			normalizar(data);
			validar(data);
			LanzadorExcepciones.verificarReferencia(
					data.getIdMotel() != null && motelRepository.existsById(data.getIdMotel()), "motel",
					data.getIdMotel());
			LanzadorExcepciones.verificarDuplicado(
					servicioAdicionalRepository.existsByMotelAndNombre(data.getIdMotel(), data.getNombre()),
					"ese nombre en el motel");

			ServicioAdicional entity = mapper.map(data, ServicioAdicional.class);
			entity.setIdServicio(null);
			if (data.getActivo() == null) {
				entity.setActivo(Boolean.TRUE);
			}
			servicioAdicionalRepository.insertar(entity);
			return 0;

		} catch (TextoInvalidoException e) {
			return "nombre".equals(e.getCampo()) ? 1 : 2;
		} catch (ValorFueraDeRangoException e) {
			return 3;
		} catch (ReferenciaNoEncontradaException e) {
			return 4;
		} catch (RegistroDuplicadoException e) {
			return 5;
		} catch (SQLIntegrityConstraintViolationException e) {
			return CodigoMySQL.esLlaveDuplicada(e) ? 5 : 7;
		} catch (Exception e) {
			e.printStackTrace();
			return 7;
		}
	}

	/**
	 * Actualiza un servicio adicional. El motel no cambia y, si {@code activo}
	 * llega nulo, se conserva el valor actual.
	 *
	 * @param id   identificador del servicio
	 * @param data objeto con los nuevos datos
	 * @return código de resultado según la tabla de la clase (0 a 3 o 5 a 7)
	 */
	@Override
	public int updateById(Long id, ServicioAdicionalDTO data) {
		try {
			Optional<ServicioAdicional> found = servicioAdicionalRepository.findById(id);
			LanzadorExcepciones.verificarExistencia(found.isPresent(), ENTIDAD, id);

			normalizar(data);
			validar(data);
			ServicioAdicional temp = found.get();
			LanzadorExcepciones.verificarDuplicado(servicioAdicionalRepository
					.existsByMotelAndNombreAndIdNot(temp.getIdMotel(), data.getNombre(), id), "ese nombre en el motel");

			temp.setNombre(data.getNombre());
			temp.setDescripcion(data.getDescripcion());
			temp.setPrecio(data.getPrecio());
			if (data.getActivo() != null) {
				temp.setActivo(data.getActivo());
			}
			servicioAdicionalRepository.actualizar(temp);
			return 0;

		} catch (RegistroNoEncontradoException e) {
			return 6;
		} catch (TextoInvalidoException e) {
			return "nombre".equals(e.getCampo()) ? 1 : 2;
		} catch (ValorFueraDeRangoException e) {
			return 3;
		} catch (RegistroDuplicadoException e) {
			return 5;
		} catch (SQLIntegrityConstraintViolationException e) {
			return CodigoMySQL.esLlaveDuplicada(e) ? 5 : 7;
		} catch (Exception e) {
			e.printStackTrace();
			return 7;
		}
	}

	/**
	 * Elimina un servicio adicional según su ID. Si ya está incluido en alguna
	 * reserva, MySQL impide el borrado; en ese caso conviene desactivarlo.
	 *
	 * @param id identificador del servicio
	 * @return 0 si se eliminó, 1 si no existe, 2 si está incluido en reservas, 3
	 *         si ocurre un error inesperado
	 */
	@Override
	public int deleteById(Long id) {
		try {
			LanzadorExcepciones.verificarExistencia(servicioAdicionalRepository.existsById(id), ENTIDAD, id);
			servicioAdicionalRepository.eliminar(id);
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
	 * Retorna todos los servicios adicionales.
	 *
	 * @return lista de {@link ServicioAdicionalDTO}
	 * @throws AccesoDatosException si falla la consulta a la base de datos
	 */
	@Override
	public List<ServicioAdicionalDTO> getAll() {
		try {
			return toDtoList(servicioAdicionalRepository.findAll());
		} catch (SQLException e) {
			throw new AccesoDatosException(e);
		}
	}

	/**
	 * Retorna todos los servicios de un motel, activos o no.
	 *
	 * @param idMotel identificador del motel
	 * @return lista de {@link ServicioAdicionalDTO}
	 * @throws AccesoDatosException si falla la consulta a la base de datos
	 */
	public List<ServicioAdicionalDTO> getByMotel(Long idMotel) {
		try {
			return toDtoList(servicioAdicionalRepository.findByMotel(idMotel));
		} catch (SQLException e) {
			throw new AccesoDatosException(e);
		}
	}

	/**
	 * Retorna los servicios activos de un motel, que son los que puede elegir un
	 * cliente.
	 *
	 * @param idMotel identificador del motel
	 * @return lista de {@link ServicioAdicionalDTO} activos
	 * @throws AccesoDatosException si falla la consulta a la base de datos
	 */
	public List<ServicioAdicionalDTO> getActivosByMotel(Long idMotel) {
		try {
			return toDtoList(servicioAdicionalRepository.findActivosByMotel(idMotel));
		} catch (SQLException e) {
			throw new AccesoDatosException(e);
		}
	}

	@Override
	public boolean exist(Long id) {
		try {
			return servicioAdicionalRepository.existsById(id);
		} catch (SQLException e) {
			throw new AccesoDatosException(e);
		}
	}

	@Override
	public long count() {
		try {
			return servicioAdicionalRepository.count();
		} catch (SQLException e) {
			throw new AccesoDatosException(e);
		}
	}

	/**
	 * Busca y retorna un servicio adicional según su ID.
	 *
	 * @param id identificador del servicio
	 * @return {@link ServicioAdicionalDTO} encontrado, o {@code null} si no existe
	 * @throws AccesoDatosException si falla la consulta a la base de datos
	 */
	public ServicioAdicionalDTO getById(Long id) {
		try {
			Optional<ServicioAdicional> found = servicioAdicionalRepository.findById(id);

			if (found.isPresent()) {
				return mapper.map(found.get(), ServicioAdicionalDTO.class);
			}

			return null;
		} catch (SQLException e) {
			throw new AccesoDatosException(e);
		}
	}

	/**
	 * Valida el nombre, la descripción y el precio.
	 *
	 * @param data datos a validar
	 * @throws TextoInvalidoException     si el nombre o la descripción son
	 *                                    inválidos
	 * @throws ValorFueraDeRangoException si el precio es inválido
	 */
	private void validar(ServicioAdicionalDTO data) throws TextoInvalidoException, ValorFueraDeRangoException {
		LanzadorExcepciones.verificarTexto(data.getNombre(), 80, "nombre");
		LanzadorExcepciones.verificarTextoOpcional(data.getDescripcion(), 300, "descripción");
		LanzadorExcepciones.verificarMonto(data.getPrecio(), true, "precio");
	}

	/**
	 * Elimina espacios sobrantes de los campos de texto.
	 *
	 * @param data datos a normalizar
	 */
	private void normalizar(ServicioAdicionalDTO data) {
		data.setNombre(Normalizador.texto(data.getNombre()));
		data.setDescripcion(Normalizador.textoOpcional(data.getDescripcion()));
	}

	/**
	 * Convierte una lista de entidades en una lista de DTOs.
	 *
	 * @param entityList lista de entidades
	 * @return lista de {@link ServicioAdicionalDTO}
	 */
	private List<ServicioAdicionalDTO> toDtoList(List<ServicioAdicional> entityList) {
		List<ServicioAdicionalDTO> dtoList = new ArrayList<>();

		entityList.forEach(entity -> {
			dtoList.add(mapper.map(entity, ServicioAdicionalDTO.class));
		});

		return dtoList;
	}
}
