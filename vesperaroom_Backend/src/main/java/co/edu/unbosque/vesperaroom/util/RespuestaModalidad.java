package co.edu.unbosque.vesperaroom.util;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Clase utilitaria que traduce los códigos de resultado de
 * {@link co.edu.unbosque.vesperaroom.service.ModalidadService} a respuestas HTTP
 * con mensaje en español. Centraliza el {@code switch} para que todos los
 * endpoints respondan igual ante el mismo error.
 */
public class RespuestaModalidad {

	private RespuestaModalidad() {
	}

	/**
	 * Construye la respuesta para las operaciones de creación y actualización de
	 * la modalidad.
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
			return new ResponseEntity<>("El nombre es obligatorio y admite máximo 40 caracteres",
					HttpStatus.BAD_REQUEST);
		case 2:
			return new ResponseEntity<>("La duración debe estar entre 1 y 72 horas", HttpStatus.BAD_REQUEST);
		case 3:
			return new ResponseEntity<>("Ya existe una modalidad con ese nombre", HttpStatus.CONFLICT);
		case 4:
			return new ResponseEntity<>("Modalidad no encontrada", HttpStatus.NOT_FOUND);
		default:
			return new ResponseEntity<>("Error al procesar la modalidad", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
