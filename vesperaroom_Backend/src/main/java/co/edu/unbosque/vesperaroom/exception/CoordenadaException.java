package co.edu.unbosque.vesperaroom.exception;

/**
 * Excepción lanzada cuando la latitud o la longitud están fuera de rango o solo se indica una de ellas.
 */
public class CoordenadaException extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructor que inicializa la excepción con su mensaje por defecto.
	 */
	public CoordenadaException() {
		super("La latitud debe estar entre -90 y 90 y la longitud entre -180 y 180, y deben indicarse juntas.");
	}
}
