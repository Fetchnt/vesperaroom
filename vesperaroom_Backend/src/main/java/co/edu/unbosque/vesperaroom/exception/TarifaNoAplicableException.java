package co.edu.unbosque.vesperaroom.exception;

/**
 * Excepción lanzada cuando la tarifa elegida no corresponde al tipo de la habitación.
 */
public class TarifaNoAplicableException extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructor que inicializa la excepción con su mensaje por defecto.
	 */
	public TarifaNoAplicableException() {
		super("La tarifa no corresponde al tipo de la habitación.");
	}
}
