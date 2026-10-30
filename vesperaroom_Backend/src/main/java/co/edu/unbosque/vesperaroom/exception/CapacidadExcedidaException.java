package co.edu.unbosque.vesperaroom.exception;

/**
 * Excepción lanzada cuando la cantidad de personas supera la capacidad del tipo de habitación.
 */
public class CapacidadExcedidaException extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructor que inicializa la excepción con su mensaje por defecto.
	 */
	public CapacidadExcedidaException() {
		super("La cantidad de personas supera la capacidad de la habitación.");
	}
}
