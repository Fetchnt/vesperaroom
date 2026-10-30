package co.edu.unbosque.vesperaroom.dto;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Objeto de transferencia de datos que representa una foto. Debe indicar
 * {@code idMotel} o {@code idHabitacion}, pero no ambos.
 */
public class FotoDTO {

	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private Long idFoto;

	private Long idMotel;

	private Long idHabitacion;

	private String uri;

	private Integer orden;

	public FotoDTO() {
	}

	public FotoDTO(Long idMotel, Long idHabitacion, String uri, Integer orden) {
		this.idMotel = idMotel;
		this.idHabitacion = idHabitacion;
		this.uri = uri;
		this.orden = orden;
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

	@Override
	public String toString() {
		return "FotoDTO [idFoto=" + idFoto + ", idMotel=" + idMotel + ", idHabitacion=" + idHabitacion
				+ ", uri=" + uri + ", orden=" + orden + "]";
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
		FotoDTO other = (FotoDTO) obj;
		return Objects.equals(idFoto, other.idFoto) && Objects.equals(idHabitacion, other.idHabitacion)
				&& Objects.equals(idMotel, other.idMotel) && Objects.equals(orden, other.orden)
				&& Objects.equals(uri, other.uri);
	}
}
