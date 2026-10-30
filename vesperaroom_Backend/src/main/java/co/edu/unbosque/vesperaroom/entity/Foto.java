package co.edu.unbosque.vesperaroom.entity;

import java.util.Objects;

/**
 * Entidad que representa una foto publicada en el catálogo y se corresponde con
 * la tabla {@code foto}. Pertenece a un motel o a una habitación, nunca a ambos:
 * exactamente uno de {@code idMotel} e {@code idHabitacion} tiene valor.
 */
public class Foto {

	private Long idFoto;
	private Long idMotel;
	private Long idHabitacion;
	private String uri;
	private Integer orden;

	public Foto() {
	}

	public Foto(Long idMotel, Long idHabitacion, String uri, Integer orden) {
		this.idMotel = idMotel;
		this.idHabitacion = idHabitacion;
		this.uri = uri;
		this.orden = orden;
	}

	@Override
	public String toString() {
		return "Foto [idFoto=" + idFoto + ", idMotel=" + idMotel + ", idHabitacion=" + idHabitacion + ", uri="
				+ uri + ", orden=" + orden + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(idFoto, idHabitacion, idMotel, orden, uri);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Foto other = (Foto) obj;
		return Objects.equals(idFoto, other.idFoto) && Objects.equals(idHabitacion, other.idHabitacion)
				&& Objects.equals(idMotel, other.idMotel) && Objects.equals(orden, other.orden)
				&& Objects.equals(uri, other.uri);
	}

	public Long getIdFoto() {
		return idFoto;
	}

	public void setIdFoto(Long idFoto) {
		this.idFoto = idFoto;
	}

	public Long getIdMotel() {
		return idMotel;
	}

	public void setIdMotel(Long idMotel) {
		this.idMotel = idMotel;
	}

	public Long getIdHabitacion() {
		return idHabitacion;
	}

	public void setIdHabitacion(Long idHabitacion) {
		this.idHabitacion = idHabitacion;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public Integer getOrden() {
		return orden;
	}

	public void setOrden(Integer orden) {
		this.orden = orden;
	}
}
