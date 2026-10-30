package co.edu.unbosque.vesperaroom.exception;

/**
 * Excepción lanzada cuando la reserva no está en un estado que permita la operación.
 */
public class EstadoReservaException extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructor que construye el mensaje a partir de los datos recibidos.
	 *
	 * @param detalle operación que no se permite
	 */
	public EstadoReservaException(String detalle) {
		super("El estado actual de la reserva no permite " + detalle + ".");
	}
}
