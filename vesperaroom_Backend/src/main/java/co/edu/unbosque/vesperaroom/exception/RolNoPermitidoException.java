package co.edu.unbosque.vesperaroom.exception;

/**
 * Excepción lanzada cuando se intenta asignar un rol no permitido para la operación.
 */
public class RolNoPermitidoException extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructor que inicializa la excepción con su mensaje por defecto.
	 */
	public RolNoPermitidoException() {
		super("El rol indicado no está permitido para esta operación.");
	}
}
