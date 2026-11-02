package co.edu.unbosque.vesperaroom.service;

import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import co.edu.unbosque.vesperaroom.dto.RestablecerDTO;
import co.edu.unbosque.vesperaroom.entity.Usuario;
import co.edu.unbosque.vesperaroom.exception.ContrasenaException;
import co.edu.unbosque.vesperaroom.exception.LanzadorExcepciones;
import co.edu.unbosque.vesperaroom.exception.PasswordSizeException;
import co.edu.unbosque.vesperaroom.repository.CodigoRecuperacionRepository;
import co.edu.unbosque.vesperaroom.repository.UsuarioRepository;

/**
 * Recuperación de contraseña con un código de 6 dígitos enviado por correo. El
 * código se guarda solo como hash BCrypt, vence a los 30 minutos, sirve una
 * vez y admite 5 intentos.
 *
 * <p>
 * Códigos de {@link #restablecer(RestablecerDTO)}: 0 éxito, 1 contraseña con
 * menos de 8 caracteres, 2 contraseña sin mayúscula, minúscula y número, 3
 * código inválido o vencido, 4 error inesperado.
 * </p>
 */
@Service
public class RecuperacionService {

	private static final Logger LOG = LoggerFactory.getLogger(RecuperacionService.class);
	private static final SecureRandom ALEATORIO = new SecureRandom();

	private final UsuarioRepository usuarioRepository;
	private final CodigoRecuperacionRepository codigoRepository;
	private final MailService mailService;
	private final PasswordEncoder passwordEncoder;
	/** Se compara contra este hash cuando no hay código, para que todas las respuestas tarden lo mismo. */
	private final String hashDeRelleno;

	public RecuperacionService(UsuarioRepository usuarioRepository, CodigoRecuperacionRepository codigoRepository,
			MailService mailService, PasswordEncoder passwordEncoder) {
		this.usuarioRepository = usuarioRepository;
		this.codigoRepository = codigoRepository;
		this.mailService = mailService;
		this.passwordEncoder = passwordEncoder;
		this.hashDeRelleno = passwordEncoder.encode("sin-codigo");
	}

	/**
	 * Envía un código al correo si pertenece a una cuenta activa. Corre en
	 * segundo plano: quien llama responde lo mismo y en el mismo tiempo exista o
	 * no la cuenta, así no se puede averiguar qué correos están registrados.
	 * Nunca lanza excepciones; los fallos quedan en el log.
	 *
	 * @param correo correo indicado por el usuario
	 */
	@Async
	public void solicitar(String correo) {
		try {
			Optional<Usuario> usuario = buscar(correo);
			if (usuario.isEmpty() || usuario.get().getEstado() != Usuario.Estado.ACTIVO) {
				return;
			}
			Long id = usuario.get().getIdUsuario();
			// Evita que alguien llene el buzón de otro pidiendo códigos sin parar.
			if (codigoRepository.enviadoHacePoco(id)) {
				return;
			}
			String codigo = String.format("%06d", ALEATORIO.nextInt(1_000_000));
			codigoRepository.guardar(id, passwordEncoder.encode(codigo));
			mailService.enviarCodigoRecuperacion(usuario.get().getCorreo(), codigo);
		} catch (Exception e) {
			LOG.error("No se pudo enviar el código de recuperación: {}", e.getMessage());
		}
	}

	/**
	 * Cambia la contraseña si el código es correcto y está vigente. Cualquier
	 * fallo del código responde igual (3), para no revelar si el correo existe.
	 *
	 * @param data correo, código recibido y contraseña nueva
	 * @return código de resultado según la tabla de la clase
	 */
	public int restablecer(RestablecerDTO data) {
		try {
			LanzadorExcepciones.verificarTamanoContrasena(data.getContrasena());
			LanzadorExcepciones.verificarContrasena(data.getContrasena());

			if (data.getCodigo() == null || !data.getCodigo().trim().matches("\\d{6}")) {
				return 3;
			}
			Optional<Usuario> usuario = buscar(data.getCorreo());
			Optional<String> hash = usuario.isPresent()
					? codigoRepository.gastarIntento(usuario.get().getIdUsuario())
					: Optional.empty();
			// Sin cuenta o sin código igual se compara: si no, la respuesta rápida delataría el correo.
			boolean coincide = passwordEncoder.matches(data.getCodigo().trim(), hash.orElse(hashDeRelleno));
			if (hash.isEmpty() || !coincide) {
				return 3;
			}
			codigoRepository.restablecer(usuario.get().getIdUsuario(), passwordEncoder.encode(data.getContrasena()));
			return 0;

		} catch (PasswordSizeException e) {
			return 1;
		} catch (ContrasenaException e) {
			return 2;
		} catch (Exception e) {
			e.printStackTrace();
			return 4;
		}
	}

	private Optional<Usuario> buscar(String correo) throws SQLException {
		if (correo == null || correo.isBlank()) {
			return Optional.empty();
		}
		return usuarioRepository.findByCorreo(correo.trim().toLowerCase());
	}
}
