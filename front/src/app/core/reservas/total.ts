import type { PoliticaReserva, ServicioAdicional } from '../api/models';

export interface ServicioElegido {
  servicio: ServicioAdicional;
  cantidad: number;
}

/**
 * Recargo por personas adicionales, igual que ReservaService.calcularRecargo:
 * (personas − capacidadMaxSinRecargo) × tarifaPersonaAdicional, y cero cuando
 * el motel no tiene política o la cantidad no supera la capacidad sin recargo.
 */
export function recargoPorPersonas(politica: PoliticaReserva | null, personas: number): number {
  if (!politica || personas <= politica.capacidadMaxSinRecargo) {
    return 0;
  }
  return (personas - politica.capacidadMaxSinRecargo) * politica.tarifaPersonaAdicional;
}

/**
 * Total estimado de una reserva: precio de la tarifa + recargo por personas
 * adicionales + servicios. El backend vuelve a calcularlo al crear la reserva;
 * esto es solo para mostrarlo antes de confirmar.
 */
export function totalEstimado(
  precioTarifa: number,
  elegidos: ServicioElegido[],
  personas: number,
  politica: PoliticaReserva | null,
): number {
  const servicios = elegidos.reduce(
    (suma, { servicio, cantidad }) => suma + servicio.precio * Math.max(0, cantidad),
    0,
  );
  return precioTarifa + recargoPorPersonas(politica, personas) + servicios;
}
