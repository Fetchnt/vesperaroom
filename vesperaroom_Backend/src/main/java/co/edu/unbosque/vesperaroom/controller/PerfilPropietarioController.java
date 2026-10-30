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

import co.edu.unbosque.vesperaroom.dto.PerfilPropietarioDTO;
import co.edu.unbosque.vesperaroom.entity.Usuario;
import co.edu.unbosque.vesperaroom.service.AutorizacionService;
import co.edu.unbosque.vesperaroom.service.PerfilPropietarioService;
import co.edu.unbosque.vesperaroom.util.RespuestaGeneral;
import co.edu.unbosque.vesperaroom.util.RespuestaPerfilPropietario;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controlador REST para los perfiles comerciales de los propietarios. El
 * propietario gestiona su propio perfil con los endpoints {@code /mi-perfil};
 * el administrador consulta, verifica y elimina cualquier perfil.
 */
@RestController
@RequestMapping("/perfilpropietario")
@Tag(name = "Perfiles de propietario", description = "Datos comerciales y bancarios de los propietarios")
@SecurityRequirement(name = "bearerAuth")
public class PerfilPropietarioController {

	private final PerfilPropietarioService perfilPropietarioService;
	private final AutorizacionService autorizacionService;

	public PerfilPropietarioController(PerfilPropietarioService perfilPropietarioService,
			AutorizacionService autorizacionService) {
		this.perfilPropietarioService = perfilPropietarioService;
		this.autorizacionService = autorizacionService;
	}

	/**
	 * Crea un perfil de propietario. Un PROPIETARIO solo puede crear el suyo (el
	 * {@code idUsuario} se toma del token); un ADMINISTRADOR indica el usuario.
	 *
	 * @param nuevo  objeto con los datos del perfil
	 * @param actual usuario autenticado
	 * @return {@link ResponseEntity} con código 201 si es exitoso, 400 si algún
	 *         dato es inválido o 409 si hay datos duplicados
	 */
	@Operation(summary = "Crear perfil (JSON)", description = "Crea el perfil del propietario autenticado, o el de "
			+ "cualquier propietario si es ADMINISTRADOR.")
	@ApiResponses(value = { @ApiResponse(responseCode = "201", description = "Perfil creado"),
			@ApiResponse(responseCode = "400", description = "Datos inválidos"),
			@ApiResponse(responseCode = "409", description = "Datos duplicados") })
	@PostMapping(path = "/createjson", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> crearJSON(@RequestBody PerfilPropietarioDTO nuevo,
			@AuthenticationPrincipal Usuario actual) {
		if (!autorizacionService.esAdministrador(actual)) {
			nuevo.setIdUsuario(actual.getIdUsuario());
		}

		int status = perfilPropietarioService.create(nuevo);
		return RespuestaPerfilPropietario.guardar(status, "Perfil creado exitosamente", HttpStatus.CREATED);
	}

	/**
	 * Retorna todos los perfiles de propietario.
	 *
	 * @return {@link ResponseEntity} con la lista y código 202, o 204 si no hay
	 *         registros
	 */
	@Operation(summary = "Obtener perfiles", description = "Retorna todos los perfiles. Solo ADMINISTRADOR.")
	@GetMapping("/getall")
	public ResponseEntity<List<PerfilPropietarioDTO>> obtenerTodos() {
		return RespuestaGeneral.lista(perfilPropietarioService.getAll());
	}

	/**
	 * Busca el perfil de un propietario.
	 *
	 * @param id identificador del perfil (id del usuario propietario)
	 * @return {@link ResponseEntity} con el perfil y código 202, o 404 si no existe
	 */
	@Operation(summary = "Obtener perfil por ID", description = "Busca un perfil por el id del propietario.")
	@GetMapping("/getbyid/{id}")
	public ResponseEntity<?> obtenerPorId(@PathVariable Long id) {
		return RespuestaGeneral.encontrado(perfilPropietarioService.getById(id), "Perfil de propietario no encontrado");
	}

	/**
	 * Retorna el perfil del propietario autenticado.
	 *
	 * @param actual usuario autenticado
	 * @return {@link ResponseEntity} con el perfil y código 202, o 404 si aún no lo
	 *         ha creado
	 */
	@Operation(summary = "Ver mi perfil", description = "Retorna el perfil del propietario autenticado.")
	@GetMapping("/mi-perfil")
	public ResponseEntity<?> obtenerMiPerfil(@AuthenticationPrincipal Usuario actual) {
		return RespuestaGeneral.encontrado(perfilPropietarioService.getById(actual.getIdUsuario()),
				"Aún no ha registrado su perfil de propietario");
	}

	/**
	 * Actualiza el perfil del propietario autenticado.
	 *
	 * @param nuevo  objeto con los nuevos datos
	 * @param actual usuario autenticado
	 * @return {@link ResponseEntity} con código 202 si es exitoso, 400 si algún
	 *         dato es inválido, 404 si no existe o 409 si hay duplicados
	 */
	@Operation(summary = "Actualizar mi perfil", description = "Actualiza el perfil del propietario autenticado.")
	@PutMapping(path = "/mi-perfil", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> actualizarMiPerfil(@RequestBody PerfilPropietarioDTO nuevo,
			@AuthenticationPrincipal Usuario actual) {
		int status = perfilPropietarioService.updateById(actual.getIdUsuario(), nuevo);
		return RespuestaPerfilPropietario.guardar(status, "Perfil actualizado exitosamente", HttpStatus.ACCEPTED);
	}

	/**
	 * Actualiza el perfil de cualquier propietario.
	 *
	 * @param id    identificador del perfil
	 * @param nuevo objeto con los nuevos datos
	 * @return {@link ResponseEntity} con código 202 si es exitoso, 400 si algún
	 *         dato es inválido, 404 si no existe o 409 si hay duplicados
	 */
	@Operation(summary = "Actualizar perfil (JSON)", description = "Actualiza cualquier perfil. Solo ADMINISTRADOR.")
	@PutMapping(path = "/updatejson", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> actualizarJSON(@RequestParam Long id, @RequestBody PerfilPropietarioDTO nuevo) {
		int status = perfilPropietarioService.updateById(id, nuevo);
		return RespuestaPerfilPropietario.guardar(status, "Perfil actualizado exitosamente", HttpStatus.ACCEPTED);
	}

	/**
	 * Marca un perfil como verificado con la fecha y hora actual.
	 *
	 * @param id identificador del perfil
	 * @return {@link ResponseEntity} con código 202 si es exitoso o 404 si no
	 *         existe
	 */
	@Operation(summary = "Verificar perfil", description = "Registra la verificación del perfil. Solo ADMINISTRADOR.")
	@PutMapping("/verificar")
	public ResponseEntity<String> verificar(@RequestParam Long id) {
		return RespuestaPerfilPropietario.verificar(perfilPropietarioService.verificar(id));
	}

	/**
	 * Elimina un perfil de propietario.
	 *
	 * @param id identificador del perfil
	 * @return {@link ResponseEntity} con código 202 si es exitoso, 404 si no
	 *         existe o 409 si tiene moteles
	 */
	@Operation(summary = "Eliminar perfil", description = "Elimina un perfil. Solo ADMINISTRADOR.")
	@DeleteMapping("/deletebyid/{id}")
	public ResponseEntity<String> eliminar(@PathVariable Long id) {
		return RespuestaGeneral.eliminar(perfilPropietarioService.deleteById(id), "perfil de propietario", false);
	}

	/**
	 * Retorna el total de perfiles registrados.
	 *
	 * @return {@link ResponseEntity} con el conteo y código 202, o 204 si es cero
	 */
	@Operation(summary = "Contar perfiles", description = "Cuenta el total de perfiles.")
	@GetMapping("/count")
	public ResponseEntity<Long> contar() {
		return RespuestaGeneral.conteo(perfilPropietarioService.count());
	}

	/**
	 * Verifica si un propietario tiene perfil.
	 *
	 * @param id identificador del propietario
	 * @return {@link ResponseEntity} con {@code true} y código 202, o
	 *         {@code false} y código 404
	 */
	@Operation(summary = "Verificar existencia", description = "Verifica si un propietario tiene perfil.")
	@GetMapping("/exists/{id}")
	public ResponseEntity<Boolean> existe(@PathVariable Long id) {
		return RespuestaGeneral.existe(perfilPropietarioService.exist(id));
	}
}
