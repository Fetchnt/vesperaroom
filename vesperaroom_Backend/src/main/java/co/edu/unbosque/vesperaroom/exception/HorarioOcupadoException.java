package co.edu.unbosque.vesperaroom.exception;

/**
 * Excepción lanzada cuando la habitación ya tiene una reserva vigente que se cruza con el horario pedido.
 */
public class HorarioOcupadoException extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructor que inicializa la excepción con su mensaje por defecto.
	 */
	public HorarioOcupadoException() {
		super("La habitación ya está reservada en ese horario.");
	}
}
