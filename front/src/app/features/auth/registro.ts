import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth/auth';
import type { Rol, Usuario } from '../../core/api/models';

@Component({
  selector: 'app-registro',
  imports: [FormsModule, RouterLink],
  template: `
    <div class="angosto pila">
      <h1>Crear cuenta</h1>

      @if (error(); as texto) {
        <p class="aviso mal">{{ texto }}</p>
      }

      <form class="tarjeta pila" (ngSubmit)="enviar()">
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

        <p class="pequeno suave">
          Como propietario, después de ingresar deberás completar tu perfil (NIT, razón social y
          cuenta bancaria) antes de publicar moteles.
        </p>

        <button type="submit" class="primario" [disabled]="enviando()">
          {{ enviando() ? 'Creando…' : 'Crear cuenta' }}
        </button>
      </form>

      <p class="pequeno suave">¿Ya tienes cuenta? <a routerLink="/login">Ingresar</a></p>
    </div>
  `,
  styles: `
    .angosto {
      max-width: 560px;
      margin: 32px auto;
    }
  `,
})
export class Registro {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

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
