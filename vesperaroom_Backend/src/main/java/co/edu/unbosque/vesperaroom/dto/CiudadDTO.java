package co.edu.unbosque.vesperaroom.dto;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Objeto de transferencia de datos que representa una ciudad. El campo
 * {@code idCiudad} es de solo lectura en la serialización JSON.
 */
public class CiudadDTO {

	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private Long idCiudad;

	private Long idDepartamento;

	private String nombre;

	public CiudadDTO() {
	}

	public CiudadDTO(Long idDepartamento, String nombre) {
		this.idDepartamento = idDepartamento;
		this.nombre = nombre;
	}

	public Long getIdCiudad() {
		return idCiudad;
	}

	public void setIdCiudad(Long idCiudad) {
		this.idCiudad = idCiudad;
	}

	public Long getIdDepartamento() {
		return idDepartamento;
	}

	public void setIdDepartamento(Long idDepartamento) {
		this.idDepartamento = idDepartamento;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	@Override
	public String toString() {
		return "CiudadDTO [idCiudad=" + idCiudad + ", idDepartamento=" + idDepartamento + ", nombre=" + nombre
				+ "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(idCiudad, idDepartamento, nombre);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CiudadDTO other = (CiudadDTO) obj;
		return Objects.equals(idCiudad, other.idCiudad)
				&& Objects.equals(idDepartamento, other.idDepartamento)
				&& Objects.equals(nombre, other.nombre);
	}
}
