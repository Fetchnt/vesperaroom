package co.edu.unbosque.vesperaroom.exception;

/**
 * Excepción lanzada cuando no se indica un campo obligatorio. Guarda el nombre del campo
 * para que el servicio sepa cuál falta.
 */
public class CampoObligatorioException extends Exception {

	private static final long serialVersionUID = 1L;

	private final String campo;

	/**
	 * Constructor que construye el mensaje a partir del campo validado.
	 *
	 * @param campo nombre del campo validado
	 */
	public CampoObligatorioException(String campo) {
		super("Debe indicar " + campo + ".");
		this.campo = campo;
	}

	public String getCampo() {
		return campo;
	}
}
