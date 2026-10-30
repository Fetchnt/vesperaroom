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

import co.edu.unbosque.vesperaroom.dto.ReservaDTO;
import co.edu.unbosque.vesperaroom.entity.Reserva.Estado;
import co.edu.unbosque.vesperaroom.entity.Usuario;
import co.edu.unbosque.vesperaroom.service.AutorizacionService;
import co.edu.unbosque.vesperaroom.service.ReservaService;
import co.edu.unbosque.vesperaroom.util.RespuestaGeneral;
import co.edu.unbosque.vesperaroom.util.RespuestaReserva;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controlador REST para las reservas. Cualquier usuario autenticado reserva a
 * su nombre y ve sus reservas; el propietario del motel administra el estado
 * de las reservas de su motel; el administrador ve y elimina todas.
 */
@RestController
@RequestMapping("/reserva")
@Tag(name = "Reservas", description = "Reservas de habitaciones")
@SecurityRequirement(name = "bearerAuth")
public class ReservaController {

	private final ReservaService reservaService;
	private final AutorizacionService autorizacionService;

	public ReservaController(ReservaService reservaService, AutorizacionService autorizacionService) {
		this.reservaService = reservaService;
		this.autorizacionService = autorizacionService;
	}

	/**
	 * Crea una reserva a nombre del usuario autenticado. Un ADMINISTRADOR puede
	 * indicar el {@code idUsuario} para reservar en nombre de un cliente.
	 *
	 * @param nueva  objeto con la habitación, la tarifa, la fecha de inicio, la
	 *               cantidad de personas y los servicios
	 * @param actual usuario autenticado
	 * @return {@link ResponseEntity} con código 201 si es exitoso, 400 si algún
	 *         dato es inválido o 409 si la habitación no está disponible
	 */
	@Operation(summary = "Crear reserva (JSON)", description = "Reserva una habitación. La fecha de inicio va en "
			+ "formato 2026-11-20T21:00:00; el servidor calcula la hora de fin, el total y el código.")
	@ApiResponses(value = { @ApiResponse(responseCode = "201", description = "Reserva creada"),
			@ApiResponse(responseCode = "400", description = "Datos inválidos"),
			@ApiResponse(responseCode = "409", description = "Habitación no disponible u ocupada") })
	@PostMapping(path = "/createjson", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> crearJSON(@RequestBody ReservaDTO nueva, @AuthenticationPrincipal Usuario actual) {
		Long idUsuario = actual.getIdUsuario();
		if (autorizacionService.esAdministrador(actual) && nueva.getIdUsuario() != null) {
			idUsuario = nueva.getIdUsuario();
		}

		int status = reservaService.crear(nueva, idUsuario);
		return RespuestaReserva.guardar(status, "Reserva creada exitosamente", HttpStatus.CREATED);
	}

	/**
	 * Retorna todas las reservas.
	 *
	 * @return {@link ResponseEntity} con la lista y código 202, o 204 si no hay
	 *         registros
	 */
	@Operation(summary = "Obtener reservas", description = "Retorna todas las reservas. Solo ADMINISTRADOR.")
	@GetMapping("/getall")
	public ResponseEntity<List<ReservaDTO>> obtenerTodas() {
		return RespuestaGeneral.lista(reservaService.getAll());
	}

	/**
	 * Retorna las reservas del usuario autenticado.
	 *
	 * @param actual usuario autenticado
	 * @return {@link ResponseEntity} con la lista y código 202, o 204 si no tiene
	 *         reservas
	 */
	@Operation(summary = "Mis reservas", description = "Reservas del usuario autenticado.")
	@GetMapping("/mis-reservas")
	public ResponseEntity<List<ReservaDTO>> obtenerMisReservas(@AuthenticationPrincipal Usuario actual) {
		return RespuestaGeneral.lista(reservaService.getByUsuario(actual.getIdUsuario()));
	}

	/**
	 * Retorna las reservas de un motel.
	 *
	 * @param idMotel identificador del motel
	 * @param actual  usuario autenticado
	 * @return {@link ResponseEntity} con la lista y código 202, 204 si no hay
	 *         reservas o 403 si no es su motel
	 */
	@Operation(summary = "Reservas de un motel", description = "Solo el propietario del motel o un ADMINISTRADOR.")
	@GetMapping("/getbymotel/{idMotel}")
	public ResponseEntity<?> obtenerPorMotel(@PathVariable Long idMotel, @AuthenticationPrincipal Usuario actual) {
		if (!autorizacionService.puedeGestionarMotel(actual, idMotel)) {
			return RespuestaGeneral.prohibido();
		}

		return RespuestaGeneral.lista(reservaService.getByMotel(idMotel));
	}

	/**
	 * Busca una reserva con su detalle de servicios.
	 *
	 * @param id     identificador de la reserva
	 * @param actual usuario autenticado
	 * @return {@link ResponseEntity} con la reserva y código 202, 403 si no tiene
	 *         acceso o 404 si no existe
	 */
	@Operation(summary = "Obtener reserva por ID", description = "Visible para el cliente, el propietario del motel "
			+ "o un ADMINISTRADOR.")
	@GetMapping("/getbyid/{id}")
	public ResponseEntity<?> obtenerPorId(@PathVariable Long id, @AuthenticationPrincipal Usuario actual) {
		if (!autorizacionService.puedeVerReserva(actual, id)) {
			return RespuestaGeneral.prohibido();
		}

		return RespuestaGeneral.encontrado(reservaService.getById(id), "Reserva no encontrada");
	}

	/**
	 * Cambia el estado de una reserva según su ciclo de vida.
	 *
	 * @param id     identificador de la reserva
	 * @param estado nuevo estado
	 * @param actual usuario autenticado
	 * @return {@link ResponseEntity} con código 202 si es exitoso, 403 si no es de
	 *         su motel, 404 si no existe o 409 si la transición no está permitida
	 */
	@Operation(summary = "Cambiar estado", description = "Confirma, inicia, finaliza o cancela una reserva. Solo el "
			+ "propietario del motel o un ADMINISTRADOR. Si el estado es CANCELADA se devuelve el 100 % de lo pagado.")
	@PutMapping("/updateestado")
	public ResponseEntity<String> actualizarEstado(@RequestParam Long id, @RequestParam Estado estado,
			@AuthenticationPrincipal Usuario actual) {
		if (!autorizacionService.puedeGestionarReserva(actual, id)) {
			return RespuestaGeneral.prohibido();
		}

		if (estado == Estado.CANCELADA) {
			return responderCancelacion(id, false);
		}
		return RespuestaReserva.estado(reservaService.cambiarEstado(id, estado), estado);
	}

	/**
	 * Cancela una reserva propia. Solo es posible mientras esté PENDIENTE o
	 * CONFIRMADA y antes de su hora de inicio. Se devuelve el porcentaje de los
	 * pagos aprobados que indique la política del motel.
	 *
	 * @param id     identificador de la reserva
	 * @param actual usuario autenticado
	 * @return {@link ResponseEntity} con código 202 y el reembolso si es exitoso,
	 *         403 si no es suya, 404 si no existe o 409 si ya no se puede cancelar
	 */
	@Operation(summary = "Cancelar mi reserva", description = "El cliente cancela una reserva propia antes de su "
			+ "hora de inicio. El reembolso sigue la política del motel.")
	@PutMapping("/cancelar/{id}")
	public ResponseEntity<String> cancelar(@PathVariable Long id, @AuthenticationPrincipal Usuario actual) {
		if (!autorizacionService.esClienteDeReserva(actual, id)) {
			return RespuestaGeneral.prohibido();
		}

		return responderCancelacion(id, true);
	}

	/**
	 * Cancela la reserva y, si tuvo éxito, la vuelve a leer para informar el
	 * reembolso registrado.
	 *
	 * @param id         identificador de la reserva
	 * @param porCliente indica si cancela el cliente
	 * @return respuesta construida por {@link RespuestaReserva#cancelar(int, ReservaDTO)}
	 */
	private ResponseEntity<String> responderCancelacion(Long id, boolean porCliente) {
		int status = reservaService.cancelar(id, porCliente);
		return RespuestaReserva.cancelar(status, status == 0 ? reservaService.getById(id) : null);
	}

	/**
	 * Elimina una reserva.
	 *
	 * @param id identificador de la reserva
	 * @return {@link ResponseEntity} con código 202 si es exitoso, 404 si no
	 *         existe o 409 si tiene pagos o reseña
	 */
	@Operation(summary = "Eliminar reserva", description = "Elimina una reserva sin pagos ni reseña. Solo "
			+ "ADMINISTRADOR.")
	@DeleteMapping("/deletebyid/{id}")
	public ResponseEntity<String> eliminar(@PathVariable Long id) {
		return RespuestaGeneral.eliminar(reservaService.deleteById(id), "reserva", true);
	}

	/**
	 * Retorna el total de reservas.
	 *
	 * @return {@link ResponseEntity} con el conteo y código 202, o 204 si es cero
	 */
	@Operation(summary = "Contar reservas", description = "Cuenta el total de reservas. Solo ADMINISTRADOR.")
	@GetMapping("/count")
	public ResponseEntity<Long> contar() {
		return RespuestaGeneral.conteo(reservaService.count());
	}
}
