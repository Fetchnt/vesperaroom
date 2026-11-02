package co.edu.unbosque.vesperaroom.exception;

/**
 * Excepción lanzada cuando se intenta reservar en un motel que todavía no tiene política de reserva.
 */
public class PoliticaNoDefinidaException extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructor que inicializa la excepción con su mensaje por defecto.
	 */
	public PoliticaNoDefinidaException() {
		super("El motel todavía no tiene política de reserva.");
	}
}
