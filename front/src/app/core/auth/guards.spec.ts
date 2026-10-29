import { TestBed } from '@angular/core/testing';
import { provideRouter, Router, type ActivatedRouteSnapshot, type RouterStateSnapshot } from '@angular/router';
import { authGuard, guestGuard, roleGuard } from './guards';
import { SessionStore } from './session';
import type { Rol } from '../api/models';

const RUTA = {} as ActivatedRouteSnapshot;
const ESTADO = { url: '/panel/moteles' } as RouterStateSnapshot;

function conSesion(rol: Rol | null): SessionStore {
  const sesion = TestBed.inject(SessionStore);
  if (rol === null) {
    sesion.limpiar();
  } else {
    sesion.guardar({ token: 't', idUsuario: 1, nombre: 'Ana', correo: 'ana@x.com', rol });
  }
  return sesion;
}

describe('guards', () => {
  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({ providers: [provideRouter([])] });
  });

  it('authGuard deja pasar con sesión', () => {
    conSesion('CLIENTE');
    expect(TestBed.runInInjectionContext(() => authGuard(RUTA, ESTADO))).toBe(true);
  });

  it('authGuard manda al login y recuerda el destino', () => {
    conSesion(null);
    const resultado = TestBed.runInInjectionContext(() => authGuard(RUTA, ESTADO));
    const url = TestBed.inject(Router).serializeUrl(resultado as never);

    expect(url).toContain('/login');
    expect(url).toContain('volverA');
  });

  it('roleGuard deja pasar al rol permitido', () => {
    conSesion('PROPIETARIO');
    expect(TestBed.runInInjectionContext(() => roleGuard('PROPIETARIO')(RUTA, ESTADO))).toBe(true);
  });

  it('roleGuard bloquea a otro rol', () => {
    conSesion('CLIENTE');
    const resultado = TestBed.runInInjectionContext(() => roleGuard('PROPIETARIO', 'ADMINISTRADOR')(RUTA, ESTADO));
    const url = TestBed.inject(Router).serializeUrl(resultado as never);

    expect(url).toContain('/sin-permisos');
  });

  it('roleGuard sin sesión manda al login, no a sin-permisos', () => {
    conSesion(null);
    const resultado = TestBed.runInInjectionContext(() => roleGuard('CLIENTE')(RUTA, ESTADO));
    const url = TestBed.inject(Router).serializeUrl(resultado as never);

    expect(url).toContain('/login');
  });

  it('guestGuard saca del login a quien ya tiene sesión', () => {
    conSesion('CLIENTE');
    const resultado = TestBed.runInInjectionContext(() => guestGuard(RUTA, ESTADO));
    expect(TestBed.inject(Router).serializeUrl(resultado as never)).toBe('/');
  });
});
