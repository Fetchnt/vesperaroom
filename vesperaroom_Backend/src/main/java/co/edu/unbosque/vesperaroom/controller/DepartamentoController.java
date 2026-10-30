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

import co.edu.unbosque.vesperaroom.dto.DepartamentoDTO;
import co.edu.unbosque.vesperaroom.service.DepartamentoService;
import co.edu.unbosque.vesperaroom.util.RespuestaDepartamento;
import co.edu.unbosque.vesperaroom.util.RespuestaGeneral;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controlador REST para el catálogo de departamentos. Las consultas son públicas y las
 * escrituras están reservadas al rol {@code ADMINISTRADOR} (ver
 * {@code SecurityConfig}). Delega la lógica a {@link DepartamentoService}.
 */
@RestController
@RequestMapping("/departamento")
@Tag(name = "Departamentos", description = "Catálogo de departamentos")
@SecurityRequirement(name = "bearerAuth")
public class DepartamentoController {

	private final DepartamentoService departamentoService;

	public DepartamentoController(DepartamentoService departamentoService) {
		this.departamentoService = departamentoService;
	}

	/**
	 * Crea un departamento recibiendo los datos en formato JSON.
	 *
	 * @param nuevo objeto con los datos a crear
	 * @return {@link ResponseEntity} con código 201 si es exitoso, 400 si algún
	 *         dato es inválido o 409 si ya existe
	 */
	@Operation(summary = "Crear departamento (JSON)", description = "Crea un departamento. Solo ADMINISTRADOR.")
	@ApiResponses(value = { @ApiResponse(responseCode = "201", description = "Departamento creado"),
			@ApiResponse(responseCode = "400", description = "Datos inválidos"),
			@ApiResponse(responseCode = "409", description = "Datos duplicados") })
	@PostMapping(path = "/createjson", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> crearJSON(@RequestBody DepartamentoDTO nuevo) {
		int status = departamentoService.create(nuevo);
		return RespuestaDepartamento.guardar(status, "Departamento creado exitosamente", HttpStatus.CREATED);
	}

	/**
	 * Retorna los departamentos. Endpoint público.
	 *
	 * @return {@link ResponseEntity} con la lista y código 202, o 204 si no hay
	 *         registros
	 */
	@Operation(summary = "Obtener departamentos", description = "Retorna el catálogo completo. Público.")
	@GetMapping("/getall")
	public ResponseEntity<List<DepartamentoDTO>> obtenerTodos() {
		return RespuestaGeneral.lista(departamentoService.getAll());
	}

	/**
	 * Busca un departamento por su ID. Endpoint público.
	 *
	 * @param id identificador a buscar
	 * @return {@link ResponseEntity} con el registro y código 202, o 404 si no
	 *         existe
	 */
	@Operation(summary = "Obtener departamento por ID", description = "Busca un departamento mediante su ID. Público.")
	@GetMapping("/getbyid/{id}")
	public ResponseEntity<?> obtenerPorId(@PathVariable Long id) {
		return RespuestaGeneral.encontrado(departamentoService.getById(id), "Departamento no encontrado");
	}

	/**
	 * Actualiza un departamento recibiendo los nuevos datos en formato JSON.
	 *
	 * @param id    identificador a actualizar
	 * @param nuevo objeto con los nuevos datos
	 * @return {@link ResponseEntity} con código 202 si es exitoso, 400 si algún
	 *         dato es inválido, 404 si no existe o 409 si hay duplicados
	 */
	@Operation(summary = "Actualizar departamento (JSON)", description = "Actualiza un departamento. Solo ADMINISTRADOR.")
	@PutMapping(path = "/updatejson", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> actualizarJSON(@RequestParam Long id, @RequestBody DepartamentoDTO nuevo) {
		int status = departamentoService.updateById(id, nuevo);
		return RespuestaDepartamento.guardar(status, "Departamento actualizado exitosamente", HttpStatus.ACCEPTED);
	}

	/**
	 * Elimina un departamento según su ID.
	 *
	 * @param id identificador a eliminar
	 * @return {@link ResponseEntity} con código 202 si es exitoso, 404 si no
	 *         existe o 409 si tiene registros asociados
	 */
	@Operation(summary = "Eliminar departamento", description = "Elimina un departamento por ID. Solo ADMINISTRADOR.")
	@DeleteMapping("/deletebyid/{id}")
	public ResponseEntity<String> eliminar(@PathVariable Long id) {
		return RespuestaGeneral.eliminar(departamentoService.deleteById(id), "departamento", false);
	}

	/**
	 * Retorna el total de departamentos.
	 *
	 * @return {@link ResponseEntity} con el conteo y código 202, o 204 si es cero
	 */
	@Operation(summary = "Contar departamentos", description = "Cuenta el total de registros.")
	@GetMapping("/count")
	public ResponseEntity<Long> contar() {
		return RespuestaGeneral.conteo(departamentoService.count());
	}

	/**
	 * Verifica si existe un departamento con el ID indicado.
	 *
	 * @param id identificador a verificar
	 * @return {@link ResponseEntity} con {@code true} y código 202, o
	 *         {@code false} y código 404
	 */
	@Operation(summary = "Verificar existencia", description = "Verifica si existe un departamento.")
	@GetMapping("/exists/{id}")
	public ResponseEntity<Boolean> existe(@PathVariable Long id) {
		return RespuestaGeneral.existe(departamentoService.exist(id));
	}
}
