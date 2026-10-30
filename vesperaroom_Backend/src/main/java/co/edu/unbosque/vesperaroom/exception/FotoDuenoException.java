package co.edu.unbosque.vesperaroom.exception;

/**
 * Excepción lanzada cuando una foto no indica exactamente un dueño (motel o habitación).
 */
public class FotoDuenoException extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructor que inicializa la excepción con su mensaje por defecto.
	 */
	public FotoDuenoException() {
		super("La foto debe pertenecer a un motel o a una habitación, no a ambos ni a ninguno.");
	}
}
