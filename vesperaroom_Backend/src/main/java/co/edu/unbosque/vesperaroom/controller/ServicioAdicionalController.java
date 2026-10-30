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

import co.edu.unbosque.vesperaroom.dto.ServicioAdicionalDTO;
import co.edu.unbosque.vesperaroom.entity.Usuario;
import co.edu.unbosque.vesperaroom.service.AutorizacionService;
import co.edu.unbosque.vesperaroom.service.ServicioAdicionalService;
import co.edu.unbosque.vesperaroom.util.RespuestaGeneral;
import co.edu.unbosque.vesperaroom.util.RespuestaServicioAdicional;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controlador REST para los servicios adicionales de cada motel. Los servicios
 * activos son públicos; la lista completa y las escrituras solo las ve el
 * propietario del motel o un administrador.
 */
@RestController
@RequestMapping("/servicio")
@Tag(name = "Servicios adicionales", description = "Servicios que cada motel vende con la reserva")
@SecurityRequirement(name = "bearerAuth")
public class ServicioAdicionalController {

	private final ServicioAdicionalService servicioAdicionalService;
	private final AutorizacionService autorizacionService;

	public ServicioAdicionalController(ServicioAdicionalService servicioAdicionalService,
			AutorizacionService autorizacionService) {
		this.servicioAdicionalService = servicioAdicionalService;
		this.autorizacionService = autorizacionService;
	}

	/**
	 * Crea un servicio adicional en un motel.
	 *
	 * @param nuevo  objeto con el motel y los datos del servicio
	 * @param actual usuario autenticado
	 * @return {@link ResponseEntity} con código 201 si es exitoso, 400 si algún
	 *         dato es inválido, 403 si no es su motel o 409 si el nombre se repite
	 */
	@Operation(summary = "Crear servicio (JSON)", description = "Crea un servicio en un motel propio.")
	@PostMapping(path = "/createjson", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> crearJSON(@RequestBody ServicioAdicionalDTO nuevo,
			@AuthenticationPrincipal Usuario actual) {
		if (!autorizacionService.puedeGestionarMotel(actual, nuevo.getIdMotel())) {
			return RespuestaGeneral.prohibido();
		}

		int status = servicioAdicionalService.create(nuevo);
		return RespuestaServicioAdicional.guardar(status, "Servicio creado exitosamente", HttpStatus.CREATED);
	}

	/**
	 * Retorna todos los servicios adicionales.
	 *
	 * @return {@link ResponseEntity} con la lista y código 202, o 204 si no hay
	 *         registros
	 */
	@Operation(summary = "Obtener servicios", description = "Retorna todos los servicios. Solo ADMINISTRADOR.")
	@GetMapping("/getall")
	public ResponseEntity<List<ServicioAdicionalDTO>> obtenerTodos() {
		return RespuestaGeneral.lista(servicioAdicionalService.getAll());
	}

	/**
	 * Retorna los servicios activos de un motel. Endpoint público.
	 *
	 * @param idMotel identificador del motel
	 * @return {@link ResponseEntity} con la lista y código 202, o 204 si no hay
	 *         servicios
	 */
	@Operation(summary = "Servicios activos de un motel", description = "Servicios que un cliente puede elegir. "
			+ "Público.")
	@GetMapping("/getbymotel/{idMotel}")
	public ResponseEntity<List<ServicioAdicionalDTO>> obtenerActivosPorMotel(@PathVariable Long idMotel) {
		return RespuestaGeneral.lista(servicioAdicionalService.getActivosByMotel(idMotel));
	}

	/**
	 * Retorna todos los servicios de un motel, incluidos los inactivos.
	 *
	 * @param idMotel identificador del motel
	 * @param actual  usuario autenticado
	 * @return {@link ResponseEntity} con la lista y código 202, 204 si no hay
	 *         servicios o 403 si no es su motel
	 */
	@Operation(summary = "Todos los servicios de un motel", description = "Incluye inactivos. Solo el propietario o "
			+ "un ADMINISTRADOR.")
	@GetMapping("/getbymotel/{idMotel}/todos")
	public ResponseEntity<?> obtenerTodosPorMotel(@PathVariable Long idMotel,
			@AuthenticationPrincipal Usuario actual) {
		if (!autorizacionService.puedeGestionarMotel(actual, idMotel)) {
			return RespuestaGeneral.prohibido();
		}

		return RespuestaGeneral.lista(servicioAdicionalService.getByMotel(idMotel));
	}

	/**
	 * Busca un servicio adicional por su ID. Endpoint público.
	 *
	 * @param id identificador del servicio
	 * @return {@link ResponseEntity} con el servicio y código 202, o 404 si no
	 *         existe
	 */
	@Operation(summary = "Obtener servicio por ID", description = "Busca un servicio adicional. Público.")
	@GetMapping("/getbyid/{id}")
	public ResponseEntity<?> obtenerPorId(@PathVariable Long id) {
		return RespuestaGeneral.encontrado(servicioAdicionalService.getById(id), "Servicio adicional no encontrado");
	}

	/**
	 * Actualiza un servicio adicional (incluido activarlo o desactivarlo).
	 *
	 * @param id     identificador del servicio
	 * @param nuevo  objeto con los nuevos datos
	 * @param actual usuario autenticado
	 * @return {@link ResponseEntity} con código 202 si es exitoso, 400 si algún
	 *         dato es inválido, 403 si no es de su motel, 404 si no existe o 409 si
	 *         el nombre se repite
	 */
	@Operation(summary = "Actualizar servicio (JSON)", description = "Actualiza un servicio propio.")
	@PutMapping(path = "/updatejson", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> actualizarJSON(@RequestParam Long id, @RequestBody ServicioAdicionalDTO nuevo,
			@AuthenticationPrincipal Usuario actual) {
		if (!autorizacionService.puedeGestionarServicio(actual, id)) {
			return RespuestaGeneral.prohibido();
		}

		int status = servicioAdicionalService.updateById(id, nuevo);
		return RespuestaServicioAdicional.guardar(status, "Servicio actualizado exitosamente", HttpStatus.ACCEPTED);
	}

	/**
	 * Elimina un servicio adicional.
	 *
	 * @param id     identificador del servicio
	 * @param actual usuario autenticado
	 * @return {@link ResponseEntity} con código 202 si es exitoso, 403 si no es de
	 *         su motel, 404 si no existe o 409 si está incluido en reservas
	 */
	@Operation(summary = "Eliminar servicio", description = "Elimina un servicio propio que no esté en reservas.")
	@DeleteMapping("/deletebyid/{id}")
	public ResponseEntity<String> eliminar(@PathVariable Long id, @AuthenticationPrincipal Usuario actual) {
		if (!autorizacionService.puedeGestionarServicio(actual, id)) {
			return RespuestaGeneral.prohibido();
		}

		return RespuestaGeneral.eliminar(servicioAdicionalService.deleteById(id), "servicio adicional", false);
	}
}
