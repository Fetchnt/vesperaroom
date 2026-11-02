package co.edu.unbosque.vesperaroom.util;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import co.edu.unbosque.vesperaroom.dto.ReservaDTO;

/**
 * Clase utilitaria que traduce los códigos de resultado de
 * {@link co.edu.unbosque.vesperaroom.service.ReservaService} a respuestas HTTP
 * con mensaje en español. Centraliza el {@code switch} para que todos los
 * endpoints respondan igual ante el mismo error.
 */
public class RespuestaReserva {

	private RespuestaReserva() {
	}

	/**
	 * Construye la respuesta para las operaciones de creación y actualización de
	 * la reserva.
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
			return new ResponseEntity<>("La cantidad de personas debe estar entre 1 y 20",
					HttpStatus.BAD_REQUEST);
		case 2:
			return new ResponseEntity<>("La fecha de inicio debe ser posterior al momento actual",
					HttpStatus.BAD_REQUEST);
		case 3:
			return new ResponseEntity<>("La habitación indicada no existe", HttpStatus.BAD_REQUEST);
		case 4:
			return new ResponseEntity<>("La habitación no está disponible para reservas",
					HttpStatus.CONFLICT);
		case 5:
			return new ResponseEntity<>("La tarifa indicada no existe", HttpStatus.BAD_REQUEST);
		case 6:
			return new ResponseEntity<>("La tarifa no corresponde al tipo de la habitación",
					HttpStatus.BAD_REQUEST);
		case 7:
			return new ResponseEntity<>("La tarifa no está vigente para la fecha de la reserva",
					HttpStatus.BAD_REQUEST);
		case 8:
			return new ResponseEntity<>("La reserva no cumple la anticipación mínima del motel",
					HttpStatus.BAD_REQUEST);
		case 9:
			return new ResponseEntity<>("La cantidad de personas supera la capacidad de la habitación",
					HttpStatus.BAD_REQUEST);
		case 10:
			return new ResponseEntity<>("Uno de los servicios no existe, está inactivo, no pertenece al motel, está repetido o su cantidad no está entre 1 y 20",
					HttpStatus.BAD_REQUEST);
		case 11:
			return new ResponseEntity<>("La habitación ya está reservada en ese horario",
					HttpStatus.CONFLICT);
		case 12:
			return new ResponseEntity<>("El motel todavía no tiene política de reserva; no se pueden recibir reservas",
					HttpStatus.CONFLICT);
		default:
			return new ResponseEntity<>("Error al procesar la reserva", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * Construye la respuesta de un cambio de estado de la reserva.
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
			return new ResponseEntity<>("Reserva no encontrada", HttpStatus.NOT_FOUND);
		case 3:
			return new ResponseEntity<>("El cambio de estado no está permitido desde el estado actual",
					HttpStatus.CONFLICT);
		default:
			return new ResponseEntity<>("Error al actualizar el estado", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * Construye la respuesta de una cancelación. Si fue exitosa, el mensaje
	 * indica cuánto se devolverá y hasta qué fecha.
	 *
	 * @param status  código devuelto por el servicio
	 * @param reserva reserva ya cancelada (solo se usa si {@code status} es 0)
	 * @return {@link ResponseEntity} con el mensaje y el código HTTP
	 *         correspondiente
	 */
	public static ResponseEntity<String> cancelar(int status, ReservaDTO reserva) {
		switch (status) {
		case 0:
			if (reserva.getMontoReembolso().signum() > 0) {
				return new ResponseEntity<>("Reserva cancelada. Se devolverán $" + reserva.getMontoReembolso()
						+ " a más tardar el " + reserva.getFechaLimiteReembolso(), HttpStatus.ACCEPTED);
			}
			return new ResponseEntity<>("Reserva cancelada sin reembolso", HttpStatus.ACCEPTED);
		case 1:
			return new ResponseEntity<>("Reserva no encontrada", HttpStatus.NOT_FOUND);
		case 2:
			return new ResponseEntity<>("Solo se pueden cancelar reservas pendientes o confirmadas",
					HttpStatus.CONFLICT);
		case 3:
			return new ResponseEntity<>("La reserva ya comenzó y no se puede cancelar", HttpStatus.CONFLICT);
		default:
			return new ResponseEntity<>("Error al cancelar la reserva", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
