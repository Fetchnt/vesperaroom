package co.edu.unbosque.vesperaroom.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Guarda en disco las imágenes que se suben como fotos. Los archivos quedan en
 * {@code app.uploads.dir}/fotos con un nombre aleatorio y se publican en
 * {@code /uploads/fotos/...} (ver {@code ArchivosConfig}); en la tabla
 * {@code foto} solo se guarda esa dirección.
 */
@Service
public class AlmacenImagenService {

	/** Ruta pública bajo la que se sirven las fotos subidas. */
	public static final String RUTA_PUBLICA = "/uploads/fotos/";

	private static final Map<String, String> EXTENSIONES = Map.of("image/jpeg", ".jpg", "image/png", ".png",
			"image/webp", ".webp");

	private final Path carpeta;

	public AlmacenImagenService(@Value("${app.uploads.dir:uploads}") String directorio) {
		this.carpeta = Path.of(directorio, "fotos").toAbsolutePath().normalize();
	}

	/**
	 * Guarda la imagen con un nombre nuevo, para que dos archivos con el mismo
	 * nombre no se pisen.
	 *
	 * @param archivo imagen ya validada
	 * @return nombre con el que quedó guardada
	 * @throws IOException si no se puede escribir en la carpeta
	 */
	public String guardar(MultipartFile archivo) throws IOException {
		Files.createDirectories(carpeta);
		String nombre = UUID.randomUUID() + EXTENSIONES.getOrDefault(archivo.getContentType(), "");
		try (InputStream entrada = archivo.getInputStream()) {
			Files.copy(entrada, carpeta.resolve(nombre), StandardCopyOption.REPLACE_EXISTING);
		}
		return nombre;
	}

	/**
	 * Borra el archivo de una foto subida. Las fotos registradas con una URL
	 * externa no tienen archivo y se ignoran.
	 *
	 * @param uri dirección guardada en la tabla {@code foto}
	 */
	public void eliminar(String uri) {
		int inicio = uri == null ? -1 : uri.indexOf(RUTA_PUBLICA);
		if (inicio < 0) {
			return;
		}
		Path archivo = carpeta.resolve(uri.substring(inicio + RUTA_PUBLICA.length())).normalize();
		try {
			if (archivo.startsWith(carpeta)) {
				Files.deleteIfExists(archivo);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Carpeta donde se guardan las fotos, para publicarla como recurso estático.
	 *
	 * @return ruta absoluta de la carpeta
	 */
	public Path getCarpeta() {
		return carpeta;
	}
}
