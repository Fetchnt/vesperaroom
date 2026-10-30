package co.edu.unbosque.vesperaroom.exception;

/**
 * Excepción lanzada cuando un servicio adicional no existe, está inactivo o no pertenece al motel de la reserva.
 */
public class ServicioNoValidoException extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructor que inicializa la excepción con su mensaje por defecto.
	 */
	public ServicioNoValidoException() {
		super("Uno de los servicios no existe, está inactivo o no pertenece al motel.");
	}
}
