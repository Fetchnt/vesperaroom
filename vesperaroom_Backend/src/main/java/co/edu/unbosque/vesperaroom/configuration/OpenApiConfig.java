package co.edu.unbosque.vesperaroom.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

/**
 * Clase de configuración de la documentación OpenAPI (Swagger UI) de la API,
 * incluyendo el esquema de seguridad Bearer JWT.
 */
@Configuration
public class OpenApiConfig {

	/**
	 * Construye la definición OpenAPI con la información general de la API y el
	 * esquema de autenticación {@code bearerAuth}.
	 *
	 * @return objeto {@link OpenAPI} personalizado
	 */
	@Bean
	public OpenAPI customOpenAPI() {
		String mainDescription = "<h2>API REST de VesperaRoom</h2>"
				+ "<p>Plataforma de reservas de habitaciones de motel. El acceso a datos se realiza con JDBC.</p>"
				+ "<h3>Flujo básico de uso:</h3><ol>"
				+ "<li>Registra un usuario con <code>/auth/register</code> (CLIENTE o PROPIETARIO)</li>"
				+ "<li>Inicia sesión con <code>/auth/login</code> para obtener un token JWT</li>"
				+ "<li>Pulsa <strong>Authorize</strong> y pega el token (sin la palabra Bearer)</li></ol>"
				+ "<h3>Roles:</h3><ul>"
				+ "<li><strong>ADMINISTRADOR</strong>: gestiona usuarios</li>"
				+ "<li><strong>PROPIETARIO</strong>: administra sus moteles</li>"
				+ "<li><strong>CLIENTE</strong>: realiza reservas</li></ul>"
				+ "<h3>Códigos de estado HTTP comunes:</h3><ul>"
				+ "<li><strong>200/201/202</strong>: operación exitosa</li>"
				+ "<li><strong>400</strong>: datos inválidos</li>"
				+ "<li><strong>401</strong>: no autenticado (token inválido o expirado)</li>"
				+ "<li><strong>403</strong>: sin permisos o cuenta inactiva</li>"
				+ "<li><strong>404</strong>: recurso no encontrado</li>"
				+ "<li><strong>409</strong>: conflicto (correo o teléfono duplicado)</li></ul>";

		Info info = new Info().title("VesperaRoom API").version("1.0").description(mainDescription);

		SecurityScheme securityScheme = new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer")
				.bearerFormat("JWT").description("Token JWT obtenido en /auth/login");

		return new OpenAPI().info(info).addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
				.components(new Components().addSecuritySchemes("bearerAuth", securityScheme));
	}
}
