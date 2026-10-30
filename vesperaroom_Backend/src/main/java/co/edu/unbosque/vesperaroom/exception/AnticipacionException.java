package co.edu.unbosque.vesperaroom.exception;

/**
 * Excepción lanzada cuando la reserva no respeta la anticipación mínima definida por el motel.
 */
public class AnticipacionException extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructor que inicializa la excepción con su mensaje por defecto.
	 */
	public AnticipacionException() {
		super("La reserva no cumple la anticipación mínima del motel.");
	}
}
