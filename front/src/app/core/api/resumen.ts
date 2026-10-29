import { inject, Injectable } from '@angular/core';
import { forkJoin, map, of, switchMap, type Observable } from 'rxjs';
import { FotoApi, ResenaApi, ServicioApi, TarifaApi, TipoHabitacionApi } from './entidades';
import type { Motel, ServicioAdicional } from './models';

/** Un motel tal como se muestra en una tarjeta del catálogo. */
export type MotelResumen = Motel & {
  foto: string | null;
  promedio: number;
  /** Tarifa más barata publicada, o null si el motel aún no tiene tarifas. */
  desde: number | null;
  servicios: ServicioAdicional[];
};

/**
 * Arma la vista de catálogo. La API no expone «precio desde» ni «foto
 * principal» de un motel, así que se componen aquí a partir de los endpoints
 * que sí existen: fotos del motel, promedio de reseñas, servicios, y las
 * tarifas de cada uno de sus tipos de habitación.
 *
 * shortcut: son cuatro peticiones por motel; con un catálogo grande habría que
 * pedirle al backend un endpoint de resumen.
 */
@Injectable({ providedIn: 'root' })
export class ResumenApi {
  private readonly fotos = inject(FotoApi);
  private readonly resenas = inject(ResenaApi);
  private readonly servicios = inject(ServicioApi);
  private readonly tipos = inject(TipoHabitacionApi);
  private readonly tarifas = inject(TarifaApi);

  resumir(moteles: Motel[]): Observable<MotelResumen[]> {
    if (moteles.length === 0) {
      return of([]);
    }

    return forkJoin(
      moteles.map((motel) =>
        forkJoin({
          foto: this.fotos.porMotel(motel.idMotel!).pipe(map((lista) => lista[0]?.uri ?? null)),
          promedio: this.resenas.promedio(motel.idMotel!),
          servicios: this.servicios.porMotel(motel.idMotel!),
          desde: this.precioDesde(motel.idMotel!),
        }).pipe(map((extra) => ({ ...motel, ...extra }))),
      ),
    );
  }

  /** La tarifa más barata entre todos los tipos de habitación del motel. */
  precioDesde(idMotel: number): Observable<number | null> {
    return this.tipos.porMotel(idMotel).pipe(
      switchMap((tipos) =>
        tipos.length === 0
          ? of<number | null>(null)
          : forkJoin(tipos.map((t) => this.tarifas.porTipo(t.idTipo!))).pipe(
              map((listas) => {
                const precios = listas.flat().map((t) => t.precio);
                return precios.length === 0 ? null : Math.min(...precios);
              }),
            ),
      ),
    );
  }
}
