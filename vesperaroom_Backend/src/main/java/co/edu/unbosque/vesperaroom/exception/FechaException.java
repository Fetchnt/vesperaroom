package co.edu.unbosque.vesperaroom.exception;

/**
 * Excepción lanzada cuando una fecha falta o un rango de fechas es inválido.
 */
public class FechaException extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructor que construye el mensaje a partir de los datos recibidos.
	 *
	 * @param detalle descripción del problema con la fecha
	 */
	public FechaException(String detalle) {
		super(detalle);
	}
}
