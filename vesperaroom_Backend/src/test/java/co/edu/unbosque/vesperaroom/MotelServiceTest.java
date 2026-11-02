package co.edu.unbosque.vesperaroom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;

import co.edu.unbosque.vesperaroom.dto.MotelResumenDTO;
import co.edu.unbosque.vesperaroom.dto.TipoResumenDTO;
import co.edu.unbosque.vesperaroom.entity.Caracteristica;
import co.edu.unbosque.vesperaroom.entity.Motel;
import co.edu.unbosque.vesperaroom.entity.Tarifa;
import co.edu.unbosque.vesperaroom.entity.TipoHabitacion;
import co.edu.unbosque.vesperaroom.repository.MotelRepository;
import co.edu.unbosque.vesperaroom.repository.TarifaRepository;
import co.edu.unbosque.vesperaroom.repository.TipoHabitacionRepository;
import co.edu.unbosque.vesperaroom.service.MotelService;

/**
 * El resumen del catálogo une cinco consultas por id. Estas pruebas fijan que
 * cada dato llega a su motel y que un motel vacío no se pierde.
 */
@ExtendWith(MockitoExtension.class)
class MotelServiceTest {

	@Mock
	private MotelRepository motelRepository;

	@Mock
	private TipoHabitacionRepository tipoHabitacionRepository;

	@Mock
	private TarifaRepository tarifaRepository;

	@Spy
	private ModelMapper mapper = estricto();

	@InjectMocks
	private MotelService motelService;

	/**
	 * Metodo comprobar que el resumen reparte foto, reseñas, tipos, tarifas y
	 * características a su motel, y que un motel sin nada sale con valores vacíos.
	 */
	@Test
	public void comprobarResumen() throws Exception {
		when(motelRepository.findActivos()).thenReturn(List.of(motel(1L, "Luna"), motel(2L, "Nuevo")));
		when(motelRepository.findAgregadosActivos())
				.thenReturn(Map.of(1L, new MotelRepository.Agregados("luna.jpg", 4.5, 2)));
		when(tipoHabitacionRepository.findAll()).thenReturn(List.of(tipo(10L, 1L)));
		when(tarifaRepository.findAll()).thenReturn(List.of(tarifa(10L, "60000"), tarifa(10L, "55000")));
		when(tipoHabitacionRepository.findCaracteristicasPorTipo())
				.thenReturn(Map.of(10L, List.of(new Caracteristica("Jacuzzi", "bath"))));

		List<MotelResumenDTO> resumen = motelService.getResumen();

		MotelResumenDTO luna = resumen.get(0);
		assertEquals("Luna", luna.getNombre());
		assertEquals("luna.jpg", luna.getFoto());
		assertEquals(4.5, luna.getPromedio());
		assertEquals(2, luna.getComentarios());
		assertEquals(new BigDecimal("55000"), luna.getDesde());
		TipoResumenDTO tipo = luna.getTipos().get(0);
		assertEquals(10L, tipo.getIdTipo());
		assertEquals(2, tipo.getTarifas().size());
		assertEquals("Jacuzzi", tipo.getCaracteristicas().get(0).getNombre());

		MotelResumenDTO nuevo = resumen.get(1);
		assertNull(nuevo.getFoto());
		assertEquals(0, nuevo.getComentarios());
		assertNull(nuevo.getDesde());
		assertTrue(nuevo.getTipos().isEmpty());
	}

	private static ModelMapper estricto() {
		ModelMapper m = new ModelMapper();
		m.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
		return m;
	}

	private static Motel motel(Long id, String nombre) {
		Motel m = new Motel(1L, 1L, nombre, null, "Calle 1", "Carrera 1", null, null);
		m.setIdMotel(id);
		return m;
	}

	private static TipoHabitacion tipo(Long idTipo, Long idMotel) {
		TipoHabitacion t = new TipoHabitacion(idMotel, "Suite", 2, null);
		t.setIdTipo(idTipo);
		return t;
	}

	private static Tarifa tarifa(Long idTipo, String precio) {
		return new Tarifa(idTipo, 1L, new BigDecimal(precio), null, null, null);
	}
}
