package co.edu.unbosque.vesperaroom.service;

import java.sql.SQLException;
import java.util.Optional;

import org.springframework.stereotype.Service;

import co.edu.unbosque.vesperaroom.entity.Usuario;
import co.edu.unbosque.vesperaroom.entity.Usuario.Rol;
import co.edu.unbosque.vesperaroom.exception.AccesoDatosException;
import co.edu.unbosque.vesperaroom.repository.AutorizacionRepository;

/**
 * Servicio que decide si un usuario autenticado puede operar sobre un recurso
 * concreto. {@code SecurityConfig} filtra por rol (por ejemplo, solo
 * PROPIETARIO o ADMINISTRADOR entran a {@code /motel/**}), pero no sabe de
 * quién es cada motel; esta clase completa esa verificación consultando el
 * propietario real en la base de datos.
 *
 * <p>
 * Reglas comunes a todos los métodos:
 * </p>
 * <ul>
 * <li>El ADMINISTRADOR siempre tiene permiso.</li>
 * <li>Si el identificador es nulo o el recurso no existe, se permite el paso
 * para que el servicio correspondiente responda "no encontrado" (404) en lugar
 * de un 403 engañoso.</li>
 * <li>En cualquier otro caso, el usuario debe ser el dueño del recurso.</li>
 * </ul>
 */
@Service
public class AutorizacionService {

	private final AutorizacionRepository autorizacionRepository;

	public AutorizacionService(AutorizacionRepository autorizacionRepository) {
		this.autorizacionRepository = autorizacionRepository;
	}

	/**
	 * Indica si el usuario tiene rol ADMINISTRADOR.
	 *
	 * @param usuario usuario autenticado
	 * @return {@code true} si es administrador
	 */
	public boolean esAdministrador(Usuario usuario) {
		return usuario.getRol() == Rol.ADMINISTRADOR;
	}

	/**
	 * Indica si el usuario puede gestionar un motel y todo lo que cuelga de él.
	 *
	 * @param usuario usuario autenticado
	 * @param idMotel identificador del motel
	 * @return {@code true} si es el propietario o un administrador
	 * @throws AccesoDatosException si falla la consulta a la base de datos
	 */
	public boolean puedeGestionarMotel(Usuario usuario, Long idMotel) {
		if (idMotel == null || esAdministrador(usuario)) {
			return true;
		}
		try {
			return esDueno(usuario, autorizacionRepository.propietarioDeMotel(idMotel));
		} catch (SQLException e) {
			throw new AccesoDatosException(e);
		}
	}

	/**
	 * Indica si el usuario puede gestionar un tipo de habitación.
	 *
	 * @param usuario usuario autenticado
	 * @param idTipo  identificador del tipo de habitación
	 * @return {@code true} si es el propietario del motel o un administrador
	 * @throws AccesoDatosException si falla la consulta a la base de datos
	 */
	public boolean puedeGestionarTipo(Usuario usuario, Long idTipo) {
		if (idTipo == null || esAdministrador(usuario)) {
			return true;
		}
		try {
			return esDueno(usuario, autorizacionRepository.propietarioDeTipo(idTipo));
		} catch (SQLException e) {
			throw new AccesoDatosException(e);
		}
	}

	/**
	 * Indica si el usuario puede gestionar una habitación.
	 *
	 * @param usuario      usuario autenticado
	 * @param idHabitacion identificador de la habitación
	 * @return {@code true} si es el propietario del motel o un administrador
	 * @throws AccesoDatosException si falla la consulta a la base de datos
	 */
	public boolean puedeGestionarHabitacion(Usuario usuario, Long idHabitacion) {
		if (idHabitacion == null || esAdministrador(usuario)) {
			return true;
		}
		try {
			return esDueno(usuario, autorizacionRepository.propietarioDeHabitacion(idHabitacion));
		} catch (SQLException e) {
			throw new AccesoDatosException(e);
		}
	}

	/**
	 * Indica si el usuario puede gestionar una tarifa.
	 *
	 * @param usuario  usuario autenticado
	 * @param idTarifa identificador de la tarifa
	 * @return {@code true} si es el propietario del motel o un administrador
	 * @throws AccesoDatosException si falla la consulta a la base de datos
	 */
	public boolean puedeGestionarTarifa(Usuario usuario, Long idTarifa) {
		if (idTarifa == null || esAdministrador(usuario)) {
			return true;
		}
		try {
			return esDueno(usuario, autorizacionRepository.propietarioDeTarifa(idTarifa));
		} catch (SQLException e) {
			throw new AccesoDatosException(e);
		}
	}

	/**
	 * Indica si el usuario puede gestionar un servicio adicional.
	 *
	 * @param usuario    usuario autenticado
	 * @param idServicio identificador del servicio
	 * @return {@code true} si es el propietario del motel o un administrador
	 * @throws AccesoDatosException si falla la consulta a la base de datos
	 */
	public boolean puedeGestionarServicio(Usuario usuario, Long idServicio) {
		if (idServicio == null || esAdministrador(usuario)) {
			return true;
		}
		try {
			return esDueno(usuario, autorizacionRepository.propietarioDeServicio(idServicio));
		} catch (SQLException e) {
			throw new AccesoDatosException(e);
		}
	}

	/**
	 * Indica si el usuario puede gestionar una foto.
	 *
	 * @param usuario usuario autenticado
	 * @param idFoto  identificador de la foto
	 * @return {@code true} si es el propietario del motel o un administrador
	 * @throws AccesoDatosException si falla la consulta a la base de datos
	 */
	public boolean puedeGestionarFoto(Usuario usuario, Long idFoto) {
		if (idFoto == null || esAdministrador(usuario)) {
			return true;
		}
		try {
			return esDueno(usuario, autorizacionRepository.propietarioDeFoto(idFoto));
		} catch (SQLException e) {
			throw new AccesoDatosException(e);
		}
	}

	/**
	 * Indica si el usuario puede administrar una reserva como propietario del
	 * motel (cambiar su estado, ver sus pagos).
	 *
	 * @param usuario   usuario autenticado
	 * @param idReserva identificador de la reserva
	 * @return {@code true} si es el propietario del motel o un administrador
	 * @throws AccesoDatosException si falla la consulta a la base de datos
	 */
	public boolean puedeGestionarReserva(Usuario usuario, Long idReserva) {
		if (idReserva == null || esAdministrador(usuario)) {
			return true;
		}
		try {
			return esDueno(usuario, autorizacionRepository.propietarioDeReserva(idReserva));
		} catch (SQLException e) {
			throw new AccesoDatosException(e);
		}
	}

	/**
	 * Indica si el usuario puede ver una reserva: el cliente que la hizo, el
	 * propietario del motel o un administrador.
	 *
	 * @param usuario   usuario autenticado
	 * @param idReserva identificador de la reserva
	 * @return {@code true} si tiene acceso
	 * @throws AccesoDatosException si falla la consulta a la base de datos
	 */
	public boolean puedeVerReserva(Usuario usuario, Long idReserva) {
		return esClienteDeReserva(usuario, idReserva) || puedeGestionarReserva(usuario, idReserva);
	}

	/**
	 * Indica si el usuario es el cliente que hizo la reserva. A diferencia de los
	 * demás métodos, el administrador no se considera cliente.
	 *
	 * @param usuario   usuario autenticado
	 * @param idReserva identificador de la reserva
	 * @return {@code true} si la reserva es del usuario, o si no existe
	 * @throws AccesoDatosException si falla la consulta a la base de datos
	 */
	public boolean esClienteDeReserva(Usuario usuario, Long idReserva) {
		if (idReserva == null) {
			return true;
		}
		try {
			return esDueno(usuario, autorizacionRepository.clienteDeReserva(idReserva));
		} catch (SQLException e) {
			throw new AccesoDatosException(e);
		}
	}

	/**
	 * Indica si el usuario puede administrar un pago como propietario del motel.
	 *
	 * @param usuario usuario autenticado
	 * @param idPago  identificador del pago
	 * @return {@code true} si es el propietario del motel o un administrador
	 * @throws AccesoDatosException si falla la consulta a la base de datos
	 */
	public boolean puedeGestionarPago(Usuario usuario, Long idPago) {
		if (idPago == null || esAdministrador(usuario)) {
			return true;
		}
		try {
			return esDueno(usuario, autorizacionRepository.propietarioDePago(idPago));
		} catch (SQLException e) {
			throw new AccesoDatosException(e);
		}
	}

	/**
	 * Indica si el usuario puede ver un pago: el cliente de la reserva, el
	 * propietario del motel o un administrador.
	 *
	 * @param usuario usuario autenticado
	 * @param idPago  identificador del pago
	 * @return {@code true} si tiene acceso
	 * @throws AccesoDatosException si falla la consulta a la base de datos
	 */
	public boolean puedeVerPago(Usuario usuario, Long idPago) {
		if (idPago == null || puedeGestionarPago(usuario, idPago)) {
			return true;
		}
		try {
			return esDueno(usuario, autorizacionRepository.clienteDePago(idPago));
		} catch (SQLException e) {
			throw new AccesoDatosException(e);
		}
	}

	/**
	 * Indica si el usuario puede responder una reseña: el propietario del motel
	 * reseñado o un administrador.
	 *
	 * @param usuario  usuario autenticado
	 * @param idResena identificador de la reseña
	 * @return {@code true} si tiene permiso
	 * @throws AccesoDatosException si falla la consulta a la base de datos
	 */
	public boolean puedeResponderResena(Usuario usuario, Long idResena) {
		if (idResena == null || esAdministrador(usuario)) {
			return true;
		}
		try {
			return esDueno(usuario, autorizacionRepository.propietarioDeResena(idResena));
		} catch (SQLException e) {
			throw new AccesoDatosException(e);
		}
	}

	/**
	 * Indica si el usuario puede editar o borrar una reseña: su autor o un
	 * administrador.
	 *
	 * @param usuario  usuario autenticado
	 * @param idResena identificador de la reseña
	 * @return {@code true} si tiene permiso
	 * @throws AccesoDatosException si falla la consulta a la base de datos
	 */
	public boolean puedeEditarResena(Usuario usuario, Long idResena) {
		if (idResena == null || esAdministrador(usuario)) {
			return true;
		}
		try {
			return esDueno(usuario, autorizacionRepository.clienteDeResena(idResena));
		} catch (SQLException e) {
			throw new AccesoDatosException(e);
		}
	}

	/**
	 * Compara el dueño encontrado con el usuario autenticado.
	 *
	 * @param usuario usuario autenticado
	 * @param dueno   identificador del dueño, o vacío si el recurso no existe
	 * @return {@code true} si el recurso no existe o el usuario es su dueño
	 */
	private boolean esDueno(Usuario usuario, Optional<Long> dueno) {
		return dueno.isEmpty() || dueno.get().equals(usuario.getIdUsuario());
	}
}
