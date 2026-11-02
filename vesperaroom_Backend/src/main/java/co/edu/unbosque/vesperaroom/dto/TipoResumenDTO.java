package co.edu.unbosque.vesperaroom.dto;

import java.util.List;

/**
 * Tipo de habitación con sus tarifas y características, para la ficha de
 * {@link MotelResumenDTO}.
 */
public class TipoResumenDTO extends TipoHabitacionDTO {

	private List<TarifaDTO> tarifas;

	private List<CaracteristicaDTO> caracteristicas;

	public List<TarifaDTO> getTarifas() {
		return tarifas;
	}

	public void setTarifas(List<TarifaDTO> tarifas) {
		this.tarifas = tarifas;
	}

	public List<CaracteristicaDTO> getCaracteristicas() {
		return caracteristicas;
	}

	public void setCaracteristicas(List<CaracteristicaDTO> caracteristicas) {
		this.caracteristicas = caracteristicas;
	}
}
