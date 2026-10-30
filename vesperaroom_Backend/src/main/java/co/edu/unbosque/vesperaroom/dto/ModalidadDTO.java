package co.edu.unbosque.vesperaroom.dto;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Objeto de transferencia de datos que representa una modalidad de alquiler.
 * El campo {@code idModalidad} es de solo lectura en la serialización JSON.
 */
public class ModalidadDTO {

	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private Long idModalidad;

	private String nombre;

	private Integer duracionHoras;

	public ModalidadDTO() {
	}

	public ModalidadDTO(String nombre, Integer duracionHoras) {
		this.nombre = nombre;
		this.duracionHoras = duracionHoras;
	}

	public Long getIdModalidad() {
		return idModalidad;
	}

	public void setIdModalidad(Long idModalidad) {
		this.idModalidad = idModalidad;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public Integer getDuracionHoras() {
		return duracionHoras;
	}

	public void setDuracionHoras(Integer duracionHoras) {
		this.duracionHoras = duracionHoras;
	}

	@Override
	public String toString() {
		return "ModalidadDTO [idModalidad=" + idModalidad + ", nombre=" + nombre + ", duracionHoras="
				+ duracionHoras + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(duracionHoras, idModalidad, nombre);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ModalidadDTO other = (ModalidadDTO) obj;
		return Objects.equals(duracionHoras, other.duracionHoras)
				&& Objects.equals(idModalidad, other.idModalidad) && Objects.equals(nombre, other.nombre);
	}
}
