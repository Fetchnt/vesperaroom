package co.edu.unbosque.vesperaroom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.modelmapper.ModelMapper;

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
import co.edu.unbosque.vesperaroom.repository.HabitacionRepository;
import co.edu.unbosque.vesperaroom.repository.ModalidadRepository;
import co.edu.unbosque.vesperaroom.repository.MotelRepository;
import co.edu.unbosque.vesperaroom.repository.PagoRepository;
import co.edu.unbosque.vesperaroom.repository.PoliticaReservaRepository;
import co.edu.unbosque.vesperaroom.repository.ReservaRepository;
import co.edu.unbosque.vesperaroom.repository.ServicioAdicionalRepository;
import co.edu.unbosque.vesperaroom.repository.TarifaRepository;
import co.edu.unbosque.vesperaroom.repository.TipoHabitacionRepository;
import co.edu.unbosque.vesperaroom.service.ReservaService;

/**
 * Clase ReservaServiceTest. El escenario base es una reserva de 3 personas en
 * la habitación 3 (Suite, motel 1) con la tarifa "Rato" de 110.000 y dos
 * botellas de champaña de 90.000. La política del motel cobra 25.000 por cada
 * persona por encima de 2.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ReservaServiceTest {

	/**
	 * Atributo repositorio reservaRepository.
	 */
	@Mock
	private ReservaRepository reservaRepository;

	/**
	 * Atributo repositorio pagoRepository.
	 */
	@Mock
	private PagoRepository pagoRepository;

	/**
	 * Atributo repositorio habitacionRepository.
	 */
	@Mock
	private HabitacionRepository habitacionRepository;

	/**
	 * Atributo repositorio motelRepository.
	 */
	@Mock
	private MotelRepository motelRepository;

	/**
	 * Atributo repositorio tipoHabitacionRepository.
	 */
	@Mock
	private TipoHabitacionRepository tipoHabitacionRepository;

	/**
	 * Atributo repositorio tarifaRepository.
	 */
	@Mock
	private TarifaRepository tarifaRepository;

	/**
	 * Atributo repositorio modalidadRepository.
	 */
	@Mock
	private ModalidadRepository modalidadRepository;

	/**
	 * Atributo repositorio politicaReservaRepository.
	 */
	@Mock
	private PoliticaReservaRepository politicaReservaRepository;

	/**
	 * Atributo repositorio servicioAdicionalRepository.
	 */
	@Mock
	private ServicioAdicionalRepository servicioAdicionalRepository;

	/**
	 * Atributo mapper real para validar el mapeo entidad - DTO.
	 */
	@Spy
	private ModelMapper mapper = new ModelMapper();

	/**
	 * Atributo service reservaService.
	 */
	@InjectMocks
	private ReservaService reservaService;

	/**
	 * Atributo habitación del escenario base.
	 */
	private Habitacion habitacion;

	/**
	 * Atributo motel del escenario base.
	 */
	private Motel motel;

	/**
	 * Atributo inicio de la reserva (dentro de dos días).
	 */
	private LocalDateTime inicio;

	/**
	 * Metodo que prepara el escenario base con todos los datos válidos.
	 */
	@BeforeEach
	void prepararEscenario() throws Exception {
		inicio = LocalDateTime.now().plusDays(2).withNano(0);

		habitacion = new Habitacion(2L, "201", 2);
		habitacion.setIdHabitacion(3L);
		habitacion.setIdMotel(1L);
		when(habitacionRepository.findById(3L)).thenReturn(Optional.of(habitacion));

		motel = new Motel(4L, 1L, "Motel Luna", null, "Calle 10", "Carrera 43A", null, null);
		motel.setIdMotel(1L);
		when(motelRepository.findById(1L)).thenReturn(Optional.of(motel));

		Tarifa tarifa = new Tarifa(2L, 1L, new BigDecimal("110000"), Tarifa.TipoDia.TODOS, LocalDate.of(2026, 1, 1),
				null);
		tarifa.setIdTarifa(3L);
		when(tarifaRepository.findById(3L)).thenReturn(Optional.of(tarifa));

		PoliticaReserva politica = new PoliticaReserva(1L, 5, 80, 2, new BigDecimal("25000"), 1, 20);
		when(politicaReservaRepository.findById(1L)).thenReturn(Optional.of(politica));

		TipoHabitacion tipo = new TipoHabitacion(1L, "Suite Jacuzzi", 4, null);
		tipo.setIdTipo(2L);
		when(tipoHabitacionRepository.findById(2L)).thenReturn(Optional.of(tipo));

		when(modalidadRepository.findById(1L)).thenReturn(Optional.of(new Modalidad("Rato", 3)));

		ServicioAdicional champana = new ServicioAdicional(1L, "Botella de champaña", null, new BigDecimal("90000"));
		champana.setIdServicio(1L);
		when(servicioAdicionalRepository.findById(1L)).thenReturn(Optional.of(champana));

		when(reservaRepository.existsSolapamiento(any(), any(), any())).thenReturn(false);
		when(reservaRepository.insertarConServicios(any(), anyList())).thenReturn(10L);
	}

	/**
	 * Metodo que construye la solicitud del escenario base.
	 */
	private ReservaDTO solicitud() {
		ReservaDTO dto = new ReservaDTO(null, 3L, 3L, 3, inicio);
		dto.setServicios(List.of(new ReservaServicioDTO(1L, 2)));
		return dto;
	}

	/**
	 * Metodo comprobar que la creación calcula total, hora de fin, código y
	 * detalle.
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void comprobarCreacionCalculaTotalYFin() throws Exception {
		assertEquals(0, reservaService.crear(solicitud(), 1L));

		ArgumentCaptor<Reserva> reserva = ArgumentCaptor.forClass(Reserva.class);
		ArgumentCaptor<List<ReservaServicio>> detalle = ArgumentCaptor.forClass(List.class);
		verify(reservaRepository).insertarConServicios(reserva.capture(), detalle.capture());

		// 110.000 tarifa + 25.000 por la tercera persona + 2 × 90.000 servicios
		assertEquals(0, new BigDecimal("315000").compareTo(reserva.getValue().getTotal()));
		assertEquals(inicio.plusHours(3), reserva.getValue().getFechaHoraFin());
		assertEquals(Estado.PENDIENTE, reserva.getValue().getEstado());
		assertEquals(1L, reserva.getValue().getIdUsuario());
		assertTrue(reserva.getValue().getCodigoReserva().matches("VR-[A-Z2-9]{8}"));
		assertEquals(1, detalle.getValue().size());
		assertEquals(0, new BigDecimal("90000").compareTo(detalle.getValue().get(0).getPrecioUnitario()));
	}

	/**
	 * Metodo comprobar validaciones de personas y fecha.
	 */
	@Test
	public void comprobarPersonasYFecha() {
		ReservaDTO sinPersonas = solicitud();
		sinPersonas.setCantidadPersonas(0);
		assertEquals(1, reservaService.crear(sinPersonas, 1L));

		ReservaDTO enElPasado = solicitud();
		enElPasado.setFechaHoraInicio(LocalDateTime.now().minusHours(1));
		assertEquals(2, reservaService.crear(enElPasado, 1L));
	}

	/**
	 * Metodo comprobar habitación inexistente y en mantenimiento.
	 */
	@Test
	public void comprobarHabitacion() {
		ReservaDTO inexistente = solicitud();
		inexistente.setIdHabitacion(99L);
		assertEquals(3, reservaService.crear(inexistente, 1L));

		habitacion.setEstado(Habitacion.Estado.MANTENIMIENTO);
		assertEquals(4, reservaService.crear(solicitud(), 1L));
	}

	/**
	 * Metodo comprobar motel inactivo.
	 */
	@Test
	public void comprobarMotelInactivo() {
		motel.setEstado(Motel.Estado.INACTIVO);
		assertEquals(4, reservaService.crear(solicitud(), 1L));
	}

	/**
	 * Metodo comprobar tarifa inexistente y de otro tipo de habitación.
	 */
	@Test
	public void comprobarTarifa() throws Exception {
		ReservaDTO sinTarifa = solicitud();
		sinTarifa.setIdTarifa(99L);
		assertEquals(5, reservaService.crear(sinTarifa, 1L));

		Tarifa otroTipo = new Tarifa(1L, 1L, new BigDecimal("60000"), Tarifa.TipoDia.TODOS, LocalDate.of(2026, 1, 1),
				null);
		otroTipo.setIdTarifa(1L);
		when(tarifaRepository.findById(1L)).thenReturn(Optional.of(otroTipo));
		ReservaDTO tarifaAjena = solicitud();
		tarifaAjena.setIdTarifa(1L);
		assertEquals(6, reservaService.crear(tarifaAjena, 1L));
	}

	/**
	 * Metodo comprobar capacidad excedida.
	 */
	@Test
	public void comprobarCapacidadExcedida() {
		ReservaDTO grupo = solicitud();
		grupo.setCantidadPersonas(5);
		assertEquals(9, reservaService.crear(grupo, 1L));
	}

	/**
	 * Metodo comprobar servicio de otro motel y servicio repetido.
	 */
	@Test
	public void comprobarServicios() throws Exception {
		ServicioAdicional ajeno = new ServicioAdicional(3L, "Kit de bienvenida", null, new BigDecimal("30000"));
		ajeno.setIdServicio(4L);
		when(servicioAdicionalRepository.findById(4L)).thenReturn(Optional.of(ajeno));
		ReservaDTO conAjeno = solicitud();
		conAjeno.setServicios(List.of(new ReservaServicioDTO(4L, 1)));
		assertEquals(10, reservaService.crear(conAjeno, 1L));

		ReservaDTO repetido = solicitud();
		repetido.setServicios(
				List.of(new ReservaServicioDTO(1L, 1), new ReservaServicioDTO(1L, 1)));
		assertEquals(10, reservaService.crear(repetido, 1L));
	}

	/**
	 * Metodo comprobar horario ocupado antes y dentro de la transacción.
	 */
	@Test
	public void comprobarHorarioOcupado() throws Exception {
		when(reservaRepository.existsSolapamiento(any(), any(), any())).thenReturn(true);
		assertEquals(11, reservaService.crear(solicitud(), 1L));
		verify(reservaRepository, never()).insertarConServicios(any(), anyList());

		when(reservaRepository.existsSolapamiento(any(), any(), any())).thenReturn(false);
		when(reservaRepository.insertarConServicios(any(), anyList())).thenReturn(null);
		assertEquals(11, reservaService.crear(solicitud(), 1L));
	}

	/**
	 * Metodo comprobar transiciones de estado permitidas y no permitidas.
	 */
	@Test
	public void comprobarTransiciones() throws Exception {
		Reserva pendiente = new Reserva(1L, 3L, 3L, 2, inicio);
		pendiente.setIdReserva(7L);
		when(reservaRepository.findById(7L)).thenReturn(Optional.of(pendiente));

		assertEquals(3, reservaService.cambiarEstado(7L, Estado.FINALIZADA));
		assertEquals(0, reservaService.cambiarEstado(7L, Estado.CONFIRMADA));
		assertEquals(1, reservaService.cambiarEstado(7L, null));
		assertEquals(2, reservaService.cambiarEstado(99L, Estado.CANCELADA));
		// Cancelar solo se permite por cancelar(), que calcula el reembolso
		assertEquals(3, reservaService.cambiarEstado(7L, Estado.CANCELADA));
	}

	/**
	 * Metodo que registra una reserva con el estado y el inicio indicados y
	 * 190.000 en pagos aprobados.
	 */
	private Reserva reservaConPagos(Estado estado, LocalDateTime inicioReserva) throws Exception {
		Reserva r = new Reserva(1L, 3L, 3L, 2, inicioReserva);
		r.setIdReserva(7L);
		r.setEstado(estado);
		when(reservaRepository.findById(7L)).thenReturn(Optional.of(r));
		when(pagoRepository.sumarMontoAprobado(7L)).thenReturn(new BigDecimal("190000"));
		when(reservaRepository.cancelar(eq(7L), any(), any(), any())).thenReturn(true);
		return r;
	}

	/**
	 * Metodo comprobar que el cliente recibe el porcentaje de la política (80 %
	 * de 190.000) y el propietario devuelve el 100 %.
	 */
	@Test
	public void comprobarReembolsoSegunQuienCancela() throws Exception {
		reservaConPagos(Estado.CONFIRMADA, inicio);
		assertEquals(0, reservaService.cancelar(7L, true));
		verify(reservaRepository).cancelar(eq(7L), any(), eq(new BigDecimal("152000.00")), notNull());

		assertEquals(0, reservaService.cancelar(7L, false));
		verify(reservaRepository).cancelar(eq(7L), any(), eq(new BigDecimal("190000.00")), notNull());
	}

	/**
	 * Metodo comprobar que sin pagos aprobados no hay monto ni fecha límite.
	 */
	@Test
	public void comprobarCancelacionSinPagos() throws Exception {
		reservaConPagos(Estado.PENDIENTE, inicio);
		when(pagoRepository.sumarMontoAprobado(7L)).thenReturn(BigDecimal.ZERO);

		assertEquals(0, reservaService.cancelar(7L, true));
		verify(reservaRepository).cancelar(eq(7L), any(), eq(new BigDecimal("0.00")), isNull());
	}

	/**
	 * Metodo comprobar los casos en que no se puede cancelar.
	 */
	@Test
	public void comprobarCancelacionNoPermitida() throws Exception {
		assertEquals(1, reservaService.cancelar(99L, true));

		Reserva r = reservaConPagos(Estado.FINALIZADA, inicio);
		assertEquals(2, reservaService.cancelar(7L, false));

		// El cliente ya no puede cancelar una reserva que comenzó; el propietario sí
		r.setEstado(Estado.CONFIRMADA);
		r.setFechaHoraInicio(LocalDateTime.now().minusMinutes(5));
		assertEquals(3, reservaService.cancelar(7L, true));
		assertEquals(0, reservaService.cancelar(7L, false));

		// Otra petición la cambió antes del UPDATE: la transacción no modifica nada
		when(reservaRepository.cancelar(eq(7L), any(), any(), any())).thenReturn(false);
		assertEquals(2, reservaService.cancelar(7L, false));
	}

	/**
	 * Metodo comprobar la tarea automática: la pendiente vencida se cancela con
	 * reembolso total y fecha límite en días hábiles (viernes 30 de octubre + 5
	 * días hábiles = viernes 6 de noviembre), y se suman los no-show y las
	 * finalizadas.
	 */
	@Test
	public void comprobarActualizacionAutomatica() throws Exception {
		LocalDateTime ahora = LocalDateTime.of(2026, 10, 30, 10, 0);
		reservaConPagos(Estado.PENDIENTE, ahora.minusMinutes(30));
		when(reservaRepository.findPendientesVencidas(ahora)).thenReturn(List.of(7L));
		when(reservaRepository.marcarNoShow(ahora)).thenReturn(2);
		when(reservaRepository.finalizarVencidas(ahora)).thenReturn(1);

		assertEquals(4, reservaService.actualizarEstadosAutomaticos(ahora));
		verify(reservaRepository).cancelar(7L, ahora, new BigDecimal("190000.00"), LocalDate.of(2026, 11, 6));
	}
}
