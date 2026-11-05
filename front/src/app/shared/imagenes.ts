/** Fotos de ambiente servidas desde public/imagenes (créditos en public/imagenes/CREDITOS.md). */
export const IMAGENES = {
  portada: '/imagenes/portada.webp',
  llaves: '/imagenes/llaves.webp',
  acceso: '/imagenes/acceso.webp',
  registro: '/imagenes/registro.webp',
  recepcion: '/imagenes/recepcion.webp',
  ciudad: '/imagenes/ciudad.webp',
  mostrador: '/imagenes/mostrador.webp',
  habitacion: '/imagenes/habitacion.webp',
  puerta: '/imagenes/puerta.webp',
  letrero: '/imagenes/letrero.webp',
  sinFoto: '/imagenes/sin-foto.webp',
} as const;

/**
 * Para el (error) de una foto de motel: la cambia por la genérica. La guarda
 * evita un ciclo si la genérica también fallara.
 */
export function usarSinFoto(evento: Event): void {
  const img = evento.target as HTMLImageElement;
  if (!img.src.endsWith(IMAGENES.sinFoto)) {
    img.src = IMAGENES.sinFoto;
  }
}
