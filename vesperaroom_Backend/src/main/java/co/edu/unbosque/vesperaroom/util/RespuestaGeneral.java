package co.edu.unbosque.vesperaroom.util;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Clase utilitaria con las respuestas HTTP que se repiten en todos los
 * controladores: listas, búsquedas por id, eliminaciones, cambios de estado,
 * conteos, verificación de existencia y acceso denegado. Mantiene los mismos
 * códigos que {@code UsuarioController}: 202 para consultas exitosas, 204 para
 * listas vacías, 404 si no existe y 409 si hay registros asociados.
 */
public class RespuestaGeneral {

	private RespuestaGeneral() {
	}

	/**
	 * Construye la respuesta estándar para consultas de listas.
	 *
	 * @param <T>   tipo de los elementos de la lista
	 * @param lista resultado de la consulta
	 * @return {@link ResponseEntity} con código 202, o 204 si la lista está vacía
	 */
	public static <T> ResponseEntity<List<T>> lista(List<T> lista) {
		if (lista.isEmpty()) {
			return new ResponseEntity<>(lista, HttpStatus.NO_CONTENT);
		}

		return new ResponseEntity<>(lista, HttpStatus.ACCEPTED);
	}

	/**
	 * Construye la respuesta de una búsqueda por identificador.
	 *
	 * @param encontrado          objeto encontrado, o {@code null} si no existe
	 * @param mensajeNoEncontrado mensaje a devolver si no existe
	 * @return {@link ResponseEntity} con el objeto y código 202, o el mensaje y
	 *         código 404
	 */
	public static ResponseEntity<?> encontrado(Object encontrado, String mensajeNoEncontrado) {
		if (encontrado != null) {
			return new ResponseEntity<>(encontrado, HttpStatus.ACCEPTED);
		}

		return new ResponseEntity<>(mensajeNoEncontrado, HttpStatus.NOT_FOUND);
	}

	/**
	 * Traduce el código de una eliminación (0 éxito, 1 no encontrado, 2 tiene
	 * registros asociados, otro error inesperado).
	 *
	 * @param status   código devuelto por el servicio
	 * @param entidad  nombre de la entidad en minúscula (por ejemplo, "ciudad")
	 * @param femenino {@code true} si el nombre es femenino, para concordar el
	 *                 mensaje
	 * @return {@link ResponseEntity} con el mensaje y el código HTTP
	 *         correspondiente
	 */
	public static ResponseEntity<String> eliminar(int status, String entidad, boolean femenino) {
		String o = femenino ? "a" : "o";
		String nombre = Character.toUpperCase(entidad.charAt(0)) + entidad.substring(1);

		switch (status) {
		case 0:
			return new ResponseEntity<>(nombre + " eliminad" + o + " exitosamente", HttpStatus.ACCEPTED);
		case 1:
			return new ResponseEntity<>(nombre + " no encontrad" + o, HttpStatus.NOT_FOUND);
		case 2:
			return new ResponseEntity<>((femenino ? "La " : "El ") + entidad
					+ " tiene registros asociados y no puede eliminarse", HttpStatus.CONFLICT);
		default:
			return new ResponseEntity<>("Error al eliminar " + (femenino ? "la " : "el ") + entidad,
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * Traduce el código de un cambio de estado simple (0 éxito, 1 estado nulo, 2
	 * no encontrado, otro error inesperado).
	 *
	 * @param status   código devuelto por el servicio
	 * @param entidad  nombre de la entidad en minúscula
	 * @param femenino {@code true} si el nombre es femenino
	 * @param estado   nuevo estado solicitado
	 * @return {@link ResponseEntity} con el mensaje y el código HTTP
	 *         correspondiente
	 */
	public static ResponseEntity<String> estado(int status, String entidad, boolean femenino, Object estado) {
		String nombre = Character.toUpperCase(entidad.charAt(0)) + entidad.substring(1);

		switch (status) {
		case 0:
			return new ResponseEntity<>("Estado actualizado a " + estado, HttpStatus.ACCEPTED);
		case 1:
			return new ResponseEntity<>("Debe indicar el estado", HttpStatus.BAD_REQUEST);
		case 2:
			return new ResponseEntity<>(nombre + " no encontrad" + (femenino ? "a" : "o"), HttpStatus.NOT_FOUND);
		default:
			return new ResponseEntity<>("Error al actualizar el estado", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * Construye la respuesta de un conteo.
	 *
	 * @param total cantidad de registros
	 * @return {@link ResponseEntity} con el conteo y código 202, o 204 si es cero
	 */
	public static ResponseEntity<Long> conteo(long total) {
		if (total == 0) {
			return new ResponseEntity<>(total, HttpStatus.NO_CONTENT);
		}

		return new ResponseEntity<>(total, HttpStatus.ACCEPTED);
	}

	/**
	 * Construye la respuesta de una verificación de existencia.
	 *
	 * @param existe resultado de la verificación
	 * @return {@link ResponseEntity} con {@code true} y código 202, o
	 *         {@code false} y código 404
	 */
	public static ResponseEntity<Boolean> existe(boolean existe) {
		if (existe) {
			return new ResponseEntity<>(true, HttpStatus.ACCEPTED);
		}

		return new ResponseEntity<>(false, HttpStatus.NOT_FOUND);
	}

	/**
	 * Construye la respuesta para un usuario autenticado que intenta operar sobre
	 * un recurso que no le pertenece (por ejemplo, el motel de otro propietario).
	 *
	 * @return {@link ResponseEntity} con código 403
	 */
	public static ResponseEntity<String> prohibido() {
		return new ResponseEntity<>("No tiene permisos sobre este recurso", HttpStatus.FORBIDDEN);
	}
}
