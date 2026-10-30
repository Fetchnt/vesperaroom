package co.edu.unbosque.vesperaroom.service;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import co.edu.unbosque.vesperaroom.dto.TarifaDTO;
import co.edu.unbosque.vesperaroom.entity.Tarifa;
import co.edu.unbosque.vesperaroom.exception.AccesoDatosException;
import co.edu.unbosque.vesperaroom.exception.CampoObligatorioException;
import co.edu.unbosque.vesperaroom.exception.FechaException;
import co.edu.unbosque.vesperaroom.exception.LanzadorExcepciones;
import co.edu.unbosque.vesperaroom.exception.ReferenciaNoEncontradaException;
import co.edu.unbosque.vesperaroom.exception.RegistroNoEncontradoException;
import co.edu.unbosque.vesperaroom.exception.ValorFueraDeRangoException;
import co.edu.unbosque.vesperaroom.repository.ModalidadRepository;
import co.edu.unbosque.vesperaroom.repository.TarifaRepository;
import co.edu.unbosque.vesperaroom.repository.TipoHabitacionRepository;
import co.edu.unbosque.vesperaroom.util.CodigoMySQL;

/**
 * Servicio que gestiona las tarifas de cada tipo de habitación. La pareja
 * (tipo, modalidad, tipo de día, fecha de inicio de vigencia) es única; esa
 * restricción la valida la base de datos con {@code uq_tarifa_vigencia}.
 *
 * <p>
 * Códigos de {@link #create(TarifaDTO)} y {@link #updateById(Long, TarifaDTO)}:
 * </p>
 * <ul>
 * <li>0: operación exitosa</li>
 * <li>1: precio nulo o menor o igual a cero</li>
 * <li>2: tipo de día no indicado</li>
 * <li>3: vigencia inválida (sin fecha inicial o con la final anterior)</li>
 * <li>4: tipo de habitación no indicado o inexistente (solo al crear)</li>
 * <li>5: modalidad no indicada o inexistente</li>
 * <li>6: ya existe una tarifa igual para ese tipo, modalidad, día y fecha</li>
 * <li>7: tarifa no encontrada (solo al actualizar)</li>
 * <li>8: error inesperado</li>
 * </ul>
 * <p>
 * Códigos de {@link #deleteById(Long)}: 0 éxito, 1 no encontrada, 2 tiene
 * reservas asociadas, 3 error inesperado.
 * </p>
 */
@Service
public class TarifaService implements CRUDOperation<TarifaDTO> {

	private static final String ENTIDAD = "tarifa";
	private static final String ENTIDAD_TIPO = "tipo de habitación";

	private final TarifaRepository tarifaRepository;
	private final TipoHabitacionRepository tipoHabitacionRepository;
	private final ModalidadRepository modalidadRepository;
	private final ModelMapper mapper;

	public TarifaService(TarifaRepository tarifaRepository, TipoHabitacionRepository tipoHabitacionRepository,
			ModalidadRepository modalidadRepository, ModelMapper mapper) {
		this.tarifaRepository = tarifaRepository;
		this.tipoHabitacionRepository = tipoHabitacionRepository;
		this.modalidadRepository = modalidadRepository;
		this.mapper = mapper;
	}

	/**
	 * Crea una nueva tarifa para un tipo de habitación.
	 *
	 * @param data objeto con el tipo, la modalidad, el precio, el tipo de día y la
	 *             vigencia
	 * @return código de resultado según la tabla de la clase (0 a 6 u 8)
	 */
	@Override
	public int create(TarifaDTO data) {
		try {
			validar(data);
			LanzadorExcepciones.verificarReferencia(
					data.getIdTipo() != null && tipoHabitacionRepository.existsById(data.getIdTipo()), ENTIDAD_TIPO,
					data.getIdTipo());
			verificarModalidad(data.getIdModalidad());

			Tarifa entity = mapper.map(data, Tarifa.class);
			entity.setIdTarifa(null);
			tarifaRepository.insertar(entity);
			return 0;

		} catch (ValorFueraDeRangoException e) {
			return 1;
		} catch (CampoObligatorioException e) {
			return 2;
		} catch (FechaException e) {
			return 3;
		} catch (ReferenciaNoEncontradaException e) {
			return ENTIDAD_TIPO.equals(e.getEntidad()) ? 4 : 5;
		} catch (SQLIntegrityConstraintViolationException e) {
			return CodigoMySQL.esLlaveDuplicada(e) ? 6 : 8;
		} catch (Exception e) {
			e.printStackTrace();
			return 8;
		}
	}

	/**
	 * Actualiza una tarifa. El tipo de habitación no cambia.
	 *
	 * @param id   identificador de la tarifa
	 * @param data objeto con los nuevos datos
	 * @return código de resultado según la tabla de la clase (0 a 3 o 5 a 8)
	 */
	@Override
	public int updateById(Long id, TarifaDTO data) {
		try {
			Optional<Tarifa> found = tarifaRepository.findById(id);
			LanzadorExcepciones.verificarExistencia(found.isPresent(), ENTIDAD, id);

			validar(data);
			verificarModalidad(data.getIdModalidad());

			Tarifa temp = found.get();
			temp.setIdModalidad(data.getIdModalidad());
			temp.setPrecio(data.getPrecio());
			temp.setTipoDia(data.getTipoDia());
			temp.setVigenteDesde(data.getVigenteDesde());
			temp.setVigenteHasta(data.getVigenteHasta());
			tarifaRepository.actualizar(temp);
			return 0;

		} catch (RegistroNoEncontradoException e) {
			return 7;
		} catch (ValorFueraDeRangoException e) {
			return 1;
		} catch (CampoObligatorioException e) {
			return 2;
		} catch (FechaException e) {
			return 3;
		} catch (ReferenciaNoEncontradaException e) {
			return 5;
		} catch (SQLIntegrityConstraintViolationException e) {
			return CodigoMySQL.esLlaveDuplicada(e) ? 6 : 8;
		} catch (Exception e) {
			e.printStackTrace();
			return 8;
		}
	}

	/**
	 * Elimina una tarifa según su ID.
	 *
	 * @param id identificador de la tarifa
	 * @return 0 si se eliminó, 1 si no existe, 2 si tiene reservas asociadas, 3 si
	 *         ocurre un error inesperado
	 */
	@Override
	public int deleteById(Long id) {
		try {
			LanzadorExcepciones.verificarExistencia(tarifaRepository.existsById(id), ENTIDAD, id);
			tarifaRepository.eliminar(id);
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
	 * Retorna todas las tarifas.
	 *
	 * @return lista de {@link TarifaDTO}
	 * @throws AccesoDatosException si falla la consulta a la base de datos
	 */
	@Override
	public List<TarifaDTO> getAll() {
		try {
			return toDtoList(tarifaRepository.findAll());
		} catch (SQLException e) {
			throw new AccesoDatosException(e);
		}
	}

	/**
	 * Retorna las tarifas de un tipo de habitación.
	 *
	 * @param idTipo identificador del tipo de habitación
	 * @return lista de {@link TarifaDTO}
	 * @throws AccesoDatosException si falla la consulta a la base de datos
	 */
	public List<TarifaDTO> getByTipo(Long idTipo) {
		try {
			return toDtoList(tarifaRepository.findByTipo(idTipo));
		} catch (SQLException e) {
			throw new AccesoDatosException(e);
		}
	}

	@Override
	public boolean exist(Long id) {
		try {
			return tarifaRepository.existsById(id);
		} catch (SQLException e) {
			throw new AccesoDatosException(e);
		}
	}

	@Override
	public long count() {
		try {
			return tarifaRepository.count();
		} catch (SQLException e) {
			throw new AccesoDatosException(e);
		}
	}

	/**
	 * Busca y retorna una tarifa según su ID.
	 *
	 * @param id identificador de la tarifa
	 * @return {@link TarifaDTO} encontrada, o {@code null} si no existe
	 * @throws AccesoDatosException si falla la consulta a la base de datos
	 */
	public TarifaDTO getById(Long id) {
		try {
			Optional<Tarifa> found = tarifaRepository.findById(id);

			if (found.isPresent()) {
				return mapper.map(found.get(), TarifaDTO.class);
			}

			return null;
		} catch (SQLException e) {
			throw new AccesoDatosException(e);
		}
	}

	/**
	 * Valida el precio, el tipo de día y la vigencia.
	 *
	 * @param data datos a validar
	 * @throws ValorFueraDeRangoException si el precio es inválido
	 * @throws CampoObligatorioException  si falta el tipo de día
	 * @throws FechaException             si la vigencia es inválida
	 */
	private void validar(TarifaDTO data)
			throws ValorFueraDeRangoException, CampoObligatorioException, FechaException {
		LanzadorExcepciones.verificarMonto(data.getPrecio(), false, "precio");
		LanzadorExcepciones.verificarObligatorio(data.getTipoDia(), "el tipo de día");
		LanzadorExcepciones.verificarVigencia(data.getVigenteDesde(), data.getVigenteHasta());
	}

	/**
	 * Verifica que la modalidad indicada exista.
	 *
	 * @param idModalidad identificador de la modalidad
	 * @throws ReferenciaNoEncontradaException si es nula o no existe
	 * @throws SQLException                    si falla la consulta
	 */
	private void verificarModalidad(Long idModalidad) throws ReferenciaNoEncontradaException, SQLException {
		LanzadorExcepciones.verificarReferencia(idModalidad != null && modalidadRepository.existsById(idModalidad),
				"modalidad", idModalidad);
	}

	/**
	 * Convierte una lista de entidades en una lista de DTOs.
	 *
	 * @param entityList lista de entidades
	 * @return lista de {@link TarifaDTO}
	 */
	private List<TarifaDTO> toDtoList(List<Tarifa> entityList) {
		List<TarifaDTO> dtoList = new ArrayList<>();

		entityList.forEach(entity -> {
			dtoList.add(mapper.map(entity, TarifaDTO.class));
		});

		return dtoList;
	}
}
