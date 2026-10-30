package co.edu.unbosque.vesperaroom.util;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Clase utilitaria que traduce los códigos de resultado de
 * {@link co.edu.unbosque.vesperaroom.service.HabitacionService} a respuestas HTTP
 * con mensaje en español. Centraliza el {@code switch} para que todos los
 * endpoints respondan igual ante el mismo error.
 */
public class RespuestaHabitacion {

	private RespuestaHabitacion() {
	}

	/**
	 * Construye la respuesta para las operaciones de creación y actualización de
	 * la habitación.
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
			return new ResponseEntity<>("El número es obligatorio y admite máximo 10 caracteres",
					HttpStatus.BAD_REQUEST);
		case 2:
			return new ResponseEntity<>("El piso debe estar entre -5 y 200", HttpStatus.BAD_REQUEST);
		case 3:
			return new ResponseEntity<>("El tipo de habitación indicado no existe", HttpStatus.BAD_REQUEST);
		case 4:
			return new ResponseEntity<>("El motel ya tiene una habitación con ese número",
					HttpStatus.CONFLICT);
		case 5:
			return new ResponseEntity<>("Habitación no encontrada", HttpStatus.NOT_FOUND);
		default:
			return new ResponseEntity<>("Error al procesar la habitación", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
