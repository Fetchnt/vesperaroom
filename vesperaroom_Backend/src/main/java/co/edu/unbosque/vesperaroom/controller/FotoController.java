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

import co.edu.unbosque.vesperaroom.dto.FotoDTO;
import co.edu.unbosque.vesperaroom.entity.Usuario;
import co.edu.unbosque.vesperaroom.service.AutorizacionService;
import co.edu.unbosque.vesperaroom.service.FotoService;
import co.edu.unbosque.vesperaroom.util.RespuestaFoto;
import co.edu.unbosque.vesperaroom.util.RespuestaGeneral;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controlador REST para las fotos de moteles y habitaciones. La consulta es
 * pública; las escrituras solo las hace el propietario del motel o un
 * administrador.
 */
@RestController
@RequestMapping("/foto")
@Tag(name = "Fotos", description = "Fotos de moteles y habitaciones")
@SecurityRequirement(name = "bearerAuth")
public class FotoController {

	private final FotoService fotoService;
	private final AutorizacionService autorizacionService;

	public FotoController(FotoService fotoService, AutorizacionService autorizacionService) {
		this.fotoService = fotoService;
		this.autorizacionService = autorizacionService;
	}

	/**
	 * Crea una foto para un motel o para una habitación.
	 *
	 * @param nuevo  objeto con el dueño (motel o habitación), la URI y el orden
	 * @param actual usuario autenticado
	 * @return {@link ResponseEntity} con código 201 si es exitoso, 400 si algún
	 *         dato es inválido, 403 si el dueño no es suyo o 409 si la posición
	 *         está ocupada
	 */
	@Operation(summary = "Crear foto (JSON)", description = "Agrega una foto a un motel o habitación propios.")
	@PostMapping(path = "/createjson", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> crearJSON(@RequestBody FotoDTO nuevo, @AuthenticationPrincipal Usuario actual) {
		// Una foto es de un motel o de una habitación, así que una de las dos
		// referencias llega nula: se verifica el dueño de cada una que venga.
		Long idMotel = nuevo.getIdMotel();
		Long idHabitacion = nuevo.getIdHabitacion();
		boolean permitido = (idMotel != null || idHabitacion != null)
				&& (idMotel == null || autorizacionService.puedeGestionarMotel(actual, idMotel))
				&& (idHabitacion == null || autorizacionService.puedeGestionarHabitacion(actual, idHabitacion));
		if (!permitido) {
			return RespuestaGeneral.prohibido();
		}

		int status = fotoService.create(nuevo);
		return RespuestaFoto.guardar(status, "Foto creada exitosamente", HttpStatus.CREATED);
	}

	/**
	 * Retorna todas las fotos.
	 *
	 * @return {@link ResponseEntity} con la lista y código 202, o 204 si no hay
	 *         registros
	 */
	@Operation(summary = "Obtener fotos", description = "Retorna todas las fotos. Solo ADMINISTRADOR.")
	@GetMapping("/getall")
	public ResponseEntity<List<FotoDTO>> obtenerTodas() {
		return RespuestaGeneral.lista(fotoService.getAll());
	}

	/**
	 * Retorna las fotos generales de un motel. Endpoint público.
	 *
	 * @param idMotel identificador del motel
	 * @return {@link ResponseEntity} con la lista y código 202, o 204 si no hay
	 *         fotos
	 */
	@Operation(summary = "Fotos de un motel", description = "Retorna las fotos generales de un motel. Público.")
	@GetMapping("/getbymotel/{idMotel}")
	public ResponseEntity<List<FotoDTO>> obtenerPorMotel(@PathVariable Long idMotel) {
		return RespuestaGeneral.lista(fotoService.getByMotel(idMotel));
	}

	/**
	 * Retorna las fotos de una habitación. Endpoint público.
	 *
	 * @param idHabitacion identificador de la habitación
	 * @return {@link ResponseEntity} con la lista y código 202, o 204 si no hay
	 *         fotos
	 */
	@Operation(summary = "Fotos de una habitación", description = "Retorna las fotos de una habitación. Público.")
	@GetMapping("/getbyhabitacion/{idHabitacion}")
	public ResponseEntity<List<FotoDTO>> obtenerPorHabitacion(@PathVariable Long idHabitacion) {
		return RespuestaGeneral.lista(fotoService.getByHabitacion(idHabitacion));
	}

	/**
	 * Busca una foto por su ID. Endpoint público.
	 *
	 * @param id identificador de la foto
	 * @return {@link ResponseEntity} con la foto y código 202, o 404 si no existe
	 */
	@Operation(summary = "Obtener foto por ID", description = "Busca una foto. Público.")
	@GetMapping("/getbyid/{id}")
	public ResponseEntity<?> obtenerPorId(@PathVariable Long id) {
		return RespuestaGeneral.encontrado(fotoService.getById(id), "Foto no encontrada");
	}

	/**
	 * Actualiza la URI y el orden de una foto.
	 *
	 * @param id     identificador de la foto
	 * @param nuevo  objeto con la nueva URI y el nuevo orden
	 * @param actual usuario autenticado
	 * @return {@link ResponseEntity} con código 202 si es exitoso, 400 si algún
	 *         dato es inválido, 403 si no es suya, 404 si no existe o 409 si la
	 *         posición está ocupada
	 */
	@Operation(summary = "Actualizar foto (JSON)", description = "Cambia la URI o la posición de una foto propia.")
	@PutMapping(path = "/updatejson", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> actualizarJSON(@RequestParam Long id, @RequestBody FotoDTO nuevo,
			@AuthenticationPrincipal Usuario actual) {
		if (!autorizacionService.puedeGestionarFoto(actual, id)) {
			return RespuestaGeneral.prohibido();
		}

		int status = fotoService.updateById(id, nuevo);
		return RespuestaFoto.guardar(status, "Foto actualizada exitosamente", HttpStatus.ACCEPTED);
	}

	/**
	 * Elimina una foto.
	 *
	 * @param id     identificador de la foto
	 * @param actual usuario autenticado
	 * @return {@link ResponseEntity} con código 202 si es exitoso, 403 si no es
	 *         suya o 404 si no existe
	 */
	@Operation(summary = "Eliminar foto", description = "Elimina una foto propia.")
	@DeleteMapping("/deletebyid/{id}")
	public ResponseEntity<String> eliminar(@PathVariable Long id, @AuthenticationPrincipal Usuario actual) {
		if (!autorizacionService.puedeGestionarFoto(actual, id)) {
			return RespuestaGeneral.prohibido();
		}

		return RespuestaGeneral.eliminar(fotoService.deleteById(id), "foto", true);
	}
}
