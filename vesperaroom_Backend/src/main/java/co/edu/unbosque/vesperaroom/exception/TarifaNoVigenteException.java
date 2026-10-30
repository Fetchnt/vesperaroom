package co.edu.unbosque.vesperaroom.exception;

/**
 * Excepción lanzada cuando la tarifa no está vigente en la fecha de la reserva.
 */
public class TarifaNoVigenteException extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructor que inicializa la excepción con su mensaje por defecto.
	 */
	public TarifaNoVigenteException() {
		super("La tarifa no está vigente para la fecha de la reserva.");
	}
}
