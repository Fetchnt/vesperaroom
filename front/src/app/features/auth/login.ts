import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth/auth';

@Component({
  selector: 'app-login',
  imports: [FormsModule, RouterLink],
  template: `
    <div class="angosto pila">
      <h1>Ingresar</h1>

      @if (expirada()) {
        <p class="aviso info">Tu sesión expiró. Vuelve a ingresar.</p>
      }
      @if (error(); as texto) {
        <p class="aviso mal">{{ texto }}</p>
      }

      <form class="tarjeta pila" (ngSubmit)="enviar()">
        <label class="campo">
          <span>Correo</span>
          <input type="email" name="correo" autocomplete="email" required [(ngModel)]="correo" />
        </label>
        <label class="campo">
          <span>Contraseña</span>
          <input
            type="password"
            name="contrasena"
            autocomplete="current-password"
            required
            [(ngModel)]="contrasena"
          />
        </label>
        <button type="submit" class="primario" [disabled]="enviando()">
          {{ enviando() ? 'Ingresando…' : 'Ingresar' }}
        </button>
      </form>

      <p class="pequeno suave">¿No tienes cuenta? <a routerLink="/registro">Crear una</a></p>
    </div>
  `,
  styles: `
    .angosto {
      max-width: 420px;
      margin: 32px auto;
    }
  `,
})
export class Login {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  protected correo = '';
  protected contrasena = '';
  protected readonly enviando = signal(false);
  protected readonly error = signal('');

  private readonly parametros = new URLSearchParams(location.search);
  protected readonly expirada = signal(this.parametros.has('expirada'));

  protected enviar(): void {
    if (this.enviando()) {
      return;
    }

    this.enviando.set(true);
    this.error.set('');
    this.expirada.set(false);

    this.auth.login({ correo: this.correo.trim(), contrasena: this.contrasena }).subscribe({
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
    const volverA = this.parametros.get('volverA');
    if (volverA) {
      return volverA;
    }
    if (rol === 'ADMINISTRADOR') {
      return '/admin/usuarios';
    }
    return rol === 'PROPIETARIO' ? '/panel/moteles' : '/';
  }
}
