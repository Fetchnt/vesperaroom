/** Enums cuyo nombre en el backend no sirve como texto en español. */
const TEXTOS: Record<string, string> = {
  NO_SHOW: 'Inasistencia',
};

/** Los enums del backend llegan como ACTIVO, FUERA_DE_SERVICIO, TARJETA_CREDITO... */
export function legible(valor: string | null | undefined): string {
  if (!valor) {
    return '—';
  }
  if (TEXTOS[valor]) {
    return TEXTOS[valor];
  }
  const texto = valor.replaceAll('_', ' ').toLowerCase();
  return texto.charAt(0).toUpperCase() + texto.slice(1);
}

export type Tono = 'ok' | 'mal' | 'aviso' | 'info' | 'neutro';

/**
 * Color de cada estado. Cubre los enums de usuario, motel, habitación,
 * reserva y pago en un solo mapa: los valores no se repiten entre entidades.
 */
const TONOS: Record<string, Tono> = {
  ACTIVO: 'ok',
  ACTIVA: 'ok',
  INACTIVO: 'neutro',
  MANTENIMIENTO: 'aviso',
  FUERA_DE_SERVICIO: 'mal',
  PENDIENTE: 'aviso',
  CONFIRMADA: 'info',
  EN_CURSO: 'info',
  FINALIZADA: 'ok',
  CANCELADA: 'mal',
  NO_SHOW: 'mal',
  APROBADO: 'ok',
  RECHAZADO: 'mal',
  REEMBOLSADO: 'neutro',
};

export function tonoDeEstado(estado: string | null | undefined): Tono {
  return (estado && TONOS[estado]) || 'neutro';
}
