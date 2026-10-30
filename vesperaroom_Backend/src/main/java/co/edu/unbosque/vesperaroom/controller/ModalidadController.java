package co.edu.unbosque.vesperaroom.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.vesperaroom.dto.ModalidadDTO;
import co.edu.unbosque.vesperaroom.service.ModalidadService;
import co.edu.unbosque.vesperaroom.util.RespuestaModalidad;
import co.edu.unbosque.vesperaroom.util.RespuestaGeneral;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controlador REST para el catálogo de modalidades de alquiler. Las consultas son públicas y las
 * escrituras están reservadas al rol {@code ADMINISTRADOR} (ver
 * {@code SecurityConfig}). Delega la lógica a {@link ModalidadService}.
 */
@RestController
@RequestMapping("/modalidad")
@Tag(name = "Modalidades", description = "Catálogo de modalidades")
@SecurityRequirement(name = "bearerAuth")
public class ModalidadController {

	private final ModalidadService modalidadService;

	public ModalidadController(ModalidadService modalidadService) {
		this.modalidadService = modalidadService;
	}

	/**
	 * Crea una modalidad recibiendo los datos en formato JSON.
	 *
	 * @param nuevo objeto con los datos a crear
	 * @return {@link ResponseEntity} con código 201 si es exitoso, 400 si algún
	 *         dato es inválido o 409 si ya existe
	 */
	@Operation(summary = "Crear modalidad (JSON)", description = "Crea una modalidad. Solo ADMINISTRADOR.")
	@ApiResponses(value = { @ApiResponse(responseCode = "201", description = "Modalidad creada"),
			@ApiResponse(responseCode = "400", description = "Datos inválidos"),
			@ApiResponse(responseCode = "409", description = "Datos duplicados") })
	@PostMapping(path = "/createjson", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> crearJSON(@RequestBody ModalidadDTO nuevo) {
		int status = modalidadService.create(nuevo);
		return RespuestaModalidad.guardar(status, "Modalidad creada exitosamente", HttpStatus.CREATED);
	}

	/**
	 * Retorna las modalidades. Endpoint público.
	 *
	 * @return {@link ResponseEntity} con la lista y código 202, o 204 si no hay
	 *         registros
	 */
	@Operation(summary = "Obtener modalidades", description = "Retorna el catálogo completo. Público.")
	@GetMapping("/getall")
	public ResponseEntity<List<ModalidadDTO>> obtenerTodos() {
		return RespuestaGeneral.lista(modalidadService.getAll());
	}

	/**
	 * Busca una modalidad por su ID. Endpoint público.
	 *
	 * @param id identificador a buscar
	 * @return {@link ResponseEntity} con el registro y código 202, o 404 si no
	 *         existe
	 */
	@Operation(summary = "Obtener modalidad por ID", description = "Busca una modalidad mediante su ID. Público.")
	@GetMapping("/getbyid/{id}")
	public ResponseEntity<?> obtenerPorId(@PathVariable Long id) {
		return RespuestaGeneral.encontrado(modalidadService.getById(id), "Modalidad no encontrada");
	}

	/**
	 * Actualiza una modalidad recibiendo los nuevos datos en formato JSON.
	 *
	 * @param id    identificador a actualizar
	 * @param nuevo objeto con los nuevos datos
	 * @return {@link ResponseEntity} con código 202 si es exitoso, 400 si algún
	 *         dato es inválido, 404 si no existe o 409 si hay duplicados
	 */
	@Operation(summary = "Actualizar modalidad (JSON)", description = "Actualiza una modalidad. Solo ADMINISTRADOR.")
	@PutMapping(path = "/updatejson", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> actualizarJSON(@RequestParam Long id, @RequestBody ModalidadDTO nuevo) {
		int status = modalidadService.updateById(id, nuevo);
		return RespuestaModalidad.guardar(status, "Modalidad actualizada exitosamente", HttpStatus.ACCEPTED);
	}

	/**
	 * Elimina una modalidad según su ID.
	 *
	 * @param id identificador a eliminar
	 * @return {@link ResponseEntity} con código 202 si es exitoso, 404 si no
	 *         existe o 409 si tiene registros asociados
	 */
	@Operation(summary = "Eliminar modalidad", description = "Elimina una modalidad por ID. Solo ADMINISTRADOR.")
	@DeleteMapping("/deletebyid/{id}")
	public ResponseEntity<String> eliminar(@PathVariable Long id) {
		return RespuestaGeneral.eliminar(modalidadService.deleteById(id), "modalidad", true);
	}

	/**
	 * Retorna el total de modalidades.
	 *
	 * @return {@link ResponseEntity} con el conteo y código 202, o 204 si es cero
	 */
	@Operation(summary = "Contar modalidades", description = "Cuenta el total de registros.")
	@GetMapping("/count")
	public ResponseEntity<Long> contar() {
		return RespuestaGeneral.conteo(modalidadService.count());
	}

	/**
	 * Verifica si existe una modalidad con el ID indicado.
	 *
	 * @param id identificador a verificar
	 * @return {@link ResponseEntity} con {@code true} y código 202, o
	 *         {@code false} y código 404
	 */
	@Operation(summary = "Verificar existencia", description = "Verifica si existe una modalidad.")
	@GetMapping("/exists/{id}")
	public ResponseEntity<Boolean> existe(@PathVariable Long id) {
		return RespuestaGeneral.existe(modalidadService.exist(id));
	}
}
