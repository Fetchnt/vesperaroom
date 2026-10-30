package co.edu.unbosque.vesperaroom.util;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Clase utilitaria que traduce los códigos de resultado de
 * {@link co.edu.unbosque.vesperaroom.service.MotelService} a respuestas HTTP
 * con mensaje en español. Centraliza el {@code switch} para que todos los
 * endpoints respondan igual ante el mismo error.
 */
public class RespuestaMotel {

	private RespuestaMotel() {
	}

	/**
	 * Construye la respuesta para las operaciones de creación y actualización de
	 * el motel.
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
			return new ResponseEntity<>("El nombre es obligatorio y admite máximo 100 caracteres",
					HttpStatus.BAD_REQUEST);
		case 2:
			return new ResponseEntity<>("La descripción admite máximo 500 caracteres",
					HttpStatus.BAD_REQUEST);
		case 3:
			return new ResponseEntity<>("La calle y la carrera son obligatorias y admiten máximo 40 caracteres",
					HttpStatus.BAD_REQUEST);
		case 4:
			return new ResponseEntity<>("La latitud debe estar entre -90 y 90 y la longitud entre -180 y 180, y deben indicarse juntas",
					HttpStatus.BAD_REQUEST);
		case 5:
			return new ResponseEntity<>("El propietario debe registrar su perfil antes de crear moteles",
					HttpStatus.BAD_REQUEST);
		case 6:
			return new ResponseEntity<>("La ciudad indicada no existe", HttpStatus.BAD_REQUEST);
		case 7:
			return new ResponseEntity<>("Motel no encontrado", HttpStatus.NOT_FOUND);
		default:
			return new ResponseEntity<>("Error al procesar el motel", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
