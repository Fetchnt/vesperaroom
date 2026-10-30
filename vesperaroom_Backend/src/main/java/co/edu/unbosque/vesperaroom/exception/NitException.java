package co.edu.unbosque.vesperaroom.exception;

/**
 * Excepción lanzada cuando el NIT no tiene el formato colombiano esperado.
 */
public class NitException extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructor que inicializa la excepción con su mensaje por defecto.
	 */
	public NitException() {
		super("El NIT debe tener 9 o 10 dígitos y, opcionalmente, un guion y el dígito de verificación.");
	}
}
