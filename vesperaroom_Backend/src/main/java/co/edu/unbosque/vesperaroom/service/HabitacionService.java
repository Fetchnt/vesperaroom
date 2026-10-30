package co.edu.unbosque.vesperaroom.service;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import co.edu.unbosque.vesperaroom.dto.HabitacionDTO;
import co.edu.unbosque.vesperaroom.entity.Habitacion;
import co.edu.unbosque.vesperaroom.entity.Habitacion.Estado;
import co.edu.unbosque.vesperaroom.entity.TipoHabitacion;
import co.edu.unbosque.vesperaroom.exception.AccesoDatosException;
import co.edu.unbosque.vesperaroom.exception.CampoObligatorioException;
import co.edu.unbosque.vesperaroom.exception.LanzadorExcepciones;
import co.edu.unbosque.vesperaroom.exception.ReferenciaNoEncontradaException;
import co.edu.unbosque.vesperaroom.exception.RegistroDuplicadoException;
import co.edu.unbosque.vesperaroom.exception.RegistroNoEncontradoException;
import co.edu.unbosque.vesperaroom.exception.TextoInvalidoException;
import co.edu.unbosque.vesperaroom.exception.ValorFueraDeRangoException;
import co.edu.unbosque.vesperaroom.repository.HabitacionRepository;
import co.edu.unbosque.vesperaroom.repository.TipoHabitacionRepository;
import co.edu.unbosque.vesperaroom.util.CodigoMySQL;
import co.edu.unbosque.vesperaroom.util.Normalizador;

/**
 * Servicio que gestiona las habitaciones de cada motel. El motel de la
 * habitación nunca se recibe del cliente: se toma del tipo de habitación, de
 * modo que {@code id_motel} siempre coincide con el motel del tipo (la FK
 * compuesta de la tabla lo garantiza también en la base).
 *
 * <p>
 * Códigos de {@link #create(HabitacionDTO)} y
 * {@link #updateById(Long, HabitacionDTO)}:
 * </p>
 * <ul>
 * <li>0: operación exitosa</li>
 * <li>1: número vacío o con más de 10 caracteres</li>
 * <li>2: piso fuera del rango de -5 a 200</li>
 * <li>3: tipo de habitación no indicado o inexistente</li>
 * <li>4: el motel ya tiene una habitación con ese número</li>
 * <li>5: habitación no encontrada (solo al actualizar)</li>
 * <li>6: error inesperado</li>
 * </ul>
 * <p>
 * Códigos de {@link #updateEstado(Long, Estado)}: 0 éxito, 1 estado nulo, 2 no
 * encontrada, 3 error inesperado. Códigos de {@link #deleteById(Long)}: 0
 * éxito, 1 no encontrada, 2 tiene reservas o fotos asociadas, 3 error
 * inesperado.
 * </p>
 */
@Service
public class HabitacionService implements CRUDOperation<HabitacionDTO> {

	private static final String ENTIDAD = "habitación";

	private final HabitacionRepository habitacionRepository;
	private final TipoHabitacionRepository tipoHabitacionRepository;
	private final ModelMapper mapper;

	public HabitacionService(HabitacionRepository habitacionRepository,
			TipoHabitacionRepository tipoHabitacionRepository, ModelMapper mapper) {
		this.habitacionRepository = habitacionRepository;
		this.tipoHabitacionRepository = tipoHabitacionRepository;
		this.mapper = mapper;
	}

	/**
	 * Crea una nueva habitación. El motel se toma del tipo indicado y, si no se
	 * envía estado, queda {@code ACTIVA}.
	 *
	 * @param data objeto con el tipo, el número, el piso y el estado
	 * @return código de resultado según la tabla de la clase (0 a 4 o 6)
	 */
	@Override
	public int create(HabitacionDTO data) {
		try {
			data.setNumero(Normalizador.texto(data.getNumero()));
			validar(data);
			TipoHabitacion tipo = obtenerTipo(data.getIdTipo());
			LanzadorExcepciones.verificarDuplicado(
					habitacionRepository.existsByMotelAndNumero(tipo.getIdMotel(), data.getNumero()),
					"ese número en el motel");

			Habitacion entity = mapper.map(data, Habitacion.class);
			entity.setIdHabitacion(null);
			entity.setIdMotel(tipo.getIdMotel());
			if (data.getEstado() == null) {
				entity.setEstado(Estado.ACTIVA);
			}
			habitacionRepository.insertar(entity);
			return 0;

		} catch (TextoInvalidoException e) {
			return 1;
		} catch (ValorFueraDeRangoException e) {
			return 2;
		} catch (ReferenciaNoEncontradaException e) {
			return 3;
		} catch (RegistroDuplicadoException e) {
			return 4;
		} catch (SQLIntegrityConstraintViolationException e) {
			return CodigoMySQL.esLlaveDuplicada(e) ? 4 : 6;
		} catch (Exception e) {
			e.printStackTrace();
			return 6;
		}
	}

	/**
	 * Actualiza una habitación. Si cambia el tipo, el motel se recalcula a partir
	 * del nuevo tipo. Si el estado llega nulo se conserva el actual.
	 *
	 * @param id   identificador de la habitación
	 * @param data objeto con los nuevos datos
	 * @return código de resultado según la tabla de la clase
	 */
	@Override
	public int updateById(Long id, HabitacionDTO data) {
		try {
			Optional<Habitacion> found = habitacionRepository.findById(id);
			LanzadorExcepciones.verificarExistencia(found.isPresent(), ENTIDAD, id);

			data.setNumero(Normalizador.texto(data.getNumero()));
			validar(data);
			TipoHabitacion tipo = obtenerTipo(data.getIdTipo());
			LanzadorExcepciones.verificarDuplicado(
					habitacionRepository.existsByMotelAndNumeroAndIdNot(tipo.getIdMotel(), data.getNumero(), id),
					"ese número en el motel");

			Habitacion temp = found.get();
			temp.setIdTipo(tipo.getIdTipo());
			temp.setIdMotel(tipo.getIdMotel());
			temp.setNumero(data.getNumero());
			temp.setPiso(data.getPiso());
			if (data.getEstado() != null) {
				temp.setEstado(data.getEstado());
			}
			habitacionRepository.actualizar(temp);
			return 0;

		} catch (RegistroNoEncontradoException e) {
			return 5;
		} catch (TextoInvalidoException e) {
			return 1;
		} catch (ValorFueraDeRangoException e) {
			return 2;
		} catch (ReferenciaNoEncontradaException e) {
			return 3;
		} catch (RegistroDuplicadoException e) {
			return 4;
		} catch (SQLIntegrityConstraintViolationException e) {
			return CodigoMySQL.esLlaveDuplicada(e) ? 4 : 6;
		} catch (Exception e) {
			e.printStackTrace();
			return 6;
		}
	}

	/**
	 * Cambia el estado operativo de una habitación.
	 *
	 * @param id     identificador de la habitación
	 * @param estado nuevo estado
	 * @return 0 si es exitoso, 1 si el estado es nulo, 2 si no existe, 3 si
	 *         ocurre un error inesperado
	 */
	public int updateEstado(Long id, Estado estado) {
		try {
			LanzadorExcepciones.verificarObligatorio(estado, "el estado");
			LanzadorExcepciones.verificarExistencia(habitacionRepository.existsById(id), ENTIDAD, id);
			habitacionRepository.actualizarEstado(id, estado);
			return 0;
		} catch (CampoObligatorioException e) {
			return 1;
		} catch (RegistroNoEncontradoException e) {
			return 2;
		} catch (Exception e) {
			e.printStackTrace();
			return 3;
		}
	}

	/**
	 * Elimina una habitación según su ID.
	 *
	 * @param id identificador de la habitación
	 * @return 0 si se eliminó, 1 si no existe, 2 si tiene reservas o fotos
	 *         asociadas, 3 si ocurre un error inesperado
	 */
	@Override
	public int deleteById(Long id) {
		try {
			LanzadorExcepciones.verificarExistencia(habitacionRepository.existsById(id), ENTIDAD, id);
			habitacionRepository.eliminar(id);
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
	 * Retorna todas las habitaciones.
	 *
	 * @return lista de {@link HabitacionDTO}
	 * @throws AccesoDatosException si falla la consulta a la base de datos
	 */
	@Override
	public List<HabitacionDTO> getAll() {
		try {
			return toDtoList(habitacionRepository.findAll());
		} catch (SQLException e) {
			throw new AccesoDatosException(e);
		}
	}

	/**
	 * Retorna las habitaciones de un motel.
	 *
	 * @param idMotel identificador del motel
	 * @return lista de {@link HabitacionDTO}
	 * @throws AccesoDatosException si falla la consulta a la base de datos
	 */
	public List<HabitacionDTO> getByMotel(Long idMotel) {
		try {
			return toDtoList(habitacionRepository.findByMotel(idMotel));
		} catch (SQLException e) {
			throw new AccesoDatosException(e);
		}
	}

	/**
	 * Retorna las habitaciones de un tipo.
	 *
	 * @param idTipo identificador del tipo de habitación
	 * @return lista de {@link HabitacionDTO}
	 * @throws AccesoDatosException si falla la consulta a la base de datos
	 */
	public List<HabitacionDTO> getByTipo(Long idTipo) {
		try {
			return toDtoList(habitacionRepository.findByTipo(idTipo));
		} catch (SQLException e) {
			throw new AccesoDatosException(e);
		}
	}

	/**
	 * Retorna las habitaciones activas de un motel que están libres en el rango
	 * indicado.
	 *
	 * @param idMotel identificador del motel
	 * @param inicio  fecha y hora de inicio
	 * @param fin     fecha y hora de fin
	 * @return lista de {@link HabitacionDTO} disponibles
	 * @throws AccesoDatosException si falla la consulta a la base de datos
	 */
	public List<HabitacionDTO> getDisponibles(Long idMotel, LocalDateTime inicio, LocalDateTime fin) {
		try {
			return toDtoList(habitacionRepository.findDisponibles(idMotel, inicio, fin));
		} catch (SQLException e) {
			throw new AccesoDatosException(e);
		}
	}

	@Override
	public boolean exist(Long id) {
		try {
			return habitacionRepository.existsById(id);
		} catch (SQLException e) {
			throw new AccesoDatosException(e);
		}
	}

	@Override
	public long count() {
		try {
			return habitacionRepository.count();
		} catch (SQLException e) {
			throw new AccesoDatosException(e);
		}
	}

	/**
	 * Busca y retorna una habitación según su ID.
	 *
	 * @param id identificador de la habitación
	 * @return {@link HabitacionDTO} encontrada, o {@code null} si no existe
	 * @throws AccesoDatosException si falla la consulta a la base de datos
	 */
	public HabitacionDTO getById(Long id) {
		try {
			Optional<Habitacion> found = habitacionRepository.findById(id);

			if (found.isPresent()) {
				return mapper.map(found.get(), HabitacionDTO.class);
			}

			return null;
		} catch (SQLException e) {
			throw new AccesoDatosException(e);
		}
	}

	/**
	 * Valida el número y el piso de la habitación.
	 *
	 * @param data datos a validar
	 * @throws TextoInvalidoException     si el número es inválido
	 * @throws ValorFueraDeRangoException si el piso está fuera de rango
	 */
	private void validar(HabitacionDTO data) throws TextoInvalidoException, ValorFueraDeRangoException {
		LanzadorExcepciones.verificarTexto(data.getNumero(), 10, "número");
		LanzadorExcepciones.verificarRango(data.getPiso(), -5, 200, "piso");
	}

	/**
	 * Busca el tipo de habitación indicado, del que se toma el motel.
	 *
	 * @param idTipo identificador del tipo
	 * @return tipo de habitación encontrado
	 * @throws ReferenciaNoEncontradaException si es nulo o no existe
	 * @throws SQLException                    si falla la consulta
	 */
	private TipoHabitacion obtenerTipo(Long idTipo) throws ReferenciaNoEncontradaException, SQLException {
		Optional<TipoHabitacion> tipo = idTipo == null ? Optional.empty() : tipoHabitacionRepository.findById(idTipo);
		LanzadorExcepciones.verificarReferencia(tipo.isPresent(), "tipo de habitación", idTipo);
		return tipo.get();
	}

	/**
	 * Convierte una lista de entidades en una lista de DTOs.
	 *
	 * @param entityList lista de entidades
	 * @return lista de {@link HabitacionDTO}
	 */
	private List<HabitacionDTO> toDtoList(List<Habitacion> entityList) {
		List<HabitacionDTO> dtoList = new ArrayList<>();

		entityList.forEach(entity -> {
			dtoList.add(mapper.map(entity, HabitacionDTO.class));
		});

		return dtoList;
	}
}
