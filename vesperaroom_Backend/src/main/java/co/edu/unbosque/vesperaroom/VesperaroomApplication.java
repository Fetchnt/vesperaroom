package co.edu.unbosque.vesperaroom;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Clase principal del aplicativo VesperaRoom, plataforma de reservas de
 * habitaciones de motel. {@link EnableScheduling} activa las tareas
 * programadas, como la actualización automática de estados de reserva.
 */
@SpringBootApplication
@EnableScheduling
public class VesperaroomApplication {

	public static void main(String[] args) {
		SpringApplication.run(VesperaroomApplication.class, args);
	}

	/**
	 * Expone el {@link ModelMapper} usado para convertir entidades en DTOs. Se
	 * configura con {@link MatchingStrategies#STRICT} para que solo copie
	 * atributos con el mismo nombre: varias entidades tienen campos parecidos
	 * ({@code idMotel}, {@code idTipo}, {@code idHabitacion}...) y la estrategia
	 * por defecto podría confundirlos.
	 *
	 * @return instancia de {@link ModelMapper} configurada
	 */
	@Bean
	public ModelMapper getModelMapper() {
		ModelMapper mapper = new ModelMapper();
		mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
		return mapper;
	}
}
