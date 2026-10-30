package co.edu.unbosque.vesperaroom.configuration;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import co.edu.unbosque.vesperaroom.entity.Usuario;
import co.edu.unbosque.vesperaroom.repository.UsuarioRepository;

/**
 * Clase de configuración que precarga el usuario administrador al arrancar la
 * aplicación si aún no existe. Las credenciales se leen de las propiedades
 * {@code app.admin.*}, nunca se escriben en el código.
 */
@Configuration
public class LoadDataBase {

	private static final Logger Log = LoggerFactory.getLogger(LoadDataBase.class);

	@Value("${app.admin.correo}")
	private String correoAdmin;

	@Value("${app.admin.telefono}")
	private String telefonoAdmin;

	@Value("${app.admin.contrasena:}")
	private String contrasenaAdmin;

	/**
	 * Crea el administrador por defecto si no existe un usuario con su correo. Si
	 * la contraseña no está configurada, omite la creación.
	 *
	 * @param usuarioRepo repositorio de usuarios
	 * @param encoder     codificador de contraseñas
	 * @return {@link CommandLineRunner} que ejecuta la precarga
	 */
	@Bean
	CommandLineRunner initDataBase(UsuarioRepository usuarioRepo, PasswordEncoder encoder) {
		return args -> {
			if (contrasenaAdmin == null || contrasenaAdmin.isBlank()) {
				Log.warn("ADMIN_PASSWORD no está configurada, se omite la precarga del administrador");
				return;
			}

			if (usuarioRepo.existsByCorreo(correoAdmin)) {
				Log.info("El administrador ya existe, omitiendo creación...");
				return;
			}

			Usuario admin = new Usuario("Administrador", "VesperaRoom", correoAdmin, telefonoAdmin,
					encoder.encode(contrasenaAdmin), Usuario.Rol.ADMINISTRADOR);
			admin.setFechaRegistro(LocalDateTime.now());

			usuarioRepo.insertar(admin);
			Log.info("Precargado administrador con éxito");
		};
	}
}
