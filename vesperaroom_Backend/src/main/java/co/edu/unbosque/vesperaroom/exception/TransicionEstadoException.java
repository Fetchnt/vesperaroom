package co.edu.unbosque.vesperaroom.exception;

/**
 * Excepción lanzada cuando se intenta un cambio de estado que el ciclo de vida no permite.
 */
public class TransicionEstadoException extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructor que inicializa la excepción con su mensaje por defecto.
	 */
	public TransicionEstadoException() {
		super("El cambio de estado solicitado no está permitido.");
	}
}
