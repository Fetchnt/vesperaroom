package co.edu.unbosque.vesperaroom.exception;

/**
 * Excepción lanzada cuando no se indica el estado de un usuario.
 */
public class EstadoException extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructor que inicializa la excepción con su mensaje por defecto.
	 */
	public EstadoException() {
		super("Debe indicar el estado del usuario.");
	}
}
