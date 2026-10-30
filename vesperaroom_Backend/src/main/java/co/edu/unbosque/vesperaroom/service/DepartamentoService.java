package co.edu.unbosque.vesperaroom.service;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import co.edu.unbosque.vesperaroom.dto.DepartamentoDTO;
import co.edu.unbosque.vesperaroom.entity.Departamento;
import co.edu.unbosque.vesperaroom.exception.AccesoDatosException;
import co.edu.unbosque.vesperaroom.exception.LanzadorExcepciones;
import co.edu.unbosque.vesperaroom.exception.RegistroDuplicadoException;
import co.edu.unbosque.vesperaroom.exception.RegistroNoEncontradoException;
import co.edu.unbosque.vesperaroom.exception.TextoInvalidoException;
import co.edu.unbosque.vesperaroom.repository.DepartamentoRepository;
import co.edu.unbosque.vesperaroom.util.CodigoMySQL;
import co.edu.unbosque.vesperaroom.util.Normalizador;

/**
 * Servicio que gestiona el catálogo de departamentos. Las validaciones se
 * delegan a {@link LanzadorExcepciones} y cada excepción se traduce a un código
 * de resultado.
 *
 * <p>
 * Códigos de {@link #create(DepartamentoDTO)} y
 * {@link #updateById(Long, DepartamentoDTO)}:
 * </p>
 * <ul>
 * <li>0: operación exitosa</li>
 * <li>1: nombre vacío o con más de 60 caracteres</li>
 * <li>2: nombre ya registrado</li>
 * <li>3: departamento no encontrado (solo al actualizar)</li>
 * <li>4: error inesperado</li>
 * </ul>
 * <p>
 * Códigos de {@link #deleteById(Long)}: 0 éxito, 1 no encontrado, 2 tiene
 * ciudades asociadas, 3 error inesperado.
 * </p>
 */
@Service
public class DepartamentoService implements CRUDOperation<DepartamentoDTO> {

	private static final String ENTIDAD = "departamento";

	private final DepartamentoRepository departamentoRepository;
	private final ModelMapper mapper;

	public DepartamentoService(DepartamentoRepository departamentoRepository, ModelMapper mapper) {
		this.departamentoRepository = departamentoRepository;
		this.mapper = mapper;
	}

	/**
	 * Crea un nuevo departamento.
	 *
	 * @param data objeto con el nombre del departamento
	 * @return código de resultado según la tabla de la clase (0 a 2 o 4)
	 */
	@Override
	public int create(DepartamentoDTO data) {
		try {
			data.setNombre(Normalizador.texto(data.getNombre()));
			LanzadorExcepciones.verificarTexto(data.getNombre(), 60, "nombre");
			LanzadorExcepciones.verificarDuplicado(departamentoRepository.existsByNombre(data.getNombre()),
					"ese nombre");

			Departamento entity = mapper.map(data, Departamento.class);
			entity.setIdDepartamento(null);
			departamentoRepository.insertar(entity);
			return 0;

		} catch (TextoInvalidoException e) {
			return 1;
		} catch (RegistroDuplicadoException e) {
			return 2;
		} catch (SQLIntegrityConstraintViolationException e) {
			return CodigoMySQL.esLlaveDuplicada(e) ? 2 : 4;
		} catch (Exception e) {
			e.printStackTrace();
			return 4;
		}
	}

	/**
	 * Actualiza el nombre de un departamento existente.
	 *
	 * @param id   identificador del departamento
	 * @param data objeto con el nuevo nombre
	 * @return código de resultado según la tabla de la clase
	 */
	@Override
	public int updateById(Long id, DepartamentoDTO data) {
		try {
			Optional<Departamento> found = departamentoRepository.findById(id);
			LanzadorExcepciones.verificarExistencia(found.isPresent(), ENTIDAD, id);

			data.setNombre(Normalizador.texto(data.getNombre()));
			LanzadorExcepciones.verificarTexto(data.getNombre(), 60, "nombre");
			LanzadorExcepciones.verificarDuplicado(
					departamentoRepository.existsByNombreAndIdNot(data.getNombre(), id), "ese nombre");

			Departamento temp = found.get();
			temp.setNombre(data.getNombre());
			departamentoRepository.actualizar(temp);
			return 0;

		} catch (RegistroNoEncontradoException e) {
			return 3;
		} catch (TextoInvalidoException e) {
			return 1;
		} catch (RegistroDuplicadoException e) {
			return 2;
		} catch (SQLIntegrityConstraintViolationException e) {
			return CodigoMySQL.esLlaveDuplicada(e) ? 2 : 4;
		} catch (Exception e) {
			e.printStackTrace();
			return 4;
		}
	}

	/**
	 * Elimina un departamento según su ID.
	 *
	 * @param id identificador del departamento
	 * @return 0 si se eliminó, 1 si no existe, 2 si tiene ciudades asociadas, 3 si
	 *         ocurre un error inesperado
	 */
	@Override
	public int deleteById(Long id) {
		try {
			LanzadorExcepciones.verificarExistencia(departamentoRepository.existsById(id), ENTIDAD, id);
			departamentoRepository.eliminar(id);
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
	 * Retorna todos los departamentos ordenados por nombre.
	 *
	 * @return lista de {@link DepartamentoDTO}
	 * @throws AccesoDatosException si falla la consulta a la base de datos
	 */
	@Override
	public List<DepartamentoDTO> getAll() {
		try {
			return toDtoList(departamentoRepository.findAll());
		} catch (SQLException e) {
			throw new AccesoDatosException(e);
		}
	}

	@Override
	public boolean exist(Long id) {
		try {
			return departamentoRepository.existsById(id);
		} catch (SQLException e) {
			throw new AccesoDatosException(e);
		}
	}

	@Override
	public long count() {
		try {
			return departamentoRepository.count();
		} catch (SQLException e) {
			throw new AccesoDatosException(e);
		}
	}

	/**
	 * Busca y retorna un departamento según su ID.
	 *
	 * @param id identificador del departamento
	 * @return {@link DepartamentoDTO} encontrado, o {@code null} si no existe
	 * @throws AccesoDatosException si falla la consulta a la base de datos
	 */
	public DepartamentoDTO getById(Long id) {
		try {
			Optional<Departamento> found = departamentoRepository.findById(id);

			if (found.isPresent()) {
				return mapper.map(found.get(), DepartamentoDTO.class);
			}

			return null;
		} catch (SQLException e) {
			throw new AccesoDatosException(e);
		}
	}

	/**
	 * Convierte una lista de entidades en una lista de DTOs.
	 *
	 * @param entityList lista de entidades
	 * @return lista de {@link DepartamentoDTO}
	 */
	private List<DepartamentoDTO> toDtoList(List<Departamento> entityList) {
		List<DepartamentoDTO> dtoList = new ArrayList<>();

		entityList.forEach(entity -> {
			dtoList.add(mapper.map(entity, DepartamentoDTO.class));
		});

		return dtoList;
	}
}
