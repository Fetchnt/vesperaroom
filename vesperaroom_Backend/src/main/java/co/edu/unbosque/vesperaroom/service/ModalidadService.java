package co.edu.unbosque.vesperaroom.service;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import co.edu.unbosque.vesperaroom.dto.ModalidadDTO;
import co.edu.unbosque.vesperaroom.entity.Modalidad;
import co.edu.unbosque.vesperaroom.exception.AccesoDatosException;
import co.edu.unbosque.vesperaroom.exception.LanzadorExcepciones;
import co.edu.unbosque.vesperaroom.exception.RegistroDuplicadoException;
import co.edu.unbosque.vesperaroom.exception.RegistroNoEncontradoException;
import co.edu.unbosque.vesperaroom.exception.TextoInvalidoException;
import co.edu.unbosque.vesperaroom.exception.ValorFueraDeRangoException;
import co.edu.unbosque.vesperaroom.repository.ModalidadRepository;
import co.edu.unbosque.vesperaroom.util.CodigoMySQL;
import co.edu.unbosque.vesperaroom.util.Normalizador;

/**
 * Servicio que gestiona el catálogo de modalidades de alquiler. La duración de
 * cada modalidad se usa para calcular la hora de fin de las reservas.
 *
 * <p>
 * Códigos de {@link #create(ModalidadDTO)} y
 * {@link #updateById(Long, ModalidadDTO)}:
 * </p>
 * <ul>
 * <li>0: operación exitosa</li>
 * <li>1: nombre vacío o con más de 40 caracteres</li>
 * <li>2: duración fuera del rango de 1 a 72 horas</li>
 * <li>3: nombre ya registrado</li>
 * <li>4: modalidad no encontrada (solo al actualizar)</li>
 * <li>5: error inesperado</li>
 * </ul>
 * <p>
 * Códigos de {@link #deleteById(Long)}: 0 éxito, 1 no encontrada, 2 tiene
 * tarifas asociadas, 3 error inesperado.
 * </p>
 */
@Service
public class ModalidadService implements CRUDOperation<ModalidadDTO> {

	private static final String ENTIDAD = "modalidad";

	private final ModalidadRepository modalidadRepository;
	private final ModelMapper mapper;

	public ModalidadService(ModalidadRepository modalidadRepository, ModelMapper mapper) {
		this.modalidadRepository = modalidadRepository;
		this.mapper = mapper;
	}

	/**
	 * Crea una nueva modalidad.
	 *
	 * @param data objeto con el nombre y la duración en horas
	 * @return código de resultado según la tabla de la clase (0 a 3 o 5)
	 */
	@Override
	public int create(ModalidadDTO data) {
		try {
			validar(data);
			LanzadorExcepciones.verificarDuplicado(modalidadRepository.existsByNombre(data.getNombre()), "ese nombre");

			Modalidad entity = mapper.map(data, Modalidad.class);
			entity.setIdModalidad(null);
			modalidadRepository.insertar(entity);
			return 0;

		} catch (TextoInvalidoException e) {
			return 1;
		} catch (ValorFueraDeRangoException e) {
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
	 * Actualiza una modalidad existente.
	 *
	 * @param id   identificador de la modalidad
	 * @param data objeto con los nuevos datos
	 * @return código de resultado según la tabla de la clase
	 */
	@Override
	public int updateById(Long id, ModalidadDTO data) {
		try {
			Optional<Modalidad> found = modalidadRepository.findById(id);
			LanzadorExcepciones.verificarExistencia(found.isPresent(), ENTIDAD, id);

			validar(data);
			LanzadorExcepciones.verificarDuplicado(modalidadRepository.existsByNombreAndIdNot(data.getNombre(), id),
					"ese nombre");

			Modalidad temp = found.get();
			temp.setNombre(data.getNombre());
			temp.setDuracionHoras(data.getDuracionHoras());
			modalidadRepository.actualizar(temp);
			return 0;

		} catch (RegistroNoEncontradoException e) {
			return 4;
		} catch (TextoInvalidoException e) {
			return 1;
		} catch (ValorFueraDeRangoException e) {
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
	 * Elimina una modalidad según su ID.
	 *
	 * @param id identificador de la modalidad
	 * @return 0 si se eliminó, 1 si no existe, 2 si tiene tarifas asociadas, 3 si
	 *         ocurre un error inesperado
	 */
	@Override
	public int deleteById(Long id) {
		try {
			LanzadorExcepciones.verificarExistencia(modalidadRepository.existsById(id), ENTIDAD, id);
			modalidadRepository.eliminar(id);
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
	 * Retorna todas las modalidades ordenadas por duración.
	 *
	 * @return lista de {@link ModalidadDTO}
	 * @throws AccesoDatosException si falla la consulta a la base de datos
	 */
	@Override
	public List<ModalidadDTO> getAll() {
		try {
			List<ModalidadDTO> dtoList = new ArrayList<>();
			modalidadRepository.findAll().forEach(entity -> {
				dtoList.add(mapper.map(entity, ModalidadDTO.class));
			});
			return dtoList;
		} catch (SQLException e) {
			throw new AccesoDatosException(e);
		}
	}

	@Override
	public boolean exist(Long id) {
		try {
			return modalidadRepository.existsById(id);
		} catch (SQLException e) {
			throw new AccesoDatosException(e);
		}
	}

	@Override
	public long count() {
		try {
			return modalidadRepository.count();
		} catch (SQLException e) {
			throw new AccesoDatosException(e);
		}
	}

	/**
	 * Busca y retorna una modalidad según su ID.
	 *
	 * @param id identificador de la modalidad
	 * @return {@link ModalidadDTO} encontrada, o {@code null} si no existe
	 * @throws AccesoDatosException si falla la consulta a la base de datos
	 */
	public ModalidadDTO getById(Long id) {
		try {
			Optional<Modalidad> found = modalidadRepository.findById(id);

			if (found.isPresent()) {
				return mapper.map(found.get(), ModalidadDTO.class);
			}

			return null;
		} catch (SQLException e) {
			throw new AccesoDatosException(e);
		}
	}

	/**
	 * Normaliza y valida el nombre y la duración.
	 *
	 * @param data datos a validar
	 * @throws TextoInvalidoException     si el nombre es inválido
	 * @throws ValorFueraDeRangoException si la duración está fuera de rango
	 */
	private void validar(ModalidadDTO data) throws TextoInvalidoException, ValorFueraDeRangoException {
		data.setNombre(Normalizador.texto(data.getNombre()));
		LanzadorExcepciones.verificarTexto(data.getNombre(), 40, "nombre");
		LanzadorExcepciones.verificarRango(data.getDuracionHoras(), 1, 72, "duración en horas");
	}
}
