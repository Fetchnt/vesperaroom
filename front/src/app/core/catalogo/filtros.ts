import type { MotelResumen } from '../api/resumen';

export type Orden = 'valorados' | 'comentarios' | 'cercanos' | null;

export interface Posicion {
  lat: number;
  lon: number;
}

export interface Criterios {
  texto: string;
  precioMin: number | null;
  precioMax: number | null;
  /** Duraciones en horas de las modalidades elegidas (3, 6, 12…). */
  horas: number[];
  caracteristicas: number[];
  destacados: boolean;
  nuevos: boolean;
  orden: Orden;
  posicion: Posicion | null;
}

/**
 * Destacado: buena nota con al menos una reseña que la respalde.
 * Nuevos: los últimos moteles dados de alta. El id es autoincremental, así que
 * el mayor es el más reciente; el modelo no guarda fecha de alta del motel.
 */
export const NOTA_DESTACADO = 4.5;
export const CANTIDAD_NUEVOS = 5;

export function esDestacado(m: MotelResumen): boolean {
  return m.promedio >= NOTA_DESTACADO && m.comentarios > 0;
}

/** Minúsculas y sin tildes: «Bogotá» y «bogota» coinciden. */
export function normalizar(texto: string): string {
  return texto.normalize('NFD').replace(/\p{Diacritic}/gu, '').toLowerCase().trim();
}

/** Distancia en km entre dos puntos (fórmula del haversine). */
export function distanciaKm(a: Posicion, b: Posicion): number {
  const rad = (g: number) => (g * Math.PI) / 180;
  const dLat = rad(b.lat - a.lat);
  const dLon = rad(b.lon - a.lon);
  const h = Math.sin(dLat / 2) ** 2 + Math.cos(rad(a.lat)) * Math.cos(rad(b.lat)) * Math.sin(dLon / 2) ** 2;
  return 6371 * 2 * Math.asin(Math.sqrt(h));
}

/**
 * Aplica los filtros del listado y el orden elegido.
 * `duraciones` traduce idModalidad → horas; `ciudad` da el nombre de la ciudad de un motel.
 */
export function filtrar(
  moteles: MotelResumen[],
  c: Criterios,
  duraciones: Map<number, number>,
  ciudad: (idCiudad: number) => string,
): MotelResumen[] {
  const texto = normalizar(c.texto);
  const nuevos = new Set(
    [...moteles]
      .sort((a, b) => (b.idMotel ?? 0) - (a.idMotel ?? 0))
      .slice(0, CANTIDAD_NUEVOS)
      .map((m) => m.idMotel),
  );

  const filtrados = moteles.filter((m) => {
    if (texto && !normalizar(`${m.nombre} ${ciudad(m.idCiudad)} ${m.calle} ${m.carrera}`).includes(texto)) {
      return false;
    }
    // Sin tarifas no hay precio que comparar: solo aparece si no se filtra por precio.
    if (c.precioMin || c.precioMax) {
      if (m.desde === null) return false;
      if (c.precioMin && m.desde < c.precioMin) return false;
      if (c.precioMax && m.desde > c.precioMax) return false;
    }
    if (c.horas.length > 0) {
      const ofrece = m.tipos.some((t) => t.tarifas.some((tf) => c.horas.includes(duraciones.get(tf.idModalidad) ?? -1)));
      if (!ofrece) return false;
    }
    if (c.caracteristicas.length > 0) {
      const tiene = new Set(m.tipos.flatMap((t) => t.caracteristicas.map((x) => x.idCaracteristica)));
      if (!c.caracteristicas.every((id) => tiene.has(id))) return false;
    }
    if (c.destacados && !esDestacado(m)) return false;
    if (c.nuevos && !nuevos.has(m.idMotel)) return false;
    return true;
  });

  switch (c.orden) {
    case 'valorados':
      return [...filtrados].sort((a, b) => b.promedio - a.promedio);
    case 'comentarios':
      return [...filtrados].sort((a, b) => b.comentarios - a.comentarios);
    case 'cercanos': {
      const p = c.posicion;
      if (!p) return filtrados;
      const lejos = (m: MotelResumen) =>
        m.latitud != null && m.longitud != null ? distanciaKm(p, { lat: m.latitud, lon: m.longitud }) : Infinity;
      return [...filtrados].sort((a, b) => lejos(a) - lejos(b));
    }
    default:
      return filtrados;
  }
}
