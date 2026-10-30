package co.edu.unbosque.vesperaroom.exception;

/**
 * Excepción lanzada cuando un registro hace referencia (llave foránea) a otro
 * que no existe. Guarda el nombre de la entidad referenciada para que el
 * servicio pueda devolver un código distinto según cuál referencia falló.
 */
public class ReferenciaNoEncontradaException extends Exception {

	private static final long serialVersionUID = 1L;

	private final String entidad;

	/**
	 * Constructor que construye el mensaje a partir de los datos recibidos.
	 *
	 * @param entidad nombre de la entidad referenciada
	 * @param id      identificador que no fue encontrado
	 */
	public ReferenciaNoEncontradaException(String entidad, Long id) {
		super("No existe " + entidad + " con id " + id + " para asociar.");
		this.entidad = entidad;
	}

	public String getEntidad() {
		return entidad;
	}
}
