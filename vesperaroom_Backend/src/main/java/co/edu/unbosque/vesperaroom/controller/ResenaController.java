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

import co.edu.unbosque.vesperaroom.dto.ResenaDTO;
import co.edu.unbosque.vesperaroom.entity.Usuario;
import co.edu.unbosque.vesperaroom.service.AutorizacionService;
import co.edu.unbosque.vesperaroom.service.ResenaService;
import co.edu.unbosque.vesperaroom.util.RespuestaGeneral;
import co.edu.unbosque.vesperaroom.util.RespuestaResena;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controlador REST para las reseñas. Las reseñas y el promedio de cada motel
 * son públicos; el cliente escribe, edita y borra las suyas.
 */
@RestController
@RequestMapping("/resena")
@Tag(name = "Reseñas", description = "Calificaciones de los clientes sobre sus reservas")
@SecurityRequirement(name = "bearerAuth")
public class ResenaController {

	private final ResenaService resenaService;
	private final AutorizacionService autorizacionService;

	public ResenaController(ResenaService resenaService, AutorizacionService autorizacionService) {
		this.resenaService = resenaService;
		this.autorizacionService = autorizacionService;
	}

	/**
	 * Crea una reseña sobre una reserva finalizada. Un cliente solo puede reseñar
	 * sus propias reservas; un ADMINISTRADOR no tiene esa restricción.
	 *
	 * @param nueva  objeto con la reserva, la calificación y el comentario
	 * @param actual usuario autenticado
	 * @return {@link ResponseEntity} con código 201 si es exitoso, 400 si algún
	 *         dato es inválido, 403 si la reserva no es suya o 409 si no está
	 *         finalizada o ya tiene reseña
	 */
	@Operation(summary = "Crear reseña (JSON)", description = "Reseña una reserva propia y finalizada.")
	@PostMapping(path = "/createjson", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> crearJSON(@RequestBody ResenaDTO nueva, @AuthenticationPrincipal Usuario actual) {
		int status = autorizacionService.esAdministrador(actual) ? resenaService.create(nueva)
				: resenaService.crear(nueva, actual.getIdUsuario());
		return RespuestaResena.guardar(status, "Reseña creada exitosamente", HttpStatus.CREATED);
	}

	/**
	 * Retorna todas las reseñas.
	 *
	 * @return {@link ResponseEntity} con la lista y código 202, o 204 si no hay
	 *         registros
	 */
	@Operation(summary = "Obtener reseñas", description = "Retorna todas las reseñas. Solo ADMINISTRADOR.")
	@GetMapping("/getall")
	public ResponseEntity<List<ResenaDTO>> obtenerTodas() {
		return RespuestaGeneral.lista(resenaService.getAll());
	}

	/**
	 * Retorna las reseñas de un motel. Endpoint público.
	 *
	 * @param idMotel identificador del motel
	 * @return {@link ResponseEntity} con la lista y código 202, o 204 si no hay
	 *         reseñas
	 */
	@Operation(summary = "Reseñas de un motel", description = "Retorna las reseñas de un motel. Público.")
	@GetMapping("/getbymotel/{idMotel}")
	public ResponseEntity<List<ResenaDTO>> obtenerPorMotel(@PathVariable Long idMotel) {
		return RespuestaGeneral.lista(resenaService.getByMotel(idMotel));
	}

	/**
	 * Retorna la calificación promedio de un motel. Endpoint público.
	 *
	 * @param idMotel identificador del motel
	 * @return {@link ResponseEntity} con el promedio y código 202, o 204 si el
	 *         motel no tiene reseñas
	 */
	@Operation(summary = "Promedio de un motel", description = "Calificación promedio de un motel. Público.")
	@GetMapping("/promedio/{idMotel}")
	public ResponseEntity<Double> obtenerPromedio(@PathVariable Long idMotel) {
		Double promedio = resenaService.getPromedioByMotel(idMotel);

		if (promedio == null) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}

		return new ResponseEntity<>(promedio, HttpStatus.ACCEPTED);
	}

	/**
	 * Retorna las reseñas escritas por el usuario autenticado.
	 *
	 * @param actual usuario autenticado
	 * @return {@link ResponseEntity} con la lista y código 202, o 204 si no tiene
	 *         reseñas
	 */
	@Operation(summary = "Mis reseñas", description = "Reseñas escritas por el usuario autenticado.")
	@GetMapping("/mis-resenas")
	public ResponseEntity<List<ResenaDTO>> obtenerMisResenas(@AuthenticationPrincipal Usuario actual) {
		return RespuestaGeneral.lista(resenaService.getByUsuario(actual.getIdUsuario()));
	}

	/**
	 * Busca una reseña por su ID. Endpoint público.
	 *
	 * @param id identificador de la reseña
	 * @return {@link ResponseEntity} con la reseña y código 202, o 404 si no
	 *         existe
	 */
	@Operation(summary = "Obtener reseña por ID", description = "Busca una reseña. Público.")
	@GetMapping("/getbyid/{id}")
	public ResponseEntity<?> obtenerPorId(@PathVariable Long id) {
		return RespuestaGeneral.encontrado(resenaService.getById(id), "Reseña no encontrada");
	}

	/**
	 * Actualiza la calificación y el comentario de una reseña propia.
	 *
	 * @param id     identificador de la reseña
	 * @param nueva  objeto con la nueva calificación y el nuevo comentario
	 * @param actual usuario autenticado
	 * @return {@link ResponseEntity} con código 202 si es exitoso, 400 si algún
	 *         dato es inválido, 403 si no es suya o 404 si no existe
	 */
	@Operation(summary = "Actualizar reseña (JSON)", description = "Edita una reseña propia.")
	@PutMapping(path = "/updatejson", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> actualizarJSON(@RequestParam Long id, @RequestBody ResenaDTO nueva,
			@AuthenticationPrincipal Usuario actual) {
		if (!autorizacionService.puedeEditarResena(actual, id)) {
			return RespuestaGeneral.prohibido();
		}

		int status = resenaService.updateById(id, nueva);
		return RespuestaResena.guardar(status, "Reseña actualizada exitosamente", HttpStatus.ACCEPTED);
	}

	/**
	 * Elimina una reseña propia junto con la respuesta del propietario.
	 *
	 * @param id     identificador de la reseña
	 * @param actual usuario autenticado
	 * @return {@link ResponseEntity} con código 202 si es exitoso, 403 si no es
	 *         suya o 404 si no existe
	 */
	@Operation(summary = "Eliminar reseña", description = "Elimina una reseña propia.")
	@DeleteMapping("/deletebyid/{id}")
	public ResponseEntity<String> eliminar(@PathVariable Long id, @AuthenticationPrincipal Usuario actual) {
		if (!autorizacionService.puedeEditarResena(actual, id)) {
			return RespuestaGeneral.prohibido();
		}

		return RespuestaGeneral.eliminar(resenaService.deleteById(id), "reseña", true);
	}
}
