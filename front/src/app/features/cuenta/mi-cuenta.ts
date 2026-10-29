import { Component, computed, inject, linkedSignal, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { rxResource } from '@angular/core/rxjs-interop';
import { UsuarioApi } from '../../core/api/entidades';
import type { Usuario } from '../../core/api/models';
import { SessionStore } from '../../core/auth/session';
import { legible } from '../../shared/etiquetas';
import { EstadoBadge } from '../../shared/estado-badge';
import { valorDe } from '../../shared/recursos';

@Component({
  selector: 'app-mi-cuenta',
  imports: [FormsModule, EstadoBadge],
  template: `
    <div class="angosto pila">
      <div class="encabezado-seccion">
        <h1>Mi cuenta</h1>
        <span class="pequeno suave">{{ rolLegible() }}</span>
      </div>

      @if (cuenta.isLoading()) {
        <p class="suave">Cargando…</p>
      } @else if (cuenta.error(); as e) {
        <p class="aviso mal">{{ e.message }}</p>
      } @else if (borrador(); as datos) {
        @if (error(); as texto) {
          <p class="aviso mal">{{ texto }}</p>
        }
        @if (mensaje(); as texto) {
          <p class="aviso ok">{{ texto }}</p>
        }

        <form class="tarjeta pila" (ngSubmit)="guardar(datos)">
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
              <input type="email" name="correo" required [(ngModel)]="datos.correo" />
            </label>
            <label class="campo">
              <span>Teléfono</span>
              <input name="telefono" required [(ngModel)]="datos.telefono" />
            </label>
            <label class="campo">
              <span>Contraseña nueva</span>
              <input
                type="password"
                name="contrasena"
                autocomplete="new-password"
                placeholder="Dejar vacío para no cambiarla"
                [(ngModel)]="datos.contrasena"
              />
            </label>
          </div>

          <div class="fila entre">
            <span class="pequeno suave">
              Estado: <app-estado [valor]="datos.estado" />
            </span>
            <button type="submit" class="primario" [disabled]="guardando()">
              {{ guardando() ? 'Guardando…' : 'Guardar cambios' }}
            </button>
          </div>
        </form>
      }
    </div>
  `,
  styles: `
    .angosto {
      max-width: 620px;
      margin: 0 auto;
    }
  `,
})
export class MiCuenta {
  protected readonly valorDe = valorDe;
  private readonly usuarios = inject(UsuarioApi);
  private readonly sesion = inject(SessionStore);

  protected readonly cuenta = rxResource({
    stream: () => this.usuarios.miCuenta(),
  });

  /** Copia editable: el formulario no debe escribir sobre el valor del recurso. */
  protected readonly borrador = linkedSignal<Usuario | null>(() => {
    const cuenta = valorDe(this.cuenta, null);
    return cuenta ? { ...cuenta } : null;
  });

  protected readonly guardando = signal(false);
  protected readonly error = signal('');
  protected readonly mensaje = signal('');

  protected readonly rolLegible = computed(() => legible(this.sesion.rol()));

  protected guardar(datos: Usuario): void {
    if (this.guardando()) {
      return;
    }

    this.guardando.set(true);
    this.error.set('');
    this.mensaje.set('');

    // Una contraseña vacía significa «no la cambies».
    const envio: Usuario = { ...datos };
    if (!envio.contrasena) {
      delete envio.contrasena;
    }

    this.usuarios.actualizarMiCuenta(envio).subscribe({
      next: (respuesta) => {
        this.guardando.set(false);
        this.mensaje.set(respuesta);
        this.cuenta.reload();
      },
      error: (e: Error) => {
        this.guardando.set(false);
        this.error.set(e.message);
      },
    });
  }
}
