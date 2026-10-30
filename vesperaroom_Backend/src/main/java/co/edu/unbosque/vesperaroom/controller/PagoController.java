package co.edu.unbosque.vesperaroom.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.vesperaroom.dto.PagoDTO;
import co.edu.unbosque.vesperaroom.entity.Pago.Estado;
import co.edu.unbosque.vesperaroom.entity.Usuario;
import co.edu.unbosque.vesperaroom.service.AutorizacionService;
import co.edu.unbosque.vesperaroom.service.PagoService;
import co.edu.unbosque.vesperaroom.util.RespuestaGeneral;
import co.edu.unbosque.vesperaroom.util.RespuestaPago;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controlador REST para los pagos de las reservas. El cliente registra pagos de
 * sus reservas; el propietario del motel los aprueba, rechaza o reembolsa; el
 * administrador ve todos.
 */
@RestController
@RequestMapping("/pago")
@Tag(name = "Pagos", description = "Pagos totales o parciales de las reservas")
@SecurityRequirement(name = "bearerAuth")
public class PagoController {

	private final PagoService pagoService;
	private final AutorizacionService autorizacionService;

	public PagoController(PagoService pagoService, AutorizacionService autorizacionService) {
		this.pagoService = pagoService;
		this.autorizacionService = autorizacionService;
	}

	/**
	 * Registra un pago de una reserva. Lo puede hacer el cliente de la reserva,
	 * el propietario del motel (por ejemplo, un pago en efectivo) o un
	 * administrador.
	 *
	 * @param nuevo  objeto con la reserva, el monto, el método y la referencia
	 * @param actual usuario autenticado
	 * @return {@link ResponseEntity} con código 201 si es exitoso, 400 si algún
	 *         dato es inválido, 403 si no tiene acceso a la reserva o 409 si la
	 *         reserva no admite el pago
	 */
	@Operation(summary = "Registrar pago (JSON)", description = "Registra un pago PENDIENTE de una reserva.")
	@PostMapping(path = "/createjson", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> crearJSON(@RequestBody PagoDTO nuevo, @AuthenticationPrincipal Usuario actual) {
		if (!autorizacionService.puedeVerReserva(actual, nuevo.getIdReserva())) {
			return RespuestaGeneral.prohibido();
		}

		int status = pagoService.crear(nuevo);
		return RespuestaPago.guardar(status, "Pago registrado exitosamente", HttpStatus.CREATED);
	}

	/**
	 * Retorna todos los pagos.
	 *
	 * @return {@link ResponseEntity} con la lista y código 202, o 204 si no hay
	 *         registros
	 */
	@Operation(summary = "Obtener pagos", description = "Retorna todos los pagos. Solo ADMINISTRADOR.")
	@GetMapping("/getall")
	public ResponseEntity<List<PagoDTO>> obtenerTodos() {
		return RespuestaGeneral.lista(pagoService.getAll());
	}

	/**
	 * Retorna los pagos de una reserva.
	 *
	 * @param idReserva identificador de la reserva
	 * @param actual    usuario autenticado
	 * @return {@link ResponseEntity} con la lista y código 202, 204 si no hay
	 *         pagos o 403 si no tiene acceso a la reserva
	 */
	@Operation(summary = "Pagos de una reserva", description = "Visible para el cliente, el propietario del motel o "
			+ "un ADMINISTRADOR.")
	@GetMapping("/getbyreserva/{idReserva}")
	public ResponseEntity<?> obtenerPorReserva(@PathVariable Long idReserva, @AuthenticationPrincipal Usuario actual) {
		if (!autorizacionService.puedeVerReserva(actual, idReserva)) {
			return RespuestaGeneral.prohibido();
		}

		return RespuestaGeneral.lista(pagoService.getByReserva(idReserva));
	}

	/**
	 * Busca un pago por su ID.
	 *
	 * @param id     identificador del pago
	 * @param actual usuario autenticado
	 * @return {@link ResponseEntity} con el pago y código 202, 403 si no tiene
	 *         acceso o 404 si no existe
	 */
	@Operation(summary = "Obtener pago por ID", description = "Visible para el cliente, el propietario del motel o "
			+ "un ADMINISTRADOR.")
	@GetMapping("/getbyid/{id}")
	public ResponseEntity<?> obtenerPorId(@PathVariable Long id, @AuthenticationPrincipal Usuario actual) {
		if (!autorizacionService.puedeVerPago(actual, id)) {
			return RespuestaGeneral.prohibido();
		}

		return RespuestaGeneral.encontrado(pagoService.getById(id), "Pago no encontrado");
	}

	/**
	 * Aprueba, rechaza o reembolsa un pago.
	 *
	 * @param id     identificador del pago
	 * @param estado nuevo estado
	 * @param actual usuario autenticado
	 * @return {@link ResponseEntity} con código 202 si es exitoso, 403 si no es de
	 *         su motel, 404 si no existe o 409 si la transición no está permitida
	 */
	@Operation(summary = "Cambiar estado", description = "Aprueba, rechaza o reembolsa un pago. Solo el propietario "
			+ "del motel o un ADMINISTRADOR.")
	@PutMapping("/updateestado")
	public ResponseEntity<String> actualizarEstado(@RequestParam Long id, @RequestParam Estado estado,
			@AuthenticationPrincipal Usuario actual) {
		if (!autorizacionService.puedeGestionarPago(actual, id)) {
			return RespuestaGeneral.prohibido();
		}

		return RespuestaPago.estado(pagoService.cambiarEstado(id, estado), estado);
	}

	/**
	 * Retorna el total de pagos.
	 *
	 * @return {@link ResponseEntity} con el conteo y código 202, o 204 si es cero
	 */
	@Operation(summary = "Contar pagos", description = "Cuenta el total de pagos. Solo ADMINISTRADOR.")
	@GetMapping("/count")
	public ResponseEntity<Long> contar() {
		return RespuestaGeneral.conteo(pagoService.count());
	}
}
