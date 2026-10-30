package co.edu.unbosque.vesperaroom.dto;

import java.math.BigDecimal;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Objeto de transferencia de datos que representa un servicio dentro de una
 * reserva. El cliente envía {@code idServicio} y {@code cantidad};
 * {@code precioUnitario} lo fija el servidor con el precio vigente del servicio.
 */
public class ReservaServicioDTO {

	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private Long idReserva;

	private Long idServicio;

	private Integer cantidad;

	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private BigDecimal precioUnitario;

	public ReservaServicioDTO() {
	}

	public ReservaServicioDTO(Long idServicio, Integer cantidad) {
		this.idServicio = idServicio;
		this.cantidad = cantidad;
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

	@Override
	public String toString() {
		return "ReservaServicioDTO [idReserva=" + idReserva + ", idServicio=" + idServicio + ", cantidad="
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
		ReservaServicioDTO other = (ReservaServicioDTO) obj;
		return Objects.equals(cantidad, other.cantidad) && Objects.equals(idReserva, other.idReserva)
				&& Objects.equals(idServicio, other.idServicio)
				&& Objects.equals(precioUnitario, other.precioUnitario);
	}
}
