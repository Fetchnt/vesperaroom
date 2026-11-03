import { HttpClient, HttpErrorResponse, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable, catchError, map, of, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';

/** Valores admitidos como parámetros de query por el backend. */
export type Params = Record<string, string | number | boolean>;

/**
 * Traduce un fallo HTTP al texto que se muestra al usuario.
 *
 * El backend responde los errores en text/plain, no en JSON. Cuando la petición
 * esperaba JSON, Angular no puede parsear ese texto y lo deja en `error.text`.
 */
export function mensajeDeError(e: HttpErrorResponse): string {
  if (e.status === 0) {
    return 'No pudimos conectarnos. Revisa tu conexión e intenta de nuevo en unos minutos.';
  }

  const cuerpo: unknown = e.error;
  if (typeof cuerpo === 'string' && cuerpo.trim()) {
    return cuerpo;
  }
  if (cuerpo && typeof cuerpo === 'object' && 'text' in cuerpo) {
    const texto = (cuerpo as { text?: unknown }).text;
    if (typeof texto === 'string' && texto.trim()) {
      return texto;
    }
  }

  return `Error ${e.status} al contactar el servidor`;
}

function fallar(e: HttpErrorResponse): Observable<never> {
  return throwError(() => new Error(mensajeDeError(e)));
}

/**
 * Cliente HTTP de la API, con las convenciones del backend ya resueltas:
 * las listas y conteos vacíos llegan como 204 sin cuerpo, `getbyid` responde
 * 404 cuando no encuentra, `exists` responde 404 para decir «false» y las
 * mutaciones devuelven un mensaje en texto plano.
 */
@Injectable({ providedIn: 'root' })
export class Api {
  private readonly http = inject(HttpClient);

  private url(ruta: string): string {
    return environment.apiUrl + ruta;
  }

  /** GET de una colección. Un 204 (colección vacía) se normaliza a []. */
  lista<T>(ruta: string, params?: Params): Observable<T[]> {
    return this.http.get<T[]>(this.url(ruta), { params: new HttpParams({ fromObject: params ?? {} }) }).pipe(
      map((datos) => datos ?? []),
      catchError(fallar),
    );
  }

  /** GET de un recurso. Un 404 no es un error: significa que no existe. */
  uno<T>(ruta: string, params?: Params): Observable<T | null> {
    return this.http.get<T>(this.url(ruta), { params: new HttpParams({ fromObject: params ?? {} }) }).pipe(
      catchError((e: HttpErrorResponse) => (e.status === 404 ? of(null) : fallar(e))),
    );
  }

  /** GET de un valor escalar JSON, como el promedio de calificaciones. */
  valor<T>(ruta: string, vacio: T): Observable<T> {
    return this.http
      .get<T>(this.url(ruta))
      .pipe(map((dato) => dato ?? vacio), catchError(fallar));
  }

  /** GET /count: el backend responde 204 cuando el total es cero. */
  conteo(ruta: string): Observable<number> {
    return this.http.get<number>(this.url(ruta)).pipe(
      map((total) => total ?? 0),
      catchError(fallar),
    );
  }

  /** GET /exists: 202 con true, o 404 para decir false. */
  existe(ruta: string): Observable<boolean> {
    return this.http.get<boolean>(this.url(ruta)).pipe(
      map((hay) => hay === true),
      catchError((e: HttpErrorResponse) => (e.status === 404 ? of(false) : fallar(e))),
    );
  }

  /** POST con cuerpo JSON. Devuelve el mensaje de confirmación del backend. */
  crear(ruta: string, cuerpo: unknown, params?: Params): Observable<string> {
    return this.http
      .post(this.url(ruta), cuerpo, {
        responseType: 'text',
        params: new HttpParams({ fromObject: params ?? {} }),
      })
      .pipe(catchError(fallar));
  }

  /** POST que además devuelve el id del registro nuevo, tomado de la cabecera Location. */
  crearConId(ruta: string, cuerpo: unknown): Observable<{ mensaje: string; id: Id | null }> {
    return this.http.post(this.url(ruta), cuerpo, { responseType: 'text', observe: 'response' }).pipe(
      map((r) => ({ mensaje: r.body ?? '', id: r.headers.get('Location')?.split('/').pop() || null })),
      catchError(fallar),
    );
  }

  /** POST multipart con un archivo en el campo `archivo`, como la imagen de una foto. */
  subir(ruta: string, archivo: File, params?: Params): Observable<string> {
    const datos = new FormData();
    datos.append('archivo', archivo);
    return this.crear(ruta, datos, params);
  }

  /** PUT con cuerpo JSON opcional. */
  actualizar(ruta: string, cuerpo: unknown, params?: Params): Observable<string> {
    return this.http
      .put(this.url(ruta), cuerpo, {
        responseType: 'text',
        params: new HttpParams({ fromObject: params ?? {} }),
      })
      .pipe(catchError(fallar));
  }

  /** DELETE. */
  eliminar(ruta: string, params?: Params): Observable<string> {
    return this.http
      .delete(this.url(ruta), {
        responseType: 'text',
        params: new HttpParams({ fromObject: params ?? {} }),
      })
      .pipe(catchError(fallar));
  }
}

/** Identificador de un registro: casi todos son numéricos. */
export type Id = number | string;

/**
 * Los siete endpoints que todos los controllers del backend repiten
 * (`/createjson`, `/getall`, `/getbyid/{id}`, `/updatejson?id=`,
 * `/deletebyid/{id}`, `/count`, `/exists/{id}`).
 *
 * Debe llamarse dentro de un contexto de inyección, por ejemplo en la
 * inicialización de un campo de componente o servicio.
 */
export function crud<T>(entidad: string) {
  const api = inject(Api);
  const base = `/${entidad}`;

  return {
    getAll: () => api.lista<T>(`${base}/getall`),
    getById: (id: Id) => api.uno<T>(`${base}/getbyid/${id}`),
    crear: (dto: T) => api.crear(`${base}/createjson`, dto),
    actualizar: (id: Id, dto: T) => api.actualizar(`${base}/updatejson`, dto, { id }),
    eliminar: (id: Id) => api.eliminar(`${base}/deletebyid/${id}`),
    contar: () => api.conteo(`${base}/count`),
    existe: (id: Id) => api.existe(`${base}/exists/${id}`),
  };
}
