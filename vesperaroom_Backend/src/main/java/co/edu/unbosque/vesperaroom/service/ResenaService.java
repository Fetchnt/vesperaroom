package co.edu.unbosque.vesperaroom.service;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import co.edu.unbosque.vesperaroom.dto.ResenaDTO;
import co.edu.unbosque.vesperaroom.entity.Resena;
import co.edu.unbosque.vesperaroom.entity.Reserva;
import co.edu.unbosque.vesperaroom.exception.AccesoDatosException;
import co.edu.unbosque.vesperaroom.exception.EstadoReservaException;
import co.edu.unbosque.vesperaroom.exception.LanzadorExcepciones;
import co.edu.unbosque.vesperaroom.exception.PropiedadReservaException;
import co.edu.unbosque.vesperaroom.exception.ReferenciaNoEncontradaException;
import co.edu.unbosque.vesperaroom.exception.RegistroDuplicadoException;
import co.edu.unbosque.vesperaroom.exception.RegistroNoEncontradoException;
import co.edu.unbosque.vesperaroom.exception.TextoInvalidoException;
import co.edu.unbosque.vesperaroom.exception.ValorFueraDeRangoException;
import co.edu.unbosque.vesperaroom.repository.ResenaRepository;
import co.edu.unbosque.vesperaroom.repository.ReservaRepository;
import co.edu.unbosque.vesperaroom.util.CodigoMySQL;
import co.edu.unbosque.vesperaroom.util.Normalizador;

/**
 * Servicio que gestiona las reseñas. Solo se puede reseñar una reserva
 * {@code FINALIZADA}, una sola vez, y (desde el endpoint del cliente) solo si la
 * reserva es del usuario que escribe.
 *
 * <p>
 * Códigos de {@link #crear(ResenaDTO, Long)}, {@link #create(ResenaDTO)} y
 * {@link #updateById(Long, ResenaDTO)}:
 * </p>
 * <ul>
 * <li>0: operación exitosa</li>
 * <li>1: calificación fuera del rango de 1 a 5</li>
 * <li>2: comentario con más de 1000 caracteres</li>
 * <li>3: reserva no indicada o inexistente</li>
 * <li>4: la reserva no pertenece al usuario</li>
 * <li>5: la reserva no está finalizada</li>
 * <li>6: la reserva ya tiene reseña</li>
 * <li>7: reseña no encontrada (solo al actualizar)</li>
 * <li>8: error inesperado</li>
 * </ul>
 * <p>
 * Códigos de {@link #deleteById(Long)}: 0 éxito, 1 no encontrada, 3 error
 * inesperado (la respuesta del propietario se borra en cascada).
 * </p>
 */
@Service
public class ResenaService implements CRUDOperation<ResenaDTO> {

	private static final String ENTIDAD = "reseña";

	private final ResenaRepository resenaRepository;
	private final ReservaRepository reservaRepository;
	private final ModelMapper mapper;

	public ResenaService(ResenaRepository resenaRepository, ReservaRepository reservaRepository,
			ModelMapper mapper) {
		this.resenaRepository = resenaRepository;
		this.reservaRepository = reservaRepository;
		this.mapper = mapper;
	}

	/**
	 * Crea una reseña sin verificar quién es el dueño de la reserva. Se usa desde
	 * la administración.
	 *
	 * @param data objeto con la reserva, la calificación y el comentario
	 * @return código de resultado según la tabla de la clase (0 a 3, 5, 6 u 8)
	 */
	@Override
	public int create(ResenaDTO data) {
		return guardarNueva(data, null);
	}

	/**
	 * Crea la reseña de un cliente sobre una de sus reservas finalizadas.
	 *
	 * @param data      objeto con la reserva, la calificación y el comentario
	 * @param idUsuario identificador del cliente que escribe
	 * @return código de resultado según la tabla de la clase (0 a 6 u 8)
	 */
	public int crear(ResenaDTO data, Long idUsuario) {
		return guardarNueva(data, idUsuario);
	}

	/**
	 * Actualiza la calificación y el comentario de una reseña. La reserva y la
	 * fecha no cambian.
	 *
	 * @param id   identificador de la reseña
	 * @param data objeto con la nueva calificación y el nuevo comentario
	 * @return código de resultado según la tabla de la clase (0 a 2, 7 u 8)
	 */
	@Override
	public int updateById(Long id, ResenaDTO data) {
		try {
			Optional<Resena> found = resenaRepository.findById(id);
			LanzadorExcepciones.verificarExistencia(found.isPresent(), ENTIDAD, id);

			data.setComentario(Normalizador.textoOpcional(data.getComentario()));
			validar(data);

			Resena temp = found.get();
			temp.setCalificacion(data.getCalificacion());
			temp.setComentario(data.getComentario());
			resenaRepository.actualizar(temp);
			return 0;

		} catch (RegistroNoEncontradoException e) {
			return 7;
		} catch (ValorFueraDeRangoException e) {
			return 1;
		} catch (TextoInvalidoException e) {
			return 2;
		} catch (Exception e) {
			e.printStackTrace();
			return 8;
		}
	}

	/**
	 * Elimina una reseña según su ID.
	 *
	 * @param id identificador de la reseña
	 * @return 0 si se eliminó, 1 si no existe, 3 si ocurre un error inesperado
	 */
	@Override
	public int deleteById(Long id) {
		try {
			LanzadorExcepciones.verificarExistencia(resenaRepository.existsById(id), ENTIDAD, id);
			resenaRepository.eliminar(id);
			return 0;
		} catch (RegistroNoEncontradoException e) {
			return 1;
		} catch (Exception e) {
			e.printStackTrace();
			return 3;
		}
	}

	/**
	 * Retorna todas las reseñas, de la más reciente a la más antigua.
	 *
	 * @return lista de {@link ResenaDTO}
	 * @throws AccesoDatosException si falla la consulta a la base de datos
	 */
	@Override
	public List<ResenaDTO> getAll() {
		try {
			return toDtoList(resenaRepository.findAll());
		} catch (SQLException e) {
			throw new AccesoDatosException(e);
		}
	}

	/**
	 * Retorna las reseñas de un motel.
	 *
	 * @param idMotel identificador del motel
	 * @return lista de {@link ResenaDTO}
	 * @throws AccesoDatosException si falla la consulta a la base de datos
	 */
	public List<ResenaDTO> getByMotel(Long idMotel) {
		try {
			return toDtoList(resenaRepository.findByMotel(idMotel));
		} catch (SQLException e) {
			throw new AccesoDatosException(e);
		}
	}

	/**
	 * Retorna las reseñas escritas por un cliente.
	 *
	 * @param idUsuario identificador del cliente
	 * @return lista de {@link ResenaDTO}
	 * @throws AccesoDatosException si falla la consulta a la base de datos
	 */
	public List<ResenaDTO> getByUsuario(Long idUsuario) {
		try {
			return toDtoList(resenaRepository.findByUsuario(idUsuario));
		} catch (SQLException e) {
			throw new AccesoDatosException(e);
		}
	}

	/**
	 * Retorna la calificación promedio de un motel.
	 *
	 * @param idMotel identificador del motel
	 * @return promedio de calificaciones, o {@code null} si no tiene reseñas
	 * @throws AccesoDatosException si falla la consulta a la base de datos
	 */
	public Double getPromedioByMotel(Long idMotel) {
		try {
			return resenaRepository.promedioByMotel(idMotel);
		} catch (SQLException e) {
			throw new AccesoDatosException(e);
		}
	}

	@Override
	public boolean exist(Long id) {
		try {
			return resenaRepository.existsById(id);
		} catch (SQLException e) {
			throw new AccesoDatosException(e);
		}
	}

	@Override
	public long count() {
		try {
			return resenaRepository.count();
		} catch (SQLException e) {
			throw new AccesoDatosException(e);
		}
	}

	/**
	 * Busca y retorna una reseña según su ID.
	 *
	 * @param id identificador de la reseña
	 * @return {@link ResenaDTO} encontrada, o {@code null} si no existe
	 * @throws AccesoDatosException si falla la consulta a la base de datos
	 */
	public ResenaDTO getById(Long id) {
		try {
			Optional<Resena> found = resenaRepository.findById(id);

			if (found.isPresent()) {
				return mapper.map(found.get(), ResenaDTO.class);
			}

			return null;
		} catch (SQLException e) {
			throw new AccesoDatosException(e);
		}
	}

	/**
	 * Valida e inserta una nueva reseña.
	 *
	 * @param data      datos de la reseña
	 * @param idUsuario cliente que debe ser dueño de la reserva, o {@code null}
	 *                  para no verificarlo
	 * @return código de resultado según la tabla de la clase
	 */
	private int guardarNueva(ResenaDTO data, Long idUsuario) {
		try {
			data.setComentario(Normalizador.textoOpcional(data.getComentario()));
			validar(data);

			Optional<Reserva> reserva = data.getIdReserva() == null ? Optional.empty()
					: reservaRepository.findById(data.getIdReserva());
			LanzadorExcepciones.verificarReferencia(reserva.isPresent(), "reserva", data.getIdReserva());
			if (idUsuario != null) {
				LanzadorExcepciones.verificarPropiedadReserva(reserva.get().getIdUsuario().equals(idUsuario));
			}
			LanzadorExcepciones.verificarEstadoReserva(reserva.get().getEstado() == Reserva.Estado.FINALIZADA,
					"reseñarla");
			LanzadorExcepciones.verificarDuplicado(resenaRepository.existsByReserva(data.getIdReserva()),
					"esa reserva");

			Resena entity = mapper.map(data, Resena.class);
			entity.setIdResena(null);
			entity.setFecha(LocalDateTime.now());
			resenaRepository.insertar(entity);
			return 0;

		} catch (ValorFueraDeRangoException e) {
			return 1;
		} catch (TextoInvalidoException e) {
			return 2;
		} catch (ReferenciaNoEncontradaException e) {
			return 3;
		} catch (PropiedadReservaException e) {
			return 4;
		} catch (EstadoReservaException e) {
			return 5;
		} catch (RegistroDuplicadoException e) {
			return 6;
		} catch (SQLIntegrityConstraintViolationException e) {
			return CodigoMySQL.esLlaveDuplicada(e) ? 6 : 8;
		} catch (Exception e) {
			e.printStackTrace();
			return 8;
		}
	}

	/**
	 * Valida la calificación y el comentario.
	 *
	 * @param data datos a validar
	 * @throws ValorFueraDeRangoException si la calificación está fuera de rango
	 * @throws TextoInvalidoException     si el comentario es demasiado largo
	 */
	private void validar(ResenaDTO data) throws ValorFueraDeRangoException, TextoInvalidoException {
		LanzadorExcepciones.verificarRango(data.getCalificacion(), 1, 5, "calificación");
		LanzadorExcepciones.verificarTextoOpcional(data.getComentario(), 1000, "comentario");
	}

	/**
	 * Convierte una lista de entidades en una lista de DTOs.
	 *
	 * @param entityList lista de entidades
	 * @return lista de {@link ResenaDTO}
	 */
	private List<ResenaDTO> toDtoList(List<Resena> entityList) {
		List<ResenaDTO> dtoList = new ArrayList<>();

		entityList.forEach(entity -> {
			dtoList.add(mapper.map(entity, ResenaDTO.class));
		});

		return dtoList;
	}
}
