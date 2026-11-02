/**
 * `recurso.value()` lanza cuando el recurso quedó en estado de error, incluso
 * si se declaró con `defaultValue`, y eso aborta la detección de cambios y deja
 * la vista congelada en «Cargando». Estos accesos devuelven el valor vacío en
 * ese caso, para que la plantilla pueda mostrar el error con calma.
 */

interface Leible<T> {
  hasValue(): boolean;
  value(): T;
}

const VACIA: readonly never[] = Object.freeze([]);

/** Lista del recurso, o una lista vacía si todavía no hay valor. */
export function lista<T>(recurso: Leible<T[]>): T[] {
  return recurso.hasValue() ? recurso.value() : (VACIA as unknown as T[]);
}

/** Valor del recurso, o el vacío indicado si todavía no hay valor. */
export function valorDe<T>(recurso: Leible<T>, vacio: T): T {
  return recurso.hasValue() ? recurso.value() : vacio;
}

/**
 * Para inputs que llegan de la URL: si el parámetro no viene, el router entrega
 * `undefined` aunque el input tenga valor por defecto. Uso: `input('', { transform: desdeUrl })`.
 */
export function desdeUrl(valor: string | undefined): string {
  return valor ?? '';
}
