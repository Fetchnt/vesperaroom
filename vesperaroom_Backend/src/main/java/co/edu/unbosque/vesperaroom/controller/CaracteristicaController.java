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

import co.edu.unbosque.vesperaroom.dto.CaracteristicaDTO;
import co.edu.unbosque.vesperaroom.service.CaracteristicaService;
import co.edu.unbosque.vesperaroom.util.RespuestaCaracteristica;
import co.edu.unbosque.vesperaroom.util.RespuestaGeneral;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controlador REST para el catálogo de características de habitación. Las consultas son públicas y las
 * escrituras están reservadas al rol {@code ADMINISTRADOR} (ver
 * {@code SecurityConfig}). Delega la lógica a {@link CaracteristicaService}.
 */
@RestController
@RequestMapping("/caracteristica")
@Tag(name = "Características", description = "Catálogo de características")
@SecurityRequirement(name = "bearerAuth")
public class CaracteristicaController {

	private final CaracteristicaService caracteristicaService;

	public CaracteristicaController(CaracteristicaService caracteristicaService) {
		this.caracteristicaService = caracteristicaService;
	}

	/**
	 * Crea una característica recibiendo los datos en formato JSON.
	 *
	 * @param nuevo objeto con los datos a crear
	 * @return {@link ResponseEntity} con código 201 si es exitoso, 400 si algún
	 *         dato es inválido o 409 si ya existe
	 */
	@Operation(summary = "Crear característica (JSON)", description = "Crea una característica. Solo ADMINISTRADOR.")
	@ApiResponses(value = { @ApiResponse(responseCode = "201", description = "Característica creada"),
			@ApiResponse(responseCode = "400", description = "Datos inválidos"),
			@ApiResponse(responseCode = "409", description = "Datos duplicados") })
	@PostMapping(path = "/createjson", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> crearJSON(@RequestBody CaracteristicaDTO nuevo) {
		int status = caracteristicaService.create(nuevo);
		return RespuestaCaracteristica.guardar(status, "Característica creada exitosamente", HttpStatus.CREATED);
	}

	/**
	 * Retorna las características. Endpoint público.
	 *
	 * @return {@link ResponseEntity} con la lista y código 202, o 204 si no hay
	 *         registros
	 */
	@Operation(summary = "Obtener características", description = "Retorna el catálogo completo. Público.")
	@GetMapping("/getall")
	public ResponseEntity<List<CaracteristicaDTO>> obtenerTodos() {
		return RespuestaGeneral.lista(caracteristicaService.getAll());
	}

	/**
	 * Busca una característica por su ID. Endpoint público.
	 *
	 * @param id identificador a buscar
	 * @return {@link ResponseEntity} con el registro y código 202, o 404 si no
	 *         existe
	 */
	@Operation(summary = "Obtener característica por ID", description = "Busca una característica mediante su ID. Público.")
	@GetMapping("/getbyid/{id}")
	public ResponseEntity<?> obtenerPorId(@PathVariable Long id) {
		return RespuestaGeneral.encontrado(caracteristicaService.getById(id), "Característica no encontrada");
	}

	/**
	 * Actualiza una característica recibiendo los nuevos datos en formato JSON.
	 *
	 * @param id    identificador a actualizar
	 * @param nuevo objeto con los nuevos datos
	 * @return {@link ResponseEntity} con código 202 si es exitoso, 400 si algún
	 *         dato es inválido, 404 si no existe o 409 si hay duplicados
	 */
	@Operation(summary = "Actualizar característica (JSON)", description = "Actualiza una característica. Solo ADMINISTRADOR.")
	@PutMapping(path = "/updatejson", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> actualizarJSON(@RequestParam Long id, @RequestBody CaracteristicaDTO nuevo) {
		int status = caracteristicaService.updateById(id, nuevo);
		return RespuestaCaracteristica.guardar(status, "Característica actualizada exitosamente", HttpStatus.ACCEPTED);
	}

	/**
	 * Elimina una característica según su ID.
	 *
	 * @param id identificador a eliminar
	 * @return {@link ResponseEntity} con código 202 si es exitoso, 404 si no
	 *         existe o 409 si tiene registros asociados
	 */
	@Operation(summary = "Eliminar característica", description = "Elimina una característica por ID. Solo ADMINISTRADOR.")
	@DeleteMapping("/deletebyid/{id}")
	public ResponseEntity<String> eliminar(@PathVariable Long id) {
		return RespuestaGeneral.eliminar(caracteristicaService.deleteById(id), "característica", true);
	}

	/**
	 * Retorna el total de características.
	 *
	 * @return {@link ResponseEntity} con el conteo y código 202, o 204 si es cero
	 */
	@Operation(summary = "Contar características", description = "Cuenta el total de registros.")
	@GetMapping("/count")
	public ResponseEntity<Long> contar() {
		return RespuestaGeneral.conteo(caracteristicaService.count());
	}

	/**
	 * Verifica si existe una característica con el ID indicado.
	 *
	 * @param id identificador a verificar
	 * @return {@link ResponseEntity} con {@code true} y código 202, o
	 *         {@code false} y código 404
	 */
	@Operation(summary = "Verificar existencia", description = "Verifica si existe una característica.")
	@GetMapping("/exists/{id}")
	public ResponseEntity<Boolean> existe(@PathVariable Long id) {
		return RespuestaGeneral.existe(caracteristicaService.exist(id));
	}
}
