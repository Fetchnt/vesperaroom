import { Component, inject, input, signal } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../core/auth/auth';
import { desdeUrl } from '../../shared/recursos';

@Component({
  standalone: false,
  selector: 'app-login',
  template: `
    <div class="pantalla-acceso">
      <div class="decorado" aria-hidden="true">
        <span class="luna">☾</span>
        <p>Tu noche empieza aquí.</p>
      </div>

      <div class="formulario-acceso">
        <h1>Iniciar sesión</h1>

        @if (creada()) {
          <p class="aviso ok">Cuenta creada. Ya puedes iniciar sesión.</p>
        }
        @if (expirada()) {
          <p class="aviso info">Tu sesión expiró. Vuelve a ingresar.</p>
        }
        @if (error(); as texto) {
          <p class="aviso mal">{{ texto }}</p>
        }

        <form class="pila" (ngSubmit)="enviar()">
          <label class="campo">
            <span class="oculto">Correo</span>
            <input type="email" name="correo" autocomplete="email" placeholder="Correo" required [(ngModel)]="correo" />
          </label>
          <label class="campo">
            <span class="oculto">Contraseña</span>
            <input
              type="password"
              name="contrasena"
              autocomplete="current-password"
              placeholder="Contraseña"
              required
              [(ngModel)]="contrasena"
            />
          </label>
          <label class="recordar">
            <input type="checkbox" name="recordar" [(ngModel)]="recordar" />
            Recuérdame
          </label>
          <button type="submit" class="primario" [disabled]="enviando()">
            {{ enviando() ? 'Ingresando…' : 'Iniciar sesión' }}
          </button>
        </form>

        <hr />
        <p class="pequeno centrado">¿No tienes una cuenta? <a routerLink="/registro">Crea una aquí</a></p>
      </div>
    </div>
  `,
})
export class Login {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  /** Parámetros que dejan el registro (?creada=1) y los guards (?expirada=1&volverA=...). */
  readonly creadaParam = input('', { alias: 'creada', transform: desdeUrl });
  readonly expiradaParam = input('', { alias: 'expirada', transform: desdeUrl });
  readonly volverA = input('', { transform: desdeUrl });

  protected correo = '';
  protected contrasena = '';
  protected recordar = true;
  protected readonly enviando = signal(false);
  protected readonly error = signal('');
  private readonly descartarAvisos = signal(false);

  protected creada(): boolean {
    return !!this.creadaParam() && !this.descartarAvisos();
  }

  protected expirada(): boolean {
    return !!this.expiradaParam() && !this.descartarAvisos();
  }

  protected enviar(): void {
    if (this.enviando()) {
      return;
    }

    this.enviando.set(true);
    this.error.set('');
    this.descartarAvisos.set(true);

    this.auth.login({ correo: this.correo.trim(), contrasena: this.contrasena }, this.recordar).subscribe({
      next: (sesion) => {
        this.enviando.set(false);
        void this.router.navigateByUrl(this.destino(sesion.rol));
      },
      error: (e: Error) => {
        this.enviando.set(false);
        this.error.set(e.message);
      },
    });
  }

  /** Respeta el ?volverA= que dejan los guards, y si no, lleva al inicio del rol. */
  private destino(rol: string): string {
    if (this.volverA()) {
      return this.volverA();
    }
    if (rol === 'ADMINISTRADOR') {
      return '/admin/usuarios';
    }
    return rol === 'PROPIETARIO' ? '/panel/moteles' : '/moteles';
  }
}
