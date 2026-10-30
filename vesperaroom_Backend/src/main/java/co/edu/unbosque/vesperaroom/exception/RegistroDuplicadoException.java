package co.edu.unbosque.vesperaroom.exception;

/**
 * Excepción lanzada cuando se intenta guardar un dato que debe ser único y ya
 * existe. Guarda la descripción del dato repetido para que el servicio pueda
 * devolver un código distinto según cuál restricción se violó.
 */
public class RegistroDuplicadoException extends Exception {

	private static final long serialVersionUID = 1L;

	private final String detalle;

	/**
	 * Constructor que construye el mensaje a partir de los datos recibidos.
	 *
	 * @param detalle descripción del dato repetido
	 */
	public RegistroDuplicadoException(String detalle) {
		super("Ya existe un registro con " + detalle + ".");
		this.detalle = detalle;
	}

	public String getDetalle() {
		return detalle;
	}
}
