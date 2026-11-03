package co.edu.unbosque.vesperaroom.service;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import co.edu.unbosque.vesperaroom.dto.FotoDTO;
import co.edu.unbosque.vesperaroom.entity.Foto;
import co.edu.unbosque.vesperaroom.exception.AccesoDatosException;
import co.edu.unbosque.vesperaroom.exception.FotoDuenoException;
import co.edu.unbosque.vesperaroom.exception.ImagenException;
import co.edu.unbosque.vesperaroom.exception.LanzadorExcepciones;
import co.edu.unbosque.vesperaroom.exception.ReferenciaNoEncontradaException;
import co.edu.unbosque.vesperaroom.exception.RegistroDuplicadoException;
import co.edu.unbosque.vesperaroom.exception.RegistroNoEncontradoException;
import co.edu.unbosque.vesperaroom.exception.UriException;
import co.edu.unbosque.vesperaroom.exception.ValorFueraDeRangoException;
import co.edu.unbosque.vesperaroom.repository.FotoRepository;
import co.edu.unbosque.vesperaroom.repository.HabitacionRepository;
import co.edu.unbosque.vesperaroom.repository.MotelRepository;
import co.edu.unbosque.vesperaroom.util.CodigoMySQL;
import co.edu.unbosque.vesperaroom.util.Normalizador;

/**
 * Servicio que gestiona las fotos del catálogo. Cada foto pertenece a un motel
 * o a una habitación, nunca a ambos; la misma regla está en el {@code CHECK}
 * {@code ck_foto_dueno} de la tabla.
 *
 * <p>
 * Códigos de {@link #create(FotoDTO)} y {@link #updateById(Long, FotoDTO)}:
 * </p>
 * <ul>
 * <li>0: operación exitosa</li>
 * <li>1: no indica exactamente un dueño (motel o habitación)</li>
 * <li>2: URI inválida</li>
 * <li>3: orden fuera del rango de 1 a 50</li>
 * <li>4: motel inexistente</li>
 * <li>5: habitación inexistente</li>
 * <li>6: el dueño ya tiene otra foto en esa posición</li>
 * <li>7: foto no encontrada (solo al actualizar)</li>
 * <li>8: error inesperado</li>
 * <li>9: el archivo no es una imagen válida (solo al subir)</li>
 * </ul>
 * <p>
 * Códigos de {@link #deleteById(Long)}: 0 éxito, 1 no encontrada, 3 error
 * inesperado.
 * </p>
 */
@Service
public class FotoService implements CRUDOperation<FotoDTO> {

	private static final String ENTIDAD = "foto";
	private static final String ENTIDAD_MOTEL = "motel";
	private static final String DETALLE_ORDEN = "esa posición";

	private final FotoRepository fotoRepository;
	private final MotelRepository motelRepository;
	private final HabitacionRepository habitacionRepository;
	private final ModelMapper mapper;
	private final AlmacenImagenService almacen;

	public FotoService(FotoRepository fotoRepository, MotelRepository motelRepository,
			HabitacionRepository habitacionRepository, ModelMapper mapper, AlmacenImagenService almacen) {
		this.fotoRepository = fotoRepository;
		this.motelRepository = motelRepository;
		this.habitacionRepository = habitacionRepository;
		this.mapper = mapper;
		this.almacen = almacen;
	}

	/**
	 * Crea una foto para un motel o una habitación.
	 *
	 * @param data objeto con el dueño, la URI y el orden
	 * @return código de resultado según la tabla de la clase (0 a 6 u 8)
	 */
	@Override
	public int create(FotoDTO data) {
		try {
			data.setUri(Normalizador.texto(data.getUri()));
			LanzadorExcepciones.verificarDuenoFoto(data.getIdMotel(), data.getIdHabitacion());
			LanzadorExcepciones.verificarUri(data.getUri());
			LanzadorExcepciones.verificarRango(data.getOrden(), 1, 50, "orden");
			verificarDueno(data);
			verificarOrdenLibre(data.getIdMotel(), data.getIdHabitacion(), data.getOrden(), 0L);

			Foto entity = mapper.map(data, Foto.class);
			entity.setIdFoto(null);
			fotoRepository.insertar(entity);
			return 0;

		} catch (FotoDuenoException e) {
			return 1;
		} catch (UriException e) {
			return 2;
		} catch (ValorFueraDeRangoException e) {
			return 3;
		} catch (ReferenciaNoEncontradaException e) {
			return ENTIDAD_MOTEL.equals(e.getEntidad()) ? 4 : 5;
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
	 * Actualiza la URI y el orden de una foto. El dueño no cambia.
	 *
	 * @param id   identificador de la foto
	 * @param data objeto con la nueva URI y el nuevo orden
	 * @return código de resultado según la tabla de la clase (0, 2, 3 o 6 a 8)
	 */
	@Override
	public int updateById(Long id, FotoDTO data) {
		try {
			Optional<Foto> found = fotoRepository.findById(id);
			LanzadorExcepciones.verificarExistencia(found.isPresent(), ENTIDAD, id);

			data.setUri(Normalizador.texto(data.getUri()));
			LanzadorExcepciones.verificarUri(data.getUri());
			LanzadorExcepciones.verificarRango(data.getOrden(), 1, 50, "orden");
			Foto temp = found.get();
			verificarOrdenLibre(temp.getIdMotel(), temp.getIdHabitacion(), data.getOrden(), id);

			temp.setUri(data.getUri());
			temp.setOrden(data.getOrden());
			fotoRepository.actualizar(temp);
			return 0;

		} catch (RegistroNoEncontradoException e) {
			return 7;
		} catch (UriException e) {
			return 2;
		} catch (ValorFueraDeRangoException e) {
			return 3;
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
	 * Sube una imagen desde el equipo del usuario: la guarda en la carpeta de
	 * fotos y registra su dirección pública en la tabla {@code foto}. Si no se
	 * indica la posición, la foto queda de última.
	 *
	 * @param archivo      imagen JPG, PNG o WEBP
	 * @param idMotel      motel dueño (o nulo)
	 * @param idHabitacion habitación dueña (o nula)
	 * @param orden        posición de la foto (o nula)
	 * @param urlBase      dirección del servidor, para armar la URI pública
	 * @return código de resultado según la tabla de la clase (0 a 6, 8 o 9)
	 */
	public int subir(MultipartFile archivo, Long idMotel, Long idHabitacion, Integer orden, String urlBase) {
		String uri = null;
		try {
			LanzadorExcepciones.verificarImagen(archivo == null ? null : archivo.getContentType(),
					archivo == null ? 0 : archivo.getSize());
			LanzadorExcepciones.verificarDuenoFoto(idMotel, idHabitacion);

			FotoDTO data = new FotoDTO();
			data.setIdMotel(idMotel);
			data.setIdHabitacion(idHabitacion);
			verificarDueno(data);
			data.setOrden(orden != null ? orden : fotoRepository.siguienteOrden(idMotel, idHabitacion));
			LanzadorExcepciones.verificarRango(data.getOrden(), 1, 50, "orden");
			verificarOrdenLibre(idMotel, idHabitacion, data.getOrden(), 0L);

			uri = urlBase + AlmacenImagenService.RUTA_PUBLICA + almacen.guardar(archivo);
			data.setUri(uri);
			LanzadorExcepciones.verificarUri(uri);

			Foto entity = mapper.map(data, Foto.class);
			entity.setIdFoto(null);
			fotoRepository.insertar(entity);
			return 0;

		} catch (ImagenException e) {
			return 9;
		} catch (FotoDuenoException e) {
			return 1;
		} catch (UriException e) {
			almacen.eliminar(uri);
			return 2;
		} catch (ValorFueraDeRangoException e) {
			return 3;
		} catch (ReferenciaNoEncontradaException e) {
			return ENTIDAD_MOTEL.equals(e.getEntidad()) ? 4 : 5;
		} catch (RegistroDuplicadoException e) {
			return 6;
		} catch (SQLIntegrityConstraintViolationException e) {
			almacen.eliminar(uri);
			return CodigoMySQL.esLlaveDuplicada(e) ? 6 : 8;
		} catch (Exception e) {
			e.printStackTrace();
			almacen.eliminar(uri);
			return 8;
		}
	}

	/**
	 * Elimina una foto según su ID. Si la imagen se había subido al servidor,
	 * también se borra su archivo.
	 *
	 * @param id identificador de la foto
	 * @return 0 si se eliminó, 1 si no existe, 3 si ocurre un error inesperado
	 */
	@Override
	public int deleteById(Long id) {
		try {
			Optional<Foto> found = fotoRepository.findById(id);
			LanzadorExcepciones.verificarExistencia(found.isPresent(), ENTIDAD, id);
			fotoRepository.eliminar(id);
			almacen.eliminar(found.get().getUri());
			return 0;
		} catch (RegistroNoEncontradoException e) {
			return 1;
		} catch (Exception e) {
			e.printStackTrace();
			return 3;
		}
	}

	/**
	 * Retorna todas las fotos.
	 *
	 * @return lista de {@link FotoDTO}
	 * @throws AccesoDatosException si falla la consulta a la base de datos
	 */
	@Override
	public List<FotoDTO> getAll() {
		try {
			return toDtoList(fotoRepository.findAll());
		} catch (SQLException e) {
			throw new AccesoDatosException(e);
		}
	}

	/**
	 * Retorna las fotos generales de un motel.
	 *
	 * @param idMotel identificador del motel
	 * @return lista de {@link FotoDTO} ordenada por posición
	 * @throws AccesoDatosException si falla la consulta a la base de datos
	 */
	public List<FotoDTO> getByMotel(Long idMotel) {
		try {
			return toDtoList(fotoRepository.findByMotel(idMotel));
		} catch (SQLException e) {
			throw new AccesoDatosException(e);
		}
	}

	/**
	 * Retorna las fotos de una habitación.
	 *
	 * @param idHabitacion identificador de la habitación
	 * @return lista de {@link FotoDTO} ordenada por posición
	 * @throws AccesoDatosException si falla la consulta a la base de datos
	 */
	public List<FotoDTO> getByHabitacion(Long idHabitacion) {
		try {
			return toDtoList(fotoRepository.findByHabitacion(idHabitacion));
		} catch (SQLException e) {
			throw new AccesoDatosException(e);
		}
	}

	@Override
	public boolean exist(Long id) {
		try {
			return fotoRepository.existsById(id);
		} catch (SQLException e) {
			throw new AccesoDatosException(e);
		}
	}

	@Override
	public long count() {
		try {
			return fotoRepository.count();
		} catch (SQLException e) {
			throw new AccesoDatosException(e);
		}
	}

	/**
	 * Busca y retorna una foto según su ID.
	 *
	 * @param id identificador de la foto
	 * @return {@link FotoDTO} encontrada, o {@code null} si no existe
	 * @throws AccesoDatosException si falla la consulta a la base de datos
	 */
	public FotoDTO getById(Long id) {
		try {
			Optional<Foto> found = fotoRepository.findById(id);

			if (found.isPresent()) {
				return mapper.map(found.get(), FotoDTO.class);
			}

			return null;
		} catch (SQLException e) {
			throw new AccesoDatosException(e);
		}
	}

	/**
	 * Verifica que exista el dueño indicado (motel o habitación).
	 *
	 * @param data datos de la foto
	 * @throws ReferenciaNoEncontradaException si el dueño no existe
	 * @throws SQLException                    si falla la consulta
	 */
	private void verificarDueno(FotoDTO data) throws ReferenciaNoEncontradaException, SQLException {
		if (data.getIdMotel() != null) {
			LanzadorExcepciones.verificarReferencia(motelRepository.existsById(data.getIdMotel()), ENTIDAD_MOTEL,
					data.getIdMotel());
		} else {
			LanzadorExcepciones.verificarReferencia(habitacionRepository.existsById(data.getIdHabitacion()),
					"habitación", data.getIdHabitacion());
		}
	}

	/**
	 * Verifica que el dueño no tenga otra foto en la misma posición.
	 *
	 * @param idMotel      motel dueño (o nulo)
	 * @param idHabitacion habitación dueña (o nula)
	 * @param orden        posición a verificar
	 * @param idExcluido   foto que se excluye de la búsqueda (0 para una nueva)
	 * @throws RegistroDuplicadoException si la posición está ocupada
	 * @throws SQLException               si falla la consulta
	 */
	private void verificarOrdenLibre(Long idMotel, Long idHabitacion, Integer orden, Long idExcluido)
			throws RegistroDuplicadoException, SQLException {
		boolean ocupado = idMotel != null ? fotoRepository.existsByMotelAndOrdenAndIdNot(idMotel, orden, idExcluido)
				: fotoRepository.existsByHabitacionAndOrdenAndIdNot(idHabitacion, orden, idExcluido);
		LanzadorExcepciones.verificarDuplicado(ocupado, DETALLE_ORDEN);
	}

	/**
	 * Convierte una lista de entidades en una lista de DTOs.
	 *
	 * @param entityList lista de entidades
	 * @return lista de {@link FotoDTO}
	 */
	private List<FotoDTO> toDtoList(List<Foto> entityList) {
		List<FotoDTO> dtoList = new ArrayList<>();

		entityList.forEach(entity -> {
			dtoList.add(mapper.map(entity, FotoDTO.class));
		});

		return dtoList;
	}
}
