package co.edu.unbosque.vesperaroom.controller;

import java.net.URI;
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

import co.edu.unbosque.vesperaroom.dto.MotelDTO;
import co.edu.unbosque.vesperaroom.dto.MotelResumenDTO;
import co.edu.unbosque.vesperaroom.entity.Motel.Estado;
import co.edu.unbosque.vesperaroom.entity.Usuario;
import co.edu.unbosque.vesperaroom.service.AutorizacionService;
import co.edu.unbosque.vesperaroom.service.MotelService;
import co.edu.unbosque.vesperaroom.util.RespuestaGeneral;
import co.edu.unbosque.vesperaroom.util.RespuestaMotel;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controlador REST para los moteles. El catálogo de moteles activos es público;
 * crear y modificar requiere rol PROPIETARIO o ADMINISTRADOR, y un propietario
 * solo puede modificar sus propios moteles (lo verifica
 * {@link AutorizacionService}).
 */
@RestController
@RequestMapping("/motel")
@Tag(name = "Moteles", description = "Gestión de moteles")
@SecurityRequirement(name = "bearerAuth")
public class MotelController {

	private final MotelService motelService;
	private final AutorizacionService autorizacionService;

	public MotelController(MotelService motelService, AutorizacionService autorizacionService) {
		this.motelService = motelService;
		this.autorizacionService = autorizacionService;
	}

	/**
	 * Crea un motel. Si lo crea un PROPIETARIO, el motel queda a su nombre; un
	 * ADMINISTRADOR debe indicar el {@code idPropietario}.
	 *
	 * @param nuevo  objeto con los datos del motel
	 * @param actual usuario autenticado
	 * @return {@link ResponseEntity} con código 201 si es exitoso o 400 si algún
	 *         dato es inválido
	 */
	@Operation(summary = "Crear motel (JSON)", description = "Crea un motel. PROPIETARIO o ADMINISTRADOR.")
	@ApiResponses(value = { @ApiResponse(responseCode = "201", description = "Motel creado"),
			@ApiResponse(responseCode = "400", description = "Datos inválidos") })
	@PostMapping(path = "/createjson", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> crearJSON(@RequestBody MotelDTO nuevo, @AuthenticationPrincipal Usuario actual) {
		if (!autorizacionService.esAdministrador(actual)) {
			nuevo.setIdPropietario(actual.getIdUsuario());
		}

		int status = motelService.create(nuevo);
		ResponseEntity<String> respuesta = RespuestaMotel.guardar(status, "Motel creado exitosamente",
				HttpStatus.CREATED);
		if (status != 0) {
			return respuesta;
		}
		// El id del motel nuevo va en Location para poder subirle la foto enseguida
		return ResponseEntity.status(respuesta.getStatusCode())
				.location(URI.create("/motel/getbyid/" + nuevo.getIdMotel())).body(respuesta.getBody());
	}

	/**
	 * Retorna los moteles activos. Endpoint público.
	 *
	 * @return {@link ResponseEntity} con la lista y código 202, o 204 si no hay
	 *         registros
	 */
	@Operation(summary = "Obtener moteles activos", description = "Catálogo público de moteles activos.")
	@GetMapping("/getall")
	public ResponseEntity<List<MotelDTO>> obtenerActivos() {
		return RespuestaGeneral.lista(motelService.getActivos());
	}

	/**
	 * Retorna las fichas del catálogo: moteles activos con foto, calificación,
	 * tarifa más barata y tipos de habitación. Endpoint público.
	 *
	 * @return {@link ResponseEntity} con la lista y código 202, o 204 si no hay
	 *         registros
	 */
	@Operation(summary = "Obtener fichas del catálogo", description = "Moteles activos con foto, calificación, "
			+ "precio desde y tipos de habitación con tarifas y características, en una sola petición.")
	@GetMapping("/resumen")
	public ResponseEntity<List<MotelResumenDTO>> obtenerResumen() {
		return RespuestaGeneral.lista(motelService.getResumen());
	}

	/**
	 * Retorna todos los moteles, activos e inactivos.
	 *
	 * @return {@link ResponseEntity} con la lista y código 202, o 204 si no hay
	 *         registros
	 */
	@Operation(summary = "Obtener todos los moteles", description = "Incluye inactivos. Solo ADMINISTRADOR.")
	@GetMapping("/getall/todos")
	public ResponseEntity<List<MotelDTO>> obtenerTodos() {
		return RespuestaGeneral.lista(motelService.getAll());
	}

	/**
	 * Retorna los moteles activos de una ciudad. Endpoint público.
	 *
	 * @param idCiudad identificador de la ciudad
	 * @return {@link ResponseEntity} con la lista y código 202, o 204 si no hay
	 *         registros
	 */
	@Operation(summary = "Moteles por ciudad", description = "Moteles activos de una ciudad. Público.")
	@GetMapping("/getbyciudad/{idCiudad}")
	public ResponseEntity<List<MotelDTO>> obtenerPorCiudad(@PathVariable Long idCiudad) {
		return RespuestaGeneral.lista(motelService.getActivosByCiudad(idCiudad));
	}

	/**
	 * Retorna los moteles del propietario autenticado.
	 *
	 * @param actual usuario autenticado
	 * @return {@link ResponseEntity} con la lista y código 202, o 204 si no tiene
	 *         moteles
	 */
	@Operation(summary = "Mis moteles", description = "Moteles del propietario autenticado.")
	@GetMapping("/mis-moteles")
	public ResponseEntity<List<MotelDTO>> obtenerMisMoteles(@AuthenticationPrincipal Usuario actual) {
		return RespuestaGeneral.lista(motelService.getByPropietario(actual.getIdUsuario()));
	}

	/**
	 * Busca un motel por su ID. Endpoint público.
	 *
	 * @param id identificador del motel
	 * @return {@link ResponseEntity} con el motel y código 202, o 404 si no existe
	 */
	@Operation(summary = "Obtener motel por ID", description = "Busca un motel mediante su ID. Público.")
	@GetMapping("/getbyid/{id}")
	public ResponseEntity<?> obtenerPorId(@PathVariable Long id) {
		return RespuestaGeneral.encontrado(motelService.getById(id), "Motel no encontrado");
	}

	/**
	 * Actualiza un motel. Un propietario no puede traspasarlo a otro propietario.
	 *
	 * @param id     identificador del motel
	 * @param nuevo  objeto con los nuevos datos
	 * @param actual usuario autenticado
	 * @return {@link ResponseEntity} con código 202 si es exitoso, 400 si algún
	 *         dato es inválido, 403 si no es su motel o 404 si no existe
	 */
	@Operation(summary = "Actualizar motel (JSON)", description = "Actualiza un motel propio o, si es "
			+ "ADMINISTRADOR, cualquiera.")
	@PutMapping(path = "/updatejson", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> actualizarJSON(@RequestParam Long id, @RequestBody MotelDTO nuevo,
			@AuthenticationPrincipal Usuario actual) {
		if (!autorizacionService.puedeGestionarMotel(actual, id)) {
			return RespuestaGeneral.prohibido();
		}
		if (!autorizacionService.esAdministrador(actual)) {
			nuevo.setIdPropietario(actual.getIdUsuario());
		}

		int status = motelService.updateById(id, nuevo);
		return RespuestaMotel.guardar(status, "Motel actualizado exitosamente", HttpStatus.ACCEPTED);
	}

	/**
	 * Activa o inactiva un motel.
	 *
	 * @param id     identificador del motel
	 * @param estado nuevo estado
	 * @param actual usuario autenticado
	 * @return {@link ResponseEntity} con código 202 si es exitoso, 403 si no es su
	 *         motel o 404 si no existe
	 */
	@Operation(summary = "Cambiar estado", description = "Activa o inactiva un motel propio.")
	@PutMapping("/updateestado")
	public ResponseEntity<String> actualizarEstado(@RequestParam Long id, @RequestParam Estado estado,
			@AuthenticationPrincipal Usuario actual) {
		if (!autorizacionService.puedeGestionarMotel(actual, id)) {
			return RespuestaGeneral.prohibido();
		}

		return RespuestaGeneral.estado(motelService.updateEstado(id, estado), "motel", false, estado);
	}

	/**
	 * Elimina un motel.
	 *
	 * @param id     identificador del motel
	 * @param actual usuario autenticado
	 * @return {@link ResponseEntity} con código 202 si es exitoso, 403 si no es su
	 *         motel, 404 si no existe o 409 si tiene registros asociados
	 */
	@Operation(summary = "Eliminar motel", description = "Elimina un motel propio sin registros asociados.")
	@DeleteMapping("/deletebyid/{id}")
	public ResponseEntity<String> eliminar(@PathVariable Long id, @AuthenticationPrincipal Usuario actual) {
		if (!autorizacionService.puedeGestionarMotel(actual, id)) {
			return RespuestaGeneral.prohibido();
		}

		return RespuestaGeneral.eliminar(motelService.deleteById(id), "motel", false);
	}

	/**
	 * Retorna el total de moteles registrados.
	 *
	 * @return {@link ResponseEntity} con el conteo y código 202, o 204 si es cero
	 */
	@Operation(summary = "Contar moteles", description = "Cuenta el total de moteles.")
	@GetMapping("/count")
	public ResponseEntity<Long> contar() {
		return RespuestaGeneral.conteo(motelService.count());
	}

	/**
	 * Verifica si existe un motel con el ID indicado.
	 *
	 * @param id identificador del motel
	 * @return {@link ResponseEntity} con {@code true} y código 202, o
	 *         {@code false} y código 404
	 */
	@Operation(summary = "Verificar existencia", description = "Verifica si existe un motel.")
	@GetMapping("/exists/{id}")
	public ResponseEntity<Boolean> existe(@PathVariable Long id) {
		return RespuestaGeneral.existe(motelService.exist(id));
	}
}
