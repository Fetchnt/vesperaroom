package co.edu.unbosque.vesperaroom.exception;

/**
 * Excepción lanzada cuando un valor numérico está fuera del rango permitido. Guarda el
 * nombre del campo para que el servicio sepa cuál valor falló.
 */
public class ValorFueraDeRangoException extends Exception {

	private static final long serialVersionUID = 1L;

	private final String campo;

	/**
	 * Constructor que construye el mensaje a partir del campo validado.
	 *
	 * @param campo nombre del campo validado
	 */
	public ValorFueraDeRangoException(String campo) {
		super("El valor de " + campo + " está fuera del rango permitido.");
		this.campo = campo;
	}

	public String getCampo() {
		return campo;
	}
}
