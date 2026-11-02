package co.edu.unbosque.vesperaroom.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Motel tal como lo muestra una ficha del catálogo: sus datos más la foto
 * principal, la calificación, la tarifa más barata y sus tipos de habitación.
 * Solo se envía; nunca llega en una petición.
 */
public class MotelResumenDTO extends MotelDTO {

	private String foto;

	private double promedio;

	private long comentarios;

	private BigDecimal desde;

	private List<TipoResumenDTO> tipos;

	public String getFoto() {
		return foto;
	}

	public void setFoto(String foto) {
		this.foto = foto;
	}

	public double getPromedio() {
		return promedio;
	}

	public void setPromedio(double promedio) {
		this.promedio = promedio;
	}

	public long getComentarios() {
		return comentarios;
	}

	public void setComentarios(long comentarios) {
		this.comentarios = comentarios;
	}

	/**
	 * @return precio de la tarifa más barata del motel, o {@code null} si aún no
	 *         publica tarifas
	 */
	public BigDecimal getDesde() {
		return desde;
	}

	public void setDesde(BigDecimal desde) {
		this.desde = desde;
	}

	public List<TipoResumenDTO> getTipos() {
		return tipos;
	}

	public void setTipos(List<TipoResumenDTO> tipos) {
		this.tipos = tipos;
	}
}
