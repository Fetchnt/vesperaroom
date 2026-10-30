package co.edu.unbosque.vesperaroom.util;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Clase utilitaria que traduce los códigos de resultado de
 * {@link co.edu.unbosque.vesperaroom.service.TipoHabitacionService} a respuestas HTTP
 * con mensaje en español. Centraliza el {@code switch} para que todos los
 * endpoints respondan igual ante el mismo error.
 */
public class RespuestaTipoHabitacion {

	private RespuestaTipoHabitacion() {
	}

	/**
	 * Construye la respuesta para las operaciones de creación y actualización de
	 * el tipo de habitación.
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
			return new ResponseEntity<>("El nombre es obligatorio y admite máximo 60 caracteres",
					HttpStatus.BAD_REQUEST);
		case 2:
			return new ResponseEntity<>("La descripción admite máximo 500 caracteres",
					HttpStatus.BAD_REQUEST);
		case 3:
			return new ResponseEntity<>("La capacidad debe estar entre 1 y 20 personas",
					HttpStatus.BAD_REQUEST);
		case 4:
			return new ResponseEntity<>("El motel indicado no existe", HttpStatus.BAD_REQUEST);
		case 5:
			return new ResponseEntity<>("El motel ya tiene un tipo de habitación con ese nombre",
					HttpStatus.CONFLICT);
		case 6:
			return new ResponseEntity<>("Tipo de habitación no encontrado", HttpStatus.NOT_FOUND);
		default:
			return new ResponseEntity<>("Error al procesar el tipo de habitación", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * Construye la respuesta de la asignación de una característica a un tipo de habitación.
	 *
	 * @param status código devuelto por el servicio
	 * @return {@link ResponseEntity} con el mensaje y el código HTTP
	 *         correspondiente
	 */
	public static ResponseEntity<String> asignar(int status) {
		switch (status) {
		case 0:
			return new ResponseEntity<>("Característica asignada exitosamente", HttpStatus.ACCEPTED);
		case 1:
			return new ResponseEntity<>("Tipo de habitación no encontrado", HttpStatus.NOT_FOUND);
		case 2:
			return new ResponseEntity<>("La característica indicada no existe", HttpStatus.NOT_FOUND);
		case 3:
			return new ResponseEntity<>("La característica ya está asignada a este tipo",
					HttpStatus.CONFLICT);
		default:
			return new ResponseEntity<>("Error al asignar la característica", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * Construye la respuesta de la eliminación de una característica de un tipo de habitación.
	 *
	 * @param status código devuelto por el servicio
	 * @return {@link ResponseEntity} con el mensaje y el código HTTP
	 *         correspondiente
	 */
	public static ResponseEntity<String> quitar(int status) {
		switch (status) {
		case 0:
			return new ResponseEntity<>("Característica quitada exitosamente", HttpStatus.ACCEPTED);
		case 1:
			return new ResponseEntity<>("La característica no estaba asignada a este tipo",
					HttpStatus.NOT_FOUND);
		default:
			return new ResponseEntity<>("Error al quitar la característica", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
