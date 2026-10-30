package co.edu.unbosque.vesperaroom.util;

import java.sql.SQLException;

/**
 * Clase utilitaria que interpreta los códigos de error de MySQL que llegan en
 * una {@link SQLException} ({@link SQLException#getErrorCode()}). Los servicios
 * la usan cuando una restricción de la base de datos detecta un problema que
 * las validaciones previas no alcanzaron a ver (por ejemplo, dos inserciones
 * simultáneas).
 */
public class CodigoMySQL {

	/** Violación de una llave única (PRIMARY KEY o UNIQUE). */
	public static final int LLAVE_DUPLICADA = 1062;

	/** No se puede borrar o modificar una fila porque otras dependen de ella. */
	public static final int TIENE_DEPENDIENTES = 1451;

	/** La llave foránea apunta a una fila que no existe. */
	public static final int REFERENCIA_INEXISTENTE = 1452;

	private CodigoMySQL() {
	}

	/**
	 * Indica si el error corresponde a una llave única duplicada.
	 *
	 * @param e excepción lanzada por el driver JDBC
	 * @return {@code true} si el código de error es 1062
	 */
	public static boolean esLlaveDuplicada(SQLException e) {
		return e.getErrorCode() == LLAVE_DUPLICADA;
	}

	/**
	 * Indica si el error corresponde a una llave foránea que apunta a una fila
	 * inexistente.
	 *
	 * @param e excepción lanzada por el driver JDBC
	 * @return {@code true} si el código de error es 1452
	 */
	public static boolean esReferenciaInexistente(SQLException e) {
		return e.getErrorCode() == REFERENCIA_INEXISTENTE;
	}
}
