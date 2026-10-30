package co.edu.unbosque.vesperaroom.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import co.edu.unbosque.vesperaroom.dto.ReservaDTO;
import co.edu.unbosque.vesperaroom.dto.ReservaServicioDTO;
import co.edu.unbosque.vesperaroom.entity.Habitacion;
import co.edu.unbosque.vesperaroom.entity.Modalidad;
import co.edu.unbosque.vesperaroom.entity.Motel;
import co.edu.unbosque.vesperaroom.entity.PoliticaReserva;
import co.edu.unbosque.vesperaroom.entity.Reserva;
import co.edu.unbosque.vesperaroom.entity.Reserva.Estado;
import co.edu.unbosque.vesperaroom.entity.ReservaServicio;
import co.edu.unbosque.vesperaroom.entity.ServicioAdicional;
import co.edu.unbosque.vesperaroom.entity.Tarifa;
import co.edu.unbosque.vesperaroom.entity.TipoHabitacion;
import co.edu.unbosque.vesperaroom.exception.AccesoDatosException;
import co.edu.unbosque.vesperaroom.exception.AnticipacionException;
import co.edu.unbosque.vesperaroom.exception.CampoObligatorioException;
import co.edu.unbosque.vesperaroom.exception.CapacidadExcedidaException;
import co.edu.unbosque.vesperaroom.exception.EstadoReservaException;
import co.edu.unbosque.vesperaroom.exception.FechaException;
import co.edu.unbosque.vesperaroom.exception.HabitacionNoDisponibleException;
import co.edu.unbosque.vesperaroom.exception.HorarioOcupadoException;
import co.edu.unbosque.vesperaroom.exception.LanzadorExcepciones;
import co.edu.unbosque.vesperaroom.exception.ReferenciaNoEncontradaException;
import co.edu.unbosque.vesperaroom.exception.RegistroNoEncontradoException;
import co.edu.unbosque.vesperaroom.exception.ServicioNoValidoException;
import co.edu.unbosque.vesperaroom.exception.TarifaNoAplicableException;
import co.edu.unbosque.vesperaroom.exception.TarifaNoVigenteException;
import co.edu.unbosque.vesperaroom.exception.TransicionEstadoException;
import co.edu.unbosque.vesperaroom.exception.ValorFueraDeRangoException;
import co.edu.unbosque.vesperaroom.repository.HabitacionRepository;
import co.edu.unbosque.vesperaroom.repository.ModalidadRepository;
import co.edu.unbosque.vesperaroom.repository.MotelRepository;
import co.edu.unbosque.vesperaroom.repository.PagoRepository;
import co.edu.unbosque.vesperaroom.repository.PoliticaReservaRepository;
import co.edu.unbosque.vesperaroom.repository.ReservaRepository;
import co.edu.unbosque.vesperaroom.repository.ServicioAdicionalRepository;
import co.edu.unbosque.vesperaroom.repository.TarifaRepository;
import co.edu.unbosque.vesperaroom.repository.TipoHabitacionRepository;

/**
 * Servicio que gestiona las reservas. Al crear una reserva calcula en el
 * servidor la hora de fin (inicio + duración de la modalidad), el total (precio
 * de la tarifa + recargo por personas adicionales + servicios) y un código
 * único, y guarda la reserva con sus servicios en una sola transacción JDBC.
 *
 * <p>
 * No implementa {@link CRUDOperation}: una reserva es un registro histórico y
 * no se edita; solo cambia de estado (y para cambiar la fecha se cancela y se
 * crea otra).
 * </p>
 *
 * <p>
 * Códigos de {@link #crear(ReservaDTO, Long)}:
 * </p>
 * <ul>
 * <li>0: operación exitosa</li>
 * <li>1: cantidad de personas fuera del rango de 1 a 20</li>
 * <li>2: fecha de inicio nula o no posterior al momento actual</li>
 * <li>3: habitación no indicada o inexistente</li>
 * <li>4: la habitación no está activa o su motel está inactivo</li>
 * <li>5: tarifa no indicada o inexistente</li>
 * <li>6: la tarifa no corresponde al tipo de la habitación</li>
 * <li>7: la tarifa no está vigente en la fecha de la reserva</li>
 * <li>8: no se cumple la anticipación mínima del motel</li>
 * <li>9: la cantidad de personas supera la capacidad de la habitación</li>
 * <li>10: algún servicio adicional no es válido</li>
 * <li>11: la habitación ya está reservada en ese horario</li>
 * <li>12: error inesperado</li>
 * </ul>
 * <p>
 * Códigos de {@link #cambiarEstado(Long, Estado)}: 0 éxito, 1 estado nulo, 2
 * no encontrada, 3 transición no permitida, 4 error inesperado. Códigos de
 * {@link #cancelar(Long, boolean)}: 0 éxito, 1 no encontrada, 2 la reserva no
 * está PENDIENTE ni CONFIRMADA, 3 el cliente intenta cancelar una reserva que
 * ya comenzó, 4 error inesperado. Códigos de {@link #deleteById(Long)}: 0
 * éxito, 1 no encontrada, 2 tiene pagos o reseña, 3 error inesperado.
 * </p>
 *
 * <p>
 * Reembolso al cancelar: si cancela el cliente se devuelve el
 * {@code porcentajeReembolso} de la política del motel sobre los pagos
 * aprobados (0 % si el motel no tiene política); si cancela el propietario, un
 * administrador o la tarea automática, se devuelve el 100 %, porque el cliente
 * no tuvo la culpa. La fecha límite de devolución se cuenta en días hábiles
 * (lunes a viernes) con {@code diasHabilesDevolucion}.
 * </p>
 */
@Service
public class ReservaService {

	private static final String ENTIDAD = "reserva";
	private static final String ENTIDAD_HABITACION = "habitación";
	private static final String ALFABETO_CODIGO = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
	private static final int LONGITUD_CODIGO = 8;
	private static final int PORCENTAJE_TOTAL = 100;
	private static final int DIAS_HABILES_POR_DEFECTO = 5;

	private final ReservaRepository reservaRepository;
	private final PagoRepository pagoRepository;
	private final HabitacionRepository habitacionRepository;
	private final MotelRepository motelRepository;
	private final TipoHabitacionRepository tipoHabitacionRepository;
	private final TarifaRepository tarifaRepository;
	private final ModalidadRepository modalidadRepository;
	private final PoliticaReservaRepository politicaReservaRepository;
	private final ServicioAdicionalRepository servicioAdicionalRepository;
	private final ModelMapper mapper;
	private final SecureRandom aleatorio = new SecureRandom();

	public ReservaService(ReservaRepository reservaRepository, PagoRepository pagoRepository,
			HabitacionRepository habitacionRepository, MotelRepository motelRepository,
			TipoHabitacionRepository tipoHabitacionRepository, TarifaRepository tarifaRepository,
			ModalidadRepository modalidadRepository, PoliticaReservaRepository politicaReservaRepository,
			ServicioAdicionalRepository servicioAdicionalRepository, ModelMapper mapper) {
		this.reservaRepository = reservaRepository;
		this.pagoRepository = pagoRepository;
		this.habitacionRepository = habitacionRepository;
		this.motelRepository = motelRepository;
		this.tipoHabitacionRepository = tipoHabitacionRepository;
		this.tarifaRepository = tarifaRepository;
		this.modalidadRepository = modalidadRepository;
		this.politicaReservaRepository = politicaReservaRepository;
		this.servicioAdicionalRepository = servicioAdicionalRepository;
		this.mapper = mapper;
	}

	/**
	 * Crea una reserva para un cliente. Valida la habitación, la tarifa, la
	 * política del motel, la capacidad, los servicios y la disponibilidad;
	 * calcula la hora de fin, el total y el código; y guarda todo en una
	 * transacción.
	 *
	 * @param data      objeto con la habitación, la tarifa, la fecha de inicio, la
	 *                  cantidad de personas y los servicios
	 * @param idUsuario identificador del cliente que reserva
	 * @return código de resultado según la tabla de la clase
	 */
	public int crear(ReservaDTO data, Long idUsuario) {
		try {
			LanzadorExcepciones.verificarRango(data.getCantidadPersonas(), 1, 20, "cantidad de personas");
			LanzadorExcepciones.verificarFechaFutura(data.getFechaHoraInicio());

			Optional<Habitacion> habitacion = data.getIdHabitacion() == null ? Optional.empty()
					: habitacionRepository.findById(data.getIdHabitacion());
			LanzadorExcepciones.verificarReferencia(habitacion.isPresent(), ENTIDAD_HABITACION,
					data.getIdHabitacion());
			Habitacion h = habitacion.get();
			Motel motel = motelRepository.findById(h.getIdMotel()).get();
			LanzadorExcepciones.verificarHabitacionDisponible(
					h.getEstado() == Habitacion.Estado.ACTIVA && motel.getEstado() == Motel.Estado.ACTIVO);

			Optional<Tarifa> tarifa = data.getIdTarifa() == null ? Optional.empty()
					: tarifaRepository.findById(data.getIdTarifa());
			LanzadorExcepciones.verificarReferencia(tarifa.isPresent(), "tarifa", data.getIdTarifa());
			Tarifa t = tarifa.get();
			LanzadorExcepciones.verificarTarifaAplicable(t.getIdTipo().equals(h.getIdTipo()));
			LanzadorExcepciones.verificarTarifaVigente(data.getFechaHoraInicio().toLocalDate(), t.getVigenteDesde(),
					t.getVigenteHasta());

			Optional<PoliticaReserva> politica = politicaReservaRepository.findById(motel.getIdMotel());
			if (politica.isPresent()) {
				LanzadorExcepciones.verificarAnticipacion(data.getFechaHoraInicio(),
						politica.get().getAnticipacionMinimaHoras());
			}

			TipoHabitacion tipo = tipoHabitacionRepository.findById(h.getIdTipo()).get();
			LanzadorExcepciones.verificarCapacidad(data.getCantidadPersonas(), tipo.getCapacidadPersonas());

			List<ReservaServicio> detalle = new ArrayList<>();
			BigDecimal totalServicios = prepararServicios(data.getServicios(), motel.getIdMotel(), detalle);

			Modalidad modalidad = modalidadRepository.findById(t.getIdModalidad()).get();
			LocalDateTime fin = data.getFechaHoraInicio().plusHours(modalidad.getDuracionHoras());
			LanzadorExcepciones.verificarHorarioLibre(
					reservaRepository.existsSolapamiento(h.getIdHabitacion(), data.getFechaHoraInicio(), fin));

			Reserva entity = new Reserva(idUsuario, h.getIdHabitacion(), t.getIdTarifa(), data.getCantidadPersonas(),
					data.getFechaHoraInicio());
			entity.setCodigoReserva(generarCodigo());
			entity.setFechaCreacion(LocalDateTime.now());
			entity.setFechaHoraFin(fin);
			entity.setEstado(Estado.PENDIENTE);
			entity.setTotal(t.getPrecio().add(calcularRecargo(politica, data.getCantidadPersonas()))
					.add(totalServicios));

			Long idReserva = reservaRepository.insertarConServicios(entity, detalle);
			LanzadorExcepciones.verificarHorarioLibre(idReserva == null);
			return 0;

		} catch (ValorFueraDeRangoException e) {
			return 1;
		} catch (FechaException e) {
			return 2;
		} catch (ReferenciaNoEncontradaException e) {
			return ENTIDAD_HABITACION.equals(e.getEntidad()) ? 3 : 5;
		} catch (HabitacionNoDisponibleException e) {
			return 4;
		} catch (TarifaNoAplicableException e) {
			return 6;
		} catch (TarifaNoVigenteException e) {
			return 7;
		} catch (AnticipacionException e) {
			return 8;
		} catch (CapacidadExcedidaException e) {
			return 9;
		} catch (ServicioNoValidoException e) {
			return 10;
		} catch (HorarioOcupadoException e) {
			return 11;
		} catch (Exception e) {
			e.printStackTrace();
			return 12;
		}
	}

	/**
	 * Cambia el estado de una reserva respetando su ciclo de vida:
	 * PENDIENTE → CONFIRMADA; CONFIRMADA → EN_CURSO o NO_SHOW; EN_CURSO →
	 * FINALIZADA. Los estados FINALIZADA, CANCELADA y NO_SHOW son finales. Para
	 * cancelar se usa {@link #cancelar(Long, boolean)}, que además calcula el
	 * reembolso.
	 *
	 * @param id    identificador de la reserva
	 * @param nuevo estado al que se quiere pasar
	 * @return código de resultado según la tabla de la clase
	 */
	public int cambiarEstado(Long id, Estado nuevo) {
		try {
			LanzadorExcepciones.verificarObligatorio(nuevo, "el estado");
			Optional<Reserva> found = reservaRepository.findById(id);
			LanzadorExcepciones.verificarExistencia(found.isPresent(), ENTIDAD, id);
			LanzadorExcepciones.verificarTransicion(transicionPermitida(found.get().getEstado(), nuevo));
			reservaRepository.actualizarEstado(id, nuevo);
			return 0;
		} catch (CampoObligatorioException e) {
			return 1;
		} catch (RegistroNoEncontradoException e) {
			return 2;
		} catch (TransicionEstadoException e) {
			return 3;
		} catch (Exception e) {
			e.printStackTrace();
			return 4;
		}
	}

	/**
	 * Cancela una reserva PENDIENTE o CONFIRMADA y registra el reembolso según la
	 * política del motel. Los pagos pendientes pasan a RECHAZADO en la misma
	 * transacción.
	 *
	 * @param id         identificador de la reserva
	 * @param porCliente {@code true} si cancela el cliente (aplica el porcentaje
	 *                   de la política y solo puede hacerlo antes de la hora de
	 *                   inicio); {@code false} si cancela el propietario o un
	 *                   administrador (reembolso del 100 %)
	 * @return código de resultado según la tabla de la clase
	 */
	public int cancelar(Long id, boolean porCliente) {
		return cancelar(id, porCliente, LocalDateTime.now());
	}

	/**
	 * Actualiza los estados que dependen solo del paso del tiempo. La invoca la
	 * tarea programada {@code ActualizadorEstadosReserva}:
	 * <ul>
	 * <li>PENDIENTE cuya hora de inicio llegó sin que el motel la confirmara →
	 * CANCELADA, con reembolso del 100 %.</li>
	 * <li>CONFIRMADA cuyo cliente no llegó dentro del tiempo de gracia →
	 * NO_SHOW, sin reembolso.</li>
	 * <li>EN_CURSO cuya hora de fin pasó → FINALIZADA.</li>
	 * </ul>
	 * El paso de CONFIRMADA a EN_CURSO sigue siendo manual porque solo el
	 * propietario sabe cuándo llega el cliente.
	 *
	 * @param ahora momento de referencia
	 * @return cantidad de reservas que cambiaron de estado
	 * @throws AccesoDatosException si falla alguna sentencia
	 */
	public int actualizarEstadosAutomaticos(LocalDateTime ahora) {
		try {
			int cambios = 0;
			for (Long id : reservaRepository.findPendientesVencidas(ahora)) {
				if (cancelar(id, false, ahora) == 0) {
					cambios++;
				}
			}
			cambios += reservaRepository.marcarNoShow(ahora);
			cambios += reservaRepository.finalizarVencidas(ahora);
			return cambios;
		} catch (SQLException e) {
			throw new AccesoDatosException(e);
		}
	}

	/**
	 * Implementación de la cancelación con un momento de referencia explícito.
	 *
	 * @param id         identificador de la reserva
	 * @param porCliente indica si cancela el cliente
	 * @param ahora      momento de la cancelación
	 * @return código de resultado según la tabla de la clase
	 */
	private int cancelar(Long id, boolean porCliente, LocalDateTime ahora) {
		try {
			Optional<Reserva> found = reservaRepository.findById(id);
			LanzadorExcepciones.verificarExistencia(found.isPresent(), ENTIDAD, id);
			Reserva reserva = found.get();
			LanzadorExcepciones.verificarEstadoReserva(
					reserva.getEstado() == Estado.PENDIENTE || reserva.getEstado() == Estado.CONFIRMADA, "cancelar");
			if (porCliente) {
				LanzadorExcepciones.verificarAntesDelInicio(reserva.getFechaHoraInicio(), ahora);
			}

			Optional<PoliticaReserva> politica = politicaReservaRepository
					.findById(habitacionRepository.findById(reserva.getIdHabitacion()).get().getIdMotel());
			int porcentaje = PORCENTAJE_TOTAL;
			if (porCliente) {
				porcentaje = politica.isPresent() ? politica.get().getPorcentajeReembolso() : 0;
			}
			int diasHabiles = politica.isPresent() ? politica.get().getDiasHabilesDevolucion()
					: DIAS_HABILES_POR_DEFECTO;

			BigDecimal monto = pagoRepository.sumarMontoAprobado(id).multiply(BigDecimal.valueOf(porcentaje))
					.divide(BigDecimal.valueOf(PORCENTAJE_TOTAL), 2, RoundingMode.HALF_UP);
			LocalDate limite = monto.signum() > 0 ? sumarDiasHabiles(ahora.toLocalDate(), diasHabiles) : null;

			boolean cancelada = reservaRepository.cancelar(id, ahora, monto, limite);
			LanzadorExcepciones.verificarEstadoReserva(cancelada, "cancelar");
			return 0;
		} catch (RegistroNoEncontradoException e) {
			return 1;
		} catch (EstadoReservaException e) {
			return 2;
		} catch (FechaException e) {
			return 3;
		} catch (Exception e) {
			e.printStackTrace();
			return 4;
		}
	}

	/**
	 * Suma días hábiles a una fecha saltando sábados y domingos. No tiene en
	 * cuenta los festivos.
	 *
	 * @param desde fecha inicial
	 * @param dias  cantidad de días hábiles a sumar
	 * @return fecha resultante
	 */
	private LocalDate sumarDiasHabiles(LocalDate desde, int dias) {
		LocalDate fecha = desde;
		int sumados = 0;
		while (sumados < dias) {
			fecha = fecha.plusDays(1);
			if (fecha.getDayOfWeek() != DayOfWeek.SATURDAY && fecha.getDayOfWeek() != DayOfWeek.SUNDAY) {
				sumados++;
			}
		}
		return fecha;
	}

	/**
	 * Elimina una reserva y su detalle de servicios (que se borra en cascada).
	 *
	 * @param id identificador de la reserva
	 * @return 0 si se eliminó, 1 si no existe, 2 si tiene pagos o reseña, 3 si
	 *         ocurre un error inesperado
	 */
	public int deleteById(Long id) {
		try {
			LanzadorExcepciones.verificarExistencia(reservaRepository.existsById(id), ENTIDAD, id);
			reservaRepository.eliminar(id);
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
	 * Retorna todas las reservas, de la más reciente a la más antigua.
	 *
	 * @return lista de {@link ReservaDTO} sin el detalle de servicios
	 * @throws AccesoDatosException si falla la consulta a la base de datos
	 */
	public List<ReservaDTO> getAll() {
		try {
			return toDtoList(reservaRepository.findAll());
		} catch (SQLException e) {
			throw new AccesoDatosException(e);
		}
	}

	/**
	 * Retorna las reservas de un cliente.
	 *
	 * @param idUsuario identificador del cliente
	 * @return lista de {@link ReservaDTO}
	 * @throws AccesoDatosException si falla la consulta a la base de datos
	 */
	public List<ReservaDTO> getByUsuario(Long idUsuario) {
		try {
			return toDtoList(reservaRepository.findByUsuario(idUsuario));
		} catch (SQLException e) {
			throw new AccesoDatosException(e);
		}
	}

	/**
	 * Retorna las reservas de todas las habitaciones de un motel.
	 *
	 * @param idMotel identificador del motel
	 * @return lista de {@link ReservaDTO}
	 * @throws AccesoDatosException si falla la consulta a la base de datos
	 */
	public List<ReservaDTO> getByMotel(Long idMotel) {
		try {
			return toDtoList(reservaRepository.findByMotel(idMotel));
		} catch (SQLException e) {
			throw new AccesoDatosException(e);
		}
	}

	public boolean exist(Long id) {
		try {
			return reservaRepository.existsById(id);
		} catch (SQLException e) {
			throw new AccesoDatosException(e);
		}
	}

	public long count() {
		try {
			return reservaRepository.count();
		} catch (SQLException e) {
			throw new AccesoDatosException(e);
		}
	}

	/**
	 * Busca una reserva y su detalle de servicios.
	 *
	 * @param id identificador de la reserva
	 * @return {@link ReservaDTO} con sus servicios, o {@code null} si no existe
	 * @throws AccesoDatosException si falla la consulta a la base de datos
	 */
	public ReservaDTO getById(Long id) {
		try {
			Optional<Reserva> found = reservaRepository.findById(id);

			if (found.isEmpty()) {
				return null;
			}

			ReservaDTO dto = mapper.map(found.get(), ReservaDTO.class);
			List<ReservaServicioDTO> servicios = new ArrayList<>();
			reservaRepository.findServicios(id).forEach(servicio -> {
				servicios.add(mapper.map(servicio, ReservaServicioDTO.class));
			});
			dto.setServicios(servicios);
			return dto;
		} catch (SQLException e) {
			throw new AccesoDatosException(e);
		}
	}

	/**
	 * Valida los servicios solicitados y arma el detalle con el precio vigente de
	 * cada uno. Un servicio es válido si existe, pertenece al motel de la
	 * habitación, está activo, su cantidad está entre 1 y 20 y no se repite.
	 *
	 * @param solicitados servicios enviados por el cliente (puede ser nulo)
	 * @param idMotel     motel de la habitación reservada
	 * @param detalle     lista donde se agregan los servicios ya validados
	 * @return suma de {@code precio × cantidad} de todos los servicios
	 * @throws ServicioNoValidoException si algún servicio no es válido
	 * @throws SQLException              si falla alguna consulta
	 */
	private BigDecimal prepararServicios(List<ReservaServicioDTO> solicitados, Long idMotel,
			List<ReservaServicio> detalle) throws ServicioNoValidoException, SQLException {
		BigDecimal total = BigDecimal.ZERO;
		if (solicitados == null) {
			return total;
		}

		Set<Long> vistos = new HashSet<>();
		for (ReservaServicioDTO solicitado : solicitados) {
			Optional<ServicioAdicional> servicio = solicitado.getIdServicio() == null ? Optional.empty()
					: servicioAdicionalRepository.findById(solicitado.getIdServicio());
			boolean valido = servicio.isPresent() && servicio.get().getIdMotel().equals(idMotel)
					&& servicio.get().getActivo() && solicitado.getCantidad() != null
					&& solicitado.getCantidad() >= 1 && solicitado.getCantidad() <= 20
					&& vistos.add(solicitado.getIdServicio());
			LanzadorExcepciones.verificarServicio(valido);

			BigDecimal precio = servicio.get().getPrecio();
			detalle.add(new ReservaServicio(null, solicitado.getIdServicio(), solicitado.getCantidad(), precio));
			total = total.add(precio.multiply(BigDecimal.valueOf(solicitado.getCantidad())));
		}
		return total;
	}

	/**
	 * Calcula el recargo por personas adicionales según la política del motel:
	 * {@code (personas - capacidadMaxSinRecargo) × tarifaPersonaAdicional} cuando
	 * la cantidad supera la capacidad sin recargo.
	 *
	 * @param politica política del motel, si existe
	 * @param personas cantidad de personas de la reserva
	 * @return recargo a sumar al total, o cero si no aplica
	 */
	private BigDecimal calcularRecargo(Optional<PoliticaReserva> politica, int personas) {
		if (politica.isEmpty() || personas <= politica.get().getCapacidadMaxSinRecargo()) {
			return BigDecimal.ZERO;
		}
		int adicionales = personas - politica.get().getCapacidadMaxSinRecargo();
		return politica.get().getTarifaPersonaAdicional().multiply(BigDecimal.valueOf(adicionales));
	}

	/**
	 * Genera un código de reserva legible, por ejemplo {@code VR-7KQ2M9XA}. Usa
	 * {@link SecureRandom} y un alfabeto sin caracteres ambiguos (sin 0, O, 1 ni
	 * I). La restricción {@code uq_reserva_codigo} garantiza que no se repita.
	 *
	 * @return código de 11 caracteres
	 */
	private String generarCodigo() {
		StringBuilder codigo = new StringBuilder("VR-");
		for (int i = 0; i < LONGITUD_CODIGO; i++) {
			codigo.append(ALFABETO_CODIGO.charAt(aleatorio.nextInt(ALFABETO_CODIGO.length())));
		}
		return codigo.toString();
	}

	/**
	 * Indica si el ciclo de vida permite pasar de un estado a otro.
	 *
	 * @param actual estado actual de la reserva
	 * @param nuevo  estado solicitado
	 * @return {@code true} si la transición está permitida
	 */
	private boolean transicionPermitida(Estado actual, Estado nuevo) {
		switch (actual) {
		case PENDIENTE:
			return nuevo == Estado.CONFIRMADA;
		case CONFIRMADA:
			return nuevo == Estado.EN_CURSO || nuevo == Estado.NO_SHOW;
		case EN_CURSO:
			return nuevo == Estado.FINALIZADA;
		default:
			return false;
		}
	}

	/**
	 * Convierte una lista de entidades en una lista de DTOs.
	 *
	 * @param entityList lista de entidades
	 * @return lista de {@link ReservaDTO}
	 */
	private List<ReservaDTO> toDtoList(List<Reserva> entityList) {
		List<ReservaDTO> dtoList = new ArrayList<>();

		entityList.forEach(entity -> {
			dtoList.add(mapper.map(entity, ReservaDTO.class));
		});

		return dtoList;
	}
}
