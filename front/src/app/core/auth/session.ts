import { Injectable, computed, signal } from '@angular/core';
import type { Rol, Sesion } from '../api/models';

const CLAVE = 'vesperaroom.sesion';

/**
 * «Recuérdame» decide dónde vive la sesión: localStorage sobrevive al cierre
 * del navegador; sessionStorage se borra al cerrar la pestaña.
 */
function almacen(recordar: boolean): Storage {
  return recordar ? localStorage : sessionStorage;
}

function leerSesionGuardada(): Sesion | null {
  try {
    const guardada = localStorage.getItem(CLAVE) ?? sessionStorage.getItem(CLAVE);
    return guardada ? (JSON.parse(guardada) as Sesion) : null;
  } catch {
    // Modo privado o almacenamiento bloqueado: se trabaja sin sesión persistida.
    return null;
  }
}

/** Sesión activa, conservada en el navegador para que sobreviva al refresco. */
@Injectable({ providedIn: 'root' })
export class SessionStore {
  private readonly sesion = signal<Sesion | null>(leerSesionGuardada());

  readonly actual = this.sesion.asReadonly();
  readonly autenticado = computed(() => this.sesion() !== null);
  readonly rol = computed<Rol | null>(() => this.sesion()?.rol ?? null);
  readonly token = computed(() => this.sesion()?.token ?? null);

  guardar(sesion: Sesion, recordar = true): void {
    this.sesion.set(sesion);
    try {
      almacen(!recordar).removeItem(CLAVE);
      almacen(recordar).setItem(CLAVE, JSON.stringify(sesion));
    } catch {
      // Sin persistencia la sesión vive solo en memoria; no es motivo de error.
    }
  }

  limpiar(): void {
    this.sesion.set(null);
    try {
      localStorage.removeItem(CLAVE);
      sessionStorage.removeItem(CLAVE);
    } catch {
      // Ídem.
    }
  }
}
