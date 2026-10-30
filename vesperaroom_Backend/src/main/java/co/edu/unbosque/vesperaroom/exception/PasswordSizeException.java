package co.edu.unbosque.vesperaroom.exception;

/**
 * Excepción lanzada cuando una contraseña no alcanza el tamaño mínimo.
 */
public class PasswordSizeException extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructor que inicializa la excepción con su mensaje por defecto.
	 */
	public PasswordSizeException() {
		super("La contraseña debe tener al menos 8 caracteres.");
	}
}
