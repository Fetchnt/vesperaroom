package co.edu.unbosque.vesperaroom.entity;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Entidad que representa un servicio adicional que un motel vende con la
 * reserva (decoración, bebidas...) y se corresponde con la tabla
 * {@code servicio_adicional}.
 */
public class ServicioAdicional {

	private Long idServicio;
	private Long idMotel;
	private String nombre;
	private String descripcion;
	private BigDecimal precio;
	private Boolean activo;

	/**
	 * Constructor por defecto que inicializa el servicio como activo.
	 */
	public ServicioAdicional() {
		this.activo = Boolean.TRUE;
	}

	public ServicioAdicional(Long idMotel, String nombre, String descripcion, BigDecimal precio) {
		this();
		this.idMotel = idMotel;
		this.nombre = nombre;
		this.descripcion = descripcion;
		this.precio = precio;
	}

	@Override
	public String toString() {
		return "ServicioAdicional [idServicio=" + idServicio + ", idMotel=" + idMotel + ", nombre=" + nombre
				+ ", descripcion=" + descripcion + ", precio=" + precio + ", activo=" + activo + "]";
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
		ServicioAdicional other = (ServicioAdicional) obj;
		return Objects.equals(activo, other.activo) && Objects.equals(descripcion, other.descripcion)
				&& Objects.equals(idMotel, other.idMotel) && Objects.equals(idServicio, other.idServicio)
				&& Objects.equals(nombre, other.nombre) && Objects.equals(precio, other.precio);
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
}
