package co.edu.unbosque.vesperaroom.security;

import java.io.IOException;

import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Filtro de autenticación JWT que se ejecuta una vez por cada solicitud HTTP.
 * Extrae y valida el token del encabezado {@code Authorization} y, si es válido
 * y la cuenta sigue activa, establece la autenticación en el contexto de
 * seguridad. Un token inválido nunca interrumpe la cadena: la solicitud sigue
 * sin autenticar y Spring Security decide si responde 401.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private static final String PREFIJO_BEARER = "Bearer ";

	private final JwtUtil jwtUtil;
	private final UserDetailsService userDetailsService;

	/**
	 * Constructor que inyecta las dependencias necesarias para la validación del
	 * token.
	 *
	 * @param jwtUtil            utilidad para operaciones sobre tokens JWT
	 * @param userDetailsService servicio para cargar los detalles del usuario
	 */
	public JwtAuthenticationFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
		this.jwtUtil = jwtUtil;
		this.userDetailsService = userDetailsService;
	}

	/**
	 * Intercepta cada solicitud HTTP para extraer y validar el token JWT. Si el
	 * token es válido, el usuario existe y su cuenta está activa, establece la
	 * autenticación en el contexto de seguridad.
	 *
	 * @param request     solicitud HTTP entrante
	 * @param response    respuesta HTTP saliente
	 * @param filterChain cadena de filtros de seguridad
	 * @throws ServletException si ocurre un error en el procesamiento del servlet
	 * @throws IOException      si ocurre un error de entrada o salida
	 */
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
			FilterChain filterChain) throws ServletException, IOException {

		final String authorizationHeader = request.getHeader("Authorization");

		if (authorizationHeader != null && authorizationHeader.startsWith(PREFIJO_BEARER)
				&& SecurityContextHolder.getContext().getAuthentication() == null) {
			String jwt = authorizationHeader.substring(PREFIJO_BEARER.length());
			try {
				String username = jwtUtil.extractUsername(jwt);
				UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

				if (userDetails.isEnabled() && jwtUtil.validateToken(jwt, userDetails)) {
					UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
							userDetails, null, userDetails.getAuthorities());
					authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
					SecurityContextHolder.getContext().setAuthentication(authenticationToken);
				}
			} catch (JwtException | IllegalArgumentException e) {
				logger.debug("Token JWT inválido: " + e.getMessage());
			} catch (UsernameNotFoundException e) {
				logger.debug("El usuario del token ya no existe: " + e.getMessage());
			} catch (InternalAuthenticationServiceException e) {
				logger.error("No se pudo validar el token por un error de base de datos", e);
			}
		}

		filterChain.doFilter(request, response);
	}
}
