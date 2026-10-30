package co.edu.unbosque.vesperaroom.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import javax.sql.DataSource;

import org.springframework.stereotype.Repository;

/**
 * Repositorio de consultas de propiedad, implementado con JDBC puro. Cada
 * método recorre las llaves foráneas con {@code JOIN} hasta llegar al motel y
 * devuelve el identificador de su propietario (o del cliente, en el caso de
 * reservas y reseñas). Lo usa
 * {@link co.edu.unbosque.vesperaroom.service.AutorizacionService} para decidir
 * si un usuario puede modificar un recurso.
 */
@Repository
public class AutorizacionRepository {

	private static final String PROPIETARIO_DE_MOTEL = "SELECT id_propietario FROM motel WHERE id_motel = ?";

	private static final String PROPIETARIO_DE_TIPO = "SELECT m.id_propietario FROM tipo_habitacion t "
			+ "JOIN motel m ON m.id_motel = t.id_motel WHERE t.id_tipo = ?";

	private static final String PROPIETARIO_DE_HABITACION = "SELECT m.id_propietario FROM habitacion h "
			+ "JOIN motel m ON m.id_motel = h.id_motel WHERE h.id_habitacion = ?";

	private static final String PROPIETARIO_DE_TARIFA = "SELECT m.id_propietario FROM tarifa ta "
			+ "JOIN tipo_habitacion t ON t.id_tipo = ta.id_tipo JOIN motel m ON m.id_motel = t.id_motel "
			+ "WHERE ta.id_tarifa = ?";

	private static final String PROPIETARIO_DE_SERVICIO = "SELECT m.id_propietario FROM servicio_adicional s "
			+ "JOIN motel m ON m.id_motel = s.id_motel WHERE s.id_servicio = ?";

	private static final String PROPIETARIO_DE_FOTO = "SELECT m.id_propietario FROM foto f "
			+ "LEFT JOIN habitacion h ON h.id_habitacion = f.id_habitacion "
			+ "JOIN motel m ON m.id_motel = COALESCE(f.id_motel, h.id_motel) WHERE f.id_foto = ?";

	private static final String PROPIETARIO_DE_RESERVA = "SELECT m.id_propietario FROM reserva r "
			+ "JOIN habitacion h ON h.id_habitacion = r.id_habitacion JOIN motel m ON m.id_motel = h.id_motel "
			+ "WHERE r.id_reserva = ?";

	private static final String PROPIETARIO_DE_PAGO = "SELECT m.id_propietario FROM pago p "
			+ "JOIN reserva r ON r.id_reserva = p.id_reserva JOIN habitacion h ON h.id_habitacion = r.id_habitacion "
			+ "JOIN motel m ON m.id_motel = h.id_motel WHERE p.id_pago = ?";

	private static final String PROPIETARIO_DE_RESENA = "SELECT m.id_propietario FROM resena re "
			+ "JOIN reserva r ON r.id_reserva = re.id_reserva JOIN habitacion h ON h.id_habitacion = r.id_habitacion "
			+ "JOIN motel m ON m.id_motel = h.id_motel WHERE re.id_resena = ?";

	private static final String CLIENTE_DE_RESERVA = "SELECT id_usuario FROM reserva WHERE id_reserva = ?";

	private static final String CLIENTE_DE_PAGO = "SELECT r.id_usuario FROM pago p "
			+ "JOIN reserva r ON r.id_reserva = p.id_reserva WHERE p.id_pago = ?";

	private static final String CLIENTE_DE_RESENA = "SELECT r.id_usuario FROM resena re "
			+ "JOIN reserva r ON r.id_reserva = re.id_reserva WHERE re.id_resena = ?";

	private final DataSource dataSource;

	public AutorizacionRepository(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * Retorna el propietario de un motel.
	 *
	 * @param idMotel identificador del motel
	 * @return {@link Optional} con el id del propietario, o vacío si el motel no
	 *         existe
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public Optional<Long> propietarioDeMotel(Long idMotel) throws SQLException {
		return buscarId(PROPIETARIO_DE_MOTEL, idMotel);
	}

	/**
	 * Retorna el propietario del motel al que pertenece un tipo de habitación.
	 *
	 * @param idTipo identificador del tipo de habitación
	 * @return {@link Optional} con el id del propietario, o vacío si no existe
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public Optional<Long> propietarioDeTipo(Long idTipo) throws SQLException {
		return buscarId(PROPIETARIO_DE_TIPO, idTipo);
	}

	/**
	 * Retorna el propietario del motel al que pertenece una habitación.
	 *
	 * @param idHabitacion identificador de la habitación
	 * @return {@link Optional} con el id del propietario, o vacío si no existe
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public Optional<Long> propietarioDeHabitacion(Long idHabitacion) throws SQLException {
		return buscarId(PROPIETARIO_DE_HABITACION, idHabitacion);
	}

	/**
	 * Retorna el propietario del motel al que pertenece una tarifa, pasando por su
	 * tipo de habitación.
	 *
	 * @param idTarifa identificador de la tarifa
	 * @return {@link Optional} con el id del propietario, o vacío si no existe
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public Optional<Long> propietarioDeTarifa(Long idTarifa) throws SQLException {
		return buscarId(PROPIETARIO_DE_TARIFA, idTarifa);
	}

	/**
	 * Retorna el propietario del motel que ofrece un servicio adicional.
	 *
	 * @param idServicio identificador del servicio
	 * @return {@link Optional} con el id del propietario, o vacío si no existe
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public Optional<Long> propietarioDeServicio(Long idServicio) throws SQLException {
		return buscarId(PROPIETARIO_DE_SERVICIO, idServicio);
	}

	/**
	 * Retorna el propietario del motel de una foto. Como la foto puede pertenecer
	 * al motel o a una habitación, se usa {@code COALESCE} para tomar el motel
	 * directo o el de la habitación.
	 *
	 * @param idFoto identificador de la foto
	 * @return {@link Optional} con el id del propietario, o vacío si no existe
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public Optional<Long> propietarioDeFoto(Long idFoto) throws SQLException {
		return buscarId(PROPIETARIO_DE_FOTO, idFoto);
	}

	/**
	 * Retorna el propietario del motel de una reserva.
	 *
	 * @param idReserva identificador de la reserva
	 * @return {@link Optional} con el id del propietario, o vacío si no existe
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public Optional<Long> propietarioDeReserva(Long idReserva) throws SQLException {
		return buscarId(PROPIETARIO_DE_RESERVA, idReserva);
	}

	/**
	 * Retorna el propietario del motel de un pago.
	 *
	 * @param idPago identificador del pago
	 * @return {@link Optional} con el id del propietario, o vacío si no existe
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public Optional<Long> propietarioDePago(Long idPago) throws SQLException {
		return buscarId(PROPIETARIO_DE_PAGO, idPago);
	}

	/**
	 * Retorna el propietario del motel sobre el que se escribió una reseña.
	 *
	 * @param idResena identificador de la reseña
	 * @return {@link Optional} con el id del propietario, o vacío si no existe
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public Optional<Long> propietarioDeResena(Long idResena) throws SQLException {
		return buscarId(PROPIETARIO_DE_RESENA, idResena);
	}

	/**
	 * Retorna el cliente que hizo una reserva.
	 *
	 * @param idReserva identificador de la reserva
	 * @return {@link Optional} con el id del cliente, o vacío si no existe
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public Optional<Long> clienteDeReserva(Long idReserva) throws SQLException {
		return buscarId(CLIENTE_DE_RESERVA, idReserva);
	}

	/**
	 * Retorna el cliente dueño de la reserva de un pago.
	 *
	 * @param idPago identificador del pago
	 * @return {@link Optional} con el id del cliente, o vacío si no existe
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public Optional<Long> clienteDePago(Long idPago) throws SQLException {
		return buscarId(CLIENTE_DE_PAGO, idPago);
	}

	/**
	 * Retorna el cliente que escribió una reseña.
	 *
	 * @param idResena identificador de la reseña
	 * @return {@link Optional} con el id del cliente, o vacío si no existe
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public Optional<Long> clienteDeResena(Long idResena) throws SQLException {
		return buscarId(CLIENTE_DE_RESENA, idResena);
	}

	/**
	 * Ejecuta una consulta que devuelve un único identificador.
	 *
	 * @param sql consulta con un marcador {@code ?}
	 * @param id  valor del marcador
	 * @return {@link Optional} con el identificador, o vacío si no hay filas
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	private Optional<Long> buscarId(String sql, Long id) throws SQLException {
		try (Connection con = dataSource.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
			ps.setLong(1, id);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return Optional.of(rs.getLong(1));
				}
				return Optional.empty();
			}
		}
	}
}
