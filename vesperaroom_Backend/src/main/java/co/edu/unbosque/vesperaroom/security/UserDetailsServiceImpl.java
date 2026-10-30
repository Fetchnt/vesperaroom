package co.edu.unbosque.vesperaroom.security;

import java.sql.SQLException;

import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import co.edu.unbosque.vesperaroom.repository.UsuarioRepository;

/**
 * Implementación de {@link UserDetailsService} que carga los datos de
 * autenticación de un usuario desde la base de datos usando su correo
 * electrónico.
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

	private final UsuarioRepository usuarioRepository;

	public UserDetailsServiceImpl(UsuarioRepository usuarioRepository) {
		this.usuarioRepository = usuarioRepository;
	}

	/**
	 * Busca y retorna los detalles de autenticación de un usuario según su correo.
	 *
	 * @param correo correo electrónico usado como identificador de autenticación
	 * @return {@link UserDetails} con los datos del usuario encontrado
	 * @throws UsernameNotFoundException             si no existe ningún usuario
	 *                                               con ese correo
	 * @throws InternalAuthenticationServiceException si falla la consulta a la
	 *                                               base de datos
	 */
	@Override
	public UserDetails loadUserByUsername(String correo) throws UsernameNotFoundException {
		try {
			return usuarioRepository.findByCorreo(correo)
					.orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con correo: " + correo));
		} catch (SQLException e) {
			throw new InternalAuthenticationServiceException("Error al consultar el usuario en la base de datos", e);
		}
	}
}
