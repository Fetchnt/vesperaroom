package co.edu.unbosque.vesperaroom.exception;

/**
 * Excepción no verificada que envuelve una {@link java.sql.SQLException} en las
 * consultas de lectura, cuyos métodos no pueden devolver un código de error
 * (por ejemplo, {@code getAll()} retorna una lista). Al propagarse, Spring
 * responde con el código HTTP 500.
 */
public class AccesoDatosException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructor que conserva la causa original del error de base de datos.
	 *
	 * @param causa excepción JDBC que originó el error
	 */
	public AccesoDatosException(Throwable causa) {
		super("Error al consultar la base de datos.", causa);
	}
}
