package co.edu.unbosque.vesperaroom.util;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Clase utilitaria que traduce los códigos de resultado de
 * {@link co.edu.unbosque.vesperaroom.service.UsuarioService} a respuestas HTTP
 * con mensaje en español. Centraliza el {@code switch} para que todos los
 * endpoints de usuario respondan igual ante el mismo error.
 */
public class RespuestaUsuario {

	private RespuestaUsuario() {
	}

	/**
	 * Construye la respuesta para las operaciones de creación, registro y
	 * actualización de usuarios.
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
			return new ResponseEntity<>("El nombre y el apellido solo pueden contener letras y espacios",
					HttpStatus.BAD_REQUEST);
		case 2:
			return new ResponseEntity<>("El correo electrónico no tiene un formato válido", HttpStatus.BAD_REQUEST);
		case 3:
			return new ResponseEntity<>("El teléfono debe tener entre 7 y 15 dígitos", HttpStatus.BAD_REQUEST);
		case 4:
			return new ResponseEntity<>("La contraseña debe tener al menos 8 caracteres", HttpStatus.BAD_REQUEST);
		case 5:
			return new ResponseEntity<>("La contraseña debe contener al menos una mayúscula, una minúscula y un número",
					HttpStatus.BAD_REQUEST);
		case 6:
			return new ResponseEntity<>("Debe indicar el rol del usuario", HttpStatus.BAD_REQUEST);
		case 7:
			return new ResponseEntity<>("Solo puede registrarse como CLIENTE o PROPIETARIO", HttpStatus.FORBIDDEN);
		case 8:
			return new ResponseEntity<>("El correo ya está registrado", HttpStatus.CONFLICT);
		case 9:
			return new ResponseEntity<>("El teléfono ya está registrado", HttpStatus.CONFLICT);
		case 10:
			return new ResponseEntity<>("Usuario no encontrado", HttpStatus.NOT_FOUND);
		default:
			return new ResponseEntity<>("Error al procesar el usuario", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
