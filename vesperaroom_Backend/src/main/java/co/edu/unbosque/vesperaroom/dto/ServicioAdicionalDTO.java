package co.edu.unbosque.vesperaroom.dto;

import java.math.BigDecimal;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Objeto de transferencia de datos que representa un servicio adicional. El
 * campo {@code idServicio} es de solo lectura en la serialización JSON.
 */
public class ServicioAdicionalDTO {

	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private Long idServicio;

	private Long idMotel;

	private String nombre;

	private String descripcion;

	private BigDecimal precio;

	private Boolean activo;

	public ServicioAdicionalDTO() {
	}

	public ServicioAdicionalDTO(Long idMotel, String nombre, String descripcion, BigDecimal precio) {
		this.idMotel = idMotel;
		this.nombre = nombre;
		this.descripcion = descripcion;
		this.precio = precio;
	}

	public Long getIdServicio() {
		return idServicio;
	}

	public void setIdServicio(Long idServicio) {
		this.idServicio = idServicio;
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

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public BigDecimal getPrecio() {
		return precio;
	}

	public void setPrecio(BigDecimal precio) {
		this.precio = precio;
	}

	public Boolean getActivo() {
		return activo;
	}

	public void setActivo(Boolean activo) {
		this.activo = activo;
	}

	@Override
	public String toString() {
		return "ServicioAdicionalDTO [idServicio=" + idServicio + ", idMotel=" + idMotel + ", nombre="
				+ nombre + ", descripcion=" + descripcion + ", precio=" + precio + ", activo=" + activo + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(activo, descripcion, idMotel, idServicio, nombre, precio);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ServicioAdicionalDTO other = (ServicioAdicionalDTO) obj;
		return Objects.equals(activo, other.activo) && Objects.equals(descripcion, other.descripcion)
				&& Objects.equals(idMotel, other.idMotel) && Objects.equals(idServicio, other.idServicio)
				&& Objects.equals(nombre, other.nombre) && Objects.equals(precio, other.precio);
	}
}
