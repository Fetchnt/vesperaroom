package co.edu.unbosque.vesperaroom.security;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import co.edu.unbosque.vesperaroom.entity.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

/**
 * Clase utilitaria para la generación, validación y extracción de información
 * de tokens JWT. Utiliza el algoritmo HMAC SHA-256 para firmar los tokens con
 * la clave definida en la propiedad {@code jwt.secret}.
 */
@Component
public class JwtUtil {

	private static final int TAMANO_MINIMO_CLAVE = 32;

	private final Key signingKey;
	private final long expirationMs;

	/**
	 * Constructor que construye la clave de firma y valida que tenga el tamaño
	 * mínimo exigido por HS256. Si la clave no es válida la aplicación no arranca,
	 * evitando firmar tokens con una clave débil o por defecto.
	 *
	 * @param secret       clave secreta configurada en {@code jwt.secret}
	 * @param expirationMs tiempo de vida del token en milisegundos
	 * @throws IllegalStateException si la clave es nula o tiene menos de 32 bytes
	 */
	public JwtUtil(@Value("${jwt.secret}") String secret, @Value("${jwt.expiration-ms:86400000}") long expirationMs) {
		if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < TAMANO_MINIMO_CLAVE) {
			throw new IllegalStateException("La propiedad jwt.secret debe tener al menos 32 caracteres");
		}
		this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
		this.expirationMs = expirationMs;
	}

	/**
	 * Extrae el nombre de usuario (correo) contenido en el token JWT.
	 *
	 * @param token token JWT del que se extrae el usuario
	 * @return correo almacenado en el subject del token
	 */
	public String extractUsername(String token) {
		return extractClaim(token, Claims::getSubject);
	}

	/**
	 * Extrae la fecha de expiración del token JWT.
	 *
	 * @param token token JWT del que se extrae la expiración
	 * @return fecha de expiración del token
	 */
	public Date extractExpiration(String token) {
		return extractClaim(token, Claims::getExpiration);
	}

	/**
	 * Extrae el rol del usuario contenido en el token JWT.
	 *
	 * @param token token JWT del que se extrae el rol
	 * @return nombre del rol almacenado en el token
	 */
	public String extractRole(String token) {
		return extractClaim(token, claims -> claims.get("role", String.class));
	}

	/**
	 * Extrae el identificador del usuario contenido en el token JWT.
	 *
	 * @param token token JWT del que se extrae el identificador
	 * @return identificador del usuario
	 */
	public Long extractUserId(String token) {
		return extractClaim(token, claims -> claims.get("userId", Long.class));
	}

	/**
	 * Extrae un claim específico del token JWT aplicando una función resolutora.
	 *
	 * @param token          token JWT del que se extrae el claim
	 * @param claimsResolver función que define qué claim extraer
	 * @return valor del claim extraído
	 */
	public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
		return claimsResolver.apply(extractAllClaims(token));
	}

	/**
	 * Extrae todos los claims del token verificando su firma.
	 *
	 * @param token token JWT a procesar
	 * @return objeto {@link Claims} con toda la información del token
	 */
	private Claims extractAllClaims(String token) {
		return Jwts.parserBuilder().setSigningKey(signingKey).build().parseClaimsJws(token).getBody();
	}

	/**
	 * Verifica si el token JWT ha expirado.
	 *
	 * @param token token JWT a verificar
	 * @return {@code true} si el token ha expirado
	 */
	private boolean isTokenExpired(String token) {
		return extractExpiration(token).before(new Date());
	}

	/**
	 * Genera un token JWT para el usuario autenticado, incluyendo su rol e
	 * identificador como claims adicionales.
	 *
	 * @param userDetails detalles del usuario autenticado
	 * @return token JWT generado
	 */
	public String generateToken(UserDetails userDetails) {
		Map<String, Object> claims = new HashMap<>();
		if (userDetails instanceof Usuario usuario) {
			claims.put("role", usuario.getRol().name());
			claims.put("userId", usuario.getIdUsuario());
		}
		return createToken(claims, userDetails.getUsername());
	}

	/**
	 * Construye y firma el token JWT con los claims, el subject y los tiempos de
	 * emisión y expiración.
	 *
	 * @param claims  mapa de claims adicionales
	 * @param subject correo del usuario que se establece como subject
	 * @return token JWT firmado en formato compacto
	 */
	private String createToken(Map<String, Object> claims, String subject) {
		long ahora = System.currentTimeMillis();
		return Jwts.builder().setClaims(claims).setSubject(subject).setIssuedAt(new Date(ahora))
				.setExpiration(new Date(ahora + expirationMs)).signWith(signingKey, SignatureAlgorithm.HS256)
				.compact();
	}

	/**
	 * Valida el token JWT verificando que el usuario coincida y que no haya
	 * expirado.
	 *
	 * @param token       token JWT a validar
	 * @param userDetails detalles del usuario contra el que se valida el token
	 * @return {@code true} si el token es válido
	 */
	public boolean validateToken(String token, UserDetails userDetails) {
		final String username = extractUsername(token);
		return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
	}
}
