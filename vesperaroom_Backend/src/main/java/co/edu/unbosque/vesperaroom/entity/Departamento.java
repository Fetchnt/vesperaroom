package co.edu.unbosque.vesperaroom.entity;

import java.util.Objects;

/**
 * Entidad que representa un departamento de Colombia y se corresponde con la
 * tabla {@code departamento}. Es un catálogo que surge de la normalización a
 * 3FN: la dependencia transitiva {@code ciudad -> departamento} se separa del
 * motel.
 */
public class Departamento {

	private Long idDepartamento;
	private String nombre;

	public Departamento() {
	}

	public Departamento(String nombre) {
		this.nombre = nombre;
	}

	@Override
	public String toString() {
		return "Departamento [idDepartamento=" + idDepartamento + ", nombre=" + nombre + "]";
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
		Departamento other = (Departamento) obj;
		return Objects.equals(idDepartamento, other.idDepartamento) && Objects.equals(nombre, other.nombre);
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
}
