package co.edu.unbosque.vesperaroom.dto;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

import co.edu.unbosque.vesperaroom.entity.Habitacion.Estado;

/**
 * Objeto de transferencia de datos que representa una habitación.
 * {@code idHabitacion} e {@code idMotel} son de solo lectura: el motel se toma
 * siempre del tipo de habitación.
 */
public class HabitacionDTO {

	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private Long idHabitacion;

	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private Long idMotel;

	private Long idTipo;

	private String numero;

	private Integer piso;

	private Estado estado;

	public HabitacionDTO() {
	}

	public HabitacionDTO(Long idTipo, String numero, Integer piso) {
		this.idTipo = idTipo;
		this.numero = numero;
		this.piso = piso;
	}

	public Long getIdHabitacion() {
		return idHabitacion;
	}

	public void setIdHabitacion(Long idHabitacion) {
		this.idHabitacion = idHabitacion;
	}

	public Long getIdMotel() {
		return idMotel;
	}

	public void setIdMotel(Long idMotel) {
		this.idMotel = idMotel;
	}

	public Long getIdTipo() {
		return idTipo;
	}

	public void setIdTipo(Long idTipo) {
		this.idTipo = idTipo;
	}

	public String getNumero() {
		return numero;
	}

	public void setNumero(String numero) {
		this.numero = numero;
	}

	public Integer getPiso() {
		return piso;
	}

	public void setPiso(Integer piso) {
		this.piso = piso;
	}

	public Estado getEstado() {
		return estado;
	}

	public void setEstado(Estado estado) {
		this.estado = estado;
	}

	@Override
	public String toString() {
		return "HabitacionDTO [idHabitacion=" + idHabitacion + ", idMotel=" + idMotel + ", idTipo=" + idTipo
				+ ", numero=" + numero + ", piso=" + piso + ", estado=" + estado + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(estado, idHabitacion, idMotel, idTipo, numero, piso);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		HabitacionDTO other = (HabitacionDTO) obj;
		return estado == other.estado && Objects.equals(idHabitacion, other.idHabitacion)
				&& Objects.equals(idMotel, other.idMotel) && Objects.equals(idTipo, other.idTipo)
				&& Objects.equals(numero, other.numero) && Objects.equals(piso, other.piso);
	}
}
