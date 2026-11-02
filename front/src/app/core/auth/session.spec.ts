import { TestBed } from '@angular/core/testing';
import type { Sesion } from '../api/models';
import { SessionStore } from './session';

const ANA: Sesion = { token: 't', idUsuario: 1, nombre: 'Ana', correo: 'ana@x.com', rol: 'CLIENTE' };
const CLAVE = 'vesperaroom.sesion';

describe('SessionStore y «Recuérdame»', () => {
  beforeEach(() => {
    localStorage.clear();
    sessionStorage.clear();
  });

  it('con «Recuérdame» la sesión queda en localStorage', () => {
    TestBed.inject(SessionStore).guardar(ANA, true);
    expect(localStorage.getItem(CLAVE)).not.toBeNull();
    expect(sessionStorage.getItem(CLAVE)).toBeNull();
  });

  it('sin «Recuérdame» queda solo en sessionStorage, que se borra al cerrar el navegador', () => {
    TestBed.inject(SessionStore).guardar(ANA, false);
    expect(sessionStorage.getItem(CLAVE)).not.toBeNull();
    expect(localStorage.getItem(CLAVE)).toBeNull();
  });

  it('no deja una copia vieja en el otro almacén al cambiar de opinión', () => {
    const sesion = TestBed.inject(SessionStore);
    sesion.guardar(ANA, true);
    sesion.guardar(ANA, false);
    expect(localStorage.getItem(CLAVE)).toBeNull();
  });

  it('cerrar sesión limpia los dos almacenes', () => {
    const sesion = TestBed.inject(SessionStore);
    sesion.guardar(ANA, false);
    sesion.limpiar();
    expect(sessionStorage.getItem(CLAVE)).toBeNull();
    expect(sesion.autenticado()).toBe(false);
  });
});
