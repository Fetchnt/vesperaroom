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

import co.edu.unbosque.vesperaroom.dto.TarifaDTO;
import co.edu.unbosque.vesperaroom.entity.Usuario;
import co.edu.unbosque.vesperaroom.service.AutorizacionService;
import co.edu.unbosque.vesperaroom.service.TarifaService;
import co.edu.unbosque.vesperaroom.util.RespuestaGeneral;
import co.edu.unbosque.vesperaroom.util.RespuestaTarifa;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controlador REST para las tarifas de los tipos de habitación. La consulta es
 * pública; las escrituras solo las hace el propietario del motel o un
 * administrador.
 */
@RestController
@RequestMapping("/tarifa")
@Tag(name = "Tarifas", description = "Precios por tipo de habitación, modalidad y vigencia")
@SecurityRequirement(name = "bearerAuth")
public class TarifaController {

	private final TarifaService tarifaService;
	private final AutorizacionService autorizacionService;

	public TarifaController(TarifaService tarifaService, AutorizacionService autorizacionService) {
		this.tarifaService = tarifaService;
		this.autorizacionService = autorizacionService;
	}

	/**
	 * Crea una tarifa para un tipo de habitación.
	 *
	 * @param nuevo  objeto con el tipo, la modalidad, el precio, el tipo de día y
	 *               la vigencia
	 * @param actual usuario autenticado
	 * @return {@link ResponseEntity} con código 201 si es exitoso, 400 si algún
	 *         dato es inválido, 403 si el tipo no es de su motel o 409 si se repite
	 */
	@Operation(summary = "Crear tarifa (JSON)", description = "Crea una tarifa para un tipo de un motel propio. "
			+ "Fechas en formato 2026-01-01.")
	@PostMapping(path = "/createjson", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> crearJSON(@RequestBody TarifaDTO nuevo, @AuthenticationPrincipal Usuario actual) {
		if (!autorizacionService.puedeGestionarTipo(actual, nuevo.getIdTipo())) {
			return RespuestaGeneral.prohibido();
		}

		int status = tarifaService.create(nuevo);
		return RespuestaTarifa.guardar(status, "Tarifa creada exitosamente", HttpStatus.CREATED);
	}

	/**
	 * Retorna todas las tarifas.
	 *
	 * @return {@link ResponseEntity} con la lista y código 202, o 204 si no hay
	 *         registros
	 */
	@Operation(summary = "Obtener tarifas", description = "Retorna todas las tarifas. Solo ADMINISTRADOR.")
	@GetMapping("/getall")
	public ResponseEntity<List<TarifaDTO>> obtenerTodas() {
		return RespuestaGeneral.lista(tarifaService.getAll());
	}

	/**
	 * Retorna las tarifas de un tipo de habitación. Endpoint público.
	 *
	 * @param idTipo identificador del tipo de habitación
	 * @return {@link ResponseEntity} con la lista y código 202, o 204 si no hay
	 *         tarifas
	 */
	@Operation(summary = "Tarifas de un tipo", description = "Retorna las tarifas de un tipo de habitación. Público.")
	@GetMapping("/getbytipo/{idTipo}")
	public ResponseEntity<List<TarifaDTO>> obtenerPorTipo(@PathVariable Long idTipo) {
		return RespuestaGeneral.lista(tarifaService.getByTipo(idTipo));
	}

	/**
	 * Busca una tarifa por su ID. Endpoint público.
	 *
	 * @param id identificador de la tarifa
	 * @return {@link ResponseEntity} con la tarifa y código 202, o 404 si no
	 *         existe
	 */
	@Operation(summary = "Obtener tarifa por ID", description = "Busca una tarifa. Público.")
	@GetMapping("/getbyid/{id}")
	public ResponseEntity<?> obtenerPorId(@PathVariable Long id) {
		return RespuestaGeneral.encontrado(tarifaService.getById(id), "Tarifa no encontrada");
	}

	/**
	 * Actualiza una tarifa.
	 *
	 * @param id     identificador de la tarifa
	 * @param nuevo  objeto con los nuevos datos
	 * @param actual usuario autenticado
	 * @return {@link ResponseEntity} con código 202 si es exitoso, 400 si algún
	 *         dato es inválido, 403 si no es de su motel, 404 si no existe o 409 si
	 *         se repite
	 */
	@Operation(summary = "Actualizar tarifa (JSON)", description = "Actualiza una tarifa propia.")
	@PutMapping(path = "/updatejson", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> actualizarJSON(@RequestParam Long id, @RequestBody TarifaDTO nuevo,
			@AuthenticationPrincipal Usuario actual) {
		if (!autorizacionService.puedeGestionarTarifa(actual, id)) {
			return RespuestaGeneral.prohibido();
		}

		int status = tarifaService.updateById(id, nuevo);
		return RespuestaTarifa.guardar(status, "Tarifa actualizada exitosamente", HttpStatus.ACCEPTED);
	}

	/**
	 * Elimina una tarifa.
	 *
	 * @param id     identificador de la tarifa
	 * @param actual usuario autenticado
	 * @return {@link ResponseEntity} con código 202 si es exitoso, 403 si no es de
	 *         su motel, 404 si no existe o 409 si tiene reservas
	 */
	@Operation(summary = "Eliminar tarifa", description = "Elimina una tarifa propia sin reservas asociadas.")
	@DeleteMapping("/deletebyid/{id}")
	public ResponseEntity<String> eliminar(@PathVariable Long id, @AuthenticationPrincipal Usuario actual) {
		if (!autorizacionService.puedeGestionarTarifa(actual, id)) {
			return RespuestaGeneral.prohibido();
		}

		return RespuestaGeneral.eliminar(tarifaService.deleteById(id), "tarifa", true);
	}
}
