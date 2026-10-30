package co.edu.unbosque.vesperaroom.exception;

/**
 * Excepción lanzada cuando un pago supera el saldo pendiente de la reserva.
 */
public class MontoExcedidoException extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructor que inicializa la excepción con su mensaje por defecto.
	 */
	public MontoExcedidoException() {
		super("El monto supera el saldo pendiente de la reserva.");
	}
}
