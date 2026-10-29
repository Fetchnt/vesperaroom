import { DatePipe } from '@angular/common';
import { Component, inject, linkedSignal, signal } from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { PerfilPropietarioApi } from '../../core/api/entidades';
import type { PerfilPropietario } from '../../core/api/models';
import { valorDe } from '../../shared/recursos';

const PERFIL_VACIO: PerfilPropietario = {
  nit: '',
  razonSocial: '',
  banco: '',
  tipoCuenta: 'AHORROS',
  numeroCuenta: '',
};

@Component({
  selector: 'app-mi-perfil',
  imports: [DatePipe, FormsModule],
  template: `
    <div class="angosto pila">
      <h1>Mi perfil de propietario</h1>

      @if (perfil.isLoading()) {
        <p class="suave">Cargando…</p>
      } @else if (perfil.error(); as e) {
        <p class="aviso mal">{{ e.message }}</p>
      } @else {
        @if (!existe()) {
          <p class="aviso info">
            Aún no tienes perfil de propietario. Complétalo para poder publicar moteles.
          </p>
        } @else if (datos().fechaVerificacion) {
          <p class="aviso ok">
            Perfil verificado el {{ datos().fechaVerificacion | date: 'mediumDate' }}.
          </p>
        } @else {
          <p class="aviso info">Perfil pendiente de verificación por un administrador.</p>
        }

        @if (error(); as texto) {
          <p class="aviso mal">{{ texto }}</p>
        }
        @if (mensaje(); as texto) {
          <p class="aviso ok">{{ texto }}</p>
        }

        <form class="tarjeta pila" (ngSubmit)="guardar()">
          <div class="formulario">
            <label class="campo">
              <span>NIT</span>
              <input name="nit" required [ngModel]="datos().nit" (ngModelChange)="poner('nit', $event)" />
            </label>
            <label class="campo">
              <span>Razón social</span>
              <input
                name="razonSocial"
                required
                [ngModel]="datos().razonSocial"
                (ngModelChange)="poner('razonSocial', $event)"
              />
            </label>
            <label class="campo">
              <span>Banco</span>
              <input name="banco" required [ngModel]="datos().banco" (ngModelChange)="poner('banco', $event)" />
            </label>
            <label class="campo">
              <span>Tipo de cuenta</span>
              <select
                name="tipoCuenta"
                [ngModel]="datos().tipoCuenta"
                (ngModelChange)="poner('tipoCuenta', $event)"
              >
                <option value="AHORROS">Ahorros</option>
                <option value="CORRIENTE">Corriente</option>
              </select>
            </label>
            <label class="campo">
              <span>Número de cuenta</span>
              <input
                name="numeroCuenta"
                required
                [ngModel]="datos().numeroCuenta"
                (ngModelChange)="poner('numeroCuenta', $event)"
              />
            </label>
          </div>

          <button type="submit" class="primario" [disabled]="guardando()">
            {{ guardando() ? 'Guardando…' : existe() ? 'Guardar cambios' : 'Crear perfil' }}
          </button>
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
export class MiPerfil {
  protected readonly valorDe = valorDe;
  private readonly api = inject(PerfilPropietarioApi);

  protected readonly perfil = rxResource({
    stream: () => this.api.miPerfil(),
  });

  protected readonly existe = () => valorDe(this.perfil, null) != null;

  protected readonly datos = linkedSignal<PerfilPropietario>(() => ({
    ...PERFIL_VACIO,
    ...(valorDe(this.perfil, null) ?? {}),
  }));

  protected readonly guardando = signal(false);
  protected readonly error = signal('');
  protected readonly mensaje = signal('');

  protected poner<C extends keyof PerfilPropietario>(clave: C, valor: PerfilPropietario[C]): void {
    this.datos.update((actual) => ({ ...actual, [clave]: valor }));
  }

  protected guardar(): void {
    if (this.guardando()) {
      return;
    }

    this.guardando.set(true);
    this.error.set('');
    this.mensaje.set('');

    const peticion = this.existe()
      ? this.api.actualizarMiPerfil(this.datos())
      : this.api.crud.crear(this.datos());

    peticion.subscribe({
      next: (respuesta) => {
        this.guardando.set(false);
        this.mensaje.set(respuesta);
        this.perfil.reload();
      },
      error: (e: Error) => {
        this.guardando.set(false);
        this.error.set(e.message);
      },
    });
  }
}
