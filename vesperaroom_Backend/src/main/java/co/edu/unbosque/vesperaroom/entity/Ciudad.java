package co.edu.unbosque.vesperaroom.entity;

import java.util.Objects;

/**
 * Entidad que representa una ciudad o municipio y se corresponde con la tabla
 * {@code ciudad}. Su nombre solo es único dentro de su departamento, porque
 * existen municipios homónimos.
 */
public class Ciudad {

	private Long idCiudad;
	private Long idDepartamento;
	private String nombre;

	public Ciudad() {
	}

	public Ciudad(Long idDepartamento, String nombre) {
		this.idDepartamento = idDepartamento;
		this.nombre = nombre;
	}

	@Override
	public String toString() {
		return "Ciudad [idCiudad=" + idCiudad + ", idDepartamento=" + idDepartamento + ", nombre=" + nombre
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
		Ciudad other = (Ciudad) obj;
		return Objects.equals(idCiudad, other.idCiudad)
				&& Objects.equals(idDepartamento, other.idDepartamento)
				&& Objects.equals(nombre, other.nombre);
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
}
