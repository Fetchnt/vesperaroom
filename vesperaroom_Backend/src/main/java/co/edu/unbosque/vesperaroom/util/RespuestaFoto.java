package co.edu.unbosque.vesperaroom.util;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Clase utilitaria que traduce los códigos de resultado de
 * {@link co.edu.unbosque.vesperaroom.service.FotoService} a respuestas HTTP
 * con mensaje en español. Centraliza el {@code switch} para que todos los
 * endpoints respondan igual ante el mismo error.
 */
public class RespuestaFoto {

	private RespuestaFoto() {
	}

	/**
	 * Construye la respuesta para las operaciones de creación y actualización de
	 * la foto.
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
			return new ResponseEntity<>("La foto debe pertenecer a un motel o a una habitación, no a ambos ni a ninguno",
					HttpStatus.BAD_REQUEST);
		case 2:
			return new ResponseEntity<>("La URI debe empezar por http:// o https:// y tener máximo 500 caracteres",
					HttpStatus.BAD_REQUEST);
		case 3:
			return new ResponseEntity<>("El orden debe estar entre 1 y 50", HttpStatus.BAD_REQUEST);
		case 4:
			return new ResponseEntity<>("El motel indicado no existe", HttpStatus.BAD_REQUEST);
		case 5:
			return new ResponseEntity<>("La habitación indicada no existe", HttpStatus.BAD_REQUEST);
		case 6:
			return new ResponseEntity<>("Ya hay otra foto en esa posición", HttpStatus.CONFLICT);
		case 7:
			return new ResponseEntity<>("Foto no encontrada", HttpStatus.NOT_FOUND);
		default:
			return new ResponseEntity<>("Error al procesar la foto", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
