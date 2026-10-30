package co.edu.unbosque.vesperaroom.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.vesperaroom.dto.UsuarioDTO;
import co.edu.unbosque.vesperaroom.entity.Usuario;
import co.edu.unbosque.vesperaroom.entity.Usuario.Estado;
import co.edu.unbosque.vesperaroom.entity.Usuario.Rol;
import co.edu.unbosque.vesperaroom.service.UsuarioService;
import co.edu.unbosque.vesperaroom.util.RespuestaUsuario;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controlador REST para la administración de usuarios en VesperaRoom. Expone
 * endpoints de gestión reservados al rol {@code ADMINISTRADOR} y endpoints de
 * "mi cuenta" para cualquier usuario autenticado, delegando la lógica al
 * servicio {@link UsuarioService}.
 */
@RestController
@RequestMapping("/usuario")
@Tag(name = "Gestión de Usuarios", description = "Endpoints para administrar usuarios")
@SecurityRequirement(name = "bearerAuth")
public class UsuarioController {

	private final UsuarioService usuarioService;

	public UsuarioController(UsuarioService usuarioService) {
		this.usuarioService = usuarioService;
	}

	/**
	 * Crea un nuevo usuario con cualquier rol recibiendo los datos en formato JSON.
	 *
	 * @param nuevo objeto con los datos del usuario a crear
	 * @return {@link ResponseEntity} con código 201 si es exitoso, 400 si algún
	 *         dato es inválido o 409 si el correo o el teléfono ya existen
	 */
	@Operation(summary = "Crear usuario (JSON)", description = "Crea un usuario con cualquier rol. Solo ADMINISTRADOR.")
	@ApiResponses(value = { @ApiResponse(responseCode = "201", description = "Usuario creado exitosamente"),
			@ApiResponse(responseCode = "400", description = "Datos inválidos"),
			@ApiResponse(responseCode = "409", description = "Datos duplicados") })
	@PostMapping(path = "/createjson", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> crearUsuarioJSON(@RequestBody UsuarioDTO nuevo) {
		int status = usuarioService.create(nuevo);
		return RespuestaUsuario.guardar(status, nuevo.getRol() + " creado exitosamente", HttpStatus.CREATED);
	}

	/**
	 * Retorna la lista de todos los usuarios registrados en el sistema.
	 *
	 * @return {@link ResponseEntity} con la lista y código 202, o 204 si no hay
	 *         registros
	 */
	@Operation(summary = "Obtener todos los usuarios", description = "Retorna todos los usuarios registrados.")
	@GetMapping("/getall")
	public ResponseEntity<List<UsuarioDTO>> obtenerTodos() {
		return responderLista(usuarioService.getAll());
	}

	/**
	 * Retorna la lista de usuarios con rol ADMINISTRADOR.
	 *
	 * @return {@link ResponseEntity} con la lista y código 202, o 204 si no hay
	 *         registros
	 */
	@Operation(summary = "Obtener administradores", description = "Retorna los usuarios con rol ADMINISTRADOR.")
	@GetMapping("/getall/administradores")
	public ResponseEntity<List<UsuarioDTO>> obtenerAdministradores() {
		return responderLista(usuarioService.getAllByRol(Rol.ADMINISTRADOR));
	}

	/**
	 * Retorna la lista de usuarios con rol CLIENTE.
	 *
	 * @return {@link ResponseEntity} con la lista y código 202, o 204 si no hay
	 *         registros
	 */
	@Operation(summary = "Obtener clientes", description = "Retorna los usuarios con rol CLIENTE.")
	@GetMapping("/getall/clientes")
	public ResponseEntity<List<UsuarioDTO>> obtenerClientes() {
		return responderLista(usuarioService.getAllByRol(Rol.CLIENTE));
	}

	/**
	 * Retorna la lista de usuarios con rol PROPIETARIO.
	 *
	 * @return {@link ResponseEntity} con la lista y código 202, o 204 si no hay
	 *         registros
	 */
	@Operation(summary = "Obtener propietarios", description = "Retorna los usuarios con rol PROPIETARIO.")
	@GetMapping("/getall/propietarios")
	public ResponseEntity<List<UsuarioDTO>> obtenerPropietarios() {
		return responderLista(usuarioService.getAllByRol(Rol.PROPIETARIO));
	}

	/**
	 * Busca y retorna un usuario según su ID.
	 *
	 * @param id identificador del usuario a buscar
	 * @return {@link ResponseEntity} con el usuario y código 202, o 404 si no
	 *         existe
	 */
	@Operation(summary = "Obtener usuario por ID", description = "Busca un usuario mediante su ID.")
	@GetMapping("/getbyid/{id}")
	public ResponseEntity<?> obtenerPorId(@PathVariable Long id) {
		UsuarioDTO encontrado = usuarioService.getById(id);

		if (encontrado != null) {
			return new ResponseEntity<>(encontrado, HttpStatus.ACCEPTED);
		}

		return new ResponseEntity<>("Usuario no encontrado", HttpStatus.NOT_FOUND);
	}

	/**
	 * Busca y retorna un usuario según su correo electrónico.
	 *
	 * @param correo correo electrónico del usuario a buscar
	 * @return {@link ResponseEntity} con el usuario y código 202, o 404 si no
	 *         existe
	 */
	@Operation(summary = "Obtener usuario por correo", description = "Busca un usuario mediante su correo.")
	@GetMapping("/getbycorreo")
	public ResponseEntity<?> obtenerPorCorreo(@RequestParam String correo) {
		UsuarioDTO encontrado = usuarioService.getByCorreo(correo);

		if (encontrado != null) {
			return new ResponseEntity<>(encontrado, HttpStatus.ACCEPTED);
		}

		return new ResponseEntity<>("Usuario no encontrado", HttpStatus.NOT_FOUND);
	}

	/**
	 * Actualiza los datos de un usuario identificado por su ID, recibiendo los
	 * nuevos datos en formato JSON. Si la contraseña llega vacía se conserva la
	 * actual.
	 *
	 * @param id    identificador del usuario a actualizar
	 * @param nuevo objeto con los nuevos datos del usuario
	 * @return {@link ResponseEntity} con código 202 si es exitoso, 400 si algún
	 *         dato es inválido, 404 si no existe o 409 si hay datos duplicados
	 */
	@Operation(summary = "Actualizar usuario (JSON)", description = "Actualiza un usuario enviando JSON.")
	@PutMapping(path = "/updatejson", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> actualizarJSON(@RequestParam Long id, @RequestBody UsuarioDTO nuevo) {
		int status = usuarioService.updateById(id, nuevo);
		return RespuestaUsuario.guardar(status, "Usuario actualizado exitosamente", HttpStatus.ACCEPTED);
	}

	/**
	 * Cambia el estado (ACTIVO o INACTIVO) de un usuario. Un administrador no
	 * puede cambiar el estado de su propia cuenta.
	 *
	 * @param id     identificador del usuario
	 * @param estado nuevo estado
	 * @param actual usuario autenticado que realiza la operación
	 * @return {@link ResponseEntity} con código 202 si es exitoso, 400 si el estado
	 *         es inválido o se intenta sobre la propia cuenta, o 404 si no existe
	 */
	@Operation(summary = "Cambiar estado", description = "Activa o inactiva la cuenta de un usuario.")
	@PutMapping("/updateestado")
	public ResponseEntity<String> actualizarEstado(@RequestParam Long id, @RequestParam Estado estado,
			@AuthenticationPrincipal Usuario actual) {
		if (actual.getIdUsuario().equals(id)) {
			return new ResponseEntity<>("No puede cambiar el estado de su propia cuenta", HttpStatus.BAD_REQUEST);
		}

		int status = usuarioService.updateEstado(id, estado);

		switch (status) {
		case 0:
			return new ResponseEntity<>("Estado actualizado a " + estado, HttpStatus.ACCEPTED);
		case 1:
			return new ResponseEntity<>("Debe indicar el estado del usuario", HttpStatus.BAD_REQUEST);
		case 2:
			return new ResponseEntity<>("Usuario no encontrado", HttpStatus.NOT_FOUND);
		default:
			return new ResponseEntity<>("Error al actualizar el estado", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * Elimina un usuario del sistema según su ID. Un administrador no puede
	 * eliminar su propia cuenta.
	 *
	 * @param id     identificador del usuario a eliminar
	 * @param actual usuario autenticado que realiza la operación
	 * @return {@link ResponseEntity} con código 202 si es exitoso, 404 si no
	 *         existe, o 409 si tiene registros asociados
	 */
	@Operation(summary = "Eliminar usuario", description = "Elimina un usuario por ID.")
	@DeleteMapping("/deletebyid/{id}")
	public ResponseEntity<String> eliminarUsuario(@PathVariable Long id, @AuthenticationPrincipal Usuario actual) {
		if (actual.getIdUsuario().equals(id)) {
			return new ResponseEntity<>("No puede eliminar su propia cuenta", HttpStatus.BAD_REQUEST);
		}

		int status = usuarioService.deleteById(id);

		switch (status) {
		case 0:
			return new ResponseEntity<>("Usuario eliminado exitosamente", HttpStatus.ACCEPTED);
		case 1:
			return new ResponseEntity<>("Usuario no encontrado", HttpStatus.NOT_FOUND);
		case 2:
			return new ResponseEntity<>("El usuario tiene registros asociados y no puede eliminarse",
					HttpStatus.CONFLICT);
		default:
			return new ResponseEntity<>("Error al eliminar el usuario", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * Retorna el total de usuarios registrados en el sistema.
	 *
	 * @return {@link ResponseEntity} con el conteo y código 202, o 204 si no hay
	 *         registros
	 */
	@Operation(summary = "Contar usuarios", description = "Cuenta el total de usuarios.")
	@GetMapping("/count")
	public ResponseEntity<Long> contar() {
		Long total = usuarioService.count();

		if (total == 0) {
			return new ResponseEntity<>(total, HttpStatus.NO_CONTENT);
		}

		return new ResponseEntity<>(total, HttpStatus.ACCEPTED);
	}

	/**
	 * Verifica si existe un usuario registrado con el ID indicado.
	 *
	 * @param id identificador del usuario a verificar
	 * @return {@link ResponseEntity} con {@code true} y código 202 si existe, o
	 *         {@code false} y código 404 si no existe
	 */
	@Operation(summary = "Verificar existencia", description = "Verifica si existe un usuario.")
	@GetMapping("/exists/{id}")
	public ResponseEntity<Boolean> existe(@PathVariable Long id) {
		if (usuarioService.exist(id)) {
			return new ResponseEntity<>(true, HttpStatus.ACCEPTED);
		}

		return new ResponseEntity<>(false, HttpStatus.NOT_FOUND);
	}

	/**
	 * Retorna los datos de la cuenta del usuario autenticado.
	 *
	 * @param actual usuario autenticado, obtenido del token JWT
	 * @return {@link ResponseEntity} con los datos del usuario y código 202, o 404
	 *         si la cuenta ya no existe
	 */
	@Operation(summary = "Ver mi cuenta", description = "Retorna los datos del usuario autenticado.")
	@GetMapping("/mi-cuenta")
	public ResponseEntity<?> obtenerMiCuenta(@AuthenticationPrincipal Usuario actual) {
		UsuarioDTO encontrado = usuarioService.getById(actual.getIdUsuario());

		if (encontrado != null) {
			return new ResponseEntity<>(encontrado, HttpStatus.ACCEPTED);
		}

		return new ResponseEntity<>("No se encontró la cuenta del usuario", HttpStatus.NOT_FOUND);
	}

	/**
	 * Actualiza los datos de la cuenta del usuario autenticado. El rol y el estado
	 * no pueden modificarse desde este endpoint.
	 *
	 * @param nuevo  objeto con los nuevos datos personales
	 * @param actual usuario autenticado, obtenido del token JWT
	 * @return {@link ResponseEntity} con código 202 si es exitoso, 400 si algún
	 *         dato es inválido o 409 si hay datos duplicados
	 */
	@Operation(summary = "Actualizar mi cuenta", description = "Actualiza los datos personales del usuario autenticado.")
	@PutMapping(path = "/mi-cuenta", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> actualizarMiCuenta(@RequestBody UsuarioDTO nuevo,
			@AuthenticationPrincipal Usuario actual) {
		nuevo.setRol(actual.getRol());
		nuevo.setEstado(actual.getEstado());

		int status = usuarioService.updateById(actual.getIdUsuario(), nuevo);
		return RespuestaUsuario.guardar(status, "Cuenta actualizada exitosamente", HttpStatus.ACCEPTED);
	}

	/**
	 * Construye la respuesta estándar para consultas de listas.
	 *
	 * @param lista resultado de la consulta
	 * @return {@link ResponseEntity} con código 202, o 204 si la lista está vacía
	 */
	private ResponseEntity<List<UsuarioDTO>> responderLista(List<UsuarioDTO> lista) {
		if (lista.isEmpty()) {
			return new ResponseEntity<>(lista, HttpStatus.NO_CONTENT);
		}

		return new ResponseEntity<>(lista, HttpStatus.ACCEPTED);
	}
}
