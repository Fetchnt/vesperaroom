package co.edu.unbosque.vesperaroom.exception;

/**
 * Excepción lanzada cuando no existe un registro con el identificador indicado.
 */
public class RegistroNoEncontradoException extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructor que construye el mensaje a partir de la entidad y el identificador buscado.
	 *
	 * @param entidad nombre de la entidad consultada
	 * @param id      identificador que no fue encontrado
	 */
	public RegistroNoEncontradoException(String entidad, Long id) {
		super("No existe " + entidad + " con id " + id + ".");
	}
}
