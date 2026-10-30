package co.edu.unbosque.vesperaroom.dto;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Objeto de transferencia de datos que representa una característica. El campo
 * {@code idCaracteristica} es de solo lectura en la serialización JSON.
 */
public class CaracteristicaDTO {

	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private Long idCaracteristica;

	private String nombre;

	private String icono;

	public CaracteristicaDTO() {
	}

	public CaracteristicaDTO(String nombre, String icono) {
		this.nombre = nombre;
		this.icono = icono;
	}

	public Long getIdCaracteristica() {
		return idCaracteristica;
	}

	public void setIdCaracteristica(Long idCaracteristica) {
		this.idCaracteristica = idCaracteristica;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getIcono() {
		return icono;
	}

	public void setIcono(String icono) {
		this.icono = icono;
	}

	@Override
	public String toString() {
		return "CaracteristicaDTO [idCaracteristica=" + idCaracteristica + ", nombre=" + nombre + ", icono="
				+ icono + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(icono, idCaracteristica, nombre);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CaracteristicaDTO other = (CaracteristicaDTO) obj;
		return Objects.equals(icono, other.icono) && Objects.equals(idCaracteristica, other.idCaracteristica)
				&& Objects.equals(nombre, other.nombre);
	}
}
