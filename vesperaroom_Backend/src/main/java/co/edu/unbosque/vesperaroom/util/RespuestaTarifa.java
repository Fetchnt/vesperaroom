package co.edu.unbosque.vesperaroom.util;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Clase utilitaria que traduce los códigos de resultado de
 * {@link co.edu.unbosque.vesperaroom.service.TarifaService} a respuestas HTTP
 * con mensaje en español. Centraliza el {@code switch} para que todos los
 * endpoints respondan igual ante el mismo error.
 */
public class RespuestaTarifa {

	private RespuestaTarifa() {
	}

	/**
	 * Construye la respuesta para las operaciones de creación y actualización de
	 * la tarifa.
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
			return new ResponseEntity<>("El precio debe ser mayor que cero", HttpStatus.BAD_REQUEST);
		case 2:
			return new ResponseEntity<>("Debe indicar el tipo de día", HttpStatus.BAD_REQUEST);
		case 3:
			return new ResponseEntity<>("La vigencia debe tener fecha inicial y la fecha final no puede ser anterior",
					HttpStatus.BAD_REQUEST);
		case 4:
			return new ResponseEntity<>("El tipo de habitación indicado no existe", HttpStatus.BAD_REQUEST);
		case 5:
			return new ResponseEntity<>("La modalidad indicada no existe", HttpStatus.BAD_REQUEST);
		case 6:
			return new ResponseEntity<>("Ya existe una tarifa para ese tipo, modalidad, tipo de día y fecha de inicio",
					HttpStatus.CONFLICT);
		case 7:
			return new ResponseEntity<>("Tarifa no encontrada", HttpStatus.NOT_FOUND);
		default:
			return new ResponseEntity<>("Error al procesar la tarifa", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
