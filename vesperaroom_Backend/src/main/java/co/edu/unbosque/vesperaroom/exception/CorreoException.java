package co.edu.unbosque.vesperaroom.exception;

/**
 * Excepción lanzada cuando un correo electrónico no tiene un formato válido.
 */
public class CorreoException extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructor que inicializa la excepción con su mensaje por defecto.
	 */
	public CorreoException() {
		super("El correo electrónico no tiene un formato válido.");
	}
}
