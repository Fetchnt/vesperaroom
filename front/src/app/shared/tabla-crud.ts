import { Component, computed, effect, input, signal, untracked } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import type { Observable } from 'rxjs';
import type { Id } from '../core/api/api';
import { EstadoBadge } from './estado-badge';
import { legible } from './etiquetas';

export type TipoCampo = 'texto' | 'numero' | 'decimal' | 'fecha' | 'fechaHora' | 'area' | 'select' | 'booleano';

export interface Opcion {
  valor: string | number;
  texto: string;
}

export interface Campo {
  clave: string;
  etiqueta: string;
  tipo?: TipoCampo;
  opciones?: Opcion[];
  requerido?: boolean;
  /** Se muestra en la tabla pero no se puede escribir (lo calcula el backend). */
  soloLectura?: boolean;
  /** Se pinta como insignia de color en la tabla. */
  esEstado?: boolean;
  /** No aparece en la tabla, solo en el formulario. */
  oculto?: boolean;
}

/** Un registro cualquiera del backend. */
export type Registro = Record<string, unknown>;

/**
 * Acción propia de una fila, para los endpoints que no son el CRUD genérico:
 * cambiar el estado de un motel, verificar un propietario, confirmar una reserva.
 */
export interface AccionFila {
  texto: string | ((fila: Registro) => string);
  ejecutar: (fila: Registro) => Observable<string>;
  visible?: (fila: Registro) => boolean;
  peligro?: boolean;
}

/**
 * Tabla con formulario de alta y edición. La usan las pantallas cuyo CRUD es
 * el genérico del backend (catálogos, tipos de habitación, tarifas, servicios,
 * fotos...), que solo se diferencian en la lista de campos.
 */
@Component({
  selector: 'app-tabla-crud',
  imports: [FormsModule, RouterLink, EstadoBadge],
  template: `
    <section class="pila">
      <div class="encabezado-seccion">
        <div>
          <h2>{{ titulo() }}</h2>
          @if (descripcion()) {
            <p class="suave pequeno">{{ descripcion() }}</p>
          }
        </div>
        @if (!soloLectura()) {
          <button type="button" class="primario" (click)="nuevoRegistro()" [disabled]="formAbierto()">
            Agregar
          </button>
        }
      </div>

      @if (error(); as texto) {
        <p class="aviso mal">{{ texto }}</p>
      }
      @if (mensaje(); as texto) {
        <p class="aviso ok">{{ texto }}</p>
      }

      @if (formAbierto()) {
        <form class="tarjeta pila" (ngSubmit)="enviar()">
          <h3>{{ editandoId() === null ? 'Nuevo registro' : 'Editar registro' }}</h3>
          <div class="formulario">
            @for (campo of camposEditables(); track campo.clave) {
              <label class="campo">
                <span>{{ campo.etiqueta }}</span>
                @switch (campo.tipo ?? 'texto') {
                  @case ('select') {
                    <select
                      [ngModel]="valor(campo.clave)"
                      (ngModelChange)="poner(campo.clave, $event)"
                      [name]="campo.clave"
                      [required]="campo.requerido ?? false"
                    >
                      <option [ngValue]="null">— Seleccione —</option>
                      @for (opcion of campo.opciones ?? []; track opcion.valor) {
                        <option [ngValue]="opcion.valor">{{ opcion.texto }}</option>
                      }
                    </select>
                  }
                  @case ('area') {
                    <textarea
                      [ngModel]="valor(campo.clave)"
                      (ngModelChange)="poner(campo.clave, $event)"
                      [name]="campo.clave"
                      [required]="campo.requerido ?? false"
                    ></textarea>
                  }
                  @case ('booleano') {
                    <input
                      type="checkbox"
                      style="width:auto"
                      [ngModel]="valor(campo.clave)"
                      (ngModelChange)="poner(campo.clave, $event)"
                      [name]="campo.clave"
                    />
                  }
                  @default {
                    <input
                      [type]="tipoHtml(campo)"
                      [step]="campo.tipo === 'decimal' ? 'any' : null"
                      [ngModel]="valor(campo.clave)"
                      (ngModelChange)="poner(campo.clave, $event)"
                      [name]="campo.clave"
                      [required]="campo.requerido ?? false"
                    />
                  }
                }
              </label>
            }
          </div>
          <div class="fila">
            <button type="submit" class="primario" [disabled]="guardando()">
              {{ guardando() ? 'Guardando…' : 'Guardar' }}
            </button>
            <button type="button" class="plano" (click)="cancelar()">Cancelar</button>
          </div>
        </form>
      }

      @if (cargando()) {
        <p class="suave">Cargando…</p>
      } @else if (filas().length === 0) {
        <p class="vacio">{{ sinDatos() }}</p>
      } @else {
        <div class="tabla-envoltura">
          <table>
            <thead>
              <tr>
                @for (campo of camposVisibles(); track campo.clave) {
                  <th>{{ campo.etiqueta }}</th>
                }
                @if (hayAcciones()) {
                  <th>Acciones</th>
                }
              </tr>
            </thead>
            <tbody>
              @for (fila of filas(); track $index) {
                <tr>
                  @for (campo of camposVisibles(); track campo.clave) {
                    <td>
                      @if (campo.esEstado) {
                        <app-estado [valor]="texto(fila, campo)" />
                      } @else {
                        {{ texto(fila, campo) }}
                      }
                    </td>
                  }
                  @if (hayAcciones()) {
                    <td>
                      @if (enlace(); as destino) {
                        <a class="plano-enlace" [routerLink]="destino(fila)">{{ textoEnlace() }}</a>
                      }
                      @for (accion of accionesVisibles(fila); track $index) {
                        <button
                          type="button"
                          class="plano"
                          [class.peligro]="accion.peligro"
                          [disabled]="ejecutando()"
                          (click)="ejecutarAccion(accion, fila)"
                        >
                          {{ textoAccion(accion, fila) }}
                        </button>
                      }
                      @if (!soloLectura()) {
                        <button type="button" class="plano" (click)="editar(fila)">Editar</button>
                      }
                      @if (borrar()) {
                        <button type="button" class="plano peligro" (click)="eliminar(fila)">Eliminar</button>
                      }
                    </td>
                  }
                </tr>
              }
            </tbody>
          </table>
        </div>
      }
    </section>
  `,
  styles: `
    .plano-enlace {
      display: inline-block;
      padding: 4px 8px;
      border-radius: var(--r-s);
      font-weight: 600;
    }
    .plano-enlace:hover {
      text-decoration: none;
      background: var(--superficie-2);
    }
  `,
})
export class TablaCrud {
  readonly titulo = input('');
  readonly descripcion = input('');
  readonly sinDatos = input('Todavía no hay registros.');
  readonly campos = input.required<Campo[]>();
  /** Nombre del campo que identifica el registro, por ejemplo idMotel. */
  readonly claveId = input.required<string>();
  readonly cargar = input.required<() => Observable<Registro[]>>();
  readonly guardar = input<((id: Id | null, dto: Registro) => Observable<string>) | null>(null);
  readonly borrar = input<((id: Id) => Observable<string>) | null>(null);
  /** Valores iniciales de un registro nuevo, como el idMotel del contexto. */
  readonly porDefecto = input<Registro>({});
  /** Destino de navegación de la fila, por ejemplo la gestión de un motel. */
  readonly enlace = input<((fila: Registro) => (string | number)[]) | null>(null);
  readonly textoEnlace = input('Abrir');
  readonly acciones = input<AccionFila[]>([]);

  protected readonly filas = signal<Registro[]>([]);
  protected readonly cargando = signal(false);
  protected readonly guardando = signal(false);
  protected readonly error = signal('');
  protected readonly mensaje = signal('');
  protected readonly editandoId = signal<Id | null>(null);
  protected readonly borrador = signal<Registro>({});
  protected readonly formAbierto = signal(false);

  protected readonly ejecutando = signal(false);

  protected readonly soloLectura = computed(() => this.guardar() === null);
  protected readonly hayAcciones = computed(
    () => !this.soloLectura() || this.borrar() !== null || this.enlace() !== null || this.acciones().length > 0,
  );
  protected readonly camposVisibles = computed(() => this.campos().filter((c) => !c.oculto));
  protected readonly camposEditables = computed(() => this.campos().filter((c) => !c.soloLectura));

  constructor() {
    effect(() => {
      const cargador = this.cargar();
      untracked(() => this.recargar(cargador));
    });
  }

  private recargar(cargador = this.cargar()): void {
    this.cargando.set(true);
    this.error.set('');
    cargador().subscribe({
      next: (datos) => {
        this.filas.set(datos);
        this.cargando.set(false);
      },
      error: (e: Error) => {
        this.error.set(e.message);
        this.filas.set([]);
        this.cargando.set(false);
      },
    });
  }

  protected tipoHtml(campo: Campo): string {
    switch (campo.tipo) {
      case 'numero':
      case 'decimal':
        return 'number';
      case 'fecha':
        return 'date';
      case 'fechaHora':
        return 'datetime-local';
      default:
        return 'text';
    }
  }

  protected valor(clave: string): unknown {
    return this.borrador()[clave] ?? null;
  }

  protected poner(clave: string, valor: unknown): void {
    this.borrador.update((actual) => ({ ...actual, [clave]: valor }));
  }

  protected texto(fila: Registro, campo: Campo): string {
    const valor = fila[campo.clave];
    if (valor === null || valor === undefined || valor === '') {
      return '—';
    }
    if (typeof valor === 'boolean') {
      return valor ? 'Sí' : 'No';
    }
    if (campo.tipo === 'select') {
      const opcion = (campo.opciones ?? []).find((o) => o.valor === valor);
      return opcion ? opcion.texto : String(valor);
    }
    if (campo.esEstado) {
      return String(valor);
    }
    if (campo.tipo === 'decimal') {
      return Number(valor).toLocaleString('es-CO', { style: 'currency', currency: 'COP', maximumFractionDigits: 0 });
    }
    return typeof valor === 'string' && /^[A-Z_]{3,}$/.test(valor) ? legible(valor) : String(valor);
  }

  protected accionesVisibles(fila: Registro): AccionFila[] {
    return this.acciones().filter((a) => a.visible?.(fila) ?? true);
  }

  protected textoAccion(accion: AccionFila, fila: Registro): string {
    return typeof accion.texto === 'function' ? accion.texto(fila) : accion.texto;
  }

  protected ejecutarAccion(accion: AccionFila, fila: Registro): void {
    if (this.ejecutando()) {
      return;
    }

    this.ejecutando.set(true);
    this.error.set('');
    accion.ejecutar(fila).subscribe({
      next: (respuesta) => {
        this.ejecutando.set(false);
        this.mensaje.set(respuesta);
        this.recargar();
      },
      error: (e: Error) => {
        this.ejecutando.set(false);
        this.error.set(e.message);
      },
    });
  }

  protected nuevoRegistro(): void {
    this.editandoId.set(null);
    this.borrador.set({ ...this.porDefecto() });
    this.mensaje.set('');
    this.formAbierto.set(true);
  }

  protected editar(fila: Registro): void {
    this.editandoId.set(fila[this.claveId()] as Id);
    this.borrador.set({ ...fila });
    this.mensaje.set('');
    this.formAbierto.set(true);
  }

  protected cancelar(): void {
    this.formAbierto.set(false);
    this.borrador.set({});
    this.editandoId.set(null);
  }

  protected enviar(): void {
    const guardar = this.guardar();
    if (!guardar || this.guardando()) {
      return;
    }

    this.guardando.set(true);
    this.error.set('');
    guardar(this.editandoId(), this.borrador()).subscribe({
      next: (respuesta) => {
        this.guardando.set(false);
        this.mensaje.set(respuesta);
        this.cancelar();
        this.recargar();
      },
      error: (e: Error) => {
        this.guardando.set(false);
        this.error.set(e.message);
      },
    });
  }

  protected eliminar(fila: Registro): void {
    const borrar = this.borrar();
    const id = fila[this.claveId()] as Id | undefined;
    if (!borrar || id === undefined || !confirm('¿Eliminar este registro?')) {
      return;
    }

    this.error.set('');
    borrar(id).subscribe({
      next: (respuesta) => {
        this.mensaje.set(respuesta);
        this.recargar();
      },
      error: (e: Error) => this.error.set(e.message),
    });
  }
}
