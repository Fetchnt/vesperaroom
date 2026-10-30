package co.edu.unbosque.vesperaroom;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.edu.unbosque.vesperaroom.entity.Usuario;
import co.edu.unbosque.vesperaroom.entity.Usuario.Rol;
import co.edu.unbosque.vesperaroom.repository.AutorizacionRepository;
import co.edu.unbosque.vesperaroom.service.AutorizacionService;

/**
 * Clase AutorizacionServiceTest.
 */
@ExtendWith(MockitoExtension.class)
class AutorizacionServiceTest {

	/**
	 * Atributo repositorio autorizacionRepository.
	 */
	@Mock
	private AutorizacionRepository autorizacionRepository;

	/**
	 * Atributo service autorizacionService.
	 */
	@InjectMocks
	private AutorizacionService autorizacionService;

	/**
	 * Metodo que construye un usuario con el id y el rol indicados.
	 */
	private Usuario usuario(Long id, Rol rol) {
		Usuario u = new Usuario("Ana", "Ruiz", "ana@mail.com", "3001234567", "hash", rol);
		u.setIdUsuario(id);
		return u;
	}

	/**
	 * Metodo comprobar que el administrador siempre tiene permiso sin consultar la
	 * base.
	 */
	@Test
	public void comprobarAdministrador() {
		assertTrue(autorizacionService.puedeGestionarMotel(usuario(6L, Rol.ADMINISTRADOR), 1L));
		verifyNoInteractions(autorizacionRepository);
	}

	/**
	 * Metodo comprobar dueño y no dueño de un motel.
	 */
	@Test
	public void comprobarPropietario() throws Exception {
		when(autorizacionRepository.propietarioDeMotel(1L)).thenReturn(Optional.of(4L));

		assertTrue(autorizacionService.puedeGestionarMotel(usuario(4L, Rol.PROPIETARIO), 1L));
		assertFalse(autorizacionService.puedeGestionarMotel(usuario(5L, Rol.PROPIETARIO), 1L));
	}

	/**
	 * Metodo comprobar que un recurso inexistente deja pasar para que el servicio
	 * responda 404.
	 */
	@Test
	public void comprobarRecursoInexistente() throws Exception {
		when(autorizacionRepository.propietarioDeHabitacion(99L)).thenReturn(Optional.empty());

		assertTrue(autorizacionService.puedeGestionarHabitacion(usuario(5L, Rol.PROPIETARIO), 99L));
	}

	/**
	 * Metodo comprobar acceso a una reserva por cliente, propietario y tercero.
	 */
	@Test
	public void comprobarVerReserva() throws Exception {
		when(autorizacionRepository.clienteDeReserva(3L)).thenReturn(Optional.of(1L));
		when(autorizacionRepository.propietarioDeReserva(3L)).thenReturn(Optional.of(5L));

		assertTrue(autorizacionService.puedeVerReserva(usuario(1L, Rol.CLIENTE), 3L));
		assertTrue(autorizacionService.puedeVerReserva(usuario(5L, Rol.PROPIETARIO), 3L));
		assertFalse(autorizacionService.puedeVerReserva(usuario(2L, Rol.CLIENTE), 3L));
	}
}
