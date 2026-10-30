package co.edu.unbosque.vesperaroom.dto;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Objeto de transferencia de datos que representa un tipo de habitación. El
 * campo {@code idTipo} es de solo lectura en la serialización JSON.
 */
public class TipoHabitacionDTO {

	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private Long idTipo;

	private Long idMotel;

	private String nombre;

	private Integer capacidadPersonas;

	private String descripcion;

	public TipoHabitacionDTO() {
	}

	public TipoHabitacionDTO(Long idMotel, String nombre, Integer capacidadPersonas, String descripcion) {
		this.idMotel = idMotel;
		this.nombre = nombre;
		this.capacidadPersonas = capacidadPersonas;
		this.descripcion = descripcion;
	}

	public Long getIdTipo() {
		return idTipo;
	}

	public void setIdTipo(Long idTipo) {
		this.idTipo = idTipo;
	}

	public Long getIdMotel() {
		return idMotel;
	}

	public void setIdMotel(Long idMotel) {
		this.idMotel = idMotel;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public Integer getCapacidadPersonas() {
		return capacidadPersonas;
	}

	public void setCapacidadPersonas(Integer capacidadPersonas) {
		this.capacidadPersonas = capacidadPersonas;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	@Override
	public String toString() {
		return "TipoHabitacionDTO [idTipo=" + idTipo + ", idMotel=" + idMotel + ", nombre=" + nombre
				+ ", capacidadPersonas=" + capacidadPersonas + ", descripcion=" + descripcion + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(capacidadPersonas, descripcion, idMotel, idTipo, nombre);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TipoHabitacionDTO other = (TipoHabitacionDTO) obj;
		return Objects.equals(capacidadPersonas, other.capacidadPersonas)
				&& Objects.equals(descripcion, other.descripcion) && Objects.equals(idMotel, other.idMotel)
				&& Objects.equals(idTipo, other.idTipo) && Objects.equals(nombre, other.nombre);
	}
}
