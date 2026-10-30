package co.edu.unbosque.vesperaroom.exception;

/**
 * Excepción lanzada cuando una contraseña no cumple las reglas de composición.
 */
public class ContrasenaException extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructor que inicializa la excepción con su mensaje por defecto.
	 */
	public ContrasenaException() {
		super("La contraseña debe contener al menos una mayúscula, una minúscula y un número.");
	}
}
