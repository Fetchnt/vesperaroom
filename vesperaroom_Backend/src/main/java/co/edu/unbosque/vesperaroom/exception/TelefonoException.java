package co.edu.unbosque.vesperaroom.exception;

/**
 * Excepción lanzada cuando un teléfono no tiene entre 7 y 15 dígitos numéricos.
 */
public class TelefonoException extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructor que inicializa la excepción con su mensaje por defecto.
	 */
	public TelefonoException() {
		super("El teléfono debe tener entre 7 y 15 dígitos.");
	}
}
