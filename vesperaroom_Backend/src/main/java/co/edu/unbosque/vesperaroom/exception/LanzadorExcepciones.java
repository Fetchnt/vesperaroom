package co.edu.unbosque.vesperaroom.exception;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import co.edu.unbosque.vesperaroom.entity.Usuario.Estado;
import co.edu.unbosque.vesperaroom.entity.Usuario.Rol;

/**
 * Clase encargada de validar datos y lanzar excepciones según las reglas del
 * sistema.
 */
public class LanzadorExcepciones {

	private static final String PATRON_CARACTERES = "[a-zA-ZáéíóúÁÉÍÓÚüÜñÑ ]+";
	private static final String PATRON_CORREO = "^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)*\\.[a-zA-Z]{2,}$";
	private static final String PATRON_TELEFONO = "^[0-9]{7,15}$";
	private static final String PATRON_URI = "^https?://\\S+$";
	private static final String PATRON_NIT = "^[0-9]{9,10}(-[0-9])?$";
	private static final String PATRON_CUENTA = "^[0-9]{6,20}$";
	private static final int TAMANO_MINIMO_CONTRASENA = 8;
	private static final BigDecimal LATITUD_MAXIMA = BigDecimal.valueOf(90);
	private static final BigDecimal LONGITUD_MAXIMA = BigDecimal.valueOf(180);

	private LanzadorExcepciones() {
	}

	/**
	 * Verifica que un texto contenga solo letras y espacios.
	 *
	 * @param valor texto a validar
	 * @throws InvalidCharException si es nulo, vacío o contiene caracteres inválidos
	 */
	public static void verificarCaracteres(String valor) throws InvalidCharException {
		if (valor == null || valor.isBlank() || !valor.matches(PATRON_CARACTERES)) {
			throw new InvalidCharException();
		}
	}

	/**
	 * Verifica que un correo electrónico tenga un formato válido.
	 *
	 * @param correo correo a validar
	 * @throws CorreoException si es nulo o no cumple el formato
	 */
	public static void verificarCorreo(String correo) throws CorreoException {
		if (correo == null || !correo.matches(PATRON_CORREO)) {
			throw new CorreoException();
		}
	}

	/**
	 * Verifica que un teléfono tenga entre 7 y 15 dígitos numéricos.
	 *
	 * @param telefono teléfono a validar
	 * @throws TelefonoException si es nulo o no cumple el formato
	 */
	public static void verificarTelefono(String telefono) throws TelefonoException {
		if (telefono == null || !telefono.matches(PATRON_TELEFONO)) {
			throw new TelefonoException();
		}
	}

	/**
	 * Verifica que una contraseña tenga el tamaño mínimo requerido.
	 *
	 * @param contrasena contraseña a validar
	 * @throws PasswordSizeException si es nula o su tamaño es menor a 8
	 */
	public static void verificarTamanoContrasena(String contrasena) throws PasswordSizeException {
		if (contrasena == null || contrasena.length() < TAMANO_MINIMO_CONTRASENA) {
			throw new PasswordSizeException();
		}
	}

	/**
	 * Verifica que la contraseña contenga al menos una mayúscula, una minúscula y
	 * un número.
	 *
	 * @param contrasena contraseña a validar
	 * @throws ContrasenaException si no cumple las condiciones
	 */
	public static void verificarContrasena(String contrasena) throws ContrasenaException {
		boolean tieneMayuscula = contrasena.matches(".*[A-Z].*");
		boolean tieneMinuscula = contrasena.matches(".*[a-z].*");
		boolean tieneNumero = contrasena.matches(".*\\d.*");
		if (!tieneMayuscula || !tieneMinuscula || !tieneNumero) {
			throw new ContrasenaException();
		}
	}

	/**
	 * Verifica que el rol no sea nulo.
	 *
	 * @param rol rol a validar
	 * @throws RolException si es nulo
	 */
	public static void verificarRol(Rol rol) throws RolException {
		if (rol == null) {
			throw new RolException();
		}
	}

	/**
	 * Verifica que el rol pueda asignarse en el registro público. Solo se permiten
	 * los roles {@code CLIENTE} y {@code PROPIETARIO}.
	 *
	 * @param rol rol a validar
	 * @throws RolNoPermitidoException si el rol es {@code ADMINISTRADOR}
	 */
	public static void verificarRolPublico(Rol rol) throws RolNoPermitidoException {
		if (rol == Rol.ADMINISTRADOR) {
			throw new RolNoPermitidoException();
		}
	}

	/**
	 * Verifica que el estado no sea nulo.
	 *
	 * @param estado estado a validar
	 * @throws EstadoException si es nulo
	 */
	public static void verificarEstado(Estado estado) throws EstadoException {
		if (estado == null) {
			throw new EstadoException();
		}
	}

	/**
	 * Verifica si un correo ya está registrado.
	 *
	 * @param existe indica si ya existe
	 * @throws CorreoDuplicadoException si ya existe
	 */
	public static void verificarCorreoDuplicado(boolean existe) throws CorreoDuplicadoException {
		if (existe) {
			throw new CorreoDuplicadoException();
		}
	}

	/**
	 * Verifica si un teléfono ya está registrado.
	 *
	 * @param existe indica si ya existe
	 * @throws TelefonoDuplicadoException si ya existe
	 */
	public static void verificarTelefonoDuplicado(boolean existe) throws TelefonoDuplicadoException {
		if (existe) {
			throw new TelefonoDuplicadoException();
		}
	}

	/**
	 * Verifica la existencia de un registro.
	 *
	 * @param existe  indica si existe
	 * @param entidad nombre de la entidad
	 * @param id      identificador
	 * @throws RegistroNoEncontradoException si no existe
	 */
	public static void verificarExistencia(boolean existe, String entidad, Long id)
			throws RegistroNoEncontradoException {
		if (!existe) {
			throw new RegistroNoEncontradoException(entidad, id);
		}
	}

	/**
	 * Verifica que un texto obligatorio no esté vacío y no supere la longitud
	 * máxima de su columna. A diferencia de {@link #verificarCaracteres(String)},
	 * admite números y signos (por ejemplo, "Motel 7 Lunas" o "Bogotá D.C.").
	 *
	 * @param valor  texto a validar
	 * @param maximo longitud máxima permitida
	 * @param campo  nombre del campo, usado en el mensaje
	 * @throws TextoInvalidoException si es nulo, vacío o demasiado largo
	 */
	public static void verificarTexto(String valor, int maximo, String campo) throws TextoInvalidoException {
		if (valor == null || valor.isBlank() || valor.length() > maximo) {
			throw new TextoInvalidoException(campo);
		}
	}

	/**
	 * Verifica que un texto opcional, si se indica, no supere la longitud máxima de
	 * su columna.
	 *
	 * @param valor  texto a validar (puede ser nulo o vacío)
	 * @param maximo longitud máxima permitida
	 * @param campo  nombre del campo, usado en el mensaje
	 * @throws TextoInvalidoException si supera la longitud máxima
	 */
	public static void verificarTextoOpcional(String valor, int maximo, String campo)
			throws TextoInvalidoException {
		if (valor != null && valor.length() > maximo) {
			throw new TextoInvalidoException(campo);
		}
	}

	/**
	 * Verifica que un valor entero esté dentro de un rango cerrado.
	 *
	 * @param valor  valor a validar
	 * @param minimo valor mínimo permitido
	 * @param maximo valor máximo permitido
	 * @param campo  nombre del campo, usado en el mensaje
	 * @throws ValorFueraDeRangoException si es nulo o está fuera del rango
	 */
	public static void verificarRango(Integer valor, int minimo, int maximo, String campo)
			throws ValorFueraDeRangoException {
		if (valor == null || valor < minimo || valor > maximo) {
			throw new ValorFueraDeRangoException(campo);
		}
	}

	/**
	 * Verifica que un valor monetario sea positivo (o cero, si se permite). Se usa
	 * {@link BigDecimal#signum()} porque los montos se manejan como
	 * {@link BigDecimal} para no perder precisión.
	 *
	 * @param valor       valor a validar
	 * @param permiteCero indica si el cero es un valor válido
	 * @param campo       nombre del campo, usado en el mensaje
	 * @throws ValorFueraDeRangoException si es nulo, negativo o cero cuando no se
	 *                                    permite
	 */
	public static void verificarMonto(BigDecimal valor, boolean permiteCero, String campo)
			throws ValorFueraDeRangoException {
		if (valor == null || valor.signum() < 0 || (!permiteCero && valor.signum() == 0)) {
			throw new ValorFueraDeRangoException(campo);
		}
	}

	/**
	 * Verifica que se haya indicado un valor obligatorio (llave foránea, enum,
	 * fecha...).
	 *
	 * @param valor valor a validar
	 * @param campo nombre del campo, usado en el mensaje
	 * @throws CampoObligatorioException si es nulo
	 */
	public static void verificarObligatorio(Object valor, String campo) throws CampoObligatorioException {
		if (valor == null) {
			throw new CampoObligatorioException(campo);
		}
	}

	/**
	 * Verifica que exista el registro al que se hace referencia con una llave
	 * foránea.
	 *
	 * @param existe  indica si el registro referenciado existe
	 * @param entidad nombre de la entidad referenciada
	 * @param id      identificador referenciado
	 * @throws ReferenciaNoEncontradaException si no existe
	 */
	public static void verificarReferencia(boolean existe, String entidad, Long id)
			throws ReferenciaNoEncontradaException {
		if (!existe) {
			throw new ReferenciaNoEncontradaException(entidad, id);
		}
	}

	/**
	 * Verifica que un dato que debe ser único no esté registrado.
	 *
	 * @param existe  indica si el dato ya existe
	 * @param detalle descripción del dato, usada en el mensaje
	 * @throws RegistroDuplicadoException si ya existe
	 */
	public static void verificarDuplicado(boolean existe, String detalle) throws RegistroDuplicadoException {
		if (existe) {
			throw new RegistroDuplicadoException(detalle);
		}
	}

	/**
	 * Verifica las coordenadas geográficas de un motel. Son opcionales, pero si se
	 * indica una deben indicarse ambas y estar en rango.
	 *
	 * @param latitud  latitud en grados (-90 a 90)
	 * @param longitud longitud en grados (-180 a 180)
	 * @throws CoordenadaException si solo se indica una o alguna está fuera de rango
	 */
	public static void verificarCoordenadas(BigDecimal latitud, BigDecimal longitud) throws CoordenadaException {
		if (latitud == null && longitud == null) {
			return;
		}
		if (latitud == null || longitud == null || latitud.abs().compareTo(LATITUD_MAXIMA) > 0
				|| longitud.abs().compareTo(LONGITUD_MAXIMA) > 0) {
			throw new CoordenadaException();
		}
	}

	/**
	 * Verifica que la dirección de una foto sea una URL http o https.
	 *
	 * @param uri dirección a validar
	 * @throws UriException si es nula, no empieza por http(s):// o supera 500
	 *                      caracteres
	 */
	public static void verificarUri(String uri) throws UriException {
		if (uri == null || uri.length() > 500 || !uri.matches(PATRON_URI)) {
			throw new UriException();
		}
	}

	/**
	 * Verifica que el NIT tenga 9 o 10 dígitos y, opcionalmente, el dígito de
	 * verificación separado por guion.
	 *
	 * @param nit NIT a validar
	 * @throws NitException si es nulo o no cumple el formato
	 */
	public static void verificarNit(String nit) throws NitException {
		if (nit == null || !nit.matches(PATRON_NIT)) {
			throw new NitException();
		}
	}

	/**
	 * Verifica que el número de cuenta bancaria tenga entre 6 y 20 dígitos.
	 *
	 * @param numeroCuenta número de cuenta a validar
	 * @throws CuentaBancariaException si es nulo o no cumple el formato
	 */
	public static void verificarNumeroCuenta(String numeroCuenta) throws CuentaBancariaException {
		if (numeroCuenta == null || !numeroCuenta.matches(PATRON_CUENTA)) {
			throw new CuentaBancariaException();
		}
	}

	/**
	 * Verifica un periodo de vigencia: la fecha inicial es obligatoria y la final,
	 * si se indica, no puede ser anterior a la inicial.
	 *
	 * @param desde fecha inicial
	 * @param hasta fecha final (nula si no tiene fin)
	 * @throws FechaException si falta la fecha inicial o el rango es inválido
	 */
	public static void verificarVigencia(LocalDate desde, LocalDate hasta) throws FechaException {
		if (desde == null || (hasta != null && hasta.isBefore(desde))) {
			throw new FechaException("La vigencia debe tener fecha inicial y la fecha final no puede ser anterior.");
		}
	}

	/**
	 * Verifica que una fecha y hora sea posterior al momento actual.
	 *
	 * @param fecha fecha y hora a validar
	 * @throws FechaException si es nula o no está en el futuro
	 */
	public static void verificarFechaFutura(LocalDateTime fecha) throws FechaException {
		if (fecha == null || !fecha.isAfter(LocalDateTime.now())) {
			throw new FechaException("La fecha de inicio debe ser posterior al momento actual.");
		}
	}

	/**
	 * Verifica que una reserva todavía no haya comenzado. Recibe el momento de
	 * referencia como parámetro para que la tarea automática y las pruebas puedan
	 * usar una hora fija.
	 *
	 * @param inicio fecha y hora de inicio de la reserva
	 * @param ahora  momento de referencia
	 * @throws FechaException si la reserva ya comenzó
	 */
	public static void verificarAntesDelInicio(LocalDateTime inicio, LocalDateTime ahora) throws FechaException {
		if (!inicio.isAfter(ahora)) {
			throw new FechaException("La reserva ya comenzó.");
		}
	}

	/**
	 * Verifica que una foto pertenezca a exactamente un dueño: un motel o una
	 * habitación.
	 *
	 * @param idMotel      identificador del motel (o nulo)
	 * @param idHabitacion identificador de la habitación (o nulo)
	 * @throws FotoDuenoException si ambos son nulos o ambos tienen valor
	 */
	public static void verificarDuenoFoto(Long idMotel, Long idHabitacion) throws FotoDuenoException {
		if ((idMotel == null) == (idHabitacion == null)) {
			throw new FotoDuenoException();
		}
	}

	/**
	 * Verifica que el rol de un usuario sea {@code PROPIETARIO}.
	 *
	 * @param rol rol a validar
	 * @throws RolPropietarioException si el rol es distinto
	 */
	public static void verificarRolPropietario(Rol rol) throws RolPropietarioException {
		if (rol != Rol.PROPIETARIO) {
			throw new RolPropietarioException();
		}
	}

	/**
	 * Verifica que una habitación pueda reservarse.
	 *
	 * @param disponible indica si la habitación está activa y su motel también
	 * @throws HabitacionNoDisponibleException si no está disponible
	 */
	public static void verificarHabitacionDisponible(boolean disponible) throws HabitacionNoDisponibleException {
		if (!disponible) {
			throw new HabitacionNoDisponibleException();
		}
	}

	/**
	 * Verifica que el horario pedido no se cruce con otra reserva.
	 *
	 * @param ocupado indica si existe una reserva que se cruza
	 * @throws HorarioOcupadoException si el horario está ocupado
	 */
	public static void verificarHorarioLibre(boolean ocupado) throws HorarioOcupadoException {
		if (ocupado) {
			throw new HorarioOcupadoException();
		}
	}

	/**
	 * Verifica que la tarifa corresponda al tipo de la habitación reservada.
	 *
	 * @param aplica indica si el tipo de la tarifa coincide con el de la habitación
	 * @throws TarifaNoAplicableException si no coincide
	 */
	public static void verificarTarifaAplicable(boolean aplica) throws TarifaNoAplicableException {
		if (!aplica) {
			throw new TarifaNoAplicableException();
		}
	}

	/**
	 * Verifica que una fecha esté dentro del periodo de vigencia de una tarifa.
	 *
	 * @param fecha fecha de la reserva
	 * @param desde inicio de la vigencia
	 * @param hasta fin de la vigencia (nulo si no tiene fin)
	 * @throws TarifaNoVigenteException si la fecha está fuera de la vigencia
	 */
	public static void verificarTarifaVigente(LocalDate fecha, LocalDate desde, LocalDate hasta)
			throws TarifaNoVigenteException {
		if (fecha.isBefore(desde) || (hasta != null && fecha.isAfter(hasta))) {
			throw new TarifaNoVigenteException();
		}
	}

	/**
	 * Verifica que el motel tenga política de reserva: sin ella no hay reglas de
	 * anticipación, recargo ni reembolso con las que aceptar la reserva.
	 *
	 * @param existe indica si el motel tiene política registrada
	 * @throws PoliticaNoDefinidaException si el motel no tiene política
	 */
	public static void verificarPoliticaDefinida(boolean existe) throws PoliticaNoDefinidaException {
		if (!existe) {
			throw new PoliticaNoDefinidaException();
		}
	}

	/**
	 * Verifica que entre el momento actual y el inicio de la reserva haya al menos
	 * las horas de anticipación que exige el motel.
	 *
	 * @param inicio fecha y hora de inicio de la reserva
	 * @param horas  anticipación mínima en horas
	 * @throws AnticipacionException si no se cumple la anticipación
	 */
	public static void verificarAnticipacion(LocalDateTime inicio, int horas) throws AnticipacionException {
		if (inicio.isBefore(LocalDateTime.now().plusHours(horas))) {
			throw new AnticipacionException();
		}
	}

	/**
	 * Verifica que la cantidad de personas no supere la capacidad de la habitación.
	 *
	 * @param personas  cantidad de personas de la reserva
	 * @param capacidad capacidad máxima del tipo de habitación
	 * @throws CapacidadExcedidaException si la supera
	 */
	public static void verificarCapacidad(int personas, int capacidad) throws CapacidadExcedidaException {
		if (personas > capacidad) {
			throw new CapacidadExcedidaException();
		}
	}

	/**
	 * Verifica que un servicio adicional pueda incluirse en la reserva.
	 *
	 * @param valido indica si el servicio existe, está activo, pertenece al motel y
	 *               su cantidad es positiva
	 * @throws ServicioNoValidoException si no es válido
	 */
	public static void verificarServicio(boolean valido) throws ServicioNoValidoException {
		if (!valido) {
			throw new ServicioNoValidoException();
		}
	}

	/**
	 * Verifica que un cambio de estado esté permitido por el ciclo de vida.
	 *
	 * @param permitida indica si la transición es válida
	 * @throws TransicionEstadoException si no está permitida
	 */
	public static void verificarTransicion(boolean permitida) throws TransicionEstadoException {
		if (!permitida) {
			throw new TransicionEstadoException();
		}
	}

	/**
	 * Verifica que la reserva esté en un estado que permita la operación.
	 *
	 * @param permitido indica si el estado actual lo permite
	 * @param detalle   operación que se intenta, usada en el mensaje
	 * @throws EstadoReservaException si el estado no lo permite
	 */
	public static void verificarEstadoReserva(boolean permitido, String detalle) throws EstadoReservaException {
		if (!permitido) {
			throw new EstadoReservaException(detalle);
		}
	}

	/**
	 * Verifica que un nuevo pago no haya superado el saldo pendiente de la
	 * reserva. La comparación la hace el repositorio dentro de la transacción,
	 * con la reserva bloqueada, para que dos pagos simultáneos no pasen los dos.
	 *
	 * @param excedido indica si el pago superaba lo que faltaba por pagar
	 * @throws MontoExcedidoException si el pago excede el saldo
	 */
	public static void verificarSaldo(boolean excedido) throws MontoExcedidoException {
		if (excedido) {
			throw new MontoExcedidoException();
		}
	}

	/**
	 * Verifica que la reserva pertenezca al usuario que hace la operación.
	 *
	 * @param esDueno indica si la reserva es del usuario
	 * @throws PropiedadReservaException si no lo es
	 */
	public static void verificarPropiedadReserva(boolean esDueno) throws PropiedadReservaException {
		if (!esDueno) {
			throw new PropiedadReservaException();
		}
	}
}
