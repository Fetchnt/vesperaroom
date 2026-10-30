package co.edu.unbosque.vesperaroom.exception;

/**
 * Excepción lanzada cuando un texto obligatorio está vacío o un texto supera su longitud máxima. Guarda el
 * nombre del campo para que el servicio sepa cuál texto falló.
 */
public class TextoInvalidoException extends Exception {

	private static final long serialVersionUID = 1L;

	private final String campo;

	/**
	 * Constructor que construye el mensaje a partir del campo validado.
	 *
	 * @param campo nombre del campo validado
	 */
	public TextoInvalidoException(String campo) {
		super("El campo " + campo + " está vacío o supera la longitud permitida.");
		this.campo = campo;
	}

	public String getCampo() {
		return campo;
	}
}
