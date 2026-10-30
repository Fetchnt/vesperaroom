package co.edu.unbosque.vesperaroom.util;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Clase utilitaria que traduce los códigos de resultado de
 * {@link co.edu.unbosque.vesperaroom.service.PoliticaReservaService} a respuestas HTTP
 * con mensaje en español. Centraliza el {@code switch} para que todos los
 * endpoints respondan igual ante el mismo error.
 */
public class RespuestaPoliticaReserva {

	private RespuestaPoliticaReserva() {
	}

	/**
	 * Construye la respuesta para las operaciones de creación y actualización de
	 * la política de reserva.
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
			return new ResponseEntity<>("Valores fuera de rango: días hábiles 0-60, porcentaje 0-100, capacidad sin recargo 1-20, tarifa adicional mayor o igual a 0, anticipación 0-720 horas y gracia 0-240 minutos",
					HttpStatus.BAD_REQUEST);
		case 2:
			return new ResponseEntity<>("El motel indicado no existe", HttpStatus.BAD_REQUEST);
		case 3:
			return new ResponseEntity<>("El motel ya tiene una política de reserva", HttpStatus.CONFLICT);
		case 4:
			return new ResponseEntity<>("Política de reserva no encontrada", HttpStatus.NOT_FOUND);
		default:
			return new ResponseEntity<>("Error al procesar la política de reserva", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
