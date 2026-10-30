package co.edu.unbosque.vesperaroom.exception;

/**
 * Excepción lanzada cuando se intenta crear un perfil de propietario para un usuario sin rol PROPIETARIO.
 */
public class RolPropietarioException extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructor que inicializa la excepción con su mensaje por defecto.
	 */
	public RolPropietarioException() {
		super("El usuario debe tener rol PROPIETARIO.");
	}
}
