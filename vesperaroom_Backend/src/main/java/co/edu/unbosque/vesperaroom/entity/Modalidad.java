package co.edu.unbosque.vesperaroom.entity;

import java.util.Objects;

/**
 * Entidad que representa una modalidad de alquiler (Rato, Noche...) y se
 * corresponde con la tabla {@code modalidad}. Surge de la normalización a 3FN:
 * la dependencia {@code nombre_modalidad -> duracion_horas} se separa de la
 * tarifa.
 */
public class Modalidad {

	private Long idModalidad;
	private String nombre;
	private Integer duracionHoras;

	public Modalidad() {
	}

	public Modalidad(String nombre, Integer duracionHoras) {
		this.nombre = nombre;
		this.duracionHoras = duracionHoras;
	}

	@Override
	public String toString() {
		return "Modalidad [idModalidad=" + idModalidad + ", nombre=" + nombre + ", duracionHoras="
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
		Modalidad other = (Modalidad) obj;
		return Objects.equals(duracionHoras, other.duracionHoras)
				&& Objects.equals(idModalidad, other.idModalidad) && Objects.equals(nombre, other.nombre);
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
}
