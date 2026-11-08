import type { PoliticaReserva, ServicioAdicional, Tarifa } from '../api/models';

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

/**
 * Tarifa que aplica a una duración (modalidad) en la fecha de entrada: vigente ese
 * día y, entre las vigentes, la del tipo de día (sábado y domingo son fin de semana,
 * como en el backend) antes que la de «todos los días». Los festivos no se detectan.
 * Sin fecha se usa hoy, para mostrar un precio de referencia.
 */
export function tarifaDelDia(tarifas: Tarifa[], idModalidad: number | null, entrada: string): Tarifa | null {
  if (idModalidad === null) {
    return null;
  }
  const fecha = /^\d{4}-\d{2}-\d{2}/.test(entrada) ? entrada.slice(0, 10) : hoy();
  const [a, m, d] = fecha.split('-').map(Number);
  const dia = new Date(a, m - 1, d).getDay();
  const tipo = dia === 0 || dia === 6 ? 'FIN_DE_SEMANA' : 'ENTRE_SEMANA';
  const vigentes = tarifas.filter(
    (t) =>
      t.idModalidad === idModalidad &&
      t.vigenteDesde.slice(0, 10) <= fecha &&
      (!t.vigenteHasta || fecha <= t.vigenteHasta.slice(0, 10)),
  );
  return vigentes.find((t) => t.tipoDia === tipo) ?? vigentes.find((t) => t.tipoDia === 'TODOS') ?? null;
}

function hoy(): string {
  const f = new Date();
  return `${f.getFullYear()}-${String(f.getMonth() + 1).padStart(2, '0')}-${String(f.getDate()).padStart(2, '0')}`;
}
