package co.edu.unbosque.vesperaroom.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.vesperaroom.dto.AuthResponseDTO;
import co.edu.unbosque.vesperaroom.dto.LoginDTO;
import co.edu.unbosque.vesperaroom.dto.UsuarioDTO;
import co.edu.unbosque.vesperaroom.entity.Usuario;
import co.edu.unbosque.vesperaroom.security.JwtUtil;
import co.edu.unbosque.vesperaroom.service.UsuarioService;
import co.edu.unbosque.vesperaroom.util.RespuestaUsuario;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controlador REST para la autenticación de usuarios en VesperaRoom. Gestiona
 * el inicio de sesión con emisión de token JWT y el registro público de nuevas
 * cuentas.
 */
@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticación", description = "Inicio de sesión y registro de usuarios")
public class AuthController {

	private final AuthenticationManager authenticationManager;
	private final JwtUtil jwtUtil;
	private final UsuarioService usuarioService;

	public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil,
			UsuarioService usuarioService) {
		this.authenticationManager = authenticationManager;
		this.jwtUtil = jwtUtil;
		this.usuarioService = usuarioService;
	}

	/**
	 * Autentica un usuario con su correo y contraseña. Si las credenciales son
	 * válidas, genera y retorna un token JWT junto con la información básica del
	 * usuario.
	 *
	 * @param loginRequest objeto con el correo y la contraseña
	 * @return {@link ResponseEntity} con el token y datos del usuario en código
	 *         200, 403 si la cuenta está inactiva, 401 si las credenciales son
	 *         inválidas, o 500 si falla el acceso a la base de datos
	 */
	@Operation(summary = "Iniciar sesión", description = "Autentica con correo y contraseña y retorna un token JWT.")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Login exitoso"),
			@ApiResponse(responseCode = "401", description = "Credenciales inválidas"),
			@ApiResponse(responseCode = "403", description = "Cuenta inactiva") })
	@PostMapping(path = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> login(@RequestBody LoginDTO loginRequest) {
		if (loginRequest.getCorreo() == null || loginRequest.getContrasena() == null) {
			return new ResponseEntity<>("Debe indicar correo y contraseña", HttpStatus.BAD_REQUEST);
		}

		try {
			Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
					loginRequest.getCorreo().trim().toLowerCase(), loginRequest.getContrasena()));

			Usuario usuario = (Usuario) authentication.getPrincipal();
			String jwt = jwtUtil.generateToken(usuario);

			return new ResponseEntity<>(new AuthResponseDTO(jwt, usuario.getIdUsuario(), usuario.getNombre(),
					usuario.getCorreo(), usuario.getRol().name()), HttpStatus.OK);

		} catch (InternalAuthenticationServiceException e) {
			return new ResponseEntity<>("Error interno al autenticar", HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (DisabledException e) {
			return new ResponseEntity<>("La cuenta está inactiva", HttpStatus.FORBIDDEN);
		} catch (AuthenticationException e) {
			return new ResponseEntity<>("Correo o contraseña inválidos", HttpStatus.UNAUTHORIZED);
		}
	}

	/**
	 * Registra una nueva cuenta. El endpoint es público y solo permite los roles
	 * {@code CLIENTE} y {@code PROPIETARIO}.
	 *
	 * @param registerRequest objeto con los datos del nuevo usuario
	 * @return {@link ResponseEntity} con código 201 si el registro es exitoso, 400
	 *         si algún dato es inválido, 403 si el rol no está permitido o 409 si
	 *         el correo o el teléfono ya existen
	 */
	@Operation(summary = "Registrar usuario", description = "Crea una cuenta de CLIENTE o PROPIETARIO. Endpoint público.")
	@ApiResponses(value = { @ApiResponse(responseCode = "201", description = "Usuario registrado exitosamente"),
			@ApiResponse(responseCode = "400", description = "Datos inválidos"),
			@ApiResponse(responseCode = "403", description = "Rol no permitido"),
			@ApiResponse(responseCode = "409", description = "Correo o teléfono duplicado") })
	@PostMapping(path = "/register", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> register(@RequestBody UsuarioDTO registerRequest) {
		int status = usuarioService.registrar(registerRequest);
		return RespuestaUsuario.guardar(status, "Usuario registrado exitosamente", HttpStatus.CREATED);
	}
}
