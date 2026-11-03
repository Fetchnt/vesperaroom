package co.edu.unbosque.vesperaroom.service;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import co.edu.unbosque.vesperaroom.dto.CaracteristicaDTO;
import co.edu.unbosque.vesperaroom.dto.MotelDTO;
import co.edu.unbosque.vesperaroom.dto.MotelResumenDTO;
import co.edu.unbosque.vesperaroom.dto.TarifaDTO;
import co.edu.unbosque.vesperaroom.dto.TipoResumenDTO;
import co.edu.unbosque.vesperaroom.entity.Caracteristica;
import co.edu.unbosque.vesperaroom.entity.Motel;
import co.edu.unbosque.vesperaroom.entity.Motel.Estado;
import co.edu.unbosque.vesperaroom.entity.Tarifa;
import co.edu.unbosque.vesperaroom.entity.TipoHabitacion;
import co.edu.unbosque.vesperaroom.exception.AccesoDatosException;
import co.edu.unbosque.vesperaroom.exception.CampoObligatorioException;
import co.edu.unbosque.vesperaroom.exception.CoordenadaException;
import co.edu.unbosque.vesperaroom.exception.LanzadorExcepciones;
import co.edu.unbosque.vesperaroom.exception.ReferenciaNoEncontradaException;
import co.edu.unbosque.vesperaroom.exception.RegistroNoEncontradoException;
import co.edu.unbosque.vesperaroom.exception.TextoInvalidoException;
import co.edu.unbosque.vesperaroom.repository.CiudadRepository;
import co.edu.unbosque.vesperaroom.repository.MotelRepository;
import co.edu.unbosque.vesperaroom.repository.PerfilPropietarioRepository;
import co.edu.unbosque.vesperaroom.repository.TarifaRepository;
import co.edu.unbosque.vesperaroom.repository.TipoHabitacionRepository;
import co.edu.unbosque.vesperaroom.util.CodigoMySQL;
import co.edu.unbosque.vesperaroom.util.Normalizador;

/**
 * Servicio que gestiona los moteles. Un motel pertenece a un propietario con
 * perfil registrado y se ubica en una ciudad del catálogo.
 *
 * <p>
 * Códigos de {@link #create(MotelDTO)} y {@link #updateById(Long, MotelDTO)}:
 * </p>
 * <ul>
 * <li>0: operación exitosa</li>
 * <li>1: nombre vacío o con más de 100 caracteres</li>
 * <li>2: descripción con más de 500 caracteres</li>
 * <li>3: calle o carrera vacías o con más de 40 caracteres</li>
 * <li>4: coordenadas fuera de rango o incompletas</li>
 * <li>5: el propietario no tiene perfil registrado</li>
 * <li>6: ciudad no indicada o inexistente</li>
 * <li>7: motel no encontrado (solo al actualizar)</li>
 * <li>8: error inesperado</li>
 * </ul>
 * <p>
 * Códigos de {@link #updateEstado(Long, Estado)}: 0 éxito, 1 estado nulo, 2 no
 * encontrado, 3 error inesperado. Códigos de {@link #deleteById(Long)}: 0
 * éxito, 1 no encontrado, 2 tiene registros asociados, 3 error inesperado.
 * </p>
 */
@Service
public class MotelService implements CRUDOperation<MotelDTO> {

	private static final String ENTIDAD = "motel";
	private static final String ENTIDAD_CIUDAD = "ciudad";

	private final MotelRepository motelRepository;
	private final PerfilPropietarioRepository perfilPropietarioRepository;
	private final CiudadRepository ciudadRepository;
	private final TipoHabitacionRepository tipoHabitacionRepository;
	private final TarifaRepository tarifaRepository;
	private final ModelMapper mapper;

	public MotelService(MotelRepository motelRepository, PerfilPropietarioRepository perfilPropietarioRepository,
			CiudadRepository ciudadRepository, TipoHabitacionRepository tipoHabitacionRepository,
			TarifaRepository tarifaRepository, ModelMapper mapper) {
		this.motelRepository = motelRepository;
		this.perfilPropietarioRepository = perfilPropietarioRepository;
		this.ciudadRepository = ciudadRepository;
		this.tipoHabitacionRepository = tipoHabitacionRepository;
		this.tarifaRepository = tarifaRepository;
		this.mapper = mapper;
	}

	/**
	 * Crea un nuevo motel. Si no se indica el estado, queda {@code ACTIVO}.
	 *
	 * @param data objeto con los datos del motel, incluido su propietario
	 * @return código de resultado según la tabla de la clase (0 a 6 u 8)
	 */
	@Override
	public int create(MotelDTO data) {
		try {
			normalizar(data);
			validar(data);

			Motel entity = mapper.map(data, Motel.class);
			entity.setIdMotel(null);
			if (data.getEstado() == null) {
				entity.setEstado(Estado.ACTIVO);
			}
			// El controlador usa el id para responder la cabecera Location
			data.setIdMotel(motelRepository.insertar(entity));
			return 0;

		} catch (TextoInvalidoException e) {
			return codigoTexto(e);
		} catch (CoordenadaException e) {
			return 4;
		} catch (ReferenciaNoEncontradaException e) {
			return ENTIDAD_CIUDAD.equals(e.getEntidad()) ? 6 : 5;
		} catch (SQLIntegrityConstraintViolationException e) {
			return CodigoMySQL.esReferenciaInexistente(e) ? 6 : 8;
		} catch (Exception e) {
			e.printStackTrace();
			return 8;
		}
	}

	/**
	 * Actualiza los datos de un motel. Si el estado llega nulo se conserva el
	 * actual.
	 *
	 * @param id   identificador del motel
	 * @param data objeto con los nuevos datos
	 * @return código de resultado según la tabla de la clase
	 */
	@Override
	public int updateById(Long id, MotelDTO data) {
		try {
			Optional<Motel> found = motelRepository.findById(id);
			LanzadorExcepciones.verificarExistencia(found.isPresent(), ENTIDAD, id);

			normalizar(data);
			validar(data);

			Motel temp = found.get();
			temp.setIdPropietario(data.getIdPropietario());
			temp.setIdCiudad(data.getIdCiudad());
			temp.setNombre(data.getNombre());
			temp.setDescripcion(data.getDescripcion());
			temp.setCalle(data.getCalle());
			temp.setCarrera(data.getCarrera());
			temp.setLatitud(data.getLatitud());
			temp.setLongitud(data.getLongitud());
			if (data.getEstado() != null) {
				temp.setEstado(data.getEstado());
			}
			motelRepository.actualizar(temp);
			return 0;

		} catch (RegistroNoEncontradoException e) {
			return 7;
		} catch (TextoInvalidoException e) {
			return codigoTexto(e);
		} catch (CoordenadaException e) {
			return 4;
		} catch (ReferenciaNoEncontradaException e) {
			return ENTIDAD_CIUDAD.equals(e.getEntidad()) ? 6 : 5;
		} catch (SQLIntegrityConstraintViolationException e) {
			return CodigoMySQL.esReferenciaInexistente(e) ? 6 : 8;
		} catch (Exception e) {
			e.printStackTrace();
			return 8;
		}
	}

	/**
	 * Cambia el estado de un motel (ACTIVO o INACTIVO).
	 *
	 * @param id     identificador del motel
	 * @param estado nuevo estado
	 * @return 0 si es exitoso, 1 si el estado es nulo, 2 si el motel no existe, 3
	 *         si ocurre un error inesperado
	 */
	public int updateEstado(Long id, Estado estado) {
		try {
			LanzadorExcepciones.verificarObligatorio(estado, "el estado");
			LanzadorExcepciones.verificarExistencia(motelRepository.existsById(id), ENTIDAD, id);
			motelRepository.actualizarEstado(id, estado);
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
	 * Elimina un motel según su ID. Su política de reserva se borra en cascada;
	 * si tiene tipos, habitaciones, fotos o servicios, MySQL impide el borrado.
	 *
	 * @param id identificador del motel
	 * @return 0 si se eliminó, 1 si no existe, 2 si tiene registros asociados, 3
	 *         si ocurre un error inesperado
	 */
	@Override
	public int deleteById(Long id) {
		try {
			LanzadorExcepciones.verificarExistencia(motelRepository.existsById(id), ENTIDAD, id);
			motelRepository.eliminar(id);
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
	 * Retorna todos los moteles, activos e inactivos.
	 *
	 * @return lista de {@link MotelDTO}
	 * @throws AccesoDatosException si falla la consulta a la base de datos
	 */
	@Override
	public List<MotelDTO> getAll() {
		try {
			return toDtoList(motelRepository.findAll());
		} catch (SQLException e) {
			throw new AccesoDatosException(e);
		}
	}

	/**
	 * Retorna los moteles activos, que son los visibles en el catálogo público.
	 *
	 * @return lista de {@link MotelDTO} activos
	 * @throws AccesoDatosException si falla la consulta a la base de datos
	 */
	public List<MotelDTO> getActivos() {
		try {
			return toDtoList(motelRepository.findActivos());
		} catch (SQLException e) {
			throw new AccesoDatosException(e);
		}
	}

	/**
	 * Arma las fichas del catálogo: cada motel activo con su foto principal,
	 * calificación, tarifa más barata y tipos de habitación con tarifas y
	 * características. Son cinco consultas en total, sin importar cuántos moteles
	 * haya; antes el front hacía varias peticiones por motel.
	 *
	 * @return lista de {@link MotelResumenDTO}, en el orden de {@link #getActivos()}
	 * @throws AccesoDatosException si falla la consulta a la base de datos
	 */
	public List<MotelResumenDTO> getResumen() {
		try {
			Map<Long, MotelRepository.Agregados> agregados = motelRepository.findAgregadosActivos();
			Map<Long, List<Caracteristica>> caracteristicas = tipoHabitacionRepository.findCaracteristicasPorTipo();
			Map<Long, List<TarifaDTO>> tarifas = new HashMap<>();
			for (Tarifa t : tarifaRepository.findAll()) {
				tarifas.computeIfAbsent(t.getIdTipo(), k -> new ArrayList<>()).add(mapper.map(t, TarifaDTO.class));
			}
			Map<Long, List<TipoResumenDTO>> tipos = new HashMap<>();
			for (TipoHabitacion t : tipoHabitacionRepository.findAll()) {
				TipoResumenDTO tipo = mapper.map(t, TipoResumenDTO.class);
				tipo.setTarifas(tarifas.getOrDefault(t.getIdTipo(), List.of()));
				tipo.setCaracteristicas(caracteristicas.getOrDefault(t.getIdTipo(), List.of()).stream()
						.map(c -> mapper.map(c, CaracteristicaDTO.class)).toList());
				tipos.computeIfAbsent(t.getIdMotel(), k -> new ArrayList<>()).add(tipo);
			}

			List<MotelResumenDTO> resumen = new ArrayList<>();
			for (Motel m : motelRepository.findActivos()) {
				MotelResumenDTO dto = mapper.map(m, MotelResumenDTO.class);
				// Un motel que se activó entre las dos consultas sale sin foto ni reseñas.
				MotelRepository.Agregados a = agregados.getOrDefault(m.getIdMotel(),
						new MotelRepository.Agregados(null, 0, 0));
				dto.setFoto(a.foto());
				dto.setPromedio(a.promedio());
				dto.setComentarios(a.comentarios());
				dto.setTipos(tipos.getOrDefault(m.getIdMotel(), List.of()));
				dto.setDesde(dto.getTipos().stream().flatMap(t -> t.getTarifas().stream())
						.map(TarifaDTO::getPrecio).min(BigDecimal::compareTo).orElse(null));
				resumen.add(dto);
			}
			return resumen;
		} catch (SQLException e) {
			throw new AccesoDatosException(e);
		}
	}

	/**
	 * Retorna los moteles activos de una ciudad.
	 *
	 * @param idCiudad identificador de la ciudad
	 * @return lista de {@link MotelDTO}
	 * @throws AccesoDatosException si falla la consulta a la base de datos
	 */
	public List<MotelDTO> getActivosByCiudad(Long idCiudad) {
		try {
			return toDtoList(motelRepository.findActivosByCiudad(idCiudad));
		} catch (SQLException e) {
			throw new AccesoDatosException(e);
		}
	}

	/**
	 * Retorna todos los moteles de un propietario.
	 *
	 * @param idPropietario identificador del propietario
	 * @return lista de {@link MotelDTO}
	 * @throws AccesoDatosException si falla la consulta a la base de datos
	 */
	public List<MotelDTO> getByPropietario(Long idPropietario) {
		try {
			return toDtoList(motelRepository.findByPropietario(idPropietario));
		} catch (SQLException e) {
			throw new AccesoDatosException(e);
		}
	}

	@Override
	public boolean exist(Long id) {
		try {
			return motelRepository.existsById(id);
		} catch (SQLException e) {
			throw new AccesoDatosException(e);
		}
	}

	@Override
	public long count() {
		try {
			return motelRepository.count();
		} catch (SQLException e) {
			throw new AccesoDatosException(e);
		}
	}

	/**
	 * Busca y retorna un motel según su ID.
	 *
	 * @param id identificador del motel
	 * @return {@link MotelDTO} encontrado, o {@code null} si no existe
	 * @throws AccesoDatosException si falla la consulta a la base de datos
	 */
	public MotelDTO getById(Long id) {
		try {
			Optional<Motel> found = motelRepository.findById(id);

			if (found.isPresent()) {
				return mapper.map(found.get(), MotelDTO.class);
			}

			return null;
		} catch (SQLException e) {
			throw new AccesoDatosException(e);
		}
	}

	/**
	 * Valida los textos, las coordenadas y las llaves foráneas del motel.
	 *
	 * @param data datos a validar
	 * @throws TextoInvalidoException          si algún texto es inválido
	 * @throws CoordenadaException             si las coordenadas son inválidas
	 * @throws ReferenciaNoEncontradaException si el propietario no tiene perfil o
	 *                                         la ciudad no existe
	 * @throws SQLException                    si falla alguna consulta
	 */
	private void validar(MotelDTO data)
			throws TextoInvalidoException, CoordenadaException, ReferenciaNoEncontradaException, SQLException {
		LanzadorExcepciones.verificarTexto(data.getNombre(), 100, "nombre");
		LanzadorExcepciones.verificarTextoOpcional(data.getDescripcion(), 500, "descripción");
		LanzadorExcepciones.verificarTexto(data.getCalle(), 40, "calle");
		LanzadorExcepciones.verificarTexto(data.getCarrera(), 40, "carrera");
		LanzadorExcepciones.verificarCoordenadas(data.getLatitud(), data.getLongitud());
		LanzadorExcepciones.verificarReferencia(
				data.getIdPropietario() != null && perfilPropietarioRepository.existsById(data.getIdPropietario()),
				"perfil de propietario", data.getIdPropietario());
		LanzadorExcepciones.verificarReferencia(
				data.getIdCiudad() != null && ciudadRepository.existsById(data.getIdCiudad()), ENTIDAD_CIUDAD,
				data.getIdCiudad());
	}

	/**
	 * Elimina espacios sobrantes de los campos de texto.
	 *
	 * @param data datos a normalizar
	 */
	private void normalizar(MotelDTO data) {
		data.setNombre(Normalizador.texto(data.getNombre()));
		data.setDescripcion(Normalizador.textoOpcional(data.getDescripcion()));
		data.setCalle(Normalizador.texto(data.getCalle()));
		data.setCarrera(Normalizador.texto(data.getCarrera()));
	}

	/**
	 * Traduce el campo de texto que falló al código de la tabla de la clase.
	 *
	 * @param e excepción lanzada por la validación
	 * @return 1 si falló el nombre, 2 si falló la descripción, 3 si falló la
	 *         dirección
	 */
	private int codigoTexto(TextoInvalidoException e) {
		switch (e.getCampo()) {
		case "nombre":
			return 1;
		case "descripción":
			return 2;
		default:
			return 3;
		}
	}

	/**
	 * Convierte una lista de entidades en una lista de DTOs.
	 *
	 * @param entityList lista de entidades
	 * @return lista de {@link MotelDTO}
	 */
	private List<MotelDTO> toDtoList(List<Motel> entityList) {
		List<MotelDTO> dtoList = new ArrayList<>();

		entityList.forEach(entity -> {
			dtoList.add(mapper.map(entity, MotelDTO.class));
		});

		return dtoList;
	}
}
