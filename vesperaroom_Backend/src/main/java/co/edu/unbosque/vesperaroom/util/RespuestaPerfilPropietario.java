package co.edu.unbosque.vesperaroom.util;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Clase utilitaria que traduce los códigos de resultado de
 * {@link co.edu.unbosque.vesperaroom.service.PerfilPropietarioService} a respuestas HTTP
 * con mensaje en español. Centraliza el {@code switch} para que todos los
 * endpoints respondan igual ante el mismo error.
 */
public class RespuestaPerfilPropietario {

	private RespuestaPerfilPropietario() {
	}

	/**
	 * Construye la respuesta para las operaciones de creación y actualización de
	 * el perfil de propietario.
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
			return new ResponseEntity<>("El NIT debe tener 9 o 10 dígitos y, opcionalmente, un guion y el dígito de verificación",
					HttpStatus.BAD_REQUEST);
		case 2:
			return new ResponseEntity<>("La razón social es obligatoria y admite máximo 120 caracteres",
					HttpStatus.BAD_REQUEST);
		case 3:
			return new ResponseEntity<>("El banco es obligatorio y admite máximo 60 caracteres",
					HttpStatus.BAD_REQUEST);
		case 4:
			return new ResponseEntity<>("Debe indicar el tipo de cuenta (AHORROS o CORRIENTE)",
					HttpStatus.BAD_REQUEST);
		case 5:
			return new ResponseEntity<>("El número de cuenta debe tener entre 6 y 20 dígitos",
					HttpStatus.BAD_REQUEST);
		case 6:
			return new ResponseEntity<>("El usuario indicado no existe", HttpStatus.BAD_REQUEST);
		case 7:
			return new ResponseEntity<>("El usuario debe tener rol PROPIETARIO", HttpStatus.BAD_REQUEST);
		case 8:
			return new ResponseEntity<>("El NIT ya está registrado", HttpStatus.CONFLICT);
		case 9:
			return new ResponseEntity<>("La cuenta bancaria ya está registrada", HttpStatus.CONFLICT);
		case 10:
			return new ResponseEntity<>("El usuario ya tiene un perfil de propietario", HttpStatus.CONFLICT);
		case 11:
			return new ResponseEntity<>("Perfil de propietario no encontrado", HttpStatus.NOT_FOUND);
		default:
			return new ResponseEntity<>("Error al procesar el perfil de propietario", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * Construye la respuesta de la verificación de un perfil por parte de un administrador.
	 *
	 * @param status código devuelto por el servicio
	 * @return {@link ResponseEntity} con el mensaje y el código HTTP
	 *         correspondiente
	 */
	public static ResponseEntity<String> verificar(int status) {
		switch (status) {
		case 0:
			return new ResponseEntity<>("Perfil verificado exitosamente", HttpStatus.ACCEPTED);
		case 1:
			return new ResponseEntity<>("Perfil de propietario no encontrado", HttpStatus.NOT_FOUND);
		default:
			return new ResponseEntity<>("Error al verificar el perfil", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
