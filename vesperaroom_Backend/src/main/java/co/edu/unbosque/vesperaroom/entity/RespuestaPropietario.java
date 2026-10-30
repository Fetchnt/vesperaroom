package co.edu.unbosque.vesperaroom.entity;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidad débil que representa la respuesta del propietario a una reseña y se
 * corresponde con la tabla {@code respuesta_propietario}. Es una relación 1:1
 * identificadora con {@link Resena}: su identificador es el mismo
 * {@code id_resena}.
 */
public class RespuestaPropietario {

	private Long idResena;
	private String comentario;
	private LocalDateTime fecha;

	public RespuestaPropietario() {
	}

	public RespuestaPropietario(Long idResena, String comentario) {
		this.idResena = idResena;
		this.comentario = comentario;
	}

	@Override
	public String toString() {
		return "RespuestaPropietario [idResena=" + idResena + ", comentario=" + comentario + ", fecha="
				+ fecha + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(comentario, fecha, idResena);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RespuestaPropietario other = (RespuestaPropietario) obj;
		return Objects.equals(comentario, other.comentario) && Objects.equals(fecha, other.fecha)
				&& Objects.equals(idResena, other.idResena);
	}

	public Long getIdResena() {
		return idResena;
	}

	public void setIdResena(Long idResena) {
		this.idResena = idResena;
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
}
