package co.edu.unbosque.vesperaroom.entity;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Entidad que representa un servicio adicional incluido en una reserva y se
 * corresponde con la tabla intermedia {@code reserva_servicio}, que resuelve la
 * relación N:M entre reserva y servicio. {@code precioUnitario} es el precio al
 * momento de la compra: depende de la pareja (reserva, servicio), no solo del
 * servicio, por eso no viola 2FN.
 */
public class ReservaServicio {

	private Long idReserva;
	private Long idServicio;
	private Integer cantidad;
	private BigDecimal precioUnitario;

	public ReservaServicio() {
	}

	public ReservaServicio(Long idReserva, Long idServicio, Integer cantidad, BigDecimal precioUnitario) {
		this.idReserva = idReserva;
		this.idServicio = idServicio;
		this.cantidad = cantidad;
		this.precioUnitario = precioUnitario;
	}

	@Override
	public String toString() {
		return "ReservaServicio [idReserva=" + idReserva + ", idServicio=" + idServicio + ", cantidad="
				+ cantidad + ", precioUnitario=" + precioUnitario + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(cantidad, idReserva, idServicio, precioUnitario);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ReservaServicio other = (ReservaServicio) obj;
		return Objects.equals(cantidad, other.cantidad) && Objects.equals(idReserva, other.idReserva)
				&& Objects.equals(idServicio, other.idServicio)
				&& Objects.equals(precioUnitario, other.precioUnitario);
	}

	public Long getIdReserva() {
		return idReserva;
	}

	public void setIdReserva(Long idReserva) {
		this.idReserva = idReserva;
	}

	public Long getIdServicio() {
		return idServicio;
	}

	public void setIdServicio(Long idServicio) {
		this.idServicio = idServicio;
	}

	public Integer getCantidad() {
		return cantidad;
	}

	public void setCantidad(Integer cantidad) {
		this.cantidad = cantidad;
	}

	public BigDecimal getPrecioUnitario() {
		return precioUnitario;
	}

	public void setPrecioUnitario(BigDecimal precioUnitario) {
		this.precioUnitario = precioUnitario;
	}
}
