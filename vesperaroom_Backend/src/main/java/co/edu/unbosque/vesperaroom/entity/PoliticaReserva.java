package co.edu.unbosque.vesperaroom.entity;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Entidad que representa la política de reservas de un motel y se corresponde
 * con la tabla {@code politica_reserva}. Es una relación 1:1 con
 * {@link Motel}: su identificador es el mismo {@code id_motel}. No guarda
 * {@code acepta_reembolso} porque se deriva de {@code porcentajeReembolso > 0}.
 */
public class PoliticaReserva {

	private Long idMotel;
	private Integer diasHabilesDevolucion;
	private Integer porcentajeReembolso;
	private Integer capacidadMaxSinRecargo;
	private BigDecimal tarifaPersonaAdicional;
	private Integer anticipacionMinimaHoras;
	private Integer tiempoGraciaNoShowMin;

	public PoliticaReserva() {
	}

	public PoliticaReserva(Long idMotel, Integer diasHabilesDevolucion, Integer porcentajeReembolso,
			Integer capacidadMaxSinRecargo, BigDecimal tarifaPersonaAdicional,
			Integer anticipacionMinimaHoras, Integer tiempoGraciaNoShowMin) {
		this.idMotel = idMotel;
		this.diasHabilesDevolucion = diasHabilesDevolucion;
		this.porcentajeReembolso = porcentajeReembolso;
		this.capacidadMaxSinRecargo = capacidadMaxSinRecargo;
		this.tarifaPersonaAdicional = tarifaPersonaAdicional;
		this.anticipacionMinimaHoras = anticipacionMinimaHoras;
		this.tiempoGraciaNoShowMin = tiempoGraciaNoShowMin;
	}

	@Override
	public String toString() {
		return "PoliticaReserva [idMotel=" + idMotel + ", diasHabilesDevolucion=" + diasHabilesDevolucion
				+ ", porcentajeReembolso=" + porcentajeReembolso + ", capacidadMaxSinRecargo="
				+ capacidadMaxSinRecargo + ", tarifaPersonaAdicional=" + tarifaPersonaAdicional
				+ ", anticipacionMinimaHoras=" + anticipacionMinimaHoras + ", tiempoGraciaNoShowMin="
				+ tiempoGraciaNoShowMin + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(anticipacionMinimaHoras, capacidadMaxSinRecargo, diasHabilesDevolucion, idMotel,
				porcentajeReembolso, tarifaPersonaAdicional, tiempoGraciaNoShowMin);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PoliticaReserva other = (PoliticaReserva) obj;
		return Objects.equals(anticipacionMinimaHoras, other.anticipacionMinimaHoras)
				&& Objects.equals(capacidadMaxSinRecargo, other.capacidadMaxSinRecargo)
				&& Objects.equals(diasHabilesDevolucion, other.diasHabilesDevolucion)
				&& Objects.equals(idMotel, other.idMotel)
				&& Objects.equals(porcentajeReembolso, other.porcentajeReembolso)
				&& Objects.equals(tarifaPersonaAdicional, other.tarifaPersonaAdicional)
				&& Objects.equals(tiempoGraciaNoShowMin, other.tiempoGraciaNoShowMin);
	}

	public Long getIdMotel() {
		return idMotel;
	}

	public void setIdMotel(Long idMotel) {
		this.idMotel = idMotel;
	}

	public Integer getDiasHabilesDevolucion() {
		return diasHabilesDevolucion;
	}

	public void setDiasHabilesDevolucion(Integer diasHabilesDevolucion) {
		this.diasHabilesDevolucion = diasHabilesDevolucion;
	}

	public Integer getPorcentajeReembolso() {
		return porcentajeReembolso;
	}

	public void setPorcentajeReembolso(Integer porcentajeReembolso) {
		this.porcentajeReembolso = porcentajeReembolso;
	}

	public Integer getCapacidadMaxSinRecargo() {
		return capacidadMaxSinRecargo;
	}

	public void setCapacidadMaxSinRecargo(Integer capacidadMaxSinRecargo) {
		this.capacidadMaxSinRecargo = capacidadMaxSinRecargo;
	}

	public BigDecimal getTarifaPersonaAdicional() {
		return tarifaPersonaAdicional;
	}

	public void setTarifaPersonaAdicional(BigDecimal tarifaPersonaAdicional) {
		this.tarifaPersonaAdicional = tarifaPersonaAdicional;
	}

	public Integer getAnticipacionMinimaHoras() {
		return anticipacionMinimaHoras;
	}

	public void setAnticipacionMinimaHoras(Integer anticipacionMinimaHoras) {
		this.anticipacionMinimaHoras = anticipacionMinimaHoras;
	}

	public Integer getTiempoGraciaNoShowMin() {
		return tiempoGraciaNoShowMin;
	}

	public void setTiempoGraciaNoShowMin(Integer tiempoGraciaNoShowMin) {
		this.tiempoGraciaNoShowMin = tiempoGraciaNoShowMin;
	}
}
