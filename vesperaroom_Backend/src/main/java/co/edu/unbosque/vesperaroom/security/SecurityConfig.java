package co.edu.unbosque.vesperaroom.security;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jakarta.servlet.http.HttpServletResponse;

/**
 * Clase de configuración de seguridad de la aplicación. Define la cadena de
 * filtros, las reglas de autorización por endpoint y rol, la política de
 * sesiones sin estado, las respuestas 401/403, la configuración CORS y los
 * beans necesarios para la autenticación mediante JWT.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

	private static final String ADMIN = "ADMINISTRADOR";
	private static final String PROPIETARIO = "PROPIETARIO";
	private static final String[] ROLES_TODOS = { ADMIN, "CLIENTE", PROPIETARIO };

	/**
	 * Consultas GET que no requieren token. En los patrones, {@code *} equivale a
	 * un segmento de la ruta y {@code **} a cualquier cantidad de segmentos.
	 */
	private static final String[] CATALOGO_PUBLICO = { "/departamento/**", "/ciudad/**", "/caracteristica/**",
			"/modalidad/**", "/motel/getall", "/motel/resumen", "/motel/getbyciudad/*", "/motel/getbyid/*", "/politica/getbyid/*",
			"/tipohabitacion/getbymotel/*", "/tipohabitacion/getbyid/*", "/tipohabitacion/caracteristicas/*",
			"/habitacion/getbymotel/*", "/habitacion/getbytipo/*", "/habitacion/getbyid/*", "/habitacion/disponibles",
			"/foto/getbymotel/*", "/foto/getbyhabitacion/*", "/foto/getbyid/*", "/tarifa/getbytipo/*",
			"/tarifa/getbyid/*", "/servicio/getbymotel/*", "/servicio/getbyid/*", "/resena/getbymotel/*",
			"/resena/promedio/*", "/resena/getbyid/*", "/respuesta/getbyid/*" };

	/** Listados globales y operaciones que solo puede hacer el administrador. */
	private static final String[] SOLO_ADMIN = { "/motel/getall/todos", "/politica/getall", "/tipohabitacion/getall",
			"/habitacion/getall", "/foto/getall", "/tarifa/getall", "/servicio/getall", "/respuesta/getall",
			"/reserva/getall", "/reserva/deletebyid/*", "/reserva/count", "/pago/getall", "/pago/count",
			"/resena/getall" };

	private final JwtAuthenticationFilter jwtAuthFilter;
	private final UserDetailsService userDetailsService;
	private final List<String> allowedOrigins;

	public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter, UserDetailsService userDetailsService,
			@Value("${app.cors.allowed-origins}") String allowedOrigins) {
		this.jwtAuthFilter = jwtAuthFilter;
		this.userDetailsService = userDetailsService;
		this.allowedOrigins = Arrays.stream(allowedOrigins.split(",")).map(String::trim).filter(o -> !o.isEmpty())
				.toList();
	}

	/**
	 * Configura la cadena de filtros de seguridad: deshabilita CSRF (API sin
	 * estado), aplica CORS, define los endpoints públicos y los protegidos por rol,
	 * establece sesiones sin estado, personaliza las respuestas 401 y 403 y
	 * registra el filtro JWT.
	 *
	 * <p>
	 * Las reglas se evalúan en orden y gana la primera que coincide, por eso las
	 * rutas específicas van antes que las generales. Estas reglas solo filtran por
	 * rol; que un propietario modifique únicamente sus propios moteles lo verifica
	 * {@code AutorizacionService} en cada controlador.
	 * </p>
	 *
	 * @param http objeto de configuración de seguridad HTTP
	 * @return {@link SecurityFilterChain} con la configuración aplicada
	 * @throws Exception si ocurre un error durante la configuración
	 */
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.csrf(csrf -> csrf.disable()).cors(cors -> cors.configurationSource(corsConfigurationSource()))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
						.requestMatchers("/error").permitAll()
						.requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
						.requestMatchers(HttpMethod.POST, "/auth/login", "/auth/register").permitAll()
						// Catálogo público: consultas que un visitante necesita para buscar y reservar
						.requestMatchers(HttpMethod.GET, CATALOGO_PUBLICO).permitAll()
						.requestMatchers("/usuario/mi-cuenta").hasAnyRole(ROLES_TODOS)
						.requestMatchers("/usuario/**").hasRole(ADMIN)
						.requestMatchers("/departamento/**", "/ciudad/**", "/caracteristica/**", "/modalidad/**")
						.hasRole(ADMIN)
						.requestMatchers("/perfilpropietario/mi-perfil").hasRole(PROPIETARIO)
						.requestMatchers("/perfilpropietario/createjson").hasAnyRole(PROPIETARIO, ADMIN)
						.requestMatchers("/perfilpropietario/**").hasRole(ADMIN)
						.requestMatchers(SOLO_ADMIN).hasRole(ADMIN)
						.requestMatchers("/motel/**", "/politica/**", "/tipohabitacion/**", "/habitacion/**",
								"/foto/**", "/tarifa/**", "/servicio/**", "/respuesta/**")
						.hasAnyRole(PROPIETARIO, ADMIN)
						.requestMatchers("/reserva/**", "/pago/**", "/resena/**").hasAnyRole(ROLES_TODOS)
						.anyRequest().authenticated())
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.exceptionHandling(ex -> ex
						.authenticationEntryPoint((request, response, e) -> escribirError(response,
								HttpStatus.UNAUTHORIZED, "No autenticado: token ausente, inválido o expirado"))
						.accessDeniedHandler((request, response, e) -> escribirError(response, HttpStatus.FORBIDDEN,
								"No tiene permisos para realizar esta operación")))
				.authenticationProvider(authenticationProvider())
				.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	/**
	 * Configura la política CORS con los orígenes definidos en
	 * {@code app.cors.allowed-origins}, los métodos HTTP estándar y las cabeceras
	 * necesarias para enviar el token.
	 *
	 * @return {@link CorsConfigurationSource} con la política CORS definida
	 */
	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowedOrigins(allowedOrigins);
		config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
		config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
		config.setAllowCredentials(true);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);
		return source;
	}

	/**
	 * Crea el proveedor de autenticación basado en {@link UserDetailsService} y el
	 * codificador BCrypt.
	 *
	 * @return {@link AuthenticationProvider} configurado para autenticación por
	 *         base de datos
	 */
	@Bean
	public AuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
		authProvider.setPasswordEncoder(passwordEncoder());
		return authProvider;
	}

	/**
	 * Expone el {@link AuthenticationManager} como bean para inyectarlo en el
	 * controlador de autenticación.
	 *
	 * @param config configuración de autenticación de Spring Security
	 * @return {@link AuthenticationManager} obtenido desde la configuración
	 * @throws Exception si ocurre un error al obtenerlo
	 */
	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}

	/**
	 * Crea el codificador de contraseñas BCrypt.
	 *
	 * @return {@link PasswordEncoder} basado en BCrypt
	 */
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	/**
	 * Escribe una respuesta de error en texto plano con el código HTTP indicado.
	 *
	 * @param response respuesta HTTP
	 * @param status   código HTTP a devolver
	 * @param mensaje  mensaje en español para el cliente
	 * @throws java.io.IOException si falla la escritura de la respuesta
	 */
	private void escribirError(HttpServletResponse response, HttpStatus status, String mensaje)
			throws java.io.IOException {
		response.setStatus(status.value());
		response.setContentType("text/plain;charset=UTF-8");
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());
		response.getWriter().write(mensaje);
	}
}
