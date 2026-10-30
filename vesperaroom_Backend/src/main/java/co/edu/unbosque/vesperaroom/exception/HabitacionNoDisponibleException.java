package co.edu.unbosque.vesperaroom.exception;

/**
 * Excepción lanzada cuando la habitación no está activa o su motel está inactivo.
 */
public class HabitacionNoDisponibleException extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructor que inicializa la excepción con su mensaje por defecto.
	 */
	public HabitacionNoDisponibleException() {
		super("La habitación no está disponible para reservas.");
	}
}
