package co.edu.unbosque.vesperaroom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import co.edu.unbosque.vesperaroom.dto.RestablecerDTO;
import co.edu.unbosque.vesperaroom.entity.Usuario;
import co.edu.unbosque.vesperaroom.entity.Usuario.Estado;
import co.edu.unbosque.vesperaroom.entity.Usuario.Rol;
import co.edu.unbosque.vesperaroom.repository.CodigoRecuperacionRepository;
import co.edu.unbosque.vesperaroom.repository.UsuarioRepository;
import co.edu.unbosque.vesperaroom.service.MailService;
import co.edu.unbosque.vesperaroom.service.RecuperacionService;

/**
 * Reglas de la recuperación de contraseña: a quién se le envía el código, que
 * se guarda solo su hash y qué hace falta para cambiar la contraseña.
 */
@ExtendWith(MockitoExtension.class)
class RecuperacionServiceTest {

	private static final String CORREO = "cliente1@vesperaroom.com";
	private static final String NUEVA = "Nueva2026x";

	@Mock
	private UsuarioRepository usuarioRepository;

	@Mock
	private CodigoRecuperacionRepository codigoRepository;

	@Mock
	private MailService mailService;

	/** Costo bajo para que las pruebas no tarden; el algoritmo es el mismo. */
	@Spy
	private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(4);

	@InjectMocks
	private RecuperacionService recuperacionService;

	private static Usuario usuario(Estado estado) {
		Usuario u = new Usuario("Ana", "Ruiz", CORREO, "3001234567", "hash", Rol.CLIENTE);
		u.setIdUsuario(1L);
		u.setEstado(estado);
		return u;
	}

	/**
	 * Metodo comprobar que una cuenta activa recibe un código de 6 dígitos y que
	 * en la base solo queda su hash.
	 */
	@Test
	public void comprobarSolicitudEnviaCodigo() throws Exception {
		when(usuarioRepository.findByCorreo(CORREO)).thenReturn(Optional.of(usuario(Estado.ACTIVO)));

		recuperacionService.solicitar("  Cliente1@VesperaRoom.com ");

		ArgumentCaptor<String> codigo = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> hash = ArgumentCaptor.forClass(String.class);
		verify(mailService).enviarCodigoRecuperacion(eq(CORREO), codigo.capture());
		verify(codigoRepository).guardar(eq(1L), hash.capture());
		assertTrue(codigo.getValue().matches("\\d{6}"));
		assertTrue(passwordEncoder.matches(codigo.getValue(), hash.getValue()));
	}

	/**
	 * Metodo comprobar que no se envía nada a un correo sin cuenta ni a una
	 * cuenta inactiva.
	 */
	@Test
	public void comprobarSolicitudSinCuentaActiva() throws Exception {
		when(usuarioRepository.findByCorreo(CORREO)).thenReturn(Optional.empty());
		recuperacionService.solicitar(CORREO);

		when(usuarioRepository.findByCorreo(CORREO)).thenReturn(Optional.of(usuario(Estado.INACTIVO)));
		recuperacionService.solicitar(CORREO);

		verifyNoInteractions(codigoRepository, mailService);
	}

	/**
	 * Metodo comprobar que no se reenvía un código antes de 60 segundos.
	 */
	@Test
	public void comprobarReenvioInmediatoSeIgnora() throws Exception {
		when(usuarioRepository.findByCorreo(CORREO)).thenReturn(Optional.of(usuario(Estado.ACTIVO)));
		when(codigoRepository.enviadoHacePoco(1L)).thenReturn(true);

		recuperacionService.solicitar(CORREO);

		verify(codigoRepository, never()).guardar(anyLong(), anyString());
		verifyNoInteractions(mailService);
	}

	/**
	 * Metodo comprobar que el código correcto cambia la contraseña (guardada con
	 * hash) y que el repositorio borra el código en la misma operación.
	 */
	@Test
	public void comprobarRestablecerConCodigoCorrecto() throws Exception {
		when(usuarioRepository.findByCorreo(CORREO)).thenReturn(Optional.of(usuario(Estado.ACTIVO)));
		String codigoHash = passwordEncoder.encode("123456");
		when(codigoRepository.gastarIntento(1L)).thenReturn(Optional.of(codigoHash));

		assertEquals(0, recuperacionService.restablecer(new RestablecerDTO(CORREO, " 123456 ", NUEVA)));

		ArgumentCaptor<String> hash = ArgumentCaptor.forClass(String.class);
		verify(codigoRepository).restablecer(eq(1L), hash.capture());
		assertTrue(passwordEncoder.matches(NUEVA, hash.getValue()));
	}

	/**
	 * Metodo comprobar que un código erróneo, uno vencido o agotado y un correo
	 * sin cuenta responden igual y no cambian nada.
	 */
	@Test
	public void comprobarCodigoInvalidoNoCambiaNada() throws Exception {
		when(usuarioRepository.findByCorreo(CORREO)).thenReturn(Optional.of(usuario(Estado.ACTIVO)));
		String codigoHash = passwordEncoder.encode("123456");
		when(codigoRepository.gastarIntento(1L)).thenReturn(Optional.of(codigoHash)).thenReturn(Optional.empty());
		when(usuarioRepository.findByCorreo("nadie@x.com")).thenReturn(Optional.empty());

		assertEquals(3, recuperacionService.restablecer(new RestablecerDTO(CORREO, "654321", NUEVA)));
		assertEquals(3, recuperacionService.restablecer(new RestablecerDTO(CORREO, "123456", NUEVA)));
		assertEquals(3, recuperacionService.restablecer(new RestablecerDTO("nadie@x.com", "123456", NUEVA)));
		assertEquals(3, recuperacionService.restablecer(new RestablecerDTO(CORREO, "12ab56", NUEVA)));

		verify(codigoRepository, never()).restablecer(anyLong(), any());
	}

	/**
	 * Metodo comprobar que una contraseña débil se rechaza sin gastar un intento
	 * del código.
	 */
	@Test
	public void comprobarContrasenaDebilNoGastaIntento() throws Exception {
		assertEquals(1, recuperacionService.restablecer(new RestablecerDTO(CORREO, "123456", "Ab1")));
		assertEquals(2, recuperacionService.restablecer(new RestablecerDTO(CORREO, "123456", "solominusculas1")));

		verifyNoInteractions(codigoRepository);
	}
}
