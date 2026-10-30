package co.edu.unbosque.vesperaroom.service;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import co.edu.unbosque.vesperaroom.dto.PoliticaReservaDTO;
import co.edu.unbosque.vesperaroom.entity.PoliticaReserva;
import co.edu.unbosque.vesperaroom.exception.AccesoDatosException;
import co.edu.unbosque.vesperaroom.exception.LanzadorExcepciones;
import co.edu.unbosque.vesperaroom.exception.ReferenciaNoEncontradaException;
import co.edu.unbosque.vesperaroom.exception.RegistroDuplicadoException;
import co.edu.unbosque.vesperaroom.exception.RegistroNoEncontradoException;
import co.edu.unbosque.vesperaroom.exception.ValorFueraDeRangoException;
import co.edu.unbosque.vesperaroom.repository.MotelRepository;
import co.edu.unbosque.vesperaroom.repository.PoliticaReservaRepository;
import co.edu.unbosque.vesperaroom.util.CodigoMySQL;

/**
 * Servicio que gestiona la política de reservas de cada motel (relación 1:1).
 * Al consultar, calcula el atributo derivado {@code aceptaReembolso} a partir
 * del porcentaje de reembolso, en lugar de guardarlo en la base (3FN).
 *
 * <p>
 * Códigos de {@link #create(PoliticaReservaDTO)} y
 * {@link #updateById(Long, PoliticaReservaDTO)}:
 * </p>
 * <ul>
 * <li>0: operación exitosa</li>
 * <li>1: algún valor numérico está fuera de su rango</li>
 * <li>2: motel no indicado o inexistente</li>
 * <li>3: el motel ya tiene política (solo al crear)</li>
 * <li>4: política no encontrada (solo al actualizar)</li>
 * <li>5: error inesperado</li>
 * </ul>
 * <p>
 * Códigos de {@link #deleteById(Long)}: 0 éxito, 1 no encontrada, 2 tiene
 * registros asociados, 3 error inesperado.
 * </p>
 */
@Service
public class PoliticaReservaService implements CRUDOperation<PoliticaReservaDTO> {

	private static final String ENTIDAD = "política de reserva";

	private final PoliticaReservaRepository politicaReservaRepository;
	private final MotelRepository motelRepository;
	private final ModelMapper mapper;

	public PoliticaReservaService(PoliticaReservaRepository politicaReservaRepository,
			MotelRepository motelRepository, ModelMapper mapper) {
		this.politicaReservaRepository = politicaReservaRepository;
		this.motelRepository = motelRepository;
		this.mapper = mapper;
	}

	/**
	 * Crea la política de reservas de un motel.
	 *
	 * @param data objeto con el id del motel y los valores de la política
	 * @return código de resultado según la tabla de la clase (0 a 3 o 5)
	 */
	@Override
	public int create(PoliticaReservaDTO data) {
		try {
			validarValores(data);
			LanzadorExcepciones.verificarReferencia(
					data.getIdMotel() != null && motelRepository.existsById(data.getIdMotel()), "motel",
					data.getIdMotel());
			LanzadorExcepciones.verificarDuplicado(politicaReservaRepository.existsById(data.getIdMotel()),
					"ese motel");

			politicaReservaRepository.insertar(mapper.map(data, PoliticaReserva.class));
			return 0;

		} catch (ValorFueraDeRangoException e) {
			return 1;
		} catch (ReferenciaNoEncontradaException e) {
			return 2;
		} catch (RegistroDuplicadoException e) {
			return 3;
		} catch (SQLIntegrityConstraintViolationException e) {
			return CodigoMySQL.esLlaveDuplicada(e) ? 3 : 5;
		} catch (Exception e) {
			e.printStackTrace();
			return 5;
		}
	}

	/**
	 * Actualiza la política de reservas de un motel.
	 *
	 * @param id   identificador del motel dueño de la política
	 * @param data objeto con los nuevos valores
	 * @return código de resultado según la tabla de la clase (0, 1, 4 o 5)
	 */
	@Override
	public int updateById(Long id, PoliticaReservaDTO data) {
		try {
			LanzadorExcepciones.verificarExistencia(politicaReservaRepository.existsById(id), ENTIDAD, id);
			validarValores(data);

			PoliticaReserva entity = mapper.map(data, PoliticaReserva.class);
			entity.setIdMotel(id);
			politicaReservaRepository.actualizar(entity);
			return 0;

		} catch (RegistroNoEncontradoException e) {
			return 4;
		} catch (ValorFueraDeRangoException e) {
			return 1;
		} catch (Exception e) {
			e.printStackTrace();
			return 5;
		}
	}

	/**
	 * Elimina la política de reservas de un motel.
	 *
	 * @param id identificador del motel dueño de la política
	 * @return 0 si se eliminó, 1 si no existe, 2 si tiene registros asociados, 3
	 *         si ocurre un error inesperado
	 */
	@Override
	public int deleteById(Long id) {
		try {
			LanzadorExcepciones.verificarExistencia(politicaReservaRepository.existsById(id), ENTIDAD, id);
			politicaReservaRepository.eliminar(id);
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
	 * Retorna las políticas de todos los moteles.
	 *
	 * @return lista de {@link PoliticaReservaDTO}
	 * @throws AccesoDatosException si falla la consulta a la base de datos
	 */
	@Override
	public List<PoliticaReservaDTO> getAll() {
		try {
			List<PoliticaReservaDTO> dtoList = new ArrayList<>();
			politicaReservaRepository.findAll().forEach(entity -> {
				dtoList.add(toDto(entity));
			});
			return dtoList;
		} catch (SQLException e) {
			throw new AccesoDatosException(e);
		}
	}

	@Override
	public boolean exist(Long id) {
		try {
			return politicaReservaRepository.existsById(id);
		} catch (SQLException e) {
			throw new AccesoDatosException(e);
		}
	}

	@Override
	public long count() {
		try {
			return politicaReservaRepository.count();
		} catch (SQLException e) {
			throw new AccesoDatosException(e);
		}
	}

	/**
	 * Busca y retorna la política de un motel.
	 *
	 * @param idMotel identificador del motel
	 * @return {@link PoliticaReservaDTO} con {@code aceptaReembolso} calculado, o
	 *         {@code null} si el motel no tiene política
	 * @throws AccesoDatosException si falla la consulta a la base de datos
	 */
	public PoliticaReservaDTO getById(Long idMotel) {
		try {
			Optional<PoliticaReserva> found = politicaReservaRepository.findById(idMotel);

			if (found.isPresent()) {
				return toDto(found.get());
			}

			return null;
		} catch (SQLException e) {
			throw new AccesoDatosException(e);
		}
	}

	/**
	 * Valida que cada valor numérico esté dentro del rango permitido por los
	 * {@code CHECK} de la tabla.
	 *
	 * @param data datos a validar
	 * @throws ValorFueraDeRangoException si algún valor está fuera de rango
	 */
	private void validarValores(PoliticaReservaDTO data) throws ValorFueraDeRangoException {
		LanzadorExcepciones.verificarRango(data.getDiasHabilesDevolucion(), 0, 60, "días hábiles de devolución");
		LanzadorExcepciones.verificarRango(data.getPorcentajeReembolso(), 0, 100, "porcentaje de reembolso");
		LanzadorExcepciones.verificarRango(data.getCapacidadMaxSinRecargo(), 1, 20, "capacidad sin recargo");
		LanzadorExcepciones.verificarMonto(data.getTarifaPersonaAdicional(), true, "tarifa por persona adicional");
		LanzadorExcepciones.verificarRango(data.getAnticipacionMinimaHoras(), 0, 720, "anticipación mínima");
		LanzadorExcepciones.verificarRango(data.getTiempoGraciaNoShowMin(), 0, 240, "tiempo de gracia");
	}

	/**
	 * Convierte la entidad en DTO y calcula el atributo derivado
	 * {@code aceptaReembolso}.
	 *
	 * @param entity entidad a convertir
	 * @return {@link PoliticaReservaDTO} con el atributo derivado
	 */
	private PoliticaReservaDTO toDto(PoliticaReserva entity) {
		PoliticaReservaDTO dto = mapper.map(entity, PoliticaReservaDTO.class);
		dto.setAceptaReembolso(entity.getPorcentajeReembolso() > 0);
		return dto;
	}
}
