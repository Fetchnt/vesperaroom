package co.edu.unbosque.vesperaroom.repository;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import org.springframework.stereotype.Repository;

import co.edu.unbosque.vesperaroom.entity.Tarifa;
import co.edu.unbosque.vesperaroom.entity.Tarifa.TipoDia;

/**
 * Repositorio para la gestión de tarifas en la tabla {@code tarifa}, implementado
 * con JDBC puro: cada método obtiene una {@link Connection} del
 * {@link DataSource}, prepara la sentencia SQL con {@link PreparedStatement},
 * asigna los parámetros, la ejecuta y recorre el {@link ResultSet}. Los
 * recursos se cierran con try-with-resources.
 */
@Repository
public class TarifaRepository {

	private static final String SELECT_BASE = "SELECT ta.id_tarifa, ta.id_tipo, ta.id_modalidad, "
			+ "ta.precio, ta.tipo_dia, ta.vigente_desde, ta.vigente_hasta FROM tarifa ta ";

	private static final String SELECT_TODOS = SELECT_BASE + "ORDER BY ta.id_tipo, ta.id_modalidad, "
			+ "ta.vigente_desde";

	private static final String SELECT_POR_ID = SELECT_BASE + "WHERE ta.id_tarifa = ?";

	private static final String SELECT_POR_TIPO = SELECT_BASE + "WHERE ta.id_tipo = ? ORDER BY "
			+ "ta.id_modalidad, ta.vigente_desde";

	private static final String INSERT = "INSERT INTO tarifa (id_tipo, id_modalidad, precio, "
			+ "tipo_dia, vigente_desde, vigente_hasta) VALUES (?, ?, ?, ?, ?, ?)";

	private static final String UPDATE = "UPDATE tarifa SET id_tipo = ?, id_modalidad = ?, precio = "
			+ "?, tipo_dia = ?, vigente_desde = ?, vigente_hasta = ? WHERE id_tarifa = ?";

	private static final String DELETE = "DELETE FROM tarifa WHERE id_tarifa = ?";

	private final DataSource dataSource;

	public TarifaRepository(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * Retorna todas las tarifas.
	 *
	 * @return lista con todas las tarifas
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public List<Tarifa> findAll() throws SQLException {
		return listar(SELECT_TODOS);
	}

	/**
	 * Busca una tarifa por su identificador.
	 *
	 * @param id identificador de la tarifa
	 * @return {@link Optional} con la tarifa encontrada, o vacío si no existe
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public Optional<Tarifa> findById(Long id) throws SQLException {
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
	 * Retorna las tarifas de un tipo de habitación.
	 *
	 * @param idTipo identificador del tipo de habitación
	 * @return lista de tarifas del tipo
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public List<Tarifa> findByTipo(Long idTipo) throws SQLException {
		return listar(SELECT_POR_TIPO, idTipo);
	}

	/**
	 * Verifica si existe una tarifa con el identificador indicado.
	 *
	 * @param id identificador a verificar
	 * @return {@code true} si existe, {@code false} en caso contrario
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public boolean existsById(Long id) throws SQLException {
		return contar("SELECT COUNT(*) FROM tarifa WHERE id_tarifa = ?", id) > 0;
	}

	/**
	 * Retorna el total de tarifas registradas.
	 *
	 * @return cantidad total de tarifas
	 * @throws SQLException si ocurre un error de acceso a la base de datos
	 */
	public long count() throws SQLException {
		return contar("SELECT COUNT(*) FROM tarifa");
	}

	/**
	 * Inserta una nueva tarifa y retorna el identificador generado por
	 * {@code AUTO_INCREMENT}.
	 *
	 * @param t tarifa a insertar
	 * @return identificador generado para el nuevo registro
	 * @throws SQLException si ocurre un error de acceso a la base de datos o se
	 *                      viola una restricción de la tabla
	 */
	public Long insertar(Tarifa t) throws SQLException {
		try (Connection con = dataSource.getConnection();
				PreparedStatement ps = con.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
			ps.setLong(1, t.getIdTipo());
			ps.setLong(2, t.getIdModalidad());
			ps.setBigDecimal(3, t.getPrecio());
			ps.setString(4, t.getTipoDia().name());
			ps.setDate(5, Date.valueOf(t.getVigenteDesde()));
			ps.setObject(6, t.getVigenteHasta(), Types.DATE);
			ps.executeUpdate();

			try (ResultSet keys = ps.getGeneratedKeys()) {
				if (keys.next()) {
					return keys.getLong(1);
				}
				throw new SQLException("La base de datos no devolvió el id generado");
			}
		}
	}

	/**
	 * Actualiza los campos editables de una tarifa existente.
	 *
	 * @param t tarifa con los nuevos datos y su identificador
	 * @return cantidad de filas afectadas
	 * @throws SQLException si ocurre un error de acceso a la base de datos o se
	 *                      viola una restricción de la tabla
	 */
	public int actualizar(Tarifa t) throws SQLException {
		try (Connection con = dataSource.getConnection(); PreparedStatement ps = con.prepareStatement(UPDATE)) {
			ps.setLong(1, t.getIdTipo());
			ps.setLong(2, t.getIdModalidad());
			ps.setBigDecimal(3, t.getPrecio());
			ps.setString(4, t.getTipoDia().name());
			ps.setDate(5, Date.valueOf(t.getVigenteDesde()));
			ps.setObject(6, t.getVigenteHasta(), Types.DATE);
			ps.setLong(7, t.getIdTarifa());
			return ps.executeUpdate();
		}
	}

	/**
	 * Elimina una tarifa según su identificador.
	 *
	 * @param id identificador de la tarifa a eliminar
	 * @return cantidad de filas afectadas
	 * @throws SQLException si ocurre un error de acceso a la base de datos o
	 *                      la tarifa tiene registros que dependen de ella
	 */
	public int eliminar(Long id) throws SQLException {
		try (Connection con = dataSource.getConnection(); PreparedStatement ps = con.prepareStatement(DELETE)) {
			ps.setLong(1, id);
			return ps.executeUpdate();
		}
	}

	/**
	 * Convierte la fila actual del {@link ResultSet} en una entidad
	 * {@link Tarifa}, leyendo cada columna por su nombre.
	 *
	 * @param rs resultado posicionado en la fila a convertir
	 * @return tarifa con los datos de la fila
	 * @throws SQLException si alguna columna no existe o no puede leerse
	 */
	private Tarifa mapear(ResultSet rs) throws SQLException {
		Tarifa t = new Tarifa();
		t.setIdTarifa(rs.getLong("id_tarifa"));
		t.setIdTipo(rs.getLong("id_tipo"));
		t.setIdModalidad(rs.getLong("id_modalidad"));
		t.setPrecio(rs.getBigDecimal("precio"));
		t.setTipoDia(TipoDia.valueOf(rs.getString("tipo_dia")));
		t.setVigenteDesde(rs.getDate("vigente_desde").toLocalDate());
		t.setVigenteHasta(rs.getObject("vigente_hasta", LocalDate.class));
		return t;
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
	private List<Tarifa> listar(String sql, Object... parametros) throws SQLException {
		try (Connection con = dataSource.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
			for (int i = 0; i < parametros.length; i++) {
				ps.setObject(i + 1, parametros[i]);
			}
			try (ResultSet rs = ps.executeQuery()) {
				List<Tarifa> lista = new ArrayList<>();
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
