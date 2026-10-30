package co.edu.unbosque.vesperaroom.util;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Clase utilitaria que traduce los códigos de resultado de
 * {@link co.edu.unbosque.vesperaroom.service.ServicioAdicionalService} a respuestas HTTP
 * con mensaje en español. Centraliza el {@code switch} para que todos los
 * endpoints respondan igual ante el mismo error.
 */
public class RespuestaServicioAdicional {

	private RespuestaServicioAdicional() {
	}

	/**
	 * Construye la respuesta para las operaciones de creación y actualización de
	 * el servicio adicional.
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
			return new ResponseEntity<>("El nombre es obligatorio y admite máximo 80 caracteres",
					HttpStatus.BAD_REQUEST);
		case 2:
			return new ResponseEntity<>("La descripción admite máximo 300 caracteres",
					HttpStatus.BAD_REQUEST);
		case 3:
			return new ResponseEntity<>("El precio no puede ser negativo", HttpStatus.BAD_REQUEST);
		case 4:
			return new ResponseEntity<>("El motel indicado no existe", HttpStatus.BAD_REQUEST);
		case 5:
			return new ResponseEntity<>("El motel ya tiene un servicio con ese nombre", HttpStatus.CONFLICT);
		case 6:
			return new ResponseEntity<>("Servicio adicional no encontrado", HttpStatus.NOT_FOUND);
		default:
			return new ResponseEntity<>("Error al procesar el servicio adicional", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
