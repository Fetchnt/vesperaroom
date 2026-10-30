package co.edu.unbosque.vesperaroom.util;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Clase utilitaria que traduce los códigos de resultado de
 * {@link co.edu.unbosque.vesperaroom.service.PagoService} a respuestas HTTP
 * con mensaje en español. Centraliza el {@code switch} para que todos los
 * endpoints respondan igual ante el mismo error.
 */
public class RespuestaPago {

	private RespuestaPago() {
	}

	/**
	 * Construye la respuesta para las operaciones de creación y actualización de
	 * el pago.
	 *
	 * @param status       código devuelto por el servicio
	 * @param mensajeExito mensaje a devolver si la operación fue exitosa
	 * @param estadoExito  código HTTP a usar si la operación fue exitosa
	 * @return {@link ResponseEntity} con el mensaje y el código HTTP
	 *         correspondiente
	 */
	public static ResponseEntity<String> guardar(int status, String mensajeExito, HttpStatus estadoExito) {
		switch (status) {
		case 0:
			return new ResponseEntity<>(mensajeExito, estadoExito);
		case 1:
			return new ResponseEntity<>("El monto debe ser mayor que cero", HttpStatus.BAD_REQUEST);
		case 2:
			return new ResponseEntity<>("Debe indicar el método de pago", HttpStatus.BAD_REQUEST);
		case 3:
			return new ResponseEntity<>("La referencia admite máximo 100 caracteres", HttpStatus.BAD_REQUEST);
		case 4:
			return new ResponseEntity<>("La reserva indicada no existe", HttpStatus.BAD_REQUEST);
		case 5:
			return new ResponseEntity<>("La reserva está cancelada o fue un no-show y no admite pagos",
					HttpStatus.CONFLICT);
		case 6:
			return new ResponseEntity<>("El monto supera el saldo pendiente de la reserva",
					HttpStatus.CONFLICT);
		case 7:
			return new ResponseEntity<>("La referencia de transacción ya está registrada",
					HttpStatus.CONFLICT);
		default:
			return new ResponseEntity<>("Error al procesar el pago", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * Construye la respuesta de un cambio de estado del pago.
	 *
	 * @param status código devuelto por el servicio
	 * @param estado nuevo estado solicitado
	 * @return {@link ResponseEntity} con el mensaje y el código HTTP
	 *         correspondiente
	 */
	public static ResponseEntity<String> estado(int status, Object estado) {
		switch (status) {
		case 0:
			return new ResponseEntity<>("Estado actualizado a " + estado, HttpStatus.ACCEPTED);
		case 1:
			return new ResponseEntity<>("Debe indicar el estado", HttpStatus.BAD_REQUEST);
		case 2:
			return new ResponseEntity<>("Pago no encontrado", HttpStatus.NOT_FOUND);
		case 3:
			return new ResponseEntity<>("El cambio de estado no está permitido desde el estado actual",
					HttpStatus.CONFLICT);
		default:
			return new ResponseEntity<>("Error al actualizar el estado", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
