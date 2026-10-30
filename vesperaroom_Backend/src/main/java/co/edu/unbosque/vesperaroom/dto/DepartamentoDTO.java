package co.edu.unbosque.vesperaroom.dto;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Objeto de transferencia de datos que representa un departamento. El campo
 * {@code idDepartamento} es de solo lectura en la serialización JSON.
 */
public class DepartamentoDTO {

	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private Long idDepartamento;

	private String nombre;

	public DepartamentoDTO() {
	}

	public DepartamentoDTO(String nombre) {
		this.nombre = nombre;
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
		return "DepartamentoDTO [idDepartamento=" + idDepartamento + ", nombre=" + nombre + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(idDepartamento, nombre);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DepartamentoDTO other = (DepartamentoDTO) obj;
		return Objects.equals(idDepartamento, other.idDepartamento) && Objects.equals(nombre, other.nombre);
	}
}
