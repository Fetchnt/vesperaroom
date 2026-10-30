package co.edu.unbosque.vesperaroom.util;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Clase utilitaria que traduce los códigos de resultado de
 * {@link co.edu.unbosque.vesperaroom.service.ResenaService} a respuestas HTTP
 * con mensaje en español. Centraliza el {@code switch} para que todos los
 * endpoints respondan igual ante el mismo error.
 */
public class RespuestaResena {

	private RespuestaResena() {
	}

	/**
	 * Construye la respuesta para las operaciones de creación y actualización de
	 * la reseña.
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
			return new ResponseEntity<>("La calificación debe estar entre 1 y 5", HttpStatus.BAD_REQUEST);
		case 2:
			return new ResponseEntity<>("El comentario admite máximo 1000 caracteres",
					HttpStatus.BAD_REQUEST);
		case 3:
			return new ResponseEntity<>("La reserva indicada no existe", HttpStatus.BAD_REQUEST);
		case 4:
			return new ResponseEntity<>("La reserva no pertenece al usuario", HttpStatus.FORBIDDEN);
		case 5:
			return new ResponseEntity<>("Solo se pueden reseñar reservas finalizadas", HttpStatus.CONFLICT);
		case 6:
			return new ResponseEntity<>("La reserva ya tiene una reseña", HttpStatus.CONFLICT);
		case 7:
			return new ResponseEntity<>("Reseña no encontrada", HttpStatus.NOT_FOUND);
		default:
			return new ResponseEntity<>("Error al procesar la reseña", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
