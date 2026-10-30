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

import co.edu.unbosque.vesperaroom.dto.PoliticaReservaDTO;
import co.edu.unbosque.vesperaroom.entity.Usuario;
import co.edu.unbosque.vesperaroom.service.AutorizacionService;
import co.edu.unbosque.vesperaroom.service.PoliticaReservaService;
import co.edu.unbosque.vesperaroom.util.RespuestaGeneral;
import co.edu.unbosque.vesperaroom.util.RespuestaPoliticaReserva;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controlador REST para la política de reservas de cada motel. Como la
 * relación es 1:1, el identificador de la política es el {@code idMotel}. La
 * consulta es pública; las escrituras solo las hace el propietario del motel o
 * un administrador.
 */
@RestController
@RequestMapping("/politica")
@Tag(name = "Políticas de reserva", description = "Reglas de reembolso, recargos y anticipación por motel")
@SecurityRequirement(name = "bearerAuth")
public class PoliticaReservaController {

	private final PoliticaReservaService politicaReservaService;
	private final AutorizacionService autorizacionService;

	public PoliticaReservaController(PoliticaReservaService politicaReservaService,
			AutorizacionService autorizacionService) {
		this.politicaReservaService = politicaReservaService;
		this.autorizacionService = autorizacionService;
	}

	/**
	 * Crea la política de reservas de un motel.
	 *
	 * @param nuevo  objeto con el {@code idMotel} y los valores de la política
	 * @param actual usuario autenticado
	 * @return {@link ResponseEntity} con código 201 si es exitoso, 400 si algún
	 *         valor es inválido, 403 si no es su motel o 409 si ya existe
	 */
	@Operation(summary = "Crear política (JSON)", description = "Crea la política de un motel propio.")
	@PostMapping(path = "/createjson", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> crearJSON(@RequestBody PoliticaReservaDTO nuevo,
			@AuthenticationPrincipal Usuario actual) {
		if (!autorizacionService.puedeGestionarMotel(actual, nuevo.getIdMotel())) {
			return RespuestaGeneral.prohibido();
		}

		int status = politicaReservaService.create(nuevo);
		return RespuestaPoliticaReserva.guardar(status, "Política creada exitosamente", HttpStatus.CREATED);
	}

	/**
	 * Retorna las políticas de todos los moteles.
	 *
	 * @return {@link ResponseEntity} con la lista y código 202, o 204 si no hay
	 *         registros
	 */
	@Operation(summary = "Obtener políticas", description = "Retorna todas las políticas. Solo ADMINISTRADOR.")
	@GetMapping("/getall")
	public ResponseEntity<List<PoliticaReservaDTO>> obtenerTodas() {
		return RespuestaGeneral.lista(politicaReservaService.getAll());
	}

	/**
	 * Retorna la política de un motel, con {@code aceptaReembolso} calculado.
	 * Endpoint público.
	 *
	 * @param id identificador del motel
	 * @return {@link ResponseEntity} con la política y código 202, o 404 si el
	 *         motel no tiene política
	 */
	@Operation(summary = "Obtener política de un motel", description = "Busca la política por el id del motel. "
			+ "Público.")
	@GetMapping("/getbyid/{id}")
	public ResponseEntity<?> obtenerPorId(@PathVariable Long id) {
		return RespuestaGeneral.encontrado(politicaReservaService.getById(id), "El motel no tiene política de reserva");
	}

	/**
	 * Actualiza la política de un motel.
	 *
	 * @param id     identificador del motel
	 * @param nuevo  objeto con los nuevos valores
	 * @param actual usuario autenticado
	 * @return {@link ResponseEntity} con código 202 si es exitoso, 400 si algún
	 *         valor es inválido, 403 si no es su motel o 404 si no existe
	 */
	@Operation(summary = "Actualizar política (JSON)", description = "Actualiza la política de un motel propio.")
	@PutMapping(path = "/updatejson", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> actualizarJSON(@RequestParam Long id, @RequestBody PoliticaReservaDTO nuevo,
			@AuthenticationPrincipal Usuario actual) {
		if (!autorizacionService.puedeGestionarMotel(actual, id)) {
			return RespuestaGeneral.prohibido();
		}

		int status = politicaReservaService.updateById(id, nuevo);
		return RespuestaPoliticaReserva.guardar(status, "Política actualizada exitosamente", HttpStatus.ACCEPTED);
	}

	/**
	 * Elimina la política de un motel.
	 *
	 * @param id     identificador del motel
	 * @param actual usuario autenticado
	 * @return {@link ResponseEntity} con código 202 si es exitoso, 403 si no es su
	 *         motel o 404 si no existe
	 */
	@Operation(summary = "Eliminar política", description = "Elimina la política de un motel propio.")
	@DeleteMapping("/deletebyid/{id}")
	public ResponseEntity<String> eliminar(@PathVariable Long id, @AuthenticationPrincipal Usuario actual) {
		if (!autorizacionService.puedeGestionarMotel(actual, id)) {
			return RespuestaGeneral.prohibido();
		}

		return RespuestaGeneral.eliminar(politicaReservaService.deleteById(id), "política de reserva", true);
	}
}
