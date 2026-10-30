package co.edu.unbosque.vesperaroom.exception;

/**
 * Excepción lanzada cuando el número de cuenta bancaria no tiene entre 6 y 20 dígitos.
 */
public class CuentaBancariaException extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructor que inicializa la excepción con su mensaje por defecto.
	 */
	public CuentaBancariaException() {
		super("El número de cuenta debe tener entre 6 y 20 dígitos.");
	}
}
