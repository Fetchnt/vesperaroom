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

import co.edu.unbosque.vesperaroom.dto.CiudadDTO;
import co.edu.unbosque.vesperaroom.service.CiudadService;
import co.edu.unbosque.vesperaroom.util.RespuestaCiudad;
import co.edu.unbosque.vesperaroom.util.RespuestaGeneral;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controlador REST para el catálogo de ciudades. Las consultas son públicas y las
 * escrituras están reservadas al rol {@code ADMINISTRADOR} (ver
 * {@code SecurityConfig}). Delega la lógica a {@link CiudadService}.
 */
@RestController
@RequestMapping("/ciudad")
@Tag(name = "Ciudades", description = "Catálogo de ciudades")
@SecurityRequirement(name = "bearerAuth")
public class CiudadController {

	private final CiudadService ciudadService;

	public CiudadController(CiudadService ciudadService) {
		this.ciudadService = ciudadService;
	}

	/**
	 * Crea una ciudad recibiendo los datos en formato JSON.
	 *
	 * @param nuevo objeto con los datos a crear
	 * @return {@link ResponseEntity} con código 201 si es exitoso, 400 si algún
	 *         dato es inválido o 409 si ya existe
	 */
	@Operation(summary = "Crear ciudad (JSON)", description = "Crea una ciudad. Solo ADMINISTRADOR.")
	@ApiResponses(value = { @ApiResponse(responseCode = "201", description = "Ciudad creada"),
			@ApiResponse(responseCode = "400", description = "Datos inválidos"),
			@ApiResponse(responseCode = "409", description = "Datos duplicados") })
	@PostMapping(path = "/createjson", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> crearJSON(@RequestBody CiudadDTO nuevo) {
		int status = ciudadService.create(nuevo);
		return RespuestaCiudad.guardar(status, "Ciudad creada exitosamente", HttpStatus.CREATED);
	}

	/**
	 * Retorna las ciudades. Endpoint público.
	 *
	 * @return {@link ResponseEntity} con la lista y código 202, o 204 si no hay
	 *         registros
	 */
	@Operation(summary = "Obtener ciudades", description = "Retorna el catálogo completo. Público.")
	@GetMapping("/getall")
	public ResponseEntity<List<CiudadDTO>> obtenerTodos() {
		return RespuestaGeneral.lista(ciudadService.getAll());
	}

	/**
	 * Retorna las ciudades de un departamento. Endpoint público.
	 *
	 * @param idDepartamento identificador del departamento
	 * @return {@link ResponseEntity} con la lista y código 202, o 204 si no hay
	 *         registros
	 */
	@Operation(summary = "Ciudades de un departamento", description = "Retorna las ciudades de un departamento. Público.")
	@GetMapping("/getbydepartamento/{idDepartamento}")
	public ResponseEntity<List<CiudadDTO>> obtenerPorDepartamento(@PathVariable Long idDepartamento) {
		return RespuestaGeneral.lista(ciudadService.getAllByDepartamento(idDepartamento));
	}

	/**
	 * Busca una ciudad por su ID. Endpoint público.
	 *
	 * @param id identificador a buscar
	 * @return {@link ResponseEntity} con el registro y código 202, o 404 si no
	 *         existe
	 */
	@Operation(summary = "Obtener ciudad por ID", description = "Busca una ciudad mediante su ID. Público.")
	@GetMapping("/getbyid/{id}")
	public ResponseEntity<?> obtenerPorId(@PathVariable Long id) {
		return RespuestaGeneral.encontrado(ciudadService.getById(id), "Ciudad no encontrada");
	}

	/**
	 * Actualiza una ciudad recibiendo los nuevos datos en formato JSON.
	 *
	 * @param id    identificador a actualizar
	 * @param nuevo objeto con los nuevos datos
	 * @return {@link ResponseEntity} con código 202 si es exitoso, 400 si algún
	 *         dato es inválido, 404 si no existe o 409 si hay duplicados
	 */
	@Operation(summary = "Actualizar ciudad (JSON)", description = "Actualiza una ciudad. Solo ADMINISTRADOR.")
	@PutMapping(path = "/updatejson", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> actualizarJSON(@RequestParam Long id, @RequestBody CiudadDTO nuevo) {
		int status = ciudadService.updateById(id, nuevo);
		return RespuestaCiudad.guardar(status, "Ciudad actualizada exitosamente", HttpStatus.ACCEPTED);
	}

	/**
	 * Elimina una ciudad según su ID.
	 *
	 * @param id identificador a eliminar
	 * @return {@link ResponseEntity} con código 202 si es exitoso, 404 si no
	 *         existe o 409 si tiene registros asociados
	 */
	@Operation(summary = "Eliminar ciudad", description = "Elimina una ciudad por ID. Solo ADMINISTRADOR.")
	@DeleteMapping("/deletebyid/{id}")
	public ResponseEntity<String> eliminar(@PathVariable Long id) {
		return RespuestaGeneral.eliminar(ciudadService.deleteById(id), "ciudad", true);
	}

	/**
	 * Retorna el total de ciudades.
	 *
	 * @return {@link ResponseEntity} con el conteo y código 202, o 204 si es cero
	 */
	@Operation(summary = "Contar ciudades", description = "Cuenta el total de registros.")
	@GetMapping("/count")
	public ResponseEntity<Long> contar() {
		return RespuestaGeneral.conteo(ciudadService.count());
	}

	/**
	 * Verifica si existe una ciudad con el ID indicado.
	 *
	 * @param id identificador a verificar
	 * @return {@link ResponseEntity} con {@code true} y código 202, o
	 *         {@code false} y código 404
	 */
	@Operation(summary = "Verificar existencia", description = "Verifica si existe una ciudad.")
	@GetMapping("/exists/{id}")
	public ResponseEntity<Boolean> existe(@PathVariable Long id) {
		return RespuestaGeneral.existe(ciudadService.exist(id));
	}
}
