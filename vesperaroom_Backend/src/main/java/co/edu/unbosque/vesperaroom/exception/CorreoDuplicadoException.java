package co.edu.unbosque.vesperaroom.exception;

/**
 * Excepción lanzada cuando el correo electrónico ya está registrado.
 */
public class CorreoDuplicadoException extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructor que inicializa la excepción con su mensaje por defecto.
	 */
	public CorreoDuplicadoException() {
		super("El correo ya está registrado.");
	}
}
