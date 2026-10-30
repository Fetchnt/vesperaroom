package co.edu.unbosque.vesperaroom.exception;

/**
 * Excepción lanzada cuando un cliente opera sobre una reserva que no es suya.
 */
public class PropiedadReservaException extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructor que inicializa la excepción con su mensaje por defecto.
	 */
	public PropiedadReservaException() {
		super("La reserva no pertenece al usuario.");
	}
}
