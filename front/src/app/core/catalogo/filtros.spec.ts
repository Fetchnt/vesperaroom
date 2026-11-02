import type { MotelResumen } from '../api/resumen';
import { distanciaKm, filtrar, normalizar, type Criterios } from './filtros';

const SIN_FILTROS: Criterios = {
  texto: '',
  precioMin: null,
  precioMax: null,
  horas: [],
  caracteristicas: [],
  destacados: false,
  nuevos: false,
  orden: null,
  posicion: null,
};

/** Los tres moteles de database/02_insercion_registros.sql, ya resumidos. */
function motel(id: number, extra: Partial<MotelResumen>): MotelResumen {
  return {
    idMotel: id,
    idCiudad: 1,
    nombre: `Motel ${id}`,
    calle: 'Calle 1',
    carrera: 'Carrera 1',
    foto: null,
    promedio: 0,
    comentarios: 0,
    desde: null,
    tipos: [],
    ...extra,
  };
}

const luna = motel(1, {
  nombre: 'Motel Luna',
  promedio: 4.5,
  comentarios: 2,
  desde: 60000,
  latitud: 6.2088,
  longitud: -75.5677,
  tipos: [
    {
      idTipo: 2,
      idMotel: 1,
      nombre: 'Suite Jacuzzi',
      capacidadPersonas: 2,
      tarifas: [{ idTipo: 2, idModalidad: 3, precio: 220000, tipoDia: 'TODOS', vigenteDesde: '2026-01-01' }],
      caracteristicas: [{ idCaracteristica: 1, nombre: 'Jacuzzi' }],
    },
  ],
});
const eclipse = motel(2, { nombre: 'Motel Eclipse', idCiudad: 2, desde: 55000, latitud: 6.1695, longitud: -75.5909 });
const sol = motel(3, { nombre: 'Motel Sol', idCiudad: 3, promedio: 4.8, comentarios: 0, desde: 70000, latitud: 4.6874, longitud: -74.0817 });

const DURACIONES = new Map([[1, 3], [2, 6], [3, 12]]);
const CIUDADES: Record<number, string> = { 1: 'Medellín', 2: 'Envigado', 3: 'Bogotá' };
const ciudad = (id: number) => CIUDADES[id] ?? '';
const nombres = (lista: MotelResumen[]) => lista.map((m) => m.nombre);

describe('filtrar', () => {
  it('busca por ciudad sin importar tildes ni mayúsculas', () => {
    expect(nombres(filtrar([luna, eclipse, sol], { ...SIN_FILTROS, texto: 'bogota' }, DURACIONES, ciudad))).toEqual(['Motel Sol']);
  });

  it('filtra por rango de precio usando el «desde»', () => {
    const r = filtrar([luna, eclipse, sol], { ...SIN_FILTROS, precioMax: 60000 }, DURACIONES, ciudad);
    expect(nombres(r)).toEqual(['Motel Luna', 'Motel Eclipse']);
  });

  it('un motel sin tarifas no aparece si se filtra por precio', () => {
    const sinTarifas = motel(9, { desde: null });
    expect(filtrar([sinTarifas], { ...SIN_FILTROS, precioMin: 1 }, DURACIONES, ciudad)).toEqual([]);
  });

  it('filtra por duración: solo Luna ofrece «Noche» (12 h)', () => {
    expect(nombres(filtrar([luna, eclipse, sol], { ...SIN_FILTROS, horas: [12] }, DURACIONES, ciudad))).toEqual(['Motel Luna']);
  });

  it('exige todas las características elegidas', () => {
    expect(nombres(filtrar([luna, eclipse], { ...SIN_FILTROS, caracteristicas: [1] }, DURACIONES, ciudad))).toEqual(['Motel Luna']);
    expect(filtrar([luna], { ...SIN_FILTROS, caracteristicas: [1, 4] }, DURACIONES, ciudad)).toEqual([]);
  });

  it('destacado necesita buena nota y al menos una reseña', () => {
    // Sol tiene 4.8 pero ninguna reseña que lo respalde.
    expect(nombres(filtrar([luna, eclipse, sol], { ...SIN_FILTROS, destacados: true }, DURACIONES, ciudad))).toEqual(['Motel Luna']);
  });

  it('nuevos son los de id más alto', () => {
    const muchos = Array.from({ length: 7 }, (_, i) => motel(i + 1, {}));
    const r = filtrar(muchos, { ...SIN_FILTROS, nuevos: true }, DURACIONES, ciudad);
    expect(r.map((m) => m.idMotel)).toEqual([3, 4, 5, 6, 7]);
  });

  it('ordena por más comentarios', () => {
    expect(nombres(filtrar([eclipse, luna], { ...SIN_FILTROS, orden: 'comentarios' }, DURACIONES, ciudad))).toEqual(['Motel Luna', 'Motel Eclipse']);
  });

  it('ordena por cercanía a la posición del usuario', () => {
    const enBogota = { lat: 4.65, lon: -74.1 };
    const r = filtrar([luna, eclipse, sol], { ...SIN_FILTROS, orden: 'cercanos', posicion: enBogota }, DURACIONES, ciudad);
    expect(r[0].nombre).toBe('Motel Sol');
  });
});

describe('ayudas', () => {
  it('normaliza tildes y mayúsculas', () => {
    expect(normalizar('  Medellín ')).toBe('medellin');
  });

  it('calcula la distancia Medellín–Bogotá en unos 240 km', () => {
    const d = distanciaKm({ lat: 6.2442, lon: -75.5812 }, { lat: 4.711, lon: -74.0721 });
    expect(d).toBeGreaterThan(230);
    expect(d).toBeLessThan(250);
  });
});
