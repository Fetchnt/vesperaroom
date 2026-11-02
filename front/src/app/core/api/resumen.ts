import { inject, Injectable } from '@angular/core';
import { catchError, forkJoin, map, of, switchMap, type MonoTypeOperatorFunction, type Observable } from 'rxjs';
import { FotoApi, ResenaApi, TarifaApi, TipoHabitacionApi } from './entidades';
import type { Caracteristica, Motel, Tarifa, TipoHabitacion } from './models';

/** Un tipo de habitación con lo necesario para mostrarlo en una ficha. */
export type TipoResumen = TipoHabitacion & {
  tarifas: Tarifa[];
  caracteristicas: Caracteristica[];
};

/** Un motel tal como se muestra en una ficha del catálogo. */
export type MotelResumen = Motel & {
  foto: string | null;
  promedio: number;
  comentarios: number;
  /** Tarifa más barata publicada, o null si el motel aún no tiene tarifas. */
  desde: number | null;
  tipos: TipoResumen[];
};

/** Si una parte falla, la ficha se muestra igual con ese dato vacío. */
const oVacio = <T>(vacio: T): MonoTypeOperatorFunction<T> => catchError(() => of(vacio));

/**
 * Arma la vista de catálogo. La API no expone un resumen por motel, así que se
 * compone aquí con los endpoints que sí existen: fotos, promedio y reseñas, y
 * por cada tipo de habitación sus tarifas y características.
 *
 * shortcut: son 3 + 2·tipos peticiones por motel; con un catálogo grande habría
 * que pedirle al backend un endpoint de resumen.
 */
@Injectable({ providedIn: 'root' })
export class ResumenApi {
  private readonly fotos = inject(FotoApi);
  private readonly resenas = inject(ResenaApi);
  private readonly tipos = inject(TipoHabitacionApi);
  private readonly tarifas = inject(TarifaApi);

  resumir(moteles: Motel[]): Observable<MotelResumen[]> {
    if (moteles.length === 0) {
      return of([]);
    }

    return forkJoin(
      moteles.map((motel) =>
        forkJoin({
          foto: this.fotos.porMotel(motel.idMotel!).pipe(
            map((lista) => lista[0]?.uri ?? null),
            oVacio<string | null>(null),
          ),
          promedio: this.resenas.promedio(motel.idMotel!).pipe(oVacio(0)),
          comentarios: this.resenas.porMotel(motel.idMotel!).pipe(
            map((lista) => lista.length),
            oVacio(0),
          ),
          tipos: this.tiposDe(motel.idMotel!),
        }).pipe(map((extra) => ({ ...motel, ...extra, desde: masBarata(extra.tipos) }))),
      ),
    );
  }

  /** La tarifa más barata entre todos los tipos de habitación del motel. */
  precioDesde(idMotel: number): Observable<number | null> {
    return this.tiposDe(idMotel).pipe(map(masBarata));
  }

  private tiposDe(idMotel: number): Observable<TipoResumen[]> {
    return this.tipos.porMotel(idMotel).pipe(
      switchMap((tipos) =>
        tipos.length === 0
          ? of<TipoResumen[]>([])
          : forkJoin(
              tipos.map((t) =>
                forkJoin({
                  tarifas: this.tarifas.porTipo(t.idTipo!).pipe(oVacio<Tarifa[]>([])),
                  caracteristicas: this.tipos.caracteristicas(t.idTipo!).pipe(oVacio<Caracteristica[]>([])),
                }).pipe(map((extra) => ({ ...t, ...extra }))),
              ),
            ),
      ),
      oVacio<TipoResumen[]>([]),
    );
  }
}

function masBarata(tipos: TipoResumen[]): number | null {
  const precios = tipos.flatMap((t) => t.tarifas.map((tarifa) => tarifa.precio));
  return precios.length === 0 ? null : Math.min(...precios);
}
