package co.edu.unbosque.vesperaroom.exception;

/**
 * Excepción lanzada cuando un texto contiene caracteres no permitidos.
 */
public class InvalidCharException extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructor que inicializa la excepción con su mensaje por defecto.
	 */
	public InvalidCharException() {
		super("El texto solo puede contener letras y espacios.");
	}
}
