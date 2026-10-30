package co.edu.unbosque.vesperaroom.dto;

import java.time.LocalDateTime;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Objeto de transferencia de datos que representa una reseña. El
 * identificador y la fecha son de solo lectura.
 */
public class ResenaDTO {

	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private Long idResena;

	private Long idReserva;

	private Integer calificacion;

	private String comentario;

	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private LocalDateTime fecha;

	public ResenaDTO() {
	}

	public ResenaDTO(Long idReserva, Integer calificacion, String comentario) {
		this.idReserva = idReserva;
		this.calificacion = calificacion;
		this.comentario = comentario;
	}

	public Long getIdResena() {
		return idResena;
	}

	public void setIdResena(Long idResena) {
		this.idResena = idResena;
	}

	public Long getIdReserva() {
		return idReserva;
	}

	public void setIdReserva(Long idReserva) {
		this.idReserva = idReserva;
	}

	public Integer getCalificacion() {
		return calificacion;
	}

	public void setCalificacion(Integer calificacion) {
		this.calificacion = calificacion;
	}

	public String getComentario() {
		return comentario;
	}

	public void setComentario(String comentario) {
		this.comentario = comentario;
	}

	public LocalDateTime getFecha() {
		return fecha;
	}

	public void setFecha(LocalDateTime fecha) {
		this.fecha = fecha;
	}

	@Override
	public String toString() {
		return "ResenaDTO [idResena=" + idResena + ", idReserva=" + idReserva + ", calificacion="
				+ calificacion + ", comentario=" + comentario + ", fecha=" + fecha + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(calificacion, comentario, fecha, idResena, idReserva);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ResenaDTO other = (ResenaDTO) obj;
		return Objects.equals(calificacion, other.calificacion)
				&& Objects.equals(comentario, other.comentario) && Objects.equals(fecha, other.fecha)
				&& Objects.equals(idResena, other.idResena) && Objects.equals(idReserva, other.idReserva);
	}
}
