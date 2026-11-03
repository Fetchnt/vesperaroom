import {
  Component,
  computed,
  effect,
  type ElementRef,
  input,
  type OnDestroy,
  signal,
  type TemplateRef,
  untracked,
  viewChild,
} from '@angular/core';
import type { Observable } from 'rxjs';
import type { Id } from '../core/api/api';
import { legible } from './etiquetas';

export type TipoCampo =
  | 'texto'
  | 'numero'
  | 'decimal'
  | 'fecha'
  | 'fechaHora'
  | 'area'
  | 'select'
  | 'booleano'
  | 'archivo'
  | 'imagen';

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
  /** Solo se pide al crear el registro, no al editarlo. */
  soloAlCrear?: boolean;
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
  /** Pregunta que se muestra antes de ejecutar; sin ella la acción va directo. */
  confirmar?: string;
}

interface Pendiente {
  pregunta: string;
  boton: string;
  peligro: boolean;
  hacer: () => void;
}

const FORMATO_FECHA: Intl.DateTimeFormatOptions = { day: 'numeric', month: 'short', year: 'numeric' };
const FORMATO_HORA: Intl.DateTimeFormatOptions = { ...FORMATO_FECHA, hour: 'numeric', minute: '2-digit' };

/**
 * Texto de una celda. Las fechas del backend llegan sin zona (LocalDate y
 * LocalDateTime), así que se leen como hora local: `new Date('2026-11-02')`
 * a secas sería medianoche UTC y en Bogotá mostraría el día anterior.
 */
export function textoCelda(valor: unknown, campo: Campo): string {
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
    return Number(valor).toLocaleString('es-CO', {
      style: 'currency',
      currency: 'COP',
      maximumFractionDigits: 0,
    });
  }
  if ((campo.tipo === 'fecha' || campo.tipo === 'fechaHora') && typeof valor === 'string') {
    const fecha = new Date(valor.length === 10 ? `${valor}T00:00` : valor);
    if (!Number.isNaN(fecha.getTime())) {
      return fecha.toLocaleString('es-CO', campo.tipo === 'fecha' ? FORMATO_FECHA : FORMATO_HORA);
    }
  }
  return typeof valor === 'string' && /^[A-Z_]{3,}$/.test(valor) ? legible(valor) : String(valor);
}

/** Minúsculas y sin tildes, para que «sanchez» encuentre «Sánchez». */
function normalizar(texto: string): string {
  return texto.normalize('NFD').replace(/\p{Diacritic}/gu, '').toLowerCase();
}

/**
 * Tabla con formulario de alta y edición. La usan las pantallas cuyo CRUD es
 * el genérico del backend (catálogos, tipos de habitación, tarifas, servicios,
 * fotos...), que solo se diferencian en la lista de campos.
 */
@Component({
  standalone: false,
  selector: 'app-tabla-crud',
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
          <button
            type="button"
            class="primario"
            (click)="nuevoRegistro()"
            [disabled]="formAbierto()"
          >
            Agregar
          </button>
        }
      </div>

      @if (error(); as texto) {
        <p class="aviso mal" role="alert">{{ texto }}</p>
      }
      @if (mensaje(); as texto) {
        <p class="aviso ok" role="status">{{ texto }}</p>
      }

      @if (formAbierto()) {
        <form class="tarjeta pila" (ngSubmit)="enviar()">
          <h3>{{ editandoId() === null ? 'Nuevo registro' : 'Editar registro' }}</h3>
          @if (vistaPrevia(); as plantilla) {
            <div class="pestanas-form" role="tablist" aria-label="Formulario">
              <button type="button" role="tab" [attr.aria-selected]="!viendoPrevia()" (click)="viendoPrevia.set(false)">
                Datos
              </button>
              <button type="button" role="tab" [attr.aria-selected]="viendoPrevia()" (click)="viendoPrevia.set(true)">
                Vista previa
              </button>
            </div>
            @if (viendoPrevia()) {
              <div role="tabpanel">
                <ng-container
                  [ngTemplateOutlet]="plantilla"
                  [ngTemplateOutletContext]="{ $implicit: borrador(), fotos: vistas() }"
                />
              </div>
            }
          }
          <!-- Se oculta en lugar de quitarse para que el archivo elegido no se pierda -->
          <div class="formulario" [hidden]="viendoPrevia()" [attr.role]="vistaPrevia() ? 'tabpanel' : null">
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
                  @case ('archivo') {
                    <input
                      type="file"
                      accept="image/jpeg,image/png,image/webp"
                      (change)="ponerArchivo(campo.clave, $event)"
                      [name]="campo.clave"
                    />
                    @if (vistas()[campo.clave]; as src) {
                      <img class="vista-previa" [src]="src" alt="Vista previa de la imagen elegida" />
                    }
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
                    @if (campo.tipo === 'imagen' && valor(campo.clave); as src) {
                      <img class="vista-previa" [src]="$any(src)" alt="Vista previa de la imagen" />
                    }
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
        <div class="tabla-envoltura" aria-busy="true">
          <span class="oculto">Cargando…</span>
          @for (n of [1, 2, 3]; track n) {
            <div class="esqueleto"></div>
          }
        </div>
      } @else if (filas().length === 0) {
        <div class="vacio pila">
          <p style="margin:0">{{ sinDatos() }}</p>
          @if (!soloLectura() && !formAbierto()) {
            <div><button type="button" class="contorno" (click)="nuevoRegistro()">Agregar el primero</button></div>
          }
        </div>
      } @else {
        <div class="barra-tabla">
          <input
            type="search"
            class="buscar-tabla"
            placeholder="Buscar…"
            aria-label="Buscar en {{ titulo() || 'la tabla' }}"
            [value]="consulta()"
            (input)="consulta.set($any($event.target).value)"
          />
          @if (estados().length > 1) {
            <div class="fila filtros-estado" role="group" aria-label="Filtrar por estado">
              <button
                type="button"
                class="contorno"
                [class.activo]="estado() === null"
                [attr.aria-pressed]="estado() === null"
                (click)="estado.set(null)"
              >
                Todos <span class="cuenta">{{ filas().length }}</span>
              </button>
              @for (e of estados(); track e.valor) {
                <button
                  type="button"
                  class="contorno"
                  [class.activo]="estado() === e.valor"
                  [attr.aria-pressed]="estado() === e.valor"
                  (click)="estado.set(e.valor)"
                >
                  {{ legible(e.valor) }} <span class="cuenta">{{ e.cantidad }}</span>
                </button>
              }
            </div>
          }
          <span class="suave pequeno total-tabla">
            {{ filasFiltradas().length }} de {{ filas().length }}
            {{ filas().length === 1 ? 'registro' : 'registros' }}
          </span>
        </div>

        @if (filasFiltradas().length === 0) {
          <p class="vacio">Ningún registro coincide con la búsqueda.</p>
        } @else {
          <div class="tabla-envoltura">
            <table>
              <thead>
                <tr>
                  @for (campo of camposVisibles(); track campo.clave) {
                    <th scope="col" [class.num]="campo.tipo === 'decimal' || campo.tipo === 'numero'">
                      {{ campo.etiqueta }}
                    </th>
                  }
                  @if (hayAcciones()) {
                    <th scope="col" class="acciones-celda"><span class="oculto">Acciones</span></th>
                  }
                </tr>
              </thead>
              <tbody>
                @for (fila of filasFiltradas(); track $index) {
                  <tr>
                    @for (campo of camposVisibles(); track campo.clave) {
                      <td [class.num]="campo.tipo === 'decimal' || campo.tipo === 'numero'">
                        @if (campo.esEstado) {
                          <app-estado [valor]="texto(fila, campo)" />
                        } @else if (campo.tipo === 'imagen' && fila[campo.clave]) {
                          <img class="miniatura" [src]="$any(fila[campo.clave])" alt="" loading="lazy" />
                        } @else {
                          {{ texto(fila, campo) }}
                        }
                      </td>
                    }
                    @if (hayAcciones()) {
                      <td class="acciones-celda">
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
                          <button type="button" class="plano peligro" (click)="eliminar(fila)">
                            Eliminar
                          </button>
                        }
                      </td>
                    }
                  </tr>
                }
              </tbody>
            </table>
          </div>
        }
      }
    </section>

    <dialog #dialogo class="dialogo" aria-labelledby="pregunta-tabla" (close)="pendiente.set(null)">
      @if (pendiente(); as p) {
        <p id="pregunta-tabla">{{ p.pregunta }}</p>
        <div class="fila" style="justify-content:flex-end">
          <button type="button" class="plano" (click)="dialogo.close()">Volver</button>
          <button type="button" [class.peligro]="p.peligro" [class.primario]="!p.peligro" (click)="confirmar()">
            {{ p.boton }}
          </button>
        </div>
      }
    </dialog>
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
    .barra-tabla {
      display: flex;
      flex-wrap: wrap;
      gap: 12px;
      align-items: center;
    }
    .buscar-tabla {
      max-width: 260px;
    }
    .filtros-estado {
      gap: 6px;
    }
    .filtros-estado button {
      padding: 4px 10px;
      font-size: 0.85rem;
    }
    .cuenta {
      opacity: 0.75;
      margin-left: 2px;
    }
    .total-tabla {
      margin-left: auto;
    }
    .num {
      text-align: right;
      font-variant-numeric: tabular-nums;
    }
    .acciones-celda {
      text-align: right;
    }
    tbody tr:hover td {
      background: var(--superficie-2);
    }
    .miniatura {
      width: 48px;
      height: 36px;
      object-fit: cover;
      border-radius: var(--r-s);
      display: block;
    }
    .vista-previa {
      margin-top: 6px;
      max-width: 200px;
      max-height: 130px;
      object-fit: cover;
      border-radius: var(--r-m);
      border: 1px solid var(--borde);
    }
    .esqueleto {
      height: 40px;
      margin: 8px 12px;
      border-radius: var(--r-s);
      background: linear-gradient(90deg, var(--superficie-2), var(--borde), var(--superficie-2));
      background-size: 200% 100%;
      animation: brillo 1.2s ease-in-out infinite;
    }
    @keyframes brillo {
      to {
        background-position: -200% 0;
      }
    }
    @media (prefers-reduced-motion: reduce) {
      .esqueleto {
        animation: none;
      }
    }
    .dialogo {
      border: 1px solid var(--borde);
      border-radius: var(--r-l);
      background: var(--superficie);
      color: var(--texto);
      box-shadow: var(--sombra);
      padding: 20px;
      max-width: 420px;
      width: calc(100% - 32px);
    }
    .dialogo::backdrop {
      background: rgb(0 0 0 / 0.45);
    }
  `,
})
export class TablaCrud implements OnDestroy {
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
  /**
   * Plantilla de la pestaña «Vista previa» del formulario. Recibe el borrador
   * como `$implicit` y las URLs de las imágenes elegidas en `fotos`.
   */
  readonly vistaPrevia = input<TemplateRef<{ $implicit: Registro; fotos: Record<string, string> }> | null>(null);

  protected readonly legible = legible;
  private readonly dialogo = viewChild.required<ElementRef<HTMLDialogElement>>('dialogo');

  protected readonly filas = signal<Registro[]>([]);
  protected readonly cargando = signal(false);
  protected readonly guardando = signal(false);
  protected readonly error = signal('');
  protected readonly mensaje = signal('');
  protected readonly editandoId = signal<Id | null>(null);
  protected readonly borrador = signal<Registro>({});
  protected readonly formAbierto = signal(false);
  protected readonly ejecutando = signal(false);
  protected readonly consulta = signal('');
  protected readonly estado = signal<string | null>(null);
  protected readonly pendiente = signal<Pendiente | null>(null);
  protected readonly viendoPrevia = signal(false);
  /** URLs temporales de las imágenes elegidas en los campos de archivo. */
  protected readonly vistas = signal<Record<string, string>>({});

  protected readonly soloLectura = computed(() => this.guardar() === null);
  protected readonly hayAcciones = computed(
    () =>
      !this.soloLectura() ||
      this.borrar() !== null ||
      this.enlace() !== null ||
      this.acciones().length > 0,
  );
  protected readonly camposVisibles = computed(() => this.campos().filter((c) => !c.oculto));
  protected readonly camposEditables = computed(() =>
    this.campos().filter((c) => !c.soloLectura && !(c.soloAlCrear && this.editandoId() !== null)),
  );
  private readonly campoEstado = computed(() => this.campos().find((c) => c.esEstado) ?? null);

  /** Estados presentes en los datos, en el orden en que aparecen, con su cantidad. */
  protected readonly estados = computed(() => {
    const campo = this.campoEstado();
    const cuenta = new Map<string, number>();
    if (campo) {
      for (const fila of this.filas()) {
        const valor = String(fila[campo.clave] ?? '');
        cuenta.set(valor, (cuenta.get(valor) ?? 0) + 1);
      }
    }
    return [...cuenta].map(([valor, cantidad]) => ({ valor, cantidad }));
  });

  protected readonly filasFiltradas = computed(() => {
    const campo = this.campoEstado();
    const estado = this.estado();
    const consulta = normalizar(this.consulta().trim());
    return this.filas().filter(
      (fila) =>
        (estado === null || !campo || String(fila[campo.clave] ?? '') === estado) &&
        (!consulta ||
          this.camposVisibles().some((c) =>
            normalizar(this.texto(fila, c) + ' ' + legible(String(fila[c.clave] ?? ''))).includes(consulta),
          )),
    );
  });

  constructor() {
    effect(() => {
      const cargador = this.cargar();
      untracked(() => this.recargar(cargador));
    });
  }

  ngOnDestroy(): void {
    this.soltarVistas();
  }

  private recargar(cargador = this.cargar()): void {
    this.cargando.set(true);
    this.error.set('');
    cargador().subscribe({
      next: (datos) => {
        this.filas.set(datos);
        // Si el estado filtrado ya no existe (p. ej. se cambió el último), se vuelve a «Todos»
        if (this.estado() !== null && !this.estados().some((e) => e.valor === this.estado())) {
          this.estado.set(null);
        }
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
      case 'imagen':
        return 'url';
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

  protected ponerArchivo(clave: string, evento: Event): void {
    const archivo = (evento.target as HTMLInputElement).files?.[0] ?? null;
    this.poner(clave, archivo);
    const anterior = this.vistas()[clave];
    if (anterior) {
      URL.revokeObjectURL(anterior);
    }
    this.vistas.update(({ [clave]: _, ...resto }) =>
      archivo ? { ...resto, [clave]: URL.createObjectURL(archivo) } : resto,
    );
  }

  private soltarVistas(): void {
    Object.values(this.vistas()).forEach((url) => URL.revokeObjectURL(url));
    this.vistas.set({});
  }

  protected texto(fila: Registro, campo: Campo): string {
    return textoCelda(fila[campo.clave], campo);
  }

  protected accionesVisibles(fila: Registro): AccionFila[] {
    return this.acciones().filter((a) => a.visible?.(fila) ?? true);
  }

  protected textoAccion(accion: AccionFila, fila: Registro): string {
    return typeof accion.texto === 'function' ? accion.texto(fila) : accion.texto;
  }

  /** Abre el diálogo de confirmación; `hacer` solo corre si la persona confirma. */
  private preguntar(pregunta: string, boton: string, peligro: boolean, hacer: () => void): void {
    this.pendiente.set({ pregunta, boton, peligro, hacer });
    this.dialogo().nativeElement.showModal();
  }

  protected confirmar(): void {
    const p = this.pendiente();
    this.dialogo().nativeElement.close();
    p?.hacer();
  }

  protected ejecutarAccion(accion: AccionFila, fila: Registro): void {
    if (this.ejecutando()) {
      return;
    }
    if (accion.confirmar) {
      const boton = this.textoAccion(accion, fila);
      this.preguntar(accion.confirmar, boton, accion.peligro ?? false, () => this.correrAccion(accion, fila));
    } else {
      this.correrAccion(accion, fila);
    }
  }

  private correrAccion(accion: AccionFila, fila: Registro): void {
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
    this.viendoPrevia.set(false);
    this.borrador.set({});
    this.editandoId.set(null);
    this.soltarVistas();
  }

  protected enviar(): void {
    const guardar = this.guardar();
    if (!guardar || this.guardando()) {
      return;
    }

    // El campo de archivo no usa ngModel, así que su obligatoriedad se revisa aquí
    const sinArchivo = this.camposEditables().find(
      (c) => c.tipo === 'archivo' && c.requerido && !this.valor(c.clave),
    );
    if (sinArchivo) {
      this.error.set(`Elige un archivo para «${sinArchivo.etiqueta}».`);
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
    if (!borrar || id === undefined) {
      return;
    }

    this.preguntar('¿Eliminar este registro? Esta acción no se puede deshacer.', 'Eliminar', true, () => {
      this.error.set('');
      borrar(id).subscribe({
        next: (respuesta) => {
          this.mensaje.set(respuesta);
          this.recargar();
        },
        error: (e: Error) => this.error.set(e.message),
      });
    });
  }
}
