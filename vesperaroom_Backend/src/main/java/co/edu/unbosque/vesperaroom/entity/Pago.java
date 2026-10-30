package co.edu.unbosque.vesperaroom.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidad que representa un pago (total o parcial) de una reserva y se
 * corresponde con la tabla {@code pago}. Una reserva puede tener varios pagos.
 */
public class Pago {

	private Long idPago;
	private Long idReserva;
	private BigDecimal monto;
	private LocalDateTime fechaPago;
	private MetodoPago metodoPago;
	private Estado estado;
	private String referenciaTransaccion;

	/**
	 * Define los medios de pago aceptados.
	 */
	public enum MetodoPago {
		EFECTIVO, TARJETA_CREDITO, TARJETA_DEBITO, PSE, NEQUI, DAVIPLATA
	}

	/**
	 * Define el ciclo de vida de un pago.
	 */
	public enum Estado {
		PENDIENTE, APROBADO, RECHAZADO, REEMBOLSADO
	}

	/**
	 * Constructor por defecto que inicializa el pago con estado {@code PENDIENTE}.
	 */
	public Pago() {
		this.estado = Estado.PENDIENTE;
	}

	public Pago(Long idReserva, BigDecimal monto, MetodoPago metodoPago, String referenciaTransaccion) {
		this();
		this.idReserva = idReserva;
		this.monto = monto;
		this.metodoPago = metodoPago;
		this.referenciaTransaccion = referenciaTransaccion;
	}

	@Override
	public String toString() {
		return "Pago [idPago=" + idPago + ", idReserva=" + idReserva + ", monto=" + monto + ", fechaPago="
				+ fechaPago + ", metodoPago=" + metodoPago + ", estado=" + estado + ", referenciaTransaccion="
				+ referenciaTransaccion + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(estado, fechaPago, idPago, idReserva, metodoPago, monto, referenciaTransaccion);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Pago other = (Pago) obj;
		return estado == other.estado && Objects.equals(fechaPago, other.fechaPago)
				&& Objects.equals(idPago, other.idPago) && Objects.equals(idReserva, other.idReserva)
				&& metodoPago == other.metodoPago && Objects.equals(monto, other.monto)
				&& Objects.equals(referenciaTransaccion, other.referenciaTransaccion);
	}

	public Long getIdPago() {
		return idPago;
	}

	public void setIdPago(Long idPago) {
		this.idPago = idPago;
	}

	public Long getIdReserva() {
		return idReserva;
	}

	public void setIdReserva(Long idReserva) {
		this.idReserva = idReserva;
	}

	public BigDecimal getMonto() {
		return monto;
	}

	public void setMonto(BigDecimal monto) {
		this.monto = monto;
	}

	public LocalDateTime getFechaPago() {
		return fechaPago;
	}

	public void setFechaPago(LocalDateTime fechaPago) {
		this.fechaPago = fechaPago;
	}

	public MetodoPago getMetodoPago() {
		return metodoPago;
	}

	public void setMetodoPago(MetodoPago metodoPago) {
		this.metodoPago = metodoPago;
	}

	public Estado getEstado() {
		return estado;
	}

	public void setEstado(Estado estado) {
		this.estado = estado;
	}

	public String getReferenciaTransaccion() {
		return referenciaTransaccion;
	}

	public void setReferenciaTransaccion(String referenciaTransaccion) {
		this.referenciaTransaccion = referenciaTransaccion;
	}
}
