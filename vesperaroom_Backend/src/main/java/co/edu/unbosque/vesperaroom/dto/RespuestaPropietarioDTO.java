package co.edu.unbosque.vesperaroom.dto;

import java.time.LocalDateTime;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Objeto de transferencia de datos que representa la respuesta de un
 * propietario a una reseña. La fecha es de solo lectura.
 */
public class RespuestaPropietarioDTO {

	private Long idResena;

	private String comentario;

	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private LocalDateTime fecha;

	public RespuestaPropietarioDTO() {
	}

	public RespuestaPropietarioDTO(Long idResena, String comentario) {
		this.idResena = idResena;
		this.comentario = comentario;
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

	@Override
	public String toString() {
		return "RespuestaPropietarioDTO [idResena=" + idResena + ", comentario=" + comentario + ", fecha="
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
		RespuestaPropietarioDTO other = (RespuestaPropietarioDTO) obj;
		return Objects.equals(comentario, other.comentario) && Objects.equals(fecha, other.fecha)
				&& Objects.equals(idResena, other.idResena);
	}
}
