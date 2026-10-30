package co.edu.unbosque.vesperaroom.configuration;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * Clase de configuración de la conexión JDBC a MySQL. Construye
 * explícitamente el {@link DataSource} (un pool de conexiones HikariCP) a
 * partir de las propiedades {@code spring.datasource.*}. Los repositorios
 * reciben este {@link DataSource} y piden conexiones con
 * {@link DataSource#getConnection()}.
 */
@Configuration
public class ConexionBD {

	private static final String DRIVER_MYSQL = "com.mysql.cj.jdbc.Driver";

	@Value("${spring.datasource.url}")
	private String url;

	@Value("${spring.datasource.username}")
	private String usuario;

	@Value("${spring.datasource.password}")
	private String contrasena;

	@Value("${app.db.pool-size:10}")
	private int tamanoPool;

	/**
	 * Crea el pool de conexiones a la base de datos. El pool mantiene conexiones
	 * abiertas y las reutiliza: cada {@code getConnection()} toma una prestada y
	 * cada {@code close()} la devuelve al pool en lugar de cerrarla.
	 *
	 * @return {@link DataSource} configurado para MySQL
	 */
	@Bean(destroyMethod = "close")
	public DataSource dataSource() {
		HikariConfig config = new HikariConfig();
		config.setDriverClassName(DRIVER_MYSQL);
		config.setJdbcUrl(url);
		config.setUsername(usuario);
		config.setPassword(contrasena);
		config.setMaximumPoolSize(tamanoPool);
		config.setPoolName("vesperaroom-pool");
		// Si no hay una conexión disponible en 5 segundos, getConnection() lanza SQLException
		config.setConnectionTimeout(5000);
		// No se conecta al arrancar: la primera conexión se abre con la primera consulta
		config.setInitializationFailTimeout(-1);
		return new HikariDataSource(config);
	}
}
