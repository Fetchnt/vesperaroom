package co.edu.unbosque.vesperaroom.scheduler;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import co.edu.unbosque.vesperaroom.service.ReservaService;

/**
 * Tarea programada que actualiza los estados de las reservas que dependen solo
 * del paso del tiempo (reservas pendientes vencidas, clientes que no llegaron y
 * estadías terminadas). Spring la ejecuta en segundo plano gracias a
 * {@code @EnableScheduling} en la clase principal.
 */
@Component
public class ActualizadorEstadosReserva {

	private static final Logger LOG = LoggerFactory.getLogger(ActualizadorEstadosReserva.class);

	private final ReservaService reservaService;

	public ActualizadorEstadosReserva(ReservaService reservaService) {
		this.reservaService = reservaService;
	}

	/**
	 * Revisa las reservas cada {@code app.reservas.intervalo-ms} milisegundos (60
	 * segundos por defecto). {@code fixedDelay} cuenta el intervalo desde que
	 * termina la ejecución anterior, así dos revisiones nunca se cruzan. Si la
	 * base de datos falla, se registra el error y se intenta en la siguiente
	 * vuelta.
	 */
	@Scheduled(fixedDelayString = "${app.reservas.intervalo-ms:60000}", initialDelayString = "${app.reservas.intervalo-ms:60000}")
	public void actualizar() {
		try {
			int cambios = reservaService.actualizarEstadosAutomaticos(LocalDateTime.now());
			if (cambios > 0) {
				LOG.info("Reservas actualizadas automáticamente: {}", cambios);
			}
		} catch (RuntimeException e) {
			LOG.error("No se pudieron actualizar los estados de las reservas", e);
		}
	}
}
