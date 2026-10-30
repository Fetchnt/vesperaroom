package co.edu.unbosque.vesperaroom.service;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import co.edu.unbosque.vesperaroom.dto.RespuestaPropietarioDTO;
import co.edu.unbosque.vesperaroom.entity.RespuestaPropietario;
import co.edu.unbosque.vesperaroom.exception.AccesoDatosException;
import co.edu.unbosque.vesperaroom.exception.LanzadorExcepciones;
import co.edu.unbosque.vesperaroom.exception.ReferenciaNoEncontradaException;
import co.edu.unbosque.vesperaroom.exception.RegistroDuplicadoException;
import co.edu.unbosque.vesperaroom.exception.RegistroNoEncontradoException;
import co.edu.unbosque.vesperaroom.exception.TextoInvalidoException;
import co.edu.unbosque.vesperaroom.repository.ResenaRepository;
import co.edu.unbosque.vesperaroom.repository.RespuestaPropietarioRepository;
import co.edu.unbosque.vesperaroom.util.CodigoMySQL;
import co.edu.unbosque.vesperaroom.util.Normalizador;

/**
 * Servicio que gestiona la respuesta del propietario a una reseña. Es una
 * entidad débil 1:1: su identificador es el mismo {@code idResena}, por lo que
 * cada reseña admite una sola respuesta.
 *
 * <p>
 * Códigos de {@link #create(RespuestaPropietarioDTO)} y
 * {@link #updateById(Long, RespuestaPropietarioDTO)}:
 * </p>
 * <ul>
 * <li>0: operación exitosa</li>
 * <li>1: comentario vacío o con más de 1000 caracteres</li>
 * <li>2: reseña no indicada o inexistente (solo al crear)</li>
 * <li>3: la reseña ya tiene respuesta (solo al crear)</li>
 * <li>4: respuesta no encontrada (solo al actualizar)</li>
 * <li>5: error inesperado</li>
 * </ul>
 * <p>
 * Códigos de {@link #deleteById(Long)}: 0 éxito, 1 no encontrada, 3 error
 * inesperado.
 * </p>
 */
@Service
public class RespuestaPropietarioService implements CRUDOperation<RespuestaPropietarioDTO> {

	private static final String ENTIDAD = "respuesta del propietario";

	private final RespuestaPropietarioRepository respuestaPropietarioRepository;
	private final ResenaRepository resenaRepository;
	private final ModelMapper mapper;

	public RespuestaPropietarioService(RespuestaPropietarioRepository respuestaPropietarioRepository,
			ResenaRepository resenaRepository, ModelMapper mapper) {
		this.respuestaPropietarioRepository = respuestaPropietarioRepository;
		this.resenaRepository = resenaRepository;
		this.mapper = mapper;
	}

	/**
	 * Crea la respuesta a una reseña.
	 *
	 * @param data objeto con el id de la reseña y el comentario
	 * @return código de resultado según la tabla de la clase (0 a 3 o 5)
	 */
	@Override
	public int create(RespuestaPropietarioDTO data) {
		try {
			data.setComentario(Normalizador.texto(data.getComentario()));
			LanzadorExcepciones.verificarTexto(data.getComentario(), 1000, "comentario");
			LanzadorExcepciones.verificarReferencia(
					data.getIdResena() != null && resenaRepository.existsById(data.getIdResena()), "reseña",
					data.getIdResena());
			LanzadorExcepciones.verificarDuplicado(respuestaPropietarioRepository.existsById(data.getIdResena()),
					"esa reseña");

			RespuestaPropietario entity = mapper.map(data, RespuestaPropietario.class);
			entity.setFecha(LocalDateTime.now());
			respuestaPropietarioRepository.insertar(entity);
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
	 * Actualiza el comentario de una respuesta. La fecha original se conserva.
	 *
	 * @param id   identificador de la reseña respondida
	 * @param data objeto con el nuevo comentario
	 * @return código de resultado según la tabla de la clase (0, 1, 4 o 5)
	 */
	@Override
	public int updateById(Long id, RespuestaPropietarioDTO data) {
		try {
			Optional<RespuestaPropietario> found = respuestaPropietarioRepository.findById(id);
			LanzadorExcepciones.verificarExistencia(found.isPresent(), ENTIDAD, id);

			data.setComentario(Normalizador.texto(data.getComentario()));
			LanzadorExcepciones.verificarTexto(data.getComentario(), 1000, "comentario");

			RespuestaPropietario temp = found.get();
			temp.setComentario(data.getComentario());
			respuestaPropietarioRepository.actualizar(temp);
			return 0;

		} catch (RegistroNoEncontradoException e) {
			return 4;
		} catch (TextoInvalidoException e) {
			return 1;
		} catch (Exception e) {
			e.printStackTrace();
			return 5;
		}
	}

	/**
	 * Elimina la respuesta a una reseña.
	 *
	 * @param id identificador de la reseña respondida
	 * @return 0 si se eliminó, 1 si no existe, 3 si ocurre un error inesperado
	 */
	@Override
	public int deleteById(Long id) {
		try {
			LanzadorExcepciones.verificarExistencia(respuestaPropietarioRepository.existsById(id), ENTIDAD, id);
			respuestaPropietarioRepository.eliminar(id);
			return 0;
		} catch (RegistroNoEncontradoException e) {
			return 1;
		} catch (Exception e) {
			e.printStackTrace();
			return 3;
		}
	}

	/**
	 * Retorna todas las respuestas, de la más reciente a la más antigua.
	 *
	 * @return lista de {@link RespuestaPropietarioDTO}
	 * @throws AccesoDatosException si falla la consulta a la base de datos
	 */
	@Override
	public List<RespuestaPropietarioDTO> getAll() {
		try {
			List<RespuestaPropietarioDTO> dtoList = new ArrayList<>();
			respuestaPropietarioRepository.findAll().forEach(entity -> {
				dtoList.add(mapper.map(entity, RespuestaPropietarioDTO.class));
			});
			return dtoList;
		} catch (SQLException e) {
			throw new AccesoDatosException(e);
		}
	}

	@Override
	public boolean exist(Long id) {
		try {
			return respuestaPropietarioRepository.existsById(id);
		} catch (SQLException e) {
			throw new AccesoDatosException(e);
		}
	}

	@Override
	public long count() {
		try {
			return respuestaPropietarioRepository.count();
		} catch (SQLException e) {
			throw new AccesoDatosException(e);
		}
	}

	/**
	 * Busca la respuesta de una reseña.
	 *
	 * @param idResena identificador de la reseña
	 * @return {@link RespuestaPropietarioDTO} encontrada, o {@code null} si la
	 *         reseña no tiene respuesta
	 * @throws AccesoDatosException si falla la consulta a la base de datos
	 */
	public RespuestaPropietarioDTO getById(Long idResena) {
		try {
			Optional<RespuestaPropietario> found = respuestaPropietarioRepository.findById(idResena);

			if (found.isPresent()) {
				return mapper.map(found.get(), RespuestaPropietarioDTO.class);
			}

			return null;
		} catch (SQLException e) {
			throw new AccesoDatosException(e);
		}
	}
}
