package co.edu.unbosque.vesperaroom.dto;

import java.math.BigDecimal;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Objeto de transferencia de datos que representa la política de reservas de
 * un motel. {@code aceptaReembolso} es un atributo derivado de solo lectura:
 * se calcula a partir del porcentaje de reembolso y no se guarda en la base.
 */
public class PoliticaReservaDTO {

	private Long idMotel;

	private Integer diasHabilesDevolucion;

	private Integer porcentajeReembolso;

	private Integer capacidadMaxSinRecargo;

	private BigDecimal tarifaPersonaAdicional;

	private Integer anticipacionMinimaHoras;

	private Integer tiempoGraciaNoShowMin;

	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private Boolean aceptaReembolso;

	public PoliticaReservaDTO() {
	}

	public PoliticaReservaDTO(Long idMotel, Integer diasHabilesDevolucion, Integer porcentajeReembolso,
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

	public Boolean getAceptaReembolso() {
		return aceptaReembolso;
	}

	public void setAceptaReembolso(Boolean aceptaReembolso) {
		this.aceptaReembolso = aceptaReembolso;
	}

	@Override
	public String toString() {
		return "PoliticaReservaDTO [idMotel=" + idMotel + ", diasHabilesDevolucion=" + diasHabilesDevolucion
				+ ", porcentajeReembolso=" + porcentajeReembolso + ", capacidadMaxSinRecargo="
				+ capacidadMaxSinRecargo + ", tarifaPersonaAdicional=" + tarifaPersonaAdicional
				+ ", anticipacionMinimaHoras=" + anticipacionMinimaHoras + ", tiempoGraciaNoShowMin="
				+ tiempoGraciaNoShowMin + ", aceptaReembolso=" + aceptaReembolso + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(aceptaReembolso, anticipacionMinimaHoras, capacidadMaxSinRecargo,
				diasHabilesDevolucion, idMotel, porcentajeReembolso, tarifaPersonaAdicional,
				tiempoGraciaNoShowMin);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PoliticaReservaDTO other = (PoliticaReservaDTO) obj;
		return Objects.equals(aceptaReembolso, other.aceptaReembolso)
				&& Objects.equals(anticipacionMinimaHoras, other.anticipacionMinimaHoras)
				&& Objects.equals(capacidadMaxSinRecargo, other.capacidadMaxSinRecargo)
				&& Objects.equals(diasHabilesDevolucion, other.diasHabilesDevolucion)
				&& Objects.equals(idMotel, other.idMotel)
				&& Objects.equals(porcentajeReembolso, other.porcentajeReembolso)
				&& Objects.equals(tarifaPersonaAdicional, other.tarifaPersonaAdicional)
				&& Objects.equals(tiempoGraciaNoShowMin, other.tiempoGraciaNoShowMin);
	}
}
