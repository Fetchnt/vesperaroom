package co.edu.unbosque.vesperaroom.service;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import co.edu.unbosque.vesperaroom.dto.CiudadDTO;
import co.edu.unbosque.vesperaroom.entity.Ciudad;
import co.edu.unbosque.vesperaroom.exception.AccesoDatosException;
import co.edu.unbosque.vesperaroom.exception.LanzadorExcepciones;
import co.edu.unbosque.vesperaroom.exception.ReferenciaNoEncontradaException;
import co.edu.unbosque.vesperaroom.exception.RegistroDuplicadoException;
import co.edu.unbosque.vesperaroom.exception.RegistroNoEncontradoException;
import co.edu.unbosque.vesperaroom.exception.TextoInvalidoException;
import co.edu.unbosque.vesperaroom.repository.CiudadRepository;
import co.edu.unbosque.vesperaroom.repository.DepartamentoRepository;
import co.edu.unbosque.vesperaroom.util.CodigoMySQL;
import co.edu.unbosque.vesperaroom.util.Normalizador;

/**
 * Servicio que gestiona el catálogo de ciudades. El nombre de la ciudad solo
 * debe ser único dentro de su departamento.
 *
 * <p>
 * Códigos de {@link #create(CiudadDTO)} y {@link #updateById(Long, CiudadDTO)}:
 * </p>
 * <ul>
 * <li>0: operación exitosa</li>
 * <li>1: nombre vacío o con más de 80 caracteres</li>
 * <li>2: departamento no indicado o inexistente</li>
 * <li>3: el departamento ya tiene una ciudad con ese nombre</li>
 * <li>4: ciudad no encontrada (solo al actualizar)</li>
 * <li>5: error inesperado</li>
 * </ul>
 * <p>
 * Códigos de {@link #deleteById(Long)}: 0 éxito, 1 no encontrada, 2 tiene
 * moteles asociados, 3 error inesperado.
 * </p>
 */
@Service
public class CiudadService implements CRUDOperation<CiudadDTO> {

	private static final String ENTIDAD = "ciudad";

	private final CiudadRepository ciudadRepository;
	private final DepartamentoRepository departamentoRepository;
	private final ModelMapper mapper;

	public CiudadService(CiudadRepository ciudadRepository, DepartamentoRepository departamentoRepository,
			ModelMapper mapper) {
		this.ciudadRepository = ciudadRepository;
		this.departamentoRepository = departamentoRepository;
		this.mapper = mapper;
	}

	/**
	 * Crea una nueva ciudad dentro de un departamento.
	 *
	 * @param data objeto con el departamento y el nombre de la ciudad
	 * @return código de resultado según la tabla de la clase (0 a 3 o 5)
	 */
	@Override
	public int create(CiudadDTO data) {
		try {
			validar(data);
			LanzadorExcepciones.verificarDuplicado(
					ciudadRepository.existsByDepartamentoAndNombre(data.getIdDepartamento(), data.getNombre()),
					"ese nombre en el departamento");

			Ciudad entity = mapper.map(data, Ciudad.class);
			entity.setIdCiudad(null);
			ciudadRepository.insertar(entity);
			return 0;

		} catch (TextoInvalidoException e) {
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
	 * Actualiza el nombre y el departamento de una ciudad existente.
	 *
	 * @param id   identificador de la ciudad
	 * @param data objeto con los nuevos datos
	 * @return código de resultado según la tabla de la clase
	 */
	@Override
	public int updateById(Long id, CiudadDTO data) {
		try {
			Optional<Ciudad> found = ciudadRepository.findById(id);
			LanzadorExcepciones.verificarExistencia(found.isPresent(), ENTIDAD, id);

			validar(data);
			LanzadorExcepciones.verificarDuplicado(ciudadRepository.existsByDepartamentoAndNombreAndIdNot(
					data.getIdDepartamento(), data.getNombre(), id), "ese nombre en el departamento");

			Ciudad temp = found.get();
			temp.setIdDepartamento(data.getIdDepartamento());
			temp.setNombre(data.getNombre());
			ciudadRepository.actualizar(temp);
			return 0;

		} catch (RegistroNoEncontradoException e) {
			return 4;
		} catch (TextoInvalidoException e) {
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
	 * Elimina una ciudad según su ID.
	 *
	 * @param id identificador de la ciudad
	 * @return 0 si se eliminó, 1 si no existe, 2 si tiene moteles asociados, 3 si
	 *         ocurre un error inesperado
	 */
	@Override
	public int deleteById(Long id) {
		try {
			LanzadorExcepciones.verificarExistencia(ciudadRepository.existsById(id), ENTIDAD, id);
			ciudadRepository.eliminar(id);
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
	 * Retorna todas las ciudades ordenadas por nombre.
	 *
	 * @return lista de {@link CiudadDTO}
	 * @throws AccesoDatosException si falla la consulta a la base de datos
	 */
	@Override
	public List<CiudadDTO> getAll() {
		try {
			return toDtoList(ciudadRepository.findAll());
		} catch (SQLException e) {
			throw new AccesoDatosException(e);
		}
	}

	/**
	 * Retorna las ciudades de un departamento.
	 *
	 * @param idDepartamento identificador del departamento
	 * @return lista de {@link CiudadDTO} del departamento
	 * @throws AccesoDatosException si falla la consulta a la base de datos
	 */
	public List<CiudadDTO> getAllByDepartamento(Long idDepartamento) {
		try {
			return toDtoList(ciudadRepository.findByDepartamento(idDepartamento));
		} catch (SQLException e) {
			throw new AccesoDatosException(e);
		}
	}

	@Override
	public boolean exist(Long id) {
		try {
			return ciudadRepository.existsById(id);
		} catch (SQLException e) {
			throw new AccesoDatosException(e);
		}
	}

	@Override
	public long count() {
		try {
			return ciudadRepository.count();
		} catch (SQLException e) {
			throw new AccesoDatosException(e);
		}
	}

	/**
	 * Busca y retorna una ciudad según su ID.
	 *
	 * @param id identificador de la ciudad
	 * @return {@link CiudadDTO} encontrada, o {@code null} si no existe
	 * @throws AccesoDatosException si falla la consulta a la base de datos
	 */
	public CiudadDTO getById(Long id) {
		try {
			Optional<Ciudad> found = ciudadRepository.findById(id);

			if (found.isPresent()) {
				return mapper.map(found.get(), CiudadDTO.class);
			}

			return null;
		} catch (SQLException e) {
			throw new AccesoDatosException(e);
		}
	}

	/**
	 * Normaliza y valida los datos de una ciudad.
	 *
	 * @param data datos a validar
	 * @throws TextoInvalidoException          si el nombre es inválido
	 * @throws ReferenciaNoEncontradaException si el departamento no existe
	 * @throws SQLException                    si falla la consulta del
	 *                                         departamento
	 */
	private void validar(CiudadDTO data) throws TextoInvalidoException, ReferenciaNoEncontradaException,
			SQLException {
		data.setNombre(Normalizador.texto(data.getNombre()));
		LanzadorExcepciones.verificarTexto(data.getNombre(), 80, "nombre");
		LanzadorExcepciones.verificarReferencia(
				data.getIdDepartamento() != null && departamentoRepository.existsById(data.getIdDepartamento()),
				"departamento", data.getIdDepartamento());
	}

	/**
	 * Convierte una lista de entidades en una lista de DTOs.
	 *
	 * @param entityList lista de entidades
	 * @return lista de {@link CiudadDTO}
	 */
	private List<CiudadDTO> toDtoList(List<Ciudad> entityList) {
		List<CiudadDTO> dtoList = new ArrayList<>();

		entityList.forEach(entity -> {
			dtoList.add(mapper.map(entity, CiudadDTO.class));
		});

		return dtoList;
	}
}
