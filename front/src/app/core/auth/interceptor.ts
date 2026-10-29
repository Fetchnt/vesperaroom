import { HttpErrorResponse, type HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { SessionStore } from './session';

/**
 * Añade el token JWT a cada petición y, si el backend responde 401, descarta
 * la sesión y manda al login: el token venció o dejó de ser válido.
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const sesion = inject(SessionStore);
  const router = inject(Router);
  const token = sesion.token();

  const autorizada = token
    ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : req;

  return next(autorizada).pipe(
    catchError((e: HttpErrorResponse) => {
      if (e.status === 401 && sesion.autenticado()) {
        sesion.limpiar();
        void router.navigate(['/login'], { queryParams: { expirada: 1 } });
      }
      return throwError(() => e);
    }),
  );
};
