package co.edu.unbosque.vesperaroom.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import org.springframework.stereotype.Repository;

import co.edu.unbosque.vesperaroom.entity.PoliticaReserva;

/**
 * Repositorio para la gestión de políticas de reserva en la tabla {@code politica_reserva}, implementado
 * con JDBC puro: cada método obtiene una {@link Connection} del
 * {@link DataSource}, prepara la sentencia SQL con {@link PreparedStatement},
 * asigna los parámetros, la ejecuta y recorre el {@link ResultSet}. Los
 * recursos se cierran con try-with-resources.
 */
@Repository
public class PoliticaReservaRepository {

	private static final String SELECT_BASE = "SELECT p.id_motel, p.dias_habiles_devolucion, "
			+ "p.porcentaje_reembolso, p.capacidad_max_sin_recargo, p.tarifa_persona_adicional, "
			+ "p.anticipacion_minima_horas, p.tiempo_gracia_no_show_min FROM politica_reserva p ";

	private static final String SELECT_TODOS = SELECT_BASE + "ORDER BY p.id_motel";

	private static final String SELECT_POR_ID = SELECT_BASE + "WHERE p.id_motel = ?";

	private static final String INSERT = "INSERT INTO politica_reserva (id_motel, "
			+ "dias_habiles_devolucion, porcentaje_reembolso, capacidad_max_sin_recargo, "
			+ "tarifa_persona_adicional, anticipacion_minima_horas, tiempo_gracia_no_show_min) "
			+ "VALUES (?, ?, ?, ?, ?, ?, ?)";

	private static final String UPDATE = "UPDATE politica_reserva SET dias_habiles_devolucion = ?, "
			+ "porcentaje_reembolso = ?, capacidad_max_sin_recargo = ?, tarifa_persona_adicional = "
			+ "?, anticipacion_minima_horas = ?, tiempo_gracia_no_show_min = ? WHERE id_motel = ?";

	private static final String DELETE = "DELETE FROM politica_reserva WHERE id_motel = ?";

	private final DataSource dataSource;

	public PoliticaReservaRepository(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * Retorna todas las políticas de reserva.
	 *
	 * @return lista con todas las políticas de reserva
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public List<PoliticaReserva> findAll() throws SQLException {
		return listar(SELECT_TODOS);
	}

	/**
	 * Busca una política de reserva por su identificador.
	 *
	 * @param id identificador de la política de reserva
	 * @return {@link Optional} con la política de reserva encontrada, o vacío si no existe
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public Optional<PoliticaReserva> findById(Long id) throws SQLException {
		try (Connection con = dataSource.getConnection();
				PreparedStatement ps = con.prepareStatement(SELECT_POR_ID)) {
			ps.setLong(1, id);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return Optional.of(mapear(rs));
				}
				return Optional.empty();
			}
		}
	}

	/**
	 * Verifica si existe una política de reserva con el identificador indicado.
	 *
	 * @param id identificador a verificar
	 * @return {@code true} si existe, {@code false} en caso contrario
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public boolean existsById(Long id) throws SQLException {
		return contar("SELECT COUNT(*) FROM politica_reserva WHERE id_motel = ?", id) > 0;
	}

	/**
	 * Retorna el total de políticas de reserva registradas.
	 *
	 * @return cantidad total de políticas de reserva
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public long count() throws SQLException {
		return contar("SELECT COUNT(*) FROM politica_reserva");
	}

	/**
	 * Inserta una nueva política de reserva. Su identificador es la llave del registro
	 * del que depende, por lo que no se genera con {@code AUTO_INCREMENT}.
	 *
	 * @param p política de reserva a insertar
	 * @return cantidad de filas afectadas
	 * @throws SQLException si ocurre un error de acceso a la base de datos o se
	 *                      viola una restricción de la tabla
	 */
	public int insertar(PoliticaReserva p) throws SQLException {
		try (Connection con = dataSource.getConnection(); PreparedStatement ps = con.prepareStatement(INSERT)) {
			ps.setLong(1, p.getIdMotel());
			ps.setInt(2, p.getDiasHabilesDevolucion());
			ps.setInt(3, p.getPorcentajeReembolso());
			ps.setInt(4, p.getCapacidadMaxSinRecargo());
			ps.setBigDecimal(5, p.getTarifaPersonaAdicional());
			ps.setInt(6, p.getAnticipacionMinimaHoras());
			ps.setInt(7, p.getTiempoGraciaNoShowMin());
			return ps.executeUpdate();
		}
	}

	/**
	 * Actualiza los campos editables de una política de reserva existente.
	 *
	 * @param p política de reserva con los nuevos datos y su identificador
	 * @return cantidad de filas afectadas
	 * @throws SQLException si ocurre un error de acceso a la base de datos o se
	 *                      viola una restricción de la tabla
	 */
	public int actualizar(PoliticaReserva p) throws SQLException {
		try (Connection con = dataSource.getConnection(); PreparedStatement ps = con.prepareStatement(UPDATE)) {
			ps.setInt(1, p.getDiasHabilesDevolucion());
			ps.setInt(2, p.getPorcentajeReembolso());
			ps.setInt(3, p.getCapacidadMaxSinRecargo());
			ps.setBigDecimal(4, p.getTarifaPersonaAdicional());
			ps.setInt(5, p.getAnticipacionMinimaHoras());
			ps.setInt(6, p.getTiempoGraciaNoShowMin());
			ps.setLong(7, p.getIdMotel());
			return ps.executeUpdate();
		}
	}

	/**
	 * Elimina una política de reserva según su identificador.
	 *
	 * @param id identificador de la política de reserva a eliminar
	 * @return cantidad de filas afectadas
	 * @throws SQLException si ocurre un error de acceso a la base de datos o
	 *                      la política de reserva tiene registros que dependen de ella
	 */
	public int eliminar(Long id) throws SQLException {
		try (Connection con = dataSource.getConnection(); PreparedStatement ps = con.prepareStatement(DELETE)) {
			ps.setLong(1, id);
			return ps.executeUpdate();
		}
	}

	/**
	 * Convierte la fila actual del {@link ResultSet} en una entidad
	 * {@link PoliticaReserva}, leyendo cada columna por su nombre.
	 *
	 * @param rs resultado posicionado en la fila a convertir
	 * @return política de reserva con los datos de la fila
	 * @throws SQLException si alguna columna no existe o no puede leerse
	 */
	private PoliticaReserva mapear(ResultSet rs) throws SQLException {
		PoliticaReserva p = new PoliticaReserva();
		p.setIdMotel(rs.getLong("id_motel"));
		p.setDiasHabilesDevolucion(rs.getInt("dias_habiles_devolucion"));
		p.setPorcentajeReembolso(rs.getInt("porcentaje_reembolso"));
		p.setCapacidadMaxSinRecargo(rs.getInt("capacidad_max_sin_recargo"));
		p.setTarifaPersonaAdicional(rs.getBigDecimal("tarifa_persona_adicional"));
		p.setAnticipacionMinimaHoras(rs.getInt("anticipacion_minima_horas"));
		p.setTiempoGraciaNoShowMin(rs.getInt("tiempo_gracia_no_show_min"));
		return p;
	}

	/**
	 * Ejecuta una consulta que devuelve varias filas y convierte cada una en una
	 * entidad con {@link #mapear(ResultSet)}. Los parámetros se asignan en orden a
	 * los marcadores {@code ?}.
	 *
	 * @param sql        sentencia de consulta
	 * @param parametros valores para los marcadores {@code ?}
	 * @return lista con las entidades encontradas
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	private List<PoliticaReserva> listar(String sql, Object... parametros) throws SQLException {
		try (Connection con = dataSource.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
			for (int i = 0; i < parametros.length; i++) {
				ps.setObject(i + 1, parametros[i]);
			}
			try (ResultSet rs = ps.executeQuery()) {
				List<PoliticaReserva> lista = new ArrayList<>();
				while (rs.next()) {
					lista.add(mapear(rs));
				}
				return lista;
			}
		}
	}

	/**
	 * Ejecuta una consulta {@code COUNT(*)} y retorna su resultado. Los parámetros
	 * se asignan en orden a los marcadores {@code ?}.
	 *
	 * @param sql        sentencia de conteo
	 * @param parametros valores para los marcadores {@code ?}
	 * @return resultado del conteo
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	private long contar(String sql, Object... parametros) throws SQLException {
		try (Connection con = dataSource.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
			for (int i = 0; i < parametros.length; i++) {
				ps.setObject(i + 1, parametros[i]);
			}
			try (ResultSet rs = ps.executeQuery()) {
				rs.next();
				return rs.getLong(1);
			}
		}
	}
}
