package co.edu.unbosque.vesperaroom.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
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

import co.edu.unbosque.vesperaroom.dto.HabitacionDTO;
import co.edu.unbosque.vesperaroom.entity.Habitacion.Estado;
import co.edu.unbosque.vesperaroom.entity.Usuario;
import co.edu.unbosque.vesperaroom.service.AutorizacionService;
import co.edu.unbosque.vesperaroom.service.HabitacionService;
import co.edu.unbosque.vesperaroom.util.RespuestaGeneral;
import co.edu.unbosque.vesperaroom.util.RespuestaHabitacion;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controlador REST para las habitaciones. Las consultas, incluida la de
 * disponibilidad, son públicas; las escrituras solo las hace el propietario del
 * motel o un administrador.
 */
@RestController
@RequestMapping("/habitacion")
@Tag(name = "Habitaciones", description = "Habitaciones de cada motel y su disponibilidad")
@SecurityRequirement(name = "bearerAuth")
public class HabitacionController {

	private final HabitacionService habitacionService;
	private final AutorizacionService autorizacionService;

	public HabitacionController(HabitacionService habitacionService, AutorizacionService autorizacionService) {
		this.habitacionService = habitacionService;
		this.autorizacionService = autorizacionService;
	}

	/**
	 * Crea una habitación. El motel se toma del tipo indicado.
	 *
	 * @param nuevo  objeto con el tipo, el número, el piso y el estado
	 * @param actual usuario autenticado
	 * @return {@link ResponseEntity} con código 201 si es exitoso, 400 si algún
	 *         dato es inválido, 403 si el tipo no es de su motel o 409 si el número
	 *         se repite
	 */
	@Operation(summary = "Crear habitación (JSON)", description = "Crea una habitación en un tipo de un motel propio.")
	@PostMapping(path = "/createjson", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> crearJSON(@RequestBody HabitacionDTO nuevo, @AuthenticationPrincipal Usuario actual) {
		if (!autorizacionService.puedeGestionarTipo(actual, nuevo.getIdTipo())) {
			return RespuestaGeneral.prohibido();
		}

		int status = habitacionService.create(nuevo);
		return RespuestaHabitacion.guardar(status, "Habitación creada exitosamente", HttpStatus.CREATED);
	}

	/**
	 * Retorna todas las habitaciones.
	 *
	 * @return {@link ResponseEntity} con la lista y código 202, o 204 si no hay
	 *         registros
	 */
	@Operation(summary = "Obtener habitaciones", description = "Retorna todas las habitaciones. Solo ADMINISTRADOR.")
	@GetMapping("/getall")
	public ResponseEntity<List<HabitacionDTO>> obtenerTodas() {
		return RespuestaGeneral.lista(habitacionService.getAll());
	}

	/**
	 * Retorna las habitaciones de un motel. Endpoint público.
	 *
	 * @param idMotel identificador del motel
	 * @return {@link ResponseEntity} con la lista y código 202, o 204 si no hay
	 *         registros
	 */
	@Operation(summary = "Habitaciones de un motel", description = "Retorna las habitaciones de un motel. Público.")
	@GetMapping("/getbymotel/{idMotel}")
	public ResponseEntity<List<HabitacionDTO>> obtenerPorMotel(@PathVariable Long idMotel) {
		return RespuestaGeneral.lista(habitacionService.getByMotel(idMotel));
	}

	/**
	 * Retorna las habitaciones de un tipo. Endpoint público.
	 *
	 * @param idTipo identificador del tipo de habitación
	 * @return {@link ResponseEntity} con la lista y código 202, o 204 si no hay
	 *         registros
	 */
	@Operation(summary = "Habitaciones de un tipo", description = "Retorna las habitaciones de un tipo. Público.")
	@GetMapping("/getbytipo/{idTipo}")
	public ResponseEntity<List<HabitacionDTO>> obtenerPorTipo(@PathVariable Long idTipo) {
		return RespuestaGeneral.lista(habitacionService.getByTipo(idTipo));
	}

	/**
	 * Retorna las habitaciones activas de un motel libres en un rango de fechas.
	 * Las fechas se reciben en formato ISO, por ejemplo
	 * {@code 2026-11-20T21:00:00}. Endpoint público.
	 *
	 * @param idMotel identificador del motel
	 * @param inicio  fecha y hora de inicio
	 * @param fin     fecha y hora de fin
	 * @return {@link ResponseEntity} con la lista y código 202, 204 si no hay
	 *         habitaciones libres, o 400 si el rango es inválido
	 */
	@Operation(summary = "Habitaciones disponibles", description = "Habitaciones libres de un motel en un rango "
			+ "(formato 2026-11-20T21:00:00). Público.")
	@GetMapping("/disponibles")
	public ResponseEntity<?> obtenerDisponibles(@RequestParam Long idMotel,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {
		if (!fin.isAfter(inicio)) {
			return new ResponseEntity<>("La fecha de fin debe ser posterior a la de inicio", HttpStatus.BAD_REQUEST);
		}

		return RespuestaGeneral.lista(habitacionService.getDisponibles(idMotel, inicio, fin));
	}

	/**
	 * Busca una habitación por su ID. Endpoint público.
	 *
	 * @param id identificador de la habitación
	 * @return {@link ResponseEntity} con la habitación y código 202, o 404 si no
	 *         existe
	 */
	@Operation(summary = "Obtener habitación por ID", description = "Busca una habitación. Público.")
	@GetMapping("/getbyid/{id}")
	public ResponseEntity<?> obtenerPorId(@PathVariable Long id) {
		return RespuestaGeneral.encontrado(habitacionService.getById(id), "Habitación no encontrada");
	}

	/**
	 * Actualiza una habitación. El usuario debe ser dueño tanto de la habitación
	 * como del nuevo tipo.
	 *
	 * @param id     identificador de la habitación
	 * @param nuevo  objeto con los nuevos datos
	 * @param actual usuario autenticado
	 * @return {@link ResponseEntity} con código 202 si es exitoso, 400 si algún
	 *         dato es inválido, 403 si no es su motel, 404 si no existe o 409 si el
	 *         número se repite
	 */
	@Operation(summary = "Actualizar habitación (JSON)", description = "Actualiza una habitación propia.")
	@PutMapping(path = "/updatejson", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> actualizarJSON(@RequestParam Long id, @RequestBody HabitacionDTO nuevo,
			@AuthenticationPrincipal Usuario actual) {
		if (!autorizacionService.puedeGestionarHabitacion(actual, id)
				|| !autorizacionService.puedeGestionarTipo(actual, nuevo.getIdTipo())) {
			return RespuestaGeneral.prohibido();
		}

		int status = habitacionService.updateById(id, nuevo);
		return RespuestaHabitacion.guardar(status, "Habitación actualizada exitosamente", HttpStatus.ACCEPTED);
	}

	/**
	 * Cambia el estado operativo de una habitación.
	 *
	 * @param id     identificador de la habitación
	 * @param estado nuevo estado
	 * @param actual usuario autenticado
	 * @return {@link ResponseEntity} con código 202 si es exitoso, 403 si no es su
	 *         motel o 404 si no existe
	 */
	@Operation(summary = "Cambiar estado", description = "Cambia el estado operativo de una habitación propia.")
	@PutMapping("/updateestado")
	public ResponseEntity<String> actualizarEstado(@RequestParam Long id, @RequestParam Estado estado,
			@AuthenticationPrincipal Usuario actual) {
		if (!autorizacionService.puedeGestionarHabitacion(actual, id)) {
			return RespuestaGeneral.prohibido();
		}

		return RespuestaGeneral.estado(habitacionService.updateEstado(id, estado), "habitación", true, estado);
	}

	/**
	 * Elimina una habitación.
	 *
	 * @param id     identificador de la habitación
	 * @param actual usuario autenticado
	 * @return {@link ResponseEntity} con código 202 si es exitoso, 403 si no es su
	 *         motel, 404 si no existe o 409 si tiene reservas o fotos
	 */
	@Operation(summary = "Eliminar habitación", description = "Elimina una habitación propia.")
	@DeleteMapping("/deletebyid/{id}")
	public ResponseEntity<String> eliminar(@PathVariable Long id, @AuthenticationPrincipal Usuario actual) {
		if (!autorizacionService.puedeGestionarHabitacion(actual, id)) {
			return RespuestaGeneral.prohibido();
		}

		return RespuestaGeneral.eliminar(habitacionService.deleteById(id), "habitación", true);
	}

	/**
	 * Retorna el total de habitaciones.
	 *
	 * @return {@link ResponseEntity} con el conteo y código 202, o 204 si es cero
	 */
	@Operation(summary = "Contar habitaciones", description = "Cuenta el total de habitaciones.")
	@GetMapping("/count")
	public ResponseEntity<Long> contar() {
		return RespuestaGeneral.conteo(habitacionService.count());
	}
}
