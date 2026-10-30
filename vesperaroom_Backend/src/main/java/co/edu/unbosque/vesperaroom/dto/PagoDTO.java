package co.edu.unbosque.vesperaroom.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

import co.edu.unbosque.vesperaroom.entity.Pago.Estado;
import co.edu.unbosque.vesperaroom.entity.Pago.MetodoPago;

/**
 * Objeto de transferencia de datos que representa un pago. La fecha y el
 * estado son de solo lectura: los asigna el servidor.
 */
public class PagoDTO {

	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private Long idPago;

	private Long idReserva;

	private BigDecimal monto;

	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private LocalDateTime fechaPago;

	private MetodoPago metodoPago;

	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private Estado estado;

	private String referenciaTransaccion;

	public PagoDTO() {
	}

	public PagoDTO(Long idReserva, BigDecimal monto, MetodoPago metodoPago, String referenciaTransaccion) {
		this.idReserva = idReserva;
		this.monto = monto;
		this.metodoPago = metodoPago;
		this.referenciaTransaccion = referenciaTransaccion;
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

	@Override
	public String toString() {
		return "PagoDTO [idPago=" + idPago + ", idReserva=" + idReserva + ", monto=" + monto + ", fechaPago="
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
		PagoDTO other = (PagoDTO) obj;
		return estado == other.estado && Objects.equals(fechaPago, other.fechaPago)
				&& Objects.equals(idPago, other.idPago) && Objects.equals(idReserva, other.idReserva)
				&& metodoPago == other.metodoPago && Objects.equals(monto, other.monto)
				&& Objects.equals(referenciaTransaccion, other.referenciaTransaccion);
	}
}
