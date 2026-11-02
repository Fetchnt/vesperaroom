package co.edu.unbosque.vesperaroom;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * La clave JWT va aquí y no en un application.properties de pruebas: ese archivo
 * reemplazaría al principal entero. Así el test arranca en cualquier clon, sin .env.
 * Se genera al azar (dos random.value suman 64 caracteres) para no dejar una clave escrita.
 */
@SpringBootTest(properties = "jwt.secret=${random.value}${random.value}")
class VesperaroomApplicationTests {

	@Test
	void contextLoads() {
	}

}
