package co.edu.unbosque.vesperaroom.util;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Clase utilitaria que traduce los códigos de resultado de
 * {@link co.edu.unbosque.vesperaroom.service.CiudadService} a respuestas HTTP
 * con mensaje en español. Centraliza el {@code switch} para que todos los
 * endpoints respondan igual ante el mismo error.
 */
public class RespuestaCiudad {

	private RespuestaCiudad() {
	}

	/**
	 * Construye la respuesta para las operaciones de creación y actualización de
	 * la ciudad.
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
			return new ResponseEntity<>("El departamento indicado no existe", HttpStatus.BAD_REQUEST);
		case 3:
			return new ResponseEntity<>("Ya existe una ciudad con ese nombre en el departamento",
					HttpStatus.CONFLICT);
		case 4:
			return new ResponseEntity<>("Ciudad no encontrada", HttpStatus.NOT_FOUND);
		default:
			return new ResponseEntity<>("Error al procesar la ciudad", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
