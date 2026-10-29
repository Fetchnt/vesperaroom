import { inject } from '@angular/core';
import { Router, type CanActivateFn } from '@angular/router';
import type { Rol } from '../api/models';
import { SessionStore } from './session';

/** Exige sesión; si no la hay, manda al login recordando a dónde iba. */
export const authGuard: CanActivateFn = (_ruta, estado) => {
  const sesion = inject(SessionStore);
  const router = inject(Router);

  if (sesion.autenticado()) {
    return true;
  }

  return router.createUrlTree(['/login'], { queryParams: { volverA: estado.url } });
};

/** Exige sesión con uno de los roles indicados. */
export function roleGuard(...permitidos: Rol[]): CanActivateFn {
  return (ruta, estado) => {
    const sesion = inject(SessionStore);
    const router = inject(Router);
    const rol = sesion.rol();

    if (rol === null) {
      return router.createUrlTree(['/login'], { queryParams: { volverA: estado.url } });
    }

    return permitidos.includes(rol) ? true : router.createUrlTree(['/sin-permisos']);
  };
}

/** Para login y registro: quien ya tiene sesión no vuelve a verlos. */
export const guestGuard: CanActivateFn = () => {
  const sesion = inject(SessionStore);
  const router = inject(Router);

  return sesion.autenticado() ? router.createUrlTree(['/']) : true;
};
