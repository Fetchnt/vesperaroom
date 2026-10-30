package co.edu.unbosque.vesperaroom.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Entidad que representa el precio de un tipo de habitación en una modalidad,
 * para un tipo de día y un periodo de vigencia, y se corresponde con la tabla
 * {@code tarifa}. {@code vigenteHasta} nulo indica que no tiene fecha de fin.
 */
public class Tarifa {

	private Long idTarifa;
	private Long idTipo;
	private Long idModalidad;
	private BigDecimal precio;
	private TipoDia tipoDia;
	private LocalDate vigenteDesde;
	private LocalDate vigenteHasta;

	/**
	 * Define el tipo de día en que aplica la tarifa.
	 */
	public enum TipoDia {
		TODOS, ENTRE_SEMANA, FIN_DE_SEMANA, FESTIVO
	}

	/**
	 * Constructor por defecto que inicializa la tarifa para {@code TODOS} los días.
	 */
	public Tarifa() {
		this.tipoDia = TipoDia.TODOS;
	}

	public Tarifa(Long idTipo, Long idModalidad, BigDecimal precio, TipoDia tipoDia, LocalDate vigenteDesde,
			LocalDate vigenteHasta) {
		this();
		this.idTipo = idTipo;
		this.idModalidad = idModalidad;
		this.precio = precio;
		this.tipoDia = tipoDia;
		this.vigenteDesde = vigenteDesde;
		this.vigenteHasta = vigenteHasta;
	}

	@Override
	public String toString() {
		return "Tarifa [idTarifa=" + idTarifa + ", idTipo=" + idTipo + ", idModalidad=" + idModalidad
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
		Tarifa other = (Tarifa) obj;
		return Objects.equals(idModalidad, other.idModalidad) && Objects.equals(idTarifa, other.idTarifa)
				&& Objects.equals(idTipo, other.idTipo) && Objects.equals(precio, other.precio)
				&& tipoDia == other.tipoDia && Objects.equals(vigenteDesde, other.vigenteDesde)
				&& Objects.equals(vigenteHasta, other.vigenteHasta);
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
}
