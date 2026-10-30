package co.edu.unbosque.vesperaroom.entity;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidad que representa la reseña que un cliente deja sobre una reserva
 * finalizada y se corresponde con la tabla {@code resena}. Es una relación 1:1
 * con la reserva. No guarda el cliente ni el motel: se obtienen por la reserva,
 * evitando dependencias transitivas.
 */
public class Resena {

	private Long idResena;
	private Long idReserva;
	private Integer calificacion;
	private String comentario;
	private LocalDateTime fecha;

	public Resena() {
	}

	public Resena(Long idReserva, Integer calificacion, String comentario) {
		this.idReserva = idReserva;
		this.calificacion = calificacion;
		this.comentario = comentario;
	}

	@Override
	public String toString() {
		return "Resena [idResena=" + idResena + ", idReserva=" + idReserva + ", calificacion=" + calificacion
				+ ", comentario=" + comentario + ", fecha=" + fecha + "]";
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
		Resena other = (Resena) obj;
		return Objects.equals(calificacion, other.calificacion)
				&& Objects.equals(comentario, other.comentario) && Objects.equals(fecha, other.fecha)
				&& Objects.equals(idResena, other.idResena) && Objects.equals(idReserva, other.idReserva);
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
}
