package co.edu.unbosque.vesperaroom;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import co.edu.unbosque.vesperaroom.entity.Usuario.Rol;
import co.edu.unbosque.vesperaroom.exception.ContrasenaException;
import co.edu.unbosque.vesperaroom.exception.CoordenadaException;
import co.edu.unbosque.vesperaroom.exception.CorreoException;
import co.edu.unbosque.vesperaroom.exception.CuentaBancariaException;
import co.edu.unbosque.vesperaroom.exception.FechaException;
import co.edu.unbosque.vesperaroom.exception.FotoDuenoException;
import co.edu.unbosque.vesperaroom.exception.InvalidCharException;
import co.edu.unbosque.vesperaroom.exception.LanzadorExcepciones;
import co.edu.unbosque.vesperaroom.exception.MontoExcedidoException;
import co.edu.unbosque.vesperaroom.exception.NitException;
import co.edu.unbosque.vesperaroom.exception.PasswordSizeException;
import co.edu.unbosque.vesperaroom.exception.RegistroNoEncontradoException;
import co.edu.unbosque.vesperaroom.exception.RolNoPermitidoException;
import co.edu.unbosque.vesperaroom.exception.TarifaNoVigenteException;
import co.edu.unbosque.vesperaroom.exception.TelefonoException;
import co.edu.unbosque.vesperaroom.exception.TextoInvalidoException;
import co.edu.unbosque.vesperaroom.exception.UriException;
import co.edu.unbosque.vesperaroom.exception.ValorFueraDeRangoException;

/**
 * Clase LanzadorExcepcionesTest.
 */
class LanzadorExcepcionesTest {

	/**
	 * Metodo comprobar caracteres.
	 */
	@Test
	public void comprobarCaracteres() {
		assertDoesNotThrow(() -> LanzadorExcepciones.verificarCaracteres("María José Núñez"));
		assertThrows(InvalidCharException.class, () -> LanzadorExcepciones.verificarCaracteres("Juan3"));
		assertThrows(InvalidCharException.class, () -> LanzadorExcepciones.verificarCaracteres("   "));
		assertThrows(InvalidCharException.class, () -> LanzadorExcepciones.verificarCaracteres(null));
	}

	/**
	 * Metodo comprobar correo.
	 */
	@Test
	public void comprobarCorreo() {
		assertDoesNotThrow(() -> LanzadorExcepciones.verificarCorreo("ana.gomez@unbosque.edu.co"));
		assertThrows(CorreoException.class, () -> LanzadorExcepciones.verificarCorreo("ana@"));
		assertThrows(CorreoException.class, () -> LanzadorExcepciones.verificarCorreo(null));
	}

	/**
	 * Metodo comprobar teléfono.
	 */
	@Test
	public void comprobarTelefono() {
		assertDoesNotThrow(() -> LanzadorExcepciones.verificarTelefono("3001234567"));
		assertThrows(TelefonoException.class, () -> LanzadorExcepciones.verificarTelefono("123456"));
		assertThrows(TelefonoException.class, () -> LanzadorExcepciones.verificarTelefono("300-123-4567"));
	}

	/**
	 * Metodo comprobar contraseña.
	 */
	@Test
	public void comprobarContrasena() {
		assertThrows(PasswordSizeException.class, () -> LanzadorExcepciones.verificarTamanoContrasena("Ab1"));
		assertThrows(PasswordSizeException.class, () -> LanzadorExcepciones.verificarTamanoContrasena(null));
		assertDoesNotThrow(() -> LanzadorExcepciones.verificarContrasena("Clave1234"));
		assertThrows(ContrasenaException.class, () -> LanzadorExcepciones.verificarContrasena("clave1234"));
		assertThrows(ContrasenaException.class, () -> LanzadorExcepciones.verificarContrasena("ClaveSinNum"));
	}

	/**
	 * Metodo comprobar rol público.
	 */
	@Test
	public void comprobarRolPublico() {
		assertDoesNotThrow(() -> LanzadorExcepciones.verificarRolPublico(Rol.PROPIETARIO));
		assertThrows(RolNoPermitidoException.class, () -> LanzadorExcepciones.verificarRolPublico(Rol.ADMINISTRADOR));
	}

	/**
	 * Metodo comprobar existencia.
	 */
	@Test
	public void comprobarExistencia() {
		assertThrows(RegistroNoEncontradoException.class,
				() -> LanzadorExcepciones.verificarExistencia(false, "usuario", 5L));
	}

	/**
	 * Metodo comprobar texto obligatorio y opcional.
	 */
	@Test
	public void comprobarTexto() {
		assertDoesNotThrow(() -> LanzadorExcepciones.verificarTexto("Motel 7 Lunas", 100, "nombre"));
		assertThrows(TextoInvalidoException.class, () -> LanzadorExcepciones.verificarTexto("  ", 100, "nombre"));
		assertThrows(TextoInvalidoException.class, () -> LanzadorExcepciones.verificarTexto("12345", 4, "número"));
		assertDoesNotThrow(() -> LanzadorExcepciones.verificarTextoOpcional(null, 10, "icono"));
		TextoInvalidoException e = assertThrows(TextoInvalidoException.class,
				() -> LanzadorExcepciones.verificarTextoOpcional("demasiado largo", 5, "icono"));
		assertEquals("icono", e.getCampo());
	}

	/**
	 * Metodo comprobar rangos numéricos y montos.
	 */
	@Test
	public void comprobarRangoYMonto() {
		assertDoesNotThrow(() -> LanzadorExcepciones.verificarRango(5, 1, 5, "calificación"));
		assertThrows(ValorFueraDeRangoException.class, () -> LanzadorExcepciones.verificarRango(6, 1, 5, "calificación"));
		assertThrows(ValorFueraDeRangoException.class, () -> LanzadorExcepciones.verificarRango(null, 1, 5, "piso"));
		assertDoesNotThrow(() -> LanzadorExcepciones.verificarMonto(BigDecimal.ZERO, true, "precio"));
		assertThrows(ValorFueraDeRangoException.class,
				() -> LanzadorExcepciones.verificarMonto(BigDecimal.ZERO, false, "precio"));
		assertThrows(ValorFueraDeRangoException.class,
				() -> LanzadorExcepciones.verificarMonto(new BigDecimal("-1"), true, "precio"));
	}

	/**
	 * Metodo comprobar coordenadas, NIT, cuenta y URI.
	 */
	@Test
	public void comprobarFormatos() {
		assertDoesNotThrow(() -> LanzadorExcepciones.verificarCoordenadas(null, null));
		assertDoesNotThrow(
				() -> LanzadorExcepciones.verificarCoordenadas(new BigDecimal("6.2088"), new BigDecimal("-75.5677")));
		assertThrows(CoordenadaException.class,
				() -> LanzadorExcepciones.verificarCoordenadas(new BigDecimal("6.2088"), null));
		assertThrows(CoordenadaException.class,
				() -> LanzadorExcepciones.verificarCoordenadas(new BigDecimal("91"), BigDecimal.ZERO));
		assertDoesNotThrow(() -> LanzadorExcepciones.verificarNit("900123456-7"));
		assertThrows(NitException.class, () -> LanzadorExcepciones.verificarNit("90012"));
		assertThrows(CuentaBancariaException.class, () -> LanzadorExcepciones.verificarNumeroCuenta("12AB34"));
		assertDoesNotThrow(() -> LanzadorExcepciones.verificarUri("https://cdn.vesperaroom.com/a.jpg"));
		assertThrows(UriException.class, () -> LanzadorExcepciones.verificarUri("ftp://servidor/a.jpg"));
	}

	/**
	 * Metodo comprobar fechas y vigencias.
	 */
	@Test
	public void comprobarFechas() {
		assertDoesNotThrow(() -> LanzadorExcepciones.verificarVigencia(LocalDate.of(2026, 1, 1), null));
		assertThrows(FechaException.class,
				() -> LanzadorExcepciones.verificarVigencia(LocalDate.of(2026, 5, 1), LocalDate.of(2026, 4, 1)));
		assertThrows(FechaException.class,
				() -> LanzadorExcepciones.verificarFechaFutura(LocalDateTime.now().minusMinutes(1)));
		assertThrows(TarifaNoVigenteException.class, () -> LanzadorExcepciones
				.verificarTarifaVigente(LocalDate.of(2025, 12, 31), LocalDate.of(2026, 1, 1), null));
		LocalDateTime ahora = LocalDateTime.of(2026, 10, 30, 10, 0);
		assertDoesNotThrow(() -> LanzadorExcepciones.verificarAntesDelInicio(ahora.plusMinutes(1), ahora));
		assertThrows(FechaException.class, () -> LanzadorExcepciones.verificarAntesDelInicio(ahora, ahora));
	}

	/**
	 * Metodo comprobar dueño de foto y saldo de pagos.
	 */
	@Test
	public void comprobarFotoYSaldo() {
		assertDoesNotThrow(() -> LanzadorExcepciones.verificarDuenoFoto(1L, null));
		assertThrows(FotoDuenoException.class, () -> LanzadorExcepciones.verificarDuenoFoto(1L, 2L));
		assertThrows(FotoDuenoException.class, () -> LanzadorExcepciones.verificarDuenoFoto(null, null));
		assertDoesNotThrow(() -> LanzadorExcepciones.verificarSaldo(new BigDecimal("100"), new BigDecimal("50"),
				new BigDecimal("150")));
		assertThrows(MontoExcedidoException.class, () -> LanzadorExcepciones
				.verificarSaldo(new BigDecimal("100"), new BigDecimal("50.01"), new BigDecimal("150")));
	}
}
