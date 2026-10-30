package co.edu.unbosque.vesperaroom.service;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import co.edu.unbosque.vesperaroom.dto.CaracteristicaDTO;
import co.edu.unbosque.vesperaroom.dto.TipoHabitacionDTO;
import co.edu.unbosque.vesperaroom.entity.TipoHabitacion;
import co.edu.unbosque.vesperaroom.exception.AccesoDatosException;
import co.edu.unbosque.vesperaroom.exception.LanzadorExcepciones;
import co.edu.unbosque.vesperaroom.exception.ReferenciaNoEncontradaException;
import co.edu.unbosque.vesperaroom.exception.RegistroDuplicadoException;
import co.edu.unbosque.vesperaroom.exception.RegistroNoEncontradoException;
import co.edu.unbosque.vesperaroom.exception.TextoInvalidoException;
import co.edu.unbosque.vesperaroom.exception.ValorFueraDeRangoException;
import co.edu.unbosque.vesperaroom.repository.CaracteristicaRepository;
import co.edu.unbosque.vesperaroom.repository.MotelRepository;
import co.edu.unbosque.vesperaroom.repository.TipoHabitacionRepository;
import co.edu.unbosque.vesperaroom.util.CodigoMySQL;
import co.edu.unbosque.vesperaroom.util.Normalizador;

/**
 * Servicio que gestiona los tipos de habitación de cada motel y sus
 * características (relación N:M a través de la tabla
 * {@code tipo_habitacion_caracteristica}).
 *
 * <p>
 * Códigos de {@link #create(TipoHabitacionDTO)} y
 * {@link #updateById(Long, TipoHabitacionDTO)}:
 * </p>
 * <ul>
 * <li>0: operación exitosa</li>
 * <li>1: nombre vacío o con más de 60 caracteres</li>
 * <li>2: descripción con más de 500 caracteres</li>
 * <li>3: capacidad fuera del rango de 1 a 20 personas</li>
 * <li>4: motel no indicado o inexistente (solo al crear)</li>
 * <li>5: el motel ya tiene un tipo con ese nombre</li>
 * <li>6: tipo no encontrado (solo al actualizar)</li>
 * <li>7: error inesperado</li>
 * </ul>
 * <p>
 * Códigos de {@link #deleteById(Long)}: 0 éxito, 1 no encontrado, 2 tiene
 * habitaciones o tarifas asociadas, 3 error inesperado.
 * </p>
 */
@Service
public class TipoHabitacionService implements CRUDOperation<TipoHabitacionDTO> {

	private static final String ENTIDAD = "tipo de habitación";

	private final TipoHabitacionRepository tipoHabitacionRepository;
	private final MotelRepository motelRepository;
	private final CaracteristicaRepository caracteristicaRepository;
	private final ModelMapper mapper;

	public TipoHabitacionService(TipoHabitacionRepository tipoHabitacionRepository, MotelRepository motelRepository,
			CaracteristicaRepository caracteristicaRepository, ModelMapper mapper) {
		this.tipoHabitacionRepository = tipoHabitacionRepository;
		this.motelRepository = motelRepository;
		this.caracteristicaRepository = caracteristicaRepository;
		this.mapper = mapper;
	}

	/**
	 * Crea un nuevo tipo de habitación en un motel.
	 *
	 * @param data objeto con el motel, el nombre, la capacidad y la descripción
	 * @return código de resultado según la tabla de la clase (0 a 5 o 7)
	 */
	@Override
	public int create(TipoHabitacionDTO data) {
		try {
			normalizar(data);
			validar(data);
			LanzadorExcepciones.verificarReferencia(
					data.getIdMotel() != null && motelRepository.existsById(data.getIdMotel()), "motel",
					data.getIdMotel());
			LanzadorExcepciones.verificarDuplicado(
					tipoHabitacionRepository.existsByMotelAndNombre(data.getIdMotel(), data.getNombre()),
					"ese nombre en el motel");

			TipoHabitacion entity = mapper.map(data, TipoHabitacion.class);
			entity.setIdTipo(null);
			tipoHabitacionRepository.insertar(entity);
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
	 * Actualiza un tipo de habitación. El motel no puede cambiar, porque sus
	 * habitaciones lo referencian con la FK compuesta {@code (id_tipo, id_motel)}.
	 *
	 * @param id   identificador del tipo
	 * @param data objeto con los nuevos datos
	 * @return código de resultado según la tabla de la clase (0 a 3 o 5 a 7)
	 */
	@Override
	public int updateById(Long id, TipoHabitacionDTO data) {
		try {
			Optional<TipoHabitacion> found = tipoHabitacionRepository.findById(id);
			LanzadorExcepciones.verificarExistencia(found.isPresent(), ENTIDAD, id);

			normalizar(data);
			validar(data);
			TipoHabitacion temp = found.get();
			LanzadorExcepciones.verificarDuplicado(
					tipoHabitacionRepository.existsByMotelAndNombreAndIdNot(temp.getIdMotel(), data.getNombre(), id),
					"ese nombre en el motel");

			temp.setNombre(data.getNombre());
			temp.setCapacidadPersonas(data.getCapacidadPersonas());
			temp.setDescripcion(data.getDescripcion());
			tipoHabitacionRepository.actualizar(temp);
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
	 * Elimina un tipo de habitación. Sus asignaciones de características se borran
	 * en cascada; si tiene habitaciones o tarifas, MySQL impide el borrado.
	 *
	 * @param id identificador del tipo
	 * @return 0 si se eliminó, 1 si no existe, 2 si tiene registros asociados, 3
	 *         si ocurre un error inesperado
	 */
	@Override
	public int deleteById(Long id) {
		try {
			LanzadorExcepciones.verificarExistencia(tipoHabitacionRepository.existsById(id), ENTIDAD, id);
			tipoHabitacionRepository.eliminar(id);
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
	 * Asigna una característica a un tipo de habitación.
	 *
	 * @param idTipo           identificador del tipo
	 * @param idCaracteristica identificador de la característica
	 * @return 0 si es exitoso, 1 si el tipo no existe, 2 si la característica no
	 *         existe, 3 si ya estaba asignada, 4 si ocurre un error inesperado
	 */
	public int asignarCaracteristica(Long idTipo, Long idCaracteristica) {
		try {
			LanzadorExcepciones.verificarExistencia(tipoHabitacionRepository.existsById(idTipo), ENTIDAD, idTipo);
			LanzadorExcepciones.verificarReferencia(caracteristicaRepository.existsById(idCaracteristica),
					"característica", idCaracteristica);
			LanzadorExcepciones.verificarDuplicado(
					tipoHabitacionRepository.existsCaracteristica(idTipo, idCaracteristica), "esa característica");
			tipoHabitacionRepository.asignarCaracteristica(idTipo, idCaracteristica);
			return 0;
		} catch (RegistroNoEncontradoException e) {
			return 1;
		} catch (ReferenciaNoEncontradaException e) {
			return 2;
		} catch (RegistroDuplicadoException e) {
			return 3;
		} catch (SQLIntegrityConstraintViolationException e) {
			return CodigoMySQL.esLlaveDuplicada(e) ? 3 : 4;
		} catch (Exception e) {
			e.printStackTrace();
			return 4;
		}
	}

	/**
	 * Quita una característica de un tipo de habitación.
	 *
	 * @param idTipo           identificador del tipo
	 * @param idCaracteristica identificador de la característica
	 * @return 0 si es exitoso, 1 si no estaba asignada, 2 si ocurre un error
	 *         inesperado
	 */
	public int quitarCaracteristica(Long idTipo, Long idCaracteristica) {
		try {
			int filas = tipoHabitacionRepository.quitarCaracteristica(idTipo, idCaracteristica);
			return filas > 0 ? 0 : 1;
		} catch (Exception e) {
			e.printStackTrace();
			return 2;
		}
	}

	/**
	 * Retorna todos los tipos de habitación.
	 *
	 * @return lista de {@link TipoHabitacionDTO}
	 * @throws AccesoDatosException si falla la consulta a la base de datos
	 */
	@Override
	public List<TipoHabitacionDTO> getAll() {
		try {
			return toDtoList(tipoHabitacionRepository.findAll());
		} catch (SQLException e) {
			throw new AccesoDatosException(e);
		}
	}

	/**
	 * Retorna los tipos de habitación de un motel.
	 *
	 * @param idMotel identificador del motel
	 * @return lista de {@link TipoHabitacionDTO}
	 * @throws AccesoDatosException si falla la consulta a la base de datos
	 */
	public List<TipoHabitacionDTO> getByMotel(Long idMotel) {
		try {
			return toDtoList(tipoHabitacionRepository.findByMotel(idMotel));
		} catch (SQLException e) {
			throw new AccesoDatosException(e);
		}
	}

	/**
	 * Retorna las características asignadas a un tipo de habitación.
	 *
	 * @param idTipo identificador del tipo
	 * @return lista de {@link CaracteristicaDTO}
	 * @throws AccesoDatosException si falla la consulta a la base de datos
	 */
	public List<CaracteristicaDTO> getCaracteristicas(Long idTipo) {
		try {
			List<CaracteristicaDTO> dtoList = new ArrayList<>();
			tipoHabitacionRepository.findCaracteristicas(idTipo).forEach(entity -> {
				dtoList.add(mapper.map(entity, CaracteristicaDTO.class));
			});
			return dtoList;
		} catch (SQLException e) {
			throw new AccesoDatosException(e);
		}
	}

	@Override
	public boolean exist(Long id) {
		try {
			return tipoHabitacionRepository.existsById(id);
		} catch (SQLException e) {
			throw new AccesoDatosException(e);
		}
	}

	@Override
	public long count() {
		try {
			return tipoHabitacionRepository.count();
		} catch (SQLException e) {
			throw new AccesoDatosException(e);
		}
	}

	/**
	 * Busca y retorna un tipo de habitación según su ID.
	 *
	 * @param id identificador del tipo
	 * @return {@link TipoHabitacionDTO} encontrado, o {@code null} si no existe
	 * @throws AccesoDatosException si falla la consulta a la base de datos
	 */
	public TipoHabitacionDTO getById(Long id) {
		try {
			Optional<TipoHabitacion> found = tipoHabitacionRepository.findById(id);

			if (found.isPresent()) {
				return mapper.map(found.get(), TipoHabitacionDTO.class);
			}

			return null;
		} catch (SQLException e) {
			throw new AccesoDatosException(e);
		}
	}

	/**
	 * Valida el nombre, la descripción y la capacidad.
	 *
	 * @param data datos a validar
	 * @throws TextoInvalidoException     si el nombre o la descripción son
	 *                                    inválidos
	 * @throws ValorFueraDeRangoException si la capacidad está fuera de rango
	 */
	private void validar(TipoHabitacionDTO data) throws TextoInvalidoException, ValorFueraDeRangoException {
		LanzadorExcepciones.verificarTexto(data.getNombre(), 60, "nombre");
		LanzadorExcepciones.verificarTextoOpcional(data.getDescripcion(), 500, "descripción");
		LanzadorExcepciones.verificarRango(data.getCapacidadPersonas(), 1, 20, "capacidad");
	}

	/**
	 * Elimina espacios sobrantes de los campos de texto.
	 *
	 * @param data datos a normalizar
	 */
	private void normalizar(TipoHabitacionDTO data) {
		data.setNombre(Normalizador.texto(data.getNombre()));
		data.setDescripcion(Normalizador.textoOpcional(data.getDescripcion()));
	}

	/**
	 * Convierte una lista de entidades en una lista de DTOs.
	 *
	 * @param entityList lista de entidades
	 * @return lista de {@link TipoHabitacionDTO}
	 */
	private List<TipoHabitacionDTO> toDtoList(List<TipoHabitacion> entityList) {
		List<TipoHabitacionDTO> dtoList = new ArrayList<>();

		entityList.forEach(entity -> {
			dtoList.add(mapper.map(entity, TipoHabitacionDTO.class));
		});

		return dtoList;
	}
}
