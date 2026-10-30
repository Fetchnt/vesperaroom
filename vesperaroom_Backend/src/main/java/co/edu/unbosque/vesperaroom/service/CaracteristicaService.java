package co.edu.unbosque.vesperaroom.service;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import co.edu.unbosque.vesperaroom.dto.CaracteristicaDTO;
import co.edu.unbosque.vesperaroom.entity.Caracteristica;
import co.edu.unbosque.vesperaroom.exception.AccesoDatosException;
import co.edu.unbosque.vesperaroom.exception.LanzadorExcepciones;
import co.edu.unbosque.vesperaroom.exception.RegistroDuplicadoException;
import co.edu.unbosque.vesperaroom.exception.RegistroNoEncontradoException;
import co.edu.unbosque.vesperaroom.exception.TextoInvalidoException;
import co.edu.unbosque.vesperaroom.repository.CaracteristicaRepository;
import co.edu.unbosque.vesperaroom.util.CodigoMySQL;
import co.edu.unbosque.vesperaroom.util.Normalizador;

/**
 * Servicio que gestiona el catálogo global de características de habitación.
 *
 * <p>
 * Códigos de {@link #create(CaracteristicaDTO)} y
 * {@link #updateById(Long, CaracteristicaDTO)}:
 * </p>
 * <ul>
 * <li>0: operación exitosa</li>
 * <li>1: nombre vacío o con más de 60 caracteres</li>
 * <li>2: icono con más de 50 caracteres</li>
 * <li>3: nombre ya registrado</li>
 * <li>4: característica no encontrada (solo al actualizar)</li>
 * <li>5: error inesperado</li>
 * </ul>
 * <p>
 * Códigos de {@link #deleteById(Long)}: 0 éxito, 1 no encontrada, 2 está
 * asignada a algún tipo de habitación, 3 error inesperado.
 * </p>
 */
@Service
public class CaracteristicaService implements CRUDOperation<CaracteristicaDTO> {

	private static final String ENTIDAD = "característica";

	private final CaracteristicaRepository caracteristicaRepository;
	private final ModelMapper mapper;

	public CaracteristicaService(CaracteristicaRepository caracteristicaRepository, ModelMapper mapper) {
		this.caracteristicaRepository = caracteristicaRepository;
		this.mapper = mapper;
	}

	/**
	 * Crea una nueva característica.
	 *
	 * @param data objeto con el nombre y el icono
	 * @return código de resultado según la tabla de la clase (0 a 3 o 5)
	 */
	@Override
	public int create(CaracteristicaDTO data) {
		try {
			validar(data);
			LanzadorExcepciones.verificarDuplicado(caracteristicaRepository.existsByNombre(data.getNombre()),
					"ese nombre");

			Caracteristica entity = mapper.map(data, Caracteristica.class);
			entity.setIdCaracteristica(null);
			caracteristicaRepository.insertar(entity);
			return 0;

		} catch (TextoInvalidoException e) {
			return codigoTexto(e);
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
	 * Actualiza una característica existente.
	 *
	 * @param id   identificador de la característica
	 * @param data objeto con los nuevos datos
	 * @return código de resultado según la tabla de la clase
	 */
	@Override
	public int updateById(Long id, CaracteristicaDTO data) {
		try {
			Optional<Caracteristica> found = caracteristicaRepository.findById(id);
			LanzadorExcepciones.verificarExistencia(found.isPresent(), ENTIDAD, id);

			validar(data);
			LanzadorExcepciones.verificarDuplicado(
					caracteristicaRepository.existsByNombreAndIdNot(data.getNombre(), id), "ese nombre");

			Caracteristica temp = found.get();
			temp.setNombre(data.getNombre());
			temp.setIcono(data.getIcono());
			caracteristicaRepository.actualizar(temp);
			return 0;

		} catch (RegistroNoEncontradoException e) {
			return 4;
		} catch (TextoInvalidoException e) {
			return codigoTexto(e);
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
	 * Elimina una característica según su ID.
	 *
	 * @param id identificador de la característica
	 * @return 0 si se eliminó, 1 si no existe, 2 si está asignada a algún tipo de
	 *         habitación, 3 si ocurre un error inesperado
	 */
	@Override
	public int deleteById(Long id) {
		try {
			LanzadorExcepciones.verificarExistencia(caracteristicaRepository.existsById(id), ENTIDAD, id);
			caracteristicaRepository.eliminar(id);
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
	 * Retorna todas las características ordenadas por nombre.
	 *
	 * @return lista de {@link CaracteristicaDTO}
	 * @throws AccesoDatosException si falla la consulta a la base de datos
	 */
	@Override
	public List<CaracteristicaDTO> getAll() {
		try {
			List<CaracteristicaDTO> dtoList = new ArrayList<>();
			caracteristicaRepository.findAll().forEach(entity -> {
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
			return caracteristicaRepository.existsById(id);
		} catch (SQLException e) {
			throw new AccesoDatosException(e);
		}
	}

	@Override
	public long count() {
		try {
			return caracteristicaRepository.count();
		} catch (SQLException e) {
			throw new AccesoDatosException(e);
		}
	}

	/**
	 * Busca y retorna una característica según su ID.
	 *
	 * @param id identificador de la característica
	 * @return {@link CaracteristicaDTO} encontrada, o {@code null} si no existe
	 * @throws AccesoDatosException si falla la consulta a la base de datos
	 */
	public CaracteristicaDTO getById(Long id) {
		try {
			Optional<Caracteristica> found = caracteristicaRepository.findById(id);

			if (found.isPresent()) {
				return mapper.map(found.get(), CaracteristicaDTO.class);
			}

			return null;
		} catch (SQLException e) {
			throw new AccesoDatosException(e);
		}
	}

	/**
	 * Normaliza y valida el nombre y el icono.
	 *
	 * @param data datos a validar
	 * @throws TextoInvalidoException si el nombre o el icono son inválidos
	 */
	private void validar(CaracteristicaDTO data) throws TextoInvalidoException {
		data.setNombre(Normalizador.texto(data.getNombre()));
		data.setIcono(Normalizador.textoOpcional(data.getIcono()));
		LanzadorExcepciones.verificarTexto(data.getNombre(), 60, "nombre");
		LanzadorExcepciones.verificarTextoOpcional(data.getIcono(), 50, "icono");
	}

	/**
	 * Distingue cuál de los dos textos falló según el campo que guarda la
	 * excepción.
	 *
	 * @param e excepción lanzada por la validación
	 * @return 2 si falló el icono, 1 si falló el nombre
	 */
	private int codigoTexto(TextoInvalidoException e) {
		return "icono".equals(e.getCampo()) ? 2 : 1;
	}
}
