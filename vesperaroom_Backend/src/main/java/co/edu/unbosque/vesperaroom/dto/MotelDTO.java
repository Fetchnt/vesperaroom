package co.edu.unbosque.vesperaroom.dto;

import java.math.BigDecimal;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

import co.edu.unbosque.vesperaroom.entity.Motel.Estado;

/**
 * Objeto de transferencia de datos que representa un motel. El campo
 * {@code idMotel} es de solo lectura en la serialización JSON.
 */
public class MotelDTO {

	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private Long idMotel;

	private Long idPropietario;

	private Long idCiudad;

	private String nombre;

	private String descripcion;

	private String calle;

	private String carrera;

	private BigDecimal latitud;

	private BigDecimal longitud;

	private Estado estado;

	public MotelDTO() {
	}

	public MotelDTO(Long idPropietario, Long idCiudad, String nombre, String descripcion, String calle,
			String carrera, BigDecimal latitud, BigDecimal longitud) {
		this.idPropietario = idPropietario;
		this.idCiudad = idCiudad;
		this.nombre = nombre;
		this.descripcion = descripcion;
		this.calle = calle;
		this.carrera = carrera;
		this.latitud = latitud;
		this.longitud = longitud;
	}

	public Long getIdMotel() {
		return idMotel;
	}

	public void setIdMotel(Long idMotel) {
		this.idMotel = idMotel;
	}

	public Long getIdPropietario() {
		return idPropietario;
	}

	public void setIdPropietario(Long idPropietario) {
		this.idPropietario = idPropietario;
	}

	public Long getIdCiudad() {
		return idCiudad;
	}

	public void setIdCiudad(Long idCiudad) {
		this.idCiudad = idCiudad;
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

	public String getCalle() {
		return calle;
	}

	public void setCalle(String calle) {
		this.calle = calle;
	}

	public String getCarrera() {
		return carrera;
	}

	public void setCarrera(String carrera) {
		this.carrera = carrera;
	}

	public BigDecimal getLatitud() {
		return latitud;
	}

	public void setLatitud(BigDecimal latitud) {
		this.latitud = latitud;
	}

	public BigDecimal getLongitud() {
		return longitud;
	}

	public void setLongitud(BigDecimal longitud) {
		this.longitud = longitud;
	}

	public Estado getEstado() {
		return estado;
	}

	public void setEstado(Estado estado) {
		this.estado = estado;
	}

	@Override
	public String toString() {
		return "MotelDTO [idMotel=" + idMotel + ", idPropietario=" + idPropietario + ", idCiudad=" + idCiudad
				+ ", nombre=" + nombre + ", descripcion=" + descripcion + ", calle=" + calle + ", carrera="
				+ carrera + ", latitud=" + latitud + ", longitud=" + longitud + ", estado=" + estado + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(calle, carrera, descripcion, estado, idCiudad, idMotel, idPropietario, latitud,
				longitud, nombre);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MotelDTO other = (MotelDTO) obj;
		return Objects.equals(calle, other.calle) && Objects.equals(carrera, other.carrera)
				&& Objects.equals(descripcion, other.descripcion) && estado == other.estado
				&& Objects.equals(idCiudad, other.idCiudad) && Objects.equals(idMotel, other.idMotel)
				&& Objects.equals(idPropietario, other.idPropietario)
				&& Objects.equals(latitud, other.latitud) && Objects.equals(longitud, other.longitud)
				&& Objects.equals(nombre, other.nombre);
	}
}
