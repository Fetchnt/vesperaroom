package co.edu.unbosque.vesperaroom.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
import co.edu.unbosque.vesperaroom.dto.TipoHabitacionDTO;
import co.edu.unbosque.vesperaroom.entity.Usuario;
import co.edu.unbosque.vesperaroom.service.AutorizacionService;
import co.edu.unbosque.vesperaroom.service.TipoHabitacionService;
import co.edu.unbosque.vesperaroom.util.RespuestaGeneral;
import co.edu.unbosque.vesperaroom.util.RespuestaTipoHabitacion;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controlador REST para los tipos de habitación y la asignación de sus
 * características (relación N:M). Las consultas son públicas; las escrituras
 * solo las hace el propietario del motel o un administrador.
 */
@RestController
@RequestMapping("/tipohabitacion")
@Tag(name = "Tipos de habitación", description = "Tipos de habitación de cada motel y sus características")
@SecurityRequirement(name = "bearerAuth")
public class TipoHabitacionController {

	private final TipoHabitacionService tipoHabitacionService;
	private final AutorizacionService autorizacionService;

	public TipoHabitacionController(TipoHabitacionService tipoHabitacionService,
			AutorizacionService autorizacionService) {
		this.tipoHabitacionService = tipoHabitacionService;
		this.autorizacionService = autorizacionService;
	}

	/**
	 * Crea un tipo de habitación en un motel.
	 *
	 * @param nuevo  objeto con el motel y los datos del tipo
	 * @param actual usuario autenticado
	 * @return {@link ResponseEntity} con código 201 si es exitoso, 400 si algún
	 *         dato es inválido, 403 si no es su motel o 409 si el nombre se repite
	 */
	@Operation(summary = "Crear tipo (JSON)", description = "Crea un tipo de habitación en un motel propio.")
	@PostMapping(path = "/createjson", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> crearJSON(@RequestBody TipoHabitacionDTO nuevo,
			@AuthenticationPrincipal Usuario actual) {
		if (!autorizacionService.puedeGestionarMotel(actual, nuevo.getIdMotel())) {
			return RespuestaGeneral.prohibido();
		}

		int status = tipoHabitacionService.create(nuevo);
		return RespuestaTipoHabitacion.guardar(status, "Tipo de habitación creado exitosamente", HttpStatus.CREATED);
	}

	/**
	 * Retorna todos los tipos de habitación.
	 *
	 * @return {@link ResponseEntity} con la lista y código 202, o 204 si no hay
	 *         registros
	 */
	@Operation(summary = "Obtener tipos", description = "Retorna todos los tipos. Solo ADMINISTRADOR.")
	@GetMapping("/getall")
	public ResponseEntity<List<TipoHabitacionDTO>> obtenerTodos() {
		return RespuestaGeneral.lista(tipoHabitacionService.getAll());
	}

	/**
	 * Retorna los tipos de habitación de un motel. Endpoint público.
	 *
	 * @param idMotel identificador del motel
	 * @return {@link ResponseEntity} con la lista y código 202, o 204 si no hay
	 *         registros
	 */
	@Operation(summary = "Tipos de un motel", description = "Retorna los tipos de habitación de un motel. Público.")
	@GetMapping("/getbymotel/{idMotel}")
	public ResponseEntity<List<TipoHabitacionDTO>> obtenerPorMotel(@PathVariable Long idMotel) {
		return RespuestaGeneral.lista(tipoHabitacionService.getByMotel(idMotel));
	}

	/**
	 * Busca un tipo de habitación por su ID. Endpoint público.
	 *
	 * @param id identificador del tipo
	 * @return {@link ResponseEntity} con el tipo y código 202, o 404 si no existe
	 */
	@Operation(summary = "Obtener tipo por ID", description = "Busca un tipo de habitación. Público.")
	@GetMapping("/getbyid/{id}")
	public ResponseEntity<?> obtenerPorId(@PathVariable Long id) {
		return RespuestaGeneral.encontrado(tipoHabitacionService.getById(id), "Tipo de habitación no encontrado");
	}

	/**
	 * Retorna las características de un tipo de habitación. Endpoint público.
	 *
	 * @param id identificador del tipo
	 * @return {@link ResponseEntity} con la lista y código 202, o 204 si no tiene
	 *         características
	 */
	@Operation(summary = "Características de un tipo", description = "Retorna las características asignadas. "
			+ "Público.")
	@GetMapping("/caracteristicas/{id}")
	public ResponseEntity<List<CaracteristicaDTO>> obtenerCaracteristicas(@PathVariable Long id) {
		return RespuestaGeneral.lista(tipoHabitacionService.getCaracteristicas(id));
	}

	/**
	 * Asigna una característica del catálogo a un tipo de habitación.
	 *
	 * @param idTipo           identificador del tipo
	 * @param idCaracteristica identificador de la característica
	 * @param actual           usuario autenticado
	 * @return {@link ResponseEntity} con código 202 si es exitoso, 403 si no es su
	 *         motel, 404 si algo no existe o 409 si ya estaba asignada
	 */
	@Operation(summary = "Asignar característica", description = "Agrega una característica a un tipo propio.")
	@PostMapping("/asignarcaracteristica")
	public ResponseEntity<String> asignarCaracteristica(@RequestParam Long idTipo, @RequestParam Long idCaracteristica,
			@AuthenticationPrincipal Usuario actual) {
		if (!autorizacionService.puedeGestionarTipo(actual, idTipo)) {
			return RespuestaGeneral.prohibido();
		}

		return RespuestaTipoHabitacion.asignar(tipoHabitacionService.asignarCaracteristica(idTipo, idCaracteristica));
	}

	/**
	 * Quita una característica de un tipo de habitación.
	 *
	 * @param idTipo           identificador del tipo
	 * @param idCaracteristica identificador de la característica
	 * @param actual           usuario autenticado
	 * @return {@link ResponseEntity} con código 202 si es exitoso, 403 si no es su
	 *         motel o 404 si no estaba asignada
	 */
	@Operation(summary = "Quitar característica", description = "Quita una característica de un tipo propio.")
	@DeleteMapping("/quitarcaracteristica")
	public ResponseEntity<String> quitarCaracteristica(@RequestParam Long idTipo, @RequestParam Long idCaracteristica,
			@AuthenticationPrincipal Usuario actual) {
		if (!autorizacionService.puedeGestionarTipo(actual, idTipo)) {
			return RespuestaGeneral.prohibido();
		}

		return RespuestaTipoHabitacion.quitar(tipoHabitacionService.quitarCaracteristica(idTipo, idCaracteristica));
	}

	/**
	 * Actualiza un tipo de habitación.
	 *
	 * @param id     identificador del tipo
	 * @param nuevo  objeto con los nuevos datos
	 * @param actual usuario autenticado
	 * @return {@link ResponseEntity} con código 202 si es exitoso, 400 si algún
	 *         dato es inválido, 403 si no es su motel, 404 si no existe o 409 si el
	 *         nombre se repite
	 */
	@Operation(summary = "Actualizar tipo (JSON)", description = "Actualiza un tipo de habitación propio.")
	@PutMapping(path = "/updatejson", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> actualizarJSON(@RequestParam Long id, @RequestBody TipoHabitacionDTO nuevo,
			@AuthenticationPrincipal Usuario actual) {
		if (!autorizacionService.puedeGestionarTipo(actual, id)) {
			return RespuestaGeneral.prohibido();
		}

		int status = tipoHabitacionService.updateById(id, nuevo);
		return RespuestaTipoHabitacion.guardar(status, "Tipo de habitación actualizado exitosamente",
				HttpStatus.ACCEPTED);
	}

	/**
	 * Elimina un tipo de habitación.
	 *
	 * @param id     identificador del tipo
	 * @param actual usuario autenticado
	 * @return {@link ResponseEntity} con código 202 si es exitoso, 403 si no es su
	 *         motel, 404 si no existe o 409 si tiene habitaciones o tarifas
	 */
	@Operation(summary = "Eliminar tipo", description = "Elimina un tipo de habitación propio.")
	@DeleteMapping("/deletebyid/{id}")
	public ResponseEntity<String> eliminar(@PathVariable Long id, @AuthenticationPrincipal Usuario actual) {
		if (!autorizacionService.puedeGestionarTipo(actual, id)) {
			return RespuestaGeneral.prohibido();
		}

		return RespuestaGeneral.eliminar(tipoHabitacionService.deleteById(id), "tipo de habitación", false);
	}

	/**
	 * Retorna el total de tipos de habitación.
	 *
	 * @return {@link ResponseEntity} con el conteo y código 202, o 204 si es cero
	 */
	@Operation(summary = "Contar tipos", description = "Cuenta el total de tipos de habitación.")
	@GetMapping("/count")
	public ResponseEntity<Long> contar() {
		return RespuestaGeneral.conteo(tipoHabitacionService.count());
	}
}
