/**
 * Tipos calcados de los DTO del backend (co.edu.unbosque.vesperaroom.dto).
 * Los campos marcados READ_ONLY allá los devuelve el servidor y no se envían.
 * Los BigDecimal/LocalDate(Time) de Java viajan como number y string ISO.
 */

export type Rol = 'ADMINISTRADOR' | 'CLIENTE' | 'PROPIETARIO';
export type EstadoUsuario = 'ACTIVO' | 'INACTIVO';
export type EstadoMotel = 'ACTIVO' | 'INACTIVO';
export type EstadoHabitacion = 'ACTIVA' | 'MANTENIMIENTO' | 'FUERA_DE_SERVICIO';
export type EstadoReserva =
  | 'PENDIENTE'
  | 'CONFIRMADA'
  | 'EN_CURSO'
  | 'FINALIZADA'
  | 'CANCELADA'
  | 'NO_SHOW';
export type EstadoPago = 'PENDIENTE' | 'APROBADO' | 'RECHAZADO' | 'REEMBOLSADO';
export type MetodoPago =
  | 'EFECTIVO'
  | 'TARJETA_CREDITO'
  | 'TARJETA_DEBITO'
  | 'PSE'
  | 'NEQUI'
  | 'DAVIPLATA';
export type TipoDia = 'TODOS' | 'ENTRE_SEMANA' | 'FIN_DE_SEMANA' | 'FESTIVO';
export type TipoCuenta = 'AHORROS' | 'CORRIENTE';

export type Sesion = {
  token: string;
  idUsuario: number;
  nombre: string;
  correo: string;
  rol: Rol;
}

export type Credenciales = {
  correo: string;
  contrasena: string;
}

export type Usuario = {
  idUsuario?: number;
  nombre: string;
  apellido: string;
  correo: string;
  telefono: string;
  contrasena?: string;
  rol: Rol;
  estado?: EstadoUsuario;
  fechaRegistro?: string;
}

export type Departamento = {
  idDepartamento?: number;
  nombre: string;
}

export type Ciudad = {
  idCiudad?: number;
  idDepartamento: number;
  nombre: string;
}

export type Caracteristica = {
  idCaracteristica?: number;
  nombre: string;
  icono?: string;
}

export type Modalidad = {
  idModalidad?: number;
  nombre: string;
  duracionHoras: number;
}

export type Motel = {
  idMotel?: number;
  idPropietario?: number;
  idCiudad: number;
  nombre: string;
  descripcion?: string;
  calle: string;
  carrera: string;
  latitud?: number;
  longitud?: number;
  estado?: EstadoMotel;
}

/** 1:1 con motel: su identificador es idMotel. */
export type PoliticaReserva = {
  idMotel: number;
  diasHabilesDevolucion: number;
  porcentajeReembolso: number;
  capacidadMaxSinRecargo: number;
  tarifaPersonaAdicional: number;
  anticipacionMinimaHoras: number;
  tiempoGraciaNoShowMin: number;
  aceptaReembolso?: boolean;
}

export type TipoHabitacion = {
  idTipo?: number;
  idMotel: number;
  nombre: string;
  capacidadPersonas: number;
  descripcion?: string;
}

export type Habitacion = {
  idHabitacion?: number;
  idMotel?: number;
  idTipo: number;
  numero: string;
  piso?: number;
  estado?: EstadoHabitacion;
}

export type Foto = {
  idFoto?: number;
  idMotel?: number | null;
  idHabitacion?: number | null;
  uri: string;
  orden?: number;
}

export type Tarifa = {
  idTarifa?: number;
  idTipo: number;
  idModalidad: number;
  precio: number;
  tipoDia: TipoDia;
  vigenteDesde: string;
  vigenteHasta?: string | null;
}

export type ServicioAdicional = {
  idServicio?: number;
  idMotel: number;
  nombre: string;
  descripcion?: string;
  precio: number;
  activo?: boolean;
}

export type ReservaServicio = {
  idReserva?: number;
  idServicio: number;
  cantidad: number;
  precioUnitario?: number;
}

export type Reserva = {
  idReserva?: number;
  codigoReserva?: string;
  idUsuario?: number;
  idHabitacion: number;
  idTarifa: number;
  cantidadPersonas: number;
  fechaCreacion?: string;
  fechaHoraInicio: string;
  fechaHoraFin?: string;
  estado?: EstadoReserva;
  total?: number;
  fechaCancelacion?: string | null;
  montoReembolso?: number | null;
  fechaLimiteReembolso?: string | null;
  servicios?: ReservaServicio[];
}

export type Pago = {
  idPago?: number;
  idReserva: number;
  monto: number;
  fechaPago?: string;
  metodoPago: MetodoPago;
  estado?: EstadoPago;
  referenciaTransaccion?: string;
}

export type Resena = {
  idResena?: number;
  idReserva: number;
  calificacion: number;
  comentario?: string;
  fecha?: string;
}

/** 1:1 con resena: su identificador es idResena. */
export type RespuestaPropietario = {
  idResena: number;
  comentario: string;
  fecha?: string;
}

/** 1:1 con usuario: su identificador es idUsuario. */
export type PerfilPropietario = {
  idUsuario?: number;
  nit: string;
  razonSocial: string;
  banco: string;
  tipoCuenta: TipoCuenta;
  numeroCuenta: string;
  fechaVerificacion?: string | null;
}
