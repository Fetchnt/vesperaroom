package co.edu.unbosque.vesperaroom.entity;

import java.util.Objects;

/**
 * Entidad que representa una habitación concreta de un motel y se corresponde
 * con la tabla {@code habitacion}. Guarda {@code idMotel} aunque se pueda
 * deducir del tipo: la tabla queda en 3FN (no en BCNF) a propósito para poder
 * declarar {@code UNIQUE(id_motel, numero)}, y una FK compuesta
 * {@code (id_tipo, id_motel)} impide que ambos datos se contradigan.
 */
public class Habitacion {

	private Long idHabitacion;
	private Long idMotel;
	private Long idTipo;
	private String numero;
	private Integer piso;
	private Estado estado;

	/**
	 * Define los estados operativos de la habitación. "Ocupada" no es un estado:
	 * se calcula a partir de las reservas vigentes, porque guardarlo sería un dato
	 * derivado.
	 */
	public enum Estado {
		ACTIVA, MANTENIMIENTO, FUERA_DE_SERVICIO
	}

	/**
	 * Constructor por defecto que inicializa la habitación con estado {@code ACTIVA}.
	 */
	public Habitacion() {
		this.estado = Estado.ACTIVA;
	}

	public Habitacion(Long idTipo, String numero, Integer piso) {
		this();
		this.idTipo = idTipo;
		this.numero = numero;
		this.piso = piso;
	}

	@Override
	public String toString() {
		return "Habitacion [idHabitacion=" + idHabitacion + ", idMotel=" + idMotel + ", idTipo=" + idTipo
				+ ", numero=" + numero + ", piso=" + piso + ", estado=" + estado + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(estado, idHabitacion, idMotel, idTipo, numero, piso);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Habitacion other = (Habitacion) obj;
		return estado == other.estado && Objects.equals(idHabitacion, other.idHabitacion)
				&& Objects.equals(idMotel, other.idMotel) && Objects.equals(idTipo, other.idTipo)
				&& Objects.equals(numero, other.numero) && Objects.equals(piso, other.piso);
	}

	public Long getIdHabitacion() {
		return idHabitacion;
	}

	public void setIdHabitacion(Long idHabitacion) {
		this.idHabitacion = idHabitacion;
	}

	public Long getIdMotel() {
		return idMotel;
	}

	public void setIdMotel(Long idMotel) {
		this.idMotel = idMotel;
	}

	public Long getIdTipo() {
		return idTipo;
	}

	public void setIdTipo(Long idTipo) {
		this.idTipo = idTipo;
	}

	public String getNumero() {
		return numero;
	}

	public void setNumero(String numero) {
		this.numero = numero;
	}

	public Integer getPiso() {
		return piso;
	}

	public void setPiso(Integer piso) {
		this.piso = piso;
	}

	public Estado getEstado() {
		return estado;
	}

	public void setEstado(Estado estado) {
		this.estado = estado;
	}
}
