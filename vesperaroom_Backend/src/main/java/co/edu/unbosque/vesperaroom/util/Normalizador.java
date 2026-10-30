package co.edu.unbosque.vesperaroom.util;

/**
 * Clase utilitaria que limpia los textos recibidos antes de validarlos y
 * guardarlos, para evitar duplicados por espacios sobrantes.
 */
public class Normalizador {

	private Normalizador() {
	}

	/**
	 * Elimina los espacios al inicio y al final de un texto obligatorio.
	 *
	 * @param valor texto a limpiar
	 * @return texto sin espacios sobrantes, o {@code null} si era nulo
	 */
	public static String texto(String valor) {
		return valor == null ? null : valor.trim();
	}

	/**
	 * Limpia un texto opcional: elimina los espacios sobrantes y convierte el texto
	 * vacío en {@code null}, para guardar {@code NULL} en la base de datos.
	 *
	 * @param valor texto a limpiar
	 * @return texto sin espacios sobrantes, o {@code null} si era nulo o vacío
	 */
	public static String textoOpcional(String valor) {
		if (valor == null || valor.isBlank()) {
			return null;
		}
		return valor.trim();
	}
}
