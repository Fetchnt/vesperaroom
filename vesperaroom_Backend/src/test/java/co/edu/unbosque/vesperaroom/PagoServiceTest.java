package co.edu.unbosque.vesperaroom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import co.edu.unbosque.vesperaroom.dto.PagoDTO;
import co.edu.unbosque.vesperaroom.entity.Pago;
import co.edu.unbosque.vesperaroom.entity.Pago.MetodoPago;
import co.edu.unbosque.vesperaroom.entity.Reserva;
import co.edu.unbosque.vesperaroom.repository.PagoRepository;
import co.edu.unbosque.vesperaroom.repository.ReservaRepository;
import co.edu.unbosque.vesperaroom.service.PagoService;

/**
 * El saldo lo comprueba el repositorio con la reserva bloqueada; el servicio
 * solo traduce su respuesta. Estas pruebas fijan esa traducción.
 */
@ExtendWith(MockitoExtension.class)
class PagoServiceTest {

	private static final BigDecimal TOTAL = new BigDecimal("380000");

	@Mock
	private PagoRepository pagoRepository;

	@Mock
	private ReservaRepository reservaRepository;

	@Spy
	private ModelMapper mapper = new ModelMapper();

	@InjectMocks
	private PagoService pagoService;

	private PagoDTO pago;

	@BeforeEach
	void preparar() throws Exception {
		Reserva reserva = new Reserva(1L, 9L, 10L, 4, LocalDateTime.now().plusDays(3));
		reserva.setIdReserva(3L);
		reserva.setEstado(Reserva.Estado.CONFIRMADA);
		reserva.setTotal(TOTAL);
		when(reservaRepository.findById(3L)).thenReturn(Optional.of(reserva));

		pago = new PagoDTO();
		pago.setIdReserva(3L);
		pago.setMonto(TOTAL);
		pago.setMetodoPago(MetodoPago.NEQUI);
	}

	/**
	 * Metodo comprobar que un pago dentro del saldo se registra.
	 */
	@Test
	public void comprobarPagoConSaldo() throws Exception {
		when(pagoRepository.insertarSiHaySaldo(any(Pago.class), eq(TOTAL))).thenReturn(1L);

		assertEquals(0, pagoService.crear(pago));
	}

	/**
	 * Metodo comprobar que, si el repositorio no inserta por falta de saldo, el
	 * servicio responde «monto excedido» (código 6), igual que antes del cambio.
	 */
	@Test
	public void comprobarPagoSinSaldo() throws Exception {
		when(pagoRepository.insertarSiHaySaldo(any(Pago.class), eq(TOTAL))).thenReturn(null);

		assertEquals(6, pagoService.crear(pago));
	}
}
