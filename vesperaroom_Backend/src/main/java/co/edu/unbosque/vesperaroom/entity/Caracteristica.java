package co.edu.unbosque.vesperaroom.entity;

import java.util.Objects;

/**
 * Entidad que representa una característica que puede tener un tipo de
 * habitación (jacuzzi, sauna, TV...) y se corresponde con la tabla
 * {@code caracteristica}. Es un catálogo global administrado por el
 * administrador.
 */
public class Caracteristica {

	private Long idCaracteristica;
	private String nombre;
	private String icono;

	public Caracteristica() {
	}

	public Caracteristica(String nombre, String icono) {
		this.nombre = nombre;
		this.icono = icono;
	}

	@Override
	public String toString() {
		return "Caracteristica [idCaracteristica=" + idCaracteristica + ", nombre=" + nombre + ", icono="
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
		Caracteristica other = (Caracteristica) obj;
		return Objects.equals(icono, other.icono) && Objects.equals(idCaracteristica, other.idCaracteristica)
				&& Objects.equals(nombre, other.nombre);
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
}
