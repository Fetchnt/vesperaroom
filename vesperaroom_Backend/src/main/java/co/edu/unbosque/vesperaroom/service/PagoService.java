package co.edu.unbosque.vesperaroom.service;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import co.edu.unbosque.vesperaroom.dto.PagoDTO;
import co.edu.unbosque.vesperaroom.entity.Pago;
import co.edu.unbosque.vesperaroom.entity.Pago.Estado;
import co.edu.unbosque.vesperaroom.entity.Reserva;
import co.edu.unbosque.vesperaroom.exception.AccesoDatosException;
import co.edu.unbosque.vesperaroom.exception.CampoObligatorioException;
import co.edu.unbosque.vesperaroom.exception.EstadoReservaException;
import co.edu.unbosque.vesperaroom.exception.LanzadorExcepciones;
import co.edu.unbosque.vesperaroom.exception.MontoExcedidoException;
import co.edu.unbosque.vesperaroom.exception.ReferenciaNoEncontradaException;
import co.edu.unbosque.vesperaroom.exception.RegistroDuplicadoException;
import co.edu.unbosque.vesperaroom.exception.RegistroNoEncontradoException;
import co.edu.unbosque.vesperaroom.exception.TextoInvalidoException;
import co.edu.unbosque.vesperaroom.exception.TransicionEstadoException;
import co.edu.unbosque.vesperaroom.exception.ValorFueraDeRangoException;
import co.edu.unbosque.vesperaroom.repository.PagoRepository;
import co.edu.unbosque.vesperaroom.repository.ReservaRepository;
import co.edu.unbosque.vesperaroom.util.CodigoMySQL;
import co.edu.unbosque.vesperaroom.util.Normalizador;

/**
 * Servicio que gestiona los pagos de las reservas. Una reserva puede pagarse
 * en varias partes, pero la suma de los pagos aprobados y pendientes nunca
 * puede superar su total.
 *
 * <p>
 * No implementa {@link CRUDOperation}: un pago es un registro financiero que no
 * se edita ni se borra; solo cambia de estado.
 * </p>
 *
 * <p>
 * Códigos de {@link #crear(PagoDTO)}:
 * </p>
 * <ul>
 * <li>0: operación exitosa</li>
 * <li>1: monto nulo o menor o igual a cero</li>
 * <li>2: método de pago no indicado</li>
 * <li>3: referencia con más de 100 caracteres</li>
 * <li>4: reserva no indicada o inexistente</li>
 * <li>5: la reserva está cancelada o fue un no-show</li>
 * <li>6: el monto supera el saldo pendiente</li>
 * <li>7: referencia de transacción ya registrada</li>
 * <li>8: error inesperado</li>
 * </ul>
 * <p>
 * Códigos de {@link #cambiarEstado(Long, Estado)}: 0 éxito, 1 estado nulo, 2
 * no encontrado, 3 transición no permitida, 4 error inesperado.
 * </p>
 */
@Service
public class PagoService {

	private static final String ENTIDAD = "pago";

	private final PagoRepository pagoRepository;
	private final ReservaRepository reservaRepository;
	private final ModelMapper mapper;

	public PagoService(PagoRepository pagoRepository, ReservaRepository reservaRepository, ModelMapper mapper) {
		this.pagoRepository = pagoRepository;
		this.reservaRepository = reservaRepository;
		this.mapper = mapper;
	}

	/**
	 * Registra un pago de una reserva. El pago nace {@code PENDIENTE} hasta que el
	 * propietario o un administrador lo apruebe.
	 *
	 * @param data objeto con la reserva, el monto, el método y la referencia
	 * @return código de resultado según la tabla de la clase
	 */
	public int crear(PagoDTO data) {
		try {
			data.setReferenciaTransaccion(Normalizador.textoOpcional(data.getReferenciaTransaccion()));
			LanzadorExcepciones.verificarMonto(data.getMonto(), false, "monto");
			LanzadorExcepciones.verificarObligatorio(data.getMetodoPago(), "el método de pago");
			LanzadorExcepciones.verificarTextoOpcional(data.getReferenciaTransaccion(), 100, "referencia");

			Optional<Reserva> reserva = data.getIdReserva() == null ? Optional.empty()
					: reservaRepository.findById(data.getIdReserva());
			LanzadorExcepciones.verificarReferencia(reserva.isPresent(), "reserva", data.getIdReserva());
			Reserva.Estado estadoReserva = reserva.get().getEstado();
			LanzadorExcepciones.verificarEstadoReserva(
					estadoReserva != Reserva.Estado.CANCELADA && estadoReserva != Reserva.Estado.NO_SHOW,
					"registrar pagos");
			if (data.getReferenciaTransaccion() != null) {
				LanzadorExcepciones.verificarDuplicado(
						pagoRepository.existsByReferencia(data.getReferenciaTransaccion()), "esa referencia");
			}

			Pago entity = mapper.map(data, Pago.class);
			entity.setIdPago(null);
			entity.setFechaPago(LocalDateTime.now());
			entity.setEstado(Estado.PENDIENTE);
			Long idPago = pagoRepository.insertarSiHaySaldo(entity, reserva.get().getTotal());
			LanzadorExcepciones.verificarSaldo(idPago == null);
			return 0;

		} catch (ValorFueraDeRangoException e) {
			return 1;
		} catch (CampoObligatorioException e) {
			return 2;
		} catch (TextoInvalidoException e) {
			return 3;
		} catch (ReferenciaNoEncontradaException e) {
			return 4;
		} catch (EstadoReservaException e) {
			return 5;
		} catch (MontoExcedidoException e) {
			return 6;
		} catch (RegistroDuplicadoException e) {
			return 7;
		} catch (SQLIntegrityConstraintViolationException e) {
			return CodigoMySQL.esLlaveDuplicada(e) ? 7 : 8;
		} catch (Exception e) {
			e.printStackTrace();
			return 8;
		}
	}

	/**
	 * Cambia el estado de un pago: PENDIENTE → APROBADO o RECHAZADO; APROBADO →
	 * REEMBOLSADO. RECHAZADO y REEMBOLSADO son finales.
	 *
	 * @param id    identificador del pago
	 * @param nuevo estado al que se quiere pasar
	 * @return código de resultado según la tabla de la clase
	 */
	public int cambiarEstado(Long id, Estado nuevo) {
		try {
			LanzadorExcepciones.verificarObligatorio(nuevo, "el estado");
			Optional<Pago> found = pagoRepository.findById(id);
			LanzadorExcepciones.verificarExistencia(found.isPresent(), ENTIDAD, id);
			Estado actual = found.get().getEstado();
			boolean permitida = (actual == Estado.PENDIENTE && (nuevo == Estado.APROBADO || nuevo == Estado.RECHAZADO))
					|| (actual == Estado.APROBADO && nuevo == Estado.REEMBOLSADO);
			LanzadorExcepciones.verificarTransicion(permitida);
			pagoRepository.actualizarEstado(id, nuevo);
			return 0;
		} catch (CampoObligatorioException e) {
			return 1;
		} catch (RegistroNoEncontradoException e) {
			return 2;
		} catch (TransicionEstadoException e) {
			return 3;
		} catch (Exception e) {
			e.printStackTrace();
			return 4;
		}
	}

	/**
	 * Retorna todos los pagos, del más reciente al más antiguo.
	 *
	 * @return lista de {@link PagoDTO}
	 * @throws AccesoDatosException si falla la consulta a la base de datos
	 */
	public List<PagoDTO> getAll() {
		try {
			return toDtoList(pagoRepository.findAll());
		} catch (SQLException e) {
			throw new AccesoDatosException(e);
		}
	}

	/**
	 * Retorna los pagos de una reserva en orden cronológico.
	 *
	 * @param idReserva identificador de la reserva
	 * @return lista de {@link PagoDTO}
	 * @throws AccesoDatosException si falla la consulta a la base de datos
	 */
	public List<PagoDTO> getByReserva(Long idReserva) {
		try {
			return toDtoList(pagoRepository.findByReserva(idReserva));
		} catch (SQLException e) {
			throw new AccesoDatosException(e);
		}
	}

	public long count() {
		try {
			return pagoRepository.count();
		} catch (SQLException e) {
			throw new AccesoDatosException(e);
		}
	}

	/**
	 * Busca y retorna un pago según su ID.
	 *
	 * @param id identificador del pago
	 * @return {@link PagoDTO} encontrado, o {@code null} si no existe
	 * @throws AccesoDatosException si falla la consulta a la base de datos
	 */
	public PagoDTO getById(Long id) {
		try {
			Optional<Pago> found = pagoRepository.findById(id);

			if (found.isPresent()) {
				return mapper.map(found.get(), PagoDTO.class);
			}

			return null;
		} catch (SQLException e) {
			throw new AccesoDatosException(e);
		}
	}

	/**
	 * Convierte una lista de entidades en una lista de DTOs.
	 *
	 * @param entityList lista de entidades
	 * @return lista de {@link PagoDTO}
	 */
	private List<PagoDTO> toDtoList(List<Pago> entityList) {
		List<PagoDTO> dtoList = new ArrayList<>();

		entityList.forEach(entity -> {
			dtoList.add(mapper.map(entity, PagoDTO.class));
		});

		return dtoList;
	}
}
