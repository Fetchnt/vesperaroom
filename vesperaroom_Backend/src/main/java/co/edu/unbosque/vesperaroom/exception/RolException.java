package co.edu.unbosque.vesperaroom.exception;

/**
 * Excepción lanzada cuando no se indica el rol de un usuario.
 */
public class RolException extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructor que inicializa la excepción con su mensaje por defecto.
	 */
	public RolException() {
		super("Debe indicar el rol del usuario.");
	}
}
