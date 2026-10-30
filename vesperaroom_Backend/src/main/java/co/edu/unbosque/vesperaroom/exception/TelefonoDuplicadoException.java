package co.edu.unbosque.vesperaroom.exception;

/**
 * Excepción lanzada cuando el teléfono ya está registrado.
 */
public class TelefonoDuplicadoException extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructor que inicializa la excepción con su mensaje por defecto.
	 */
	public TelefonoDuplicadoException() {
		super("El teléfono ya está registrado.");
	}
}
