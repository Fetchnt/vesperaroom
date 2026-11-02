import { Component, inject, input, signal, type OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../core/auth/auth';
import type { Rol, Usuario } from '../../core/api/models';
import { desdeUrl } from '../../shared/recursos';

@Component({
  standalone: false,
  selector: 'app-registro',
  template: `
    <div class="pantalla-acceso">
      <div class="decorado" aria-hidden="true">
        <span class="luna">☾</span>
        <p>{{ datos.rol === 'PROPIETARIO' ? 'Publica tu motel y recibe reservas.' : 'Reserva en un paso, paga al llegar.' }}</p>
      </div>

      <div class="formulario-acceso">
      <h1>Crear cuenta</h1>

      @if (error(); as texto) {
        <p class="aviso mal">{{ texto }}</p>
      }

      <form class="pila" (ngSubmit)="enviar()">
        <div class="formulario">
          <label class="campo">
            <span>Nombre</span>
            <input name="nombre" required [(ngModel)]="datos.nombre" />
          </label>
          <label class="campo">
            <span>Apellido</span>
            <input name="apellido" required [(ngModel)]="datos.apellido" />
          </label>
          <label class="campo">
            <span>Correo</span>
            <input type="email" name="correo" autocomplete="email" required [(ngModel)]="datos.correo" />
          </label>
          <label class="campo">
            <span>Teléfono</span>
            <input name="telefono" inputmode="tel" required [(ngModel)]="datos.telefono" />
          </label>
          <label class="campo">
            <span>Contraseña</span>
            <input
              type="password"
              name="contrasena"
              autocomplete="new-password"
              required
              [(ngModel)]="datos.contrasena"
            />
          </label>
          <label class="campo">
            <span>Tipo de cuenta</span>
            <select name="rol" [(ngModel)]="datos.rol">
              <option value="CLIENTE">Cliente</option>
              <option value="PROPIETARIO">Propietario</option>
            </select>
          </label>
        </div>

        @if (datos.rol === 'PROPIETARIO') {
          <p class="pequeno suave">
            Después de ingresar completa tu perfil (NIT, razón social y cuenta bancaria) para publicar
            tus moteles.
          </p>
        }

        <button type="submit" class="primario" [disabled]="enviando()">
          {{ enviando() ? 'Creando…' : 'Crear cuenta' }}
        </button>
      </form>

      <hr />
      <p class="pequeno centrado">¿Ya tienes cuenta? <a routerLink="/login">Inicia sesión</a></p>
      </div>
    </div>
  `,
})
export class Registro implements OnInit {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  /** ?rol=PROPIETARIO desde «¿Eres dueño de un motel?». */
  readonly rol = input('', { transform: desdeUrl });

  ngOnInit(): void {
    if (this.rol() === 'PROPIETARIO') {
      this.datos.rol = 'PROPIETARIO';
    }
  }

  protected datos: Usuario & { rol: Rol } = {
    nombre: '',
    apellido: '',
    correo: '',
    telefono: '',
    contrasena: '',
    rol: 'CLIENTE',
  };

  protected readonly enviando = signal(false);
  protected readonly error = signal('');

  protected enviar(): void {
    if (this.enviando()) {
      return;
    }

    this.enviando.set(true);
    this.error.set('');

    this.auth.registrar({ ...this.datos, correo: this.datos.correo.trim() }).subscribe({
      next: () => {
        this.enviando.set(false);
        void this.router.navigate(['/login'], { queryParams: { creada: 1 } });
      },
      error: (e: Error) => {
        this.enviando.set(false);
        this.error.set(e.message);
      },
    });
  }
}
