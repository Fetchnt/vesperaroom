package co.edu.unbosque.vesperaroom.exception;

/**
 * Excepción lanzada cuando la dirección de una foto no es una URL http o https válida.
 */
public class UriException extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructor que inicializa la excepción con su mensaje por defecto.
	 */
	public UriException() {
		super("La URI debe empezar por http:// o https:// y tener máximo 500 caracteres.");
	}
}
