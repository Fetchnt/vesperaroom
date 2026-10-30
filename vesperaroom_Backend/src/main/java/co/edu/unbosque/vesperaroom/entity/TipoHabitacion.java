package co.edu.unbosque.vesperaroom.entity;

import java.util.Objects;

/**
 * Entidad que representa un tipo de habitación ofrecido por un motel (por
 * ejemplo, Estándar o Suite) y se corresponde con la tabla
 * {@code tipo_habitacion}. Sus características se guardan en la tabla
 * intermedia {@code tipo_habitacion_caracteristica}.
 */
public class TipoHabitacion {

	private Long idTipo;
	private Long idMotel;
	private String nombre;
	private Integer capacidadPersonas;
	private String descripcion;

	public TipoHabitacion() {
	}

	public TipoHabitacion(Long idMotel, String nombre, Integer capacidadPersonas, String descripcion) {
		this.idMotel = idMotel;
		this.nombre = nombre;
		this.capacidadPersonas = capacidadPersonas;
		this.descripcion = descripcion;
	}

	@Override
	public String toString() {
		return "TipoHabitacion [idTipo=" + idTipo + ", idMotel=" + idMotel + ", nombre=" + nombre
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
		TipoHabitacion other = (TipoHabitacion) obj;
		return Objects.equals(capacidadPersonas, other.capacidadPersonas)
				&& Objects.equals(descripcion, other.descripcion) && Objects.equals(idMotel, other.idMotel)
				&& Objects.equals(idTipo, other.idTipo) && Objects.equals(nombre, other.nombre);
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
}
