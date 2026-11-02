package co.edu.unbosque.vesperaroom;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * La clave JWT va aquí y no en un application.properties de pruebas: ese archivo
 * reemplazaría al principal entero. Así el test arranca en cualquier clon, sin .env.
 */
@SpringBootTest(properties = "jwt.secret=clave-solo-para-pruebas-con-mas-de-32-caracteres")
class VesperaroomApplicationTests {

	@Test
	void contextLoads() {
	}

}
