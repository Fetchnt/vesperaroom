package co.edu.unbosque.vesperaroom.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import co.edu.unbosque.vesperaroom.service.AlmacenImagenService;

/**
 * Publica las fotos subidas como archivos estáticos en {@code /uploads/fotos/**},
 * que es la dirección que queda guardada en la tabla {@code foto}.
 */
@Configuration
public class ArchivosConfig implements WebMvcConfigurer {

	private final AlmacenImagenService almacen;

	public ArchivosConfig(AlmacenImagenService almacen) {
		this.almacen = almacen;
	}

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler(AlmacenImagenService.RUTA_PUBLICA + "**")
				.addResourceLocations(almacen.getCarpeta().toUri().toString());
	}
}
