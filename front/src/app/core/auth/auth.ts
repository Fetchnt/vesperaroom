import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable, catchError, tap, throwError } from 'rxjs';
import { Api, mensajeDeError } from '../api/api';
import type { Credenciales, Sesion, Usuario } from '../api/models';
import { SessionStore } from './session';
import { environment } from '../../../environments/environment';

/** Login, registro y cierre de sesión contra /auth. */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly api = inject(Api);
  private readonly sesion = inject(SessionStore);

  /** POST /auth/login. Responde 200 con el token, o 401/403 con texto plano. */
  login(credenciales: Credenciales): Observable<Sesion> {
    return this.http.post<Sesion>(environment.apiUrl + '/auth/login', credenciales).pipe(
      tap((sesion) => this.sesion.guardar(sesion)),
      catchError((e) => throwError(() => new Error(mensajeDeError(e)))),
    );
  }

  /** POST /auth/register. Solo admite los roles CLIENTE y PROPIETARIO. */
  registrar(usuario: Usuario): Observable<string> {
    return this.api.crear('/auth/register', usuario);
  }

  salir(): void {
    this.sesion.limpiar();
  }
}
