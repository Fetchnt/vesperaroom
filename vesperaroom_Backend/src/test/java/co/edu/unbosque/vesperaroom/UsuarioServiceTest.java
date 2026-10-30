package co.edu.unbosque.vesperaroom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;

import co.edu.unbosque.vesperaroom.dto.UsuarioDTO;
import co.edu.unbosque.vesperaroom.entity.Usuario;
import co.edu.unbosque.vesperaroom.entity.Usuario.Estado;
import co.edu.unbosque.vesperaroom.entity.Usuario.Rol;
import co.edu.unbosque.vesperaroom.repository.UsuarioRepository;
import co.edu.unbosque.vesperaroom.service.UsuarioService;

/**
 * Clase UsuarioServiceTest.
 */
@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

	/**
	 * Atributo repositorio usuarioRepository.
	 */
	@Mock
	private UsuarioRepository usuarioRepository;

	/**
	 * Atributo mapper real para validar el mapeo entidad - DTO.
	 */
	@Spy
	private ModelMapper mapper = new ModelMapper();

	/**
	 * Atributo passwordEncoder.
	 */
	@Mock
	private PasswordEncoder passwordEncoder;

	/**
	 * Atributo service usuarioService.
	 */
	@InjectMocks
	private UsuarioService usuarioService;

	/**
	 * Metodo que construye un DTO válido para las pruebas.
	 */
	private UsuarioDTO dtoValido() {
		return new UsuarioDTO("Juan", "Pérez", "juan@mail.com", "3001234567", "Clave1234", Rol.CLIENTE);
	}

	/**
	 * Metodo que construye una entidad existente para las pruebas.
	 */
	private Usuario usuarioExistente() {
		Usuario u = new Usuario("Juan", "Pérez", "juan@mail.com", "3001234567", "hashViejo", Rol.CLIENTE);
		u.setIdUsuario(1L);
		u.setFechaRegistro(LocalDateTime.now());
		return u;
	}

	/**
	 * Metodo comprobar crear.
	 */
	@Test
	public void comprobarCrear() throws Exception {
		when(usuarioRepository.existsByCorreo("juan@mail.com")).thenReturn(false);
		when(usuarioRepository.existsByTelefono("3001234567")).thenReturn(false);
		when(passwordEncoder.encode("Clave1234")).thenReturn("hash");

		assertEquals(0, usuarioService.create(dtoValido()));

		ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
		verify(usuarioRepository).insertar(captor.capture());
		assertEquals("hash", captor.getValue().getContrasena());
		assertEquals(Estado.ACTIVO, captor.getValue().getEstado());
		assertNull(captor.getValue().getIdUsuario());
	}

	/**
	 * Metodo comprobar que el correo se normaliza antes de guardar.
	 */
	@Test
	public void comprobarCrearNormalizaCorreo() throws Exception {
		UsuarioDTO dto = dtoValido();
		dto.setCorreo("  Juan@Mail.COM ");
		when(passwordEncoder.encode(anyString())).thenReturn("hash");

		assertEquals(0, usuarioService.create(dto));

		ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
		verify(usuarioRepository).insertar(captor.capture());
		assertEquals("juan@mail.com", captor.getValue().getCorreo());
	}

	/**
	 * Metodo comprobar crear con nombre inválido.
	 */
	@Test
	public void comprobarCrearNombreInvalido() throws Exception {
		UsuarioDTO dto = dtoValido();
		dto.setNombre("Juan123");
		assertEquals(1, usuarioService.create(dto));
	}

	/**
	 * Metodo comprobar crear con correo inválido.
	 */
	@Test
	public void comprobarCrearCorreoInvalido() throws Exception {
		UsuarioDTO dto = dtoValido();
		dto.setCorreo("juan.mail.com");
		assertEquals(2, usuarioService.create(dto));
	}

	/**
	 * Metodo comprobar crear con teléfono inválido.
	 */
	@Test
	public void comprobarCrearTelefonoInvalido() throws Exception {
		UsuarioDTO dto = dtoValido();
		dto.setTelefono("30A");
		assertEquals(3, usuarioService.create(dto));
	}

	/**
	 * Metodo comprobar crear con contraseña corta.
	 */
	@Test
	public void comprobarCrearContrasenaCorta() throws Exception {
		UsuarioDTO dto = dtoValido();
		dto.setContrasena("Ab1");
		assertEquals(4, usuarioService.create(dto));
	}

	/**
	 * Metodo comprobar crear con contraseña sin número.
	 */
	@Test
	public void comprobarCrearContrasenaSinNumero() throws Exception {
		UsuarioDTO dto = dtoValido();
		dto.setContrasena("Abcdefghi");
		assertEquals(5, usuarioService.create(dto));
	}

	/**
	 * Metodo comprobar crear sin rol.
	 */
	@Test
	public void comprobarCrearSinRol() throws Exception {
		UsuarioDTO dto = dtoValido();
		dto.setRol(null);
		assertEquals(6, usuarioService.create(dto));
	}

	/**
	 * Metodo comprobar crear con correo duplicado.
	 */
	@Test
	public void comprobarCrearCorreoDuplicado() throws Exception {
		when(usuarioRepository.existsByCorreo("juan@mail.com")).thenReturn(true);

		assertEquals(8, usuarioService.create(dtoValido()));
		verify(usuarioRepository, never()).insertar(any());
	}

	/**
	 * Metodo comprobar crear con teléfono duplicado.
	 */
	@Test
	public void comprobarCrearTelefonoDuplicado() throws Exception {
		when(usuarioRepository.existsByCorreo("juan@mail.com")).thenReturn(false);
		when(usuarioRepository.existsByTelefono("3001234567")).thenReturn(true);

		assertEquals(9, usuarioService.create(dtoValido()));
		verify(usuarioRepository, never()).insertar(any());
	}

	/**
	 * Metodo comprobar que el registro público rechaza el rol administrador.
	 */
	@Test
	public void comprobarRegistrarAdministrador() throws Exception {
		UsuarioDTO dto = dtoValido();
		dto.setRol(Rol.ADMINISTRADOR);

		assertEquals(7, usuarioService.registrar(dto));
		verify(usuarioRepository, never()).insertar(any());
	}

	/**
	 * Metodo comprobar que el registro público fuerza el estado activo.
	 */
	@Test
	public void comprobarRegistrarFuerzaEstadoActivo() throws Exception {
		UsuarioDTO dto = dtoValido();
		dto.setEstado(Estado.INACTIVO);
		when(passwordEncoder.encode(anyString())).thenReturn("hash");

		assertEquals(0, usuarioService.registrar(dto));

		ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
		verify(usuarioRepository).insertar(captor.capture());
		assertEquals(Estado.ACTIVO, captor.getValue().getEstado());
	}

	/**
	 * Metodo comprobar actualizar un usuario inexistente.
	 */
	@Test
	public void comprobarActualizarNoEncontrado() throws Exception {
		when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());
		assertEquals(10, usuarioService.updateById(99L, dtoValido()));
	}

	/**
	 * Metodo comprobar que actualizar sin contraseña conserva la actual.
	 */
	@Test
	public void comprobarActualizarConservaContrasena() throws Exception {
		UsuarioDTO dto = dtoValido();
		dto.setContrasena(null);
		dto.setNombre("Juana");
		when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioExistente()));

		assertEquals(0, usuarioService.updateById(1L, dto));

		ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
		verify(usuarioRepository).actualizar(captor.capture());
		assertEquals("hashViejo", captor.getValue().getContrasena());
		assertEquals("Juana", captor.getValue().getNombre());
		verify(passwordEncoder, never()).encode(anyString());
	}

	/**
	 * Metodo comprobar actualizar con correo de otro usuario.
	 */
	@Test
	public void comprobarActualizarCorreoDuplicado() throws Exception {
		when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioExistente()));
		when(usuarioRepository.existsByCorreoAndIdNot("juan@mail.com", 1L)).thenReturn(true);

		assertEquals(8, usuarioService.updateById(1L, dtoValido()));
	}

	/**
	 * Metodo comprobar cambiar estado sin indicar el estado.
	 */
	@Test
	public void comprobarActualizarEstadoNulo() throws Exception {
		assertEquals(1, usuarioService.updateEstado(1L, null));
	}

	/**
	 * Metodo comprobar cambiar estado de un usuario inexistente.
	 */
	@Test
	public void comprobarActualizarEstadoNoEncontrado() throws Exception {
		when(usuarioRepository.existsById(99L)).thenReturn(false);
		assertEquals(2, usuarioService.updateEstado(99L, Estado.INACTIVO));
	}

	/**
	 * Metodo comprobar eliminar.
	 */
	@Test
	public void comprobarEliminar() throws Exception {
		when(usuarioRepository.existsById(1L)).thenReturn(true);

		assertEquals(0, usuarioService.deleteById(1L));
		verify(usuarioRepository).eliminar(1L);
	}

	/**
	 * Metodo comprobar eliminar un usuario inexistente.
	 */
	@Test
	public void comprobarEliminarNoEncontrado() throws Exception {
		when(usuarioRepository.existsById(99L)).thenReturn(false);

		assertEquals(1, usuarioService.deleteById(99L));
		verify(usuarioRepository, never()).eliminar(any());
	}

	/**
	 * Metodo comprobar que las consultas no devuelven la contraseña.
	 */
	@Test
	public void comprobarGetAllSinContrasena() throws Exception {
		when(usuarioRepository.findAll()).thenReturn(List.of(usuarioExistente()));

		List<UsuarioDTO> lista = usuarioService.getAll();

		assertEquals(1, lista.size());
		assertEquals(1L, lista.get(0).getIdUsuario());
		assertEquals("juan@mail.com", lista.get(0).getCorreo());
		assertNull(lista.get(0).getContrasena());
	}

	/**
	 * Metodo comprobar buscar por id inexistente.
	 */
	@Test
	public void comprobarGetByIdNoEncontrado() throws Exception {
		when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());
		assertNull(usuarioService.getById(99L));
	}

	/**
	 * Metodo comprobar que un teléfono duplicado detectado por MySQL (error 1062)
	 * se traduce al código 9.
	 */
	@Test
	public void comprobarCrearDuplicadoDetectadoPorBD() throws Exception {
		when(passwordEncoder.encode(anyString())).thenReturn("hash");
		when(usuarioRepository.insertar(any())).thenThrow(new SQLIntegrityConstraintViolationException(
				"Duplicate entry '3001234567' for key 'usuario.uq_usuario_telefono'", "23000", 1062));

		assertEquals(9, usuarioService.create(dtoValido()));
	}

	/**
	 * Metodo comprobar que una violación de integridad distinta a llave duplicada
	 * se trata como error inesperado.
	 */
	@Test
	public void comprobarCrearOtraViolacionIntegridad() throws Exception {
		when(passwordEncoder.encode(anyString())).thenReturn("hash");
		when(usuarioRepository.insertar(any()))
				.thenThrow(new SQLIntegrityConstraintViolationException("Column cannot be null", "23000", 1048));

		assertEquals(11, usuarioService.create(dtoValido()));
	}

	/**
	 * Metodo comprobar eliminar un usuario con registros asociados.
	 */
	@Test
	public void comprobarEliminarConRegistrosAsociados() throws Exception {
		when(usuarioRepository.existsById(1L)).thenReturn(true);
		doThrow(new SQLIntegrityConstraintViolationException("Cannot delete or update a parent row", "23000", 1451))
				.when(usuarioRepository).eliminar(1L);

		assertEquals(2, usuarioService.deleteById(1L));
	}
}
