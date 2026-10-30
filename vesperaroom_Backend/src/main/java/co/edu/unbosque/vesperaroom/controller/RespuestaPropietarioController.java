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

import co.edu.unbosque.vesperaroom.dto.RespuestaPropietarioDTO;
import co.edu.unbosque.vesperaroom.entity.Usuario;
import co.edu.unbosque.vesperaroom.service.AutorizacionService;
import co.edu.unbosque.vesperaroom.service.RespuestaPropietarioService;
import co.edu.unbosque.vesperaroom.util.RespuestaGeneral;
import co.edu.unbosque.vesperaroom.util.RespuestaRespuestaPropietario;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controlador REST para las respuestas de los propietarios a las reseñas. Como
 * es una entidad débil 1:1, su identificador es el {@code idResena}. La
 * consulta es pública; solo el propietario del motel reseñado (o un
 * administrador) puede responder.
 */
@RestController
@RequestMapping("/respuesta")
@Tag(name = "Respuestas del propietario", description = "Respuestas de los propietarios a las reseñas")
@SecurityRequirement(name = "bearerAuth")
public class RespuestaPropietarioController {

	private final RespuestaPropietarioService respuestaPropietarioService;
	private final AutorizacionService autorizacionService;

	public RespuestaPropietarioController(RespuestaPropietarioService respuestaPropietarioService,
			AutorizacionService autorizacionService) {
		this.respuestaPropietarioService = respuestaPropietarioService;
		this.autorizacionService = autorizacionService;
	}

	/**
	 * Responde una reseña de uno de sus moteles.
	 *
	 * @param nueva  objeto con el {@code idResena} y el comentario
	 * @param actual usuario autenticado
	 * @return {@link ResponseEntity} con código 201 si es exitoso, 400 si algún
	 *         dato es inválido, 403 si la reseña no es de su motel o 409 si ya fue
	 *         respondida
	 */
	@Operation(summary = "Responder reseña (JSON)", description = "Responde una reseña de un motel propio.")
	@PostMapping(path = "/createjson", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> crearJSON(@RequestBody RespuestaPropietarioDTO nueva,
			@AuthenticationPrincipal Usuario actual) {
		if (!autorizacionService.puedeResponderResena(actual, nueva.getIdResena())) {
			return RespuestaGeneral.prohibido();
		}

		int status = respuestaPropietarioService.create(nueva);
		return RespuestaRespuestaPropietario.guardar(status, "Respuesta publicada exitosamente", HttpStatus.CREATED);
	}

	/**
	 * Retorna todas las respuestas.
	 *
	 * @return {@link ResponseEntity} con la lista y código 202, o 204 si no hay
	 *         registros
	 */
	@Operation(summary = "Obtener respuestas", description = "Retorna todas las respuestas. Solo ADMINISTRADOR.")
	@GetMapping("/getall")
	public ResponseEntity<List<RespuestaPropietarioDTO>> obtenerTodas() {
		return RespuestaGeneral.lista(respuestaPropietarioService.getAll());
	}

	/**
	 * Retorna la respuesta de una reseña. Endpoint público.
	 *
	 * @param id identificador de la reseña
	 * @return {@link ResponseEntity} con la respuesta y código 202, o 404 si la
	 *         reseña no tiene respuesta
	 */
	@Operation(summary = "Respuesta de una reseña", description = "Busca la respuesta por el id de la reseña. "
			+ "Público.")
	@GetMapping("/getbyid/{id}")
	public ResponseEntity<?> obtenerPorId(@PathVariable Long id) {
		return RespuestaGeneral.encontrado(respuestaPropietarioService.getById(id), "La reseña no tiene respuesta");
	}

	/**
	 * Edita la respuesta a una reseña.
	 *
	 * @param id     identificador de la reseña
	 * @param nueva  objeto con el nuevo comentario
	 * @param actual usuario autenticado
	 * @return {@link ResponseEntity} con código 202 si es exitoso, 400 si el
	 *         comentario es inválido, 403 si no es de su motel o 404 si no existe
	 */
	@Operation(summary = "Actualizar respuesta (JSON)", description = "Edita la respuesta a una reseña propia.")
	@PutMapping(path = "/updatejson", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> actualizarJSON(@RequestParam Long id, @RequestBody RespuestaPropietarioDTO nueva,
			@AuthenticationPrincipal Usuario actual) {
		if (!autorizacionService.puedeResponderResena(actual, id)) {
			return RespuestaGeneral.prohibido();
		}

		int status = respuestaPropietarioService.updateById(id, nueva);
		return RespuestaRespuestaPropietario.guardar(status, "Respuesta actualizada exitosamente",
				HttpStatus.ACCEPTED);
	}

	/**
	 * Elimina la respuesta a una reseña.
	 *
	 * @param id     identificador de la reseña
	 * @param actual usuario autenticado
	 * @return {@link ResponseEntity} con código 202 si es exitoso, 403 si no es de
	 *         su motel o 404 si no existe
	 */
	@Operation(summary = "Eliminar respuesta", description = "Elimina la respuesta a una reseña propia.")
	@DeleteMapping("/deletebyid/{id}")
	public ResponseEntity<String> eliminar(@PathVariable Long id, @AuthenticationPrincipal Usuario actual) {
		if (!autorizacionService.puedeResponderResena(actual, id)) {
			return RespuestaGeneral.prohibido();
		}

		return RespuestaGeneral.eliminar(respuestaPropietarioService.deleteById(id), "respuesta", true);
	}
}
