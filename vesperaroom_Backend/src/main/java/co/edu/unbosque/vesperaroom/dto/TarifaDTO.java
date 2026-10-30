package co.edu.unbosque.vesperaroom.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

import co.edu.unbosque.vesperaroom.entity.Tarifa.TipoDia;

/**
 * Objeto de transferencia de datos que representa una tarifa. El campo
 * {@code idTarifa} es de solo lectura en la serialización JSON.
 */
public class TarifaDTO {

	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private Long idTarifa;

	private Long idTipo;

	private Long idModalidad;

	private BigDecimal precio;

	private TipoDia tipoDia;

	private LocalDate vigenteDesde;

	private LocalDate vigenteHasta;

	public TarifaDTO() {
	}

	public TarifaDTO(Long idTipo, Long idModalidad, BigDecimal precio, TipoDia tipoDia,
			LocalDate vigenteDesde, LocalDate vigenteHasta) {
		this.idTipo = idTipo;
		this.idModalidad = idModalidad;
		this.precio = precio;
		this.tipoDia = tipoDia;
		this.vigenteDesde = vigenteDesde;
		this.vigenteHasta = vigenteHasta;
	}

	public Long getIdTarifa() {
		return idTarifa;
	}

	public void setIdTarifa(Long idTarifa) {
		this.idTarifa = idTarifa;
	}

	public Long getIdTipo() {
		return idTipo;
	}

	public void setIdTipo(Long idTipo) {
		this.idTipo = idTipo;
	}

	public Long getIdModalidad() {
		return idModalidad;
	}

	public void setIdModalidad(Long idModalidad) {
		this.idModalidad = idModalidad;
	}

	public BigDecimal getPrecio() {
		return precio;
	}

	public void setPrecio(BigDecimal precio) {
		this.precio = precio;
	}

	public TipoDia getTipoDia() {
		return tipoDia;
	}

	public void setTipoDia(TipoDia tipoDia) {
		this.tipoDia = tipoDia;
	}

	public LocalDate getVigenteDesde() {
		return vigenteDesde;
	}

	public void setVigenteDesde(LocalDate vigenteDesde) {
		this.vigenteDesde = vigenteDesde;
	}

	public LocalDate getVigenteHasta() {
		return vigenteHasta;
	}

	public void setVigenteHasta(LocalDate vigenteHasta) {
		this.vigenteHasta = vigenteHasta;
	}

	@Override
	public String toString() {
		return "TarifaDTO [idTarifa=" + idTarifa + ", idTipo=" + idTipo + ", idModalidad=" + idModalidad
				+ ", precio=" + precio + ", tipoDia=" + tipoDia + ", vigenteDesde=" + vigenteDesde
				+ ", vigenteHasta=" + vigenteHasta + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(idModalidad, idTarifa, idTipo, precio, tipoDia, vigenteDesde, vigenteHasta);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TarifaDTO other = (TarifaDTO) obj;
		return Objects.equals(idModalidad, other.idModalidad) && Objects.equals(idTarifa, other.idTarifa)
				&& Objects.equals(idTipo, other.idTipo) && Objects.equals(precio, other.precio)
				&& tipoDia == other.tipoDia && Objects.equals(vigenteDesde, other.vigenteDesde)
				&& Objects.equals(vigenteHasta, other.vigenteHasta);
	}
}
