import { Component, computed, inject, input, linkedSignal, signal } from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';
import { Router } from '@angular/router';
import { forkJoin, of, switchMap } from 'rxjs';
import { HabitacionApi, ModalidadApi, MotelApi, PoliticaApi, ServicioApi, TarifaApi, TipoHabitacionApi } from '../../core/api/entidades';
import type { Reserva, ServicioAdicional } from '../../core/api/models';
import { ReservaApi } from '../../core/api/entidades';
import { recargoPorPersonas, totalEstimado, type ServicioElegido } from '../../core/reservas/total';
import { legible } from '../../shared/etiquetas';
import { lista, valorDe } from '../../shared/recursos';

@Component({
  standalone: false,
  selector: 'app-reservar',
  template: `
    <div class="angosto pila">
      <h1>Confirmar reserva</h1>

      @if (datos.isLoading()) {
        <p class="suave">Cargando…</p>
      } @else if (datos.error(); as e) {
        <p class="aviso mal">{{ e.message }}</p>
      } @else if (valorDe(datos, null); as d) {
        <p class="suave">
          Habitación {{ d.habitacion?.numero }}
          @if (d.tipo) {
            · {{ d.tipo.nombre }} · hasta {{ d.tipo.capacidadPersonas }} personas
          }
        </p>

        @if (error(); as texto) {
          <p class="aviso mal">{{ texto }}</p>
        }

        <form class="tarjeta pila" (ngSubmit)="enviar()">
          <div class="pestanas-form" role="tablist" aria-label="Reserva">
            <button type="button" role="tab" [attr.aria-selected]="!viendoPrevia()" (click)="viendoPrevia.set(false)">
              Datos
            </button>
            <button type="button" role="tab" [attr.aria-selected]="viendoPrevia()" (click)="viendoPrevia.set(true)">
              Vista previa
            </button>
          </div>

          @if (viendoPrevia()) {
            <div role="tabpanel" class="previa pila">
              <div>
                <span class="suave pequeno">{{ d.motel?.nombre ?? 'Motel' }}</span>
                <h2 style="margin:0">
                  Habitación {{ d.habitacion?.numero }}
                  @if (d.tipo) {
                    <small class="suave">· {{ d.tipo.nombre }}</small>
                  }
                </h2>
              </div>
              <dl class="detalle">
                <dt>Entrada</dt>
                <dd>{{ inicio() ? (inicio() | date: 'EEEE d MMM, h:mm a') : 'Sin elegir' }}</dd>
                <dt>Salida</dt>
                <dd>{{ salida() ? (salida() | date: 'EEEE d MMM, h:mm a') : 'Elige entrada y tarifa' }}</dd>
                <dt>Personas</dt>
                <dd>
                  {{ personas() }}
                  @if (recargo() > 0) {
                    <small class="suave">(recargo {{ recargo() | currency: 'COP' : 'symbol-narrow' : '1.0-0' }})</small>
                  }
                </dd>
                <dt>Tarifa</dt>
                <dd>
                  @if (tarifaElegida(); as t) {
                    {{ nombreModalidad(t.idModalidad) }} · {{ legible(t.tipoDia) }} ·
                    {{ t.precio | currency: 'COP' : 'symbol-narrow' : '1.0-0' }}
                  } @else {
                    Sin elegir
                  }
                </dd>
                <dt>Servicios</dt>
                <dd>
                  @for (e of elegidos(); track e.servicio.idServicio) {
                    <div>
                      {{ e.cantidad }} × {{ e.servicio.nombre }} ·
                      {{ e.servicio.precio * e.cantidad | currency: 'COP' : 'symbol-narrow' : '1.0-0' }}
                    </div>
                  } @empty {
                    Ninguno
                  }
                </dd>
              </dl>
            </div>
          }

          <div class="pila" role="tabpanel" [hidden]="viendoPrevia()">
          <div class="formulario">
            <label class="campo">
              <span>Entrada</span>
              <input
                type="datetime-local"
                name="inicio"
                required
                [ngModel]="inicio()"
                (ngModelChange)="inicio.set($event)"
              />
            </label>

            <label class="campo">
              <span>Personas</span>
              <input
                type="number"
                name="personas"
                min="1"
                max="20"
                required
                [ngModel]="personas()"
                (ngModelChange)="personas.set($event)"
              />
            </label>

            <label class="campo">
              <span>Modalidad y tarifa</span>
              <select
                name="tarifa"
                required
                [ngModel]="idTarifa()"
                (ngModelChange)="idTarifa.set($event)"
              >
                <option [ngValue]="null">— Seleccione —</option>
                @for (t of d.tarifas; track t.idTarifa) {
                  <option [ngValue]="t.idTarifa">
                    {{ nombreModalidad(t.idModalidad) }} · {{ legible(t.tipoDia) }} ·
                    {{ t.precio | currency: 'COP' : 'symbol-narrow' : '1.0-0' }}
                  </option>
                }
              </select>
            </label>
          </div>

          @if (d.tarifas.length === 0) {
            <p class="aviso mal">
              Esta habitación no tiene tarifas publicadas; no se puede reservar.
            </p>
          }

          @if (d.servicios.length > 0) {
            <fieldset class="pila">
              <legend>Servicios adicionales</legend>
              @for (s of d.servicios; track s.idServicio) {
                <label class="servicio">
                  <span class="crece">
                    {{ s.nombre }}
                    <small class="suave">{{
                      s.precio | currency: 'COP' : 'symbol-narrow' : '1.0-0'
                    }}</small>
                  </span>
                  <input
                    type="number"
                    min="0"
                    max="20"
                    [name]="'servicio-' + s.idServicio"
                    [ngModel]="cantidadDe(s)"
                    (ngModelChange)="ponerCantidad(s, $event)"
                  />
                </label>
              }
            </fieldset>
          }

          </div>

          <div class="fila entre resumen">
            <div>
              <span class="suave pequeno">Total estimado</span>
              <strong class="total">{{
                total() | currency: 'COP' : 'symbol-narrow' : '1.0-0'
              }}</strong>
              <p class="pequeno suave">
                El servidor recalcula el total y la hora de salida al crear la reserva.
              </p>
            </div>
            <button type="submit" class="primario" [disabled]="enviando() || idTarifa() === null">
              {{ enviando() ? 'Reservando…' : 'Reservar' }}
            </button>
          </div>
        </form>
      }
    </div>
  `,
  styles: `
    .angosto {
      max-width: 680px;
      margin: 0 auto;
    }
    fieldset {
      border: 1px solid var(--borde);
      border-radius: var(--r-m);
    }
    legend {
      font-size: 0.8rem;
      font-weight: 700;
      text-transform: uppercase;
      color: var(--texto-suave);
    }
    .servicio {
      display: flex;
      gap: 12px;
      align-items: center;
    }
    .servicio input {
      width: 80px;
    }
    .resumen {
      align-items: flex-end;
    }
    .previa {
      padding: 4px 0;
    }
    .detalle {
      display: grid;
      grid-template-columns: max-content 1fr;
      gap: 8px 20px;
      margin: 0;
    }
    .detalle dt {
      color: var(--texto-suave);
      font-size: 0.85rem;
    }
    .detalle dd {
      margin: 0;
    }
    .detalle dd::first-letter {
      text-transform: uppercase;
    }
    .total {
      display: block;
      font-size: 1.4rem;
    }
    .resumen p {
      margin: 2px 0 0;
      max-width: 34ch;
    }
  `,
})
export class Reservar {
  protected readonly lista = lista;
  protected readonly valorDe = valorDe;
  /** :idHabitacion de la ruta, y el inicio sugerido que trae el buscador. */
  readonly idHabitacion = input.required<string>();
  /** El router lo deja en undefined cuando la URL no trae ?inicio=, por ejemplo al recargar. */
  readonly inicioSugerido = input<string | undefined>('', { alias: 'inicio' });

  private readonly habitacionApi = inject(HabitacionApi);
  private readonly tipoApi = inject(TipoHabitacionApi);
  private readonly tarifaApi = inject(TarifaApi);
  private readonly servicioApi = inject(ServicioApi);
  private readonly politicaApi = inject(PoliticaApi);
  private readonly modalidadApi = inject(ModalidadApi);
  private readonly motelApi = inject(MotelApi);
  private readonly reservaApi = inject(ReservaApi);
  private readonly router = inject(Router);

  protected readonly legible = legible;

  /** El input datetime-local no admite segundos, así que se recortan. */
  protected readonly inicio = linkedSignal(() => (this.inicioSugerido() ?? '').slice(0, 16));
  protected readonly personas = signal(2);
  protected readonly idTarifa = signal<number | null>(null);
  protected readonly cantidades = signal<Record<number, number>>({});
  protected readonly enviando = signal(false);
  protected readonly error = signal('');
  protected readonly viendoPrevia = signal(false);

  /**
   * Todo lo que hace falta para armar la reserva. La habitación da el tipo y el
   * motel; de ahí salen las tarifas aplicables, los servicios y la política.
   */
  protected readonly datos = rxResource({
    params: () => this.idHabitacion(),
    stream: ({ params }) =>
      this.habitacionApi.crud.getById(params).pipe(
        switchMap((habitacion) => {
          if (!habitacion) {
            return of({ habitacion: null, motel: null, tipo: null, tarifas: [], servicios: [], politica: null });
          }
          return forkJoin({
            habitacion: of(habitacion),
            motel: habitacion.idMotel ? this.motelApi.crud.getById(habitacion.idMotel) : of(null),
            tipo: this.tipoApi.crud.getById(habitacion.idTipo),
            tarifas: this.tarifaApi.porTipo(habitacion.idTipo),
            servicios: habitacion.idMotel ? this.servicioApi.porMotel(habitacion.idMotel) : of([]),
            politica: habitacion.idMotel ? this.politicaApi.deMotel(habitacion.idMotel) : of(null),
          });
        }),
      ),
  });

  protected readonly modalidades = rxResource({
    stream: () => this.modalidadApi.crud.getAll(),
    defaultValue: [],
  });

  protected readonly elegidos = computed<ServicioElegido[]>(() => {
    const cantidades = this.cantidades();
    return (valorDe(this.datos, null)?.servicios ?? [])
      .filter((s) => (cantidades[s.idServicio!] ?? 0) > 0)
      .map((servicio) => ({ servicio, cantidad: cantidades[servicio.idServicio!] }));
  });

  protected readonly tarifaElegida = computed(
    () => valorDe(this.datos, null)?.tarifas.find((t) => t.idTarifa === this.idTarifa()) ?? null,
  );

  protected readonly recargo = computed(() =>
    recargoPorPersonas(valorDe(this.datos, null)?.politica ?? null, Number(this.personas())),
  );

  /** Estimada en el navegador con la duración de la modalidad; el backend la fija al crear. */
  protected readonly salida = computed(() => {
    const tarifa = this.tarifaElegida();
    const modalidad = lista(this.modalidades).find((m) => m.idModalidad === tarifa?.idModalidad);
    const inicio = new Date(this.inicio());
    if (!modalidad || Number.isNaN(inicio.getTime())) {
      return null;
    }
    return new Date(inicio.getTime() + modalidad.duracionHoras * 3_600_000);
  });

  protected readonly total = computed(() => {
    const datos = valorDe(this.datos, null);
    const tarifa = datos?.tarifas.find((t) => t.idTarifa === this.idTarifa());
    return totalEstimado(
      tarifa?.precio ?? 0,
      this.elegidos(),
      this.personas(),
      datos?.politica ?? null,
    );
  });

  protected nombreModalidad(idModalidad: number): string {
    const modalidad = lista(this.modalidades).find((m) => m.idModalidad === idModalidad);
    return modalidad
      ? `${modalidad.nombre} (${modalidad.duracionHoras} h)`
      : `Modalidad ${idModalidad}`;
  }

  protected cantidadDe(servicio: ServicioAdicional): number {
    return this.cantidades()[servicio.idServicio!] ?? 0;
  }

  protected ponerCantidad(servicio: ServicioAdicional, cantidad: number): void {
    this.cantidades.update((actual) => ({
      ...actual,
      [servicio.idServicio!]: Number(cantidad) || 0,
    }));
  }

  protected enviar(): void {
    const idTarifa = this.idTarifa();
    if (this.enviando() || idTarifa === null) {
      return;
    }

    this.enviando.set(true);
    this.error.set('');

    const reserva: Reserva = {
      idHabitacion: Number(this.idHabitacion()),
      idTarifa,
      cantidadPersonas: Number(this.personas()),
      fechaHoraInicio: `${this.inicio()}:00`,
      servicios: this.elegidos().map(({ servicio, cantidad }) => ({
        idServicio: servicio.idServicio!,
        cantidad,
      })),
    };

    this.reservaApi.crud.crear(reserva).subscribe({
      next: () => {
        this.enviando.set(false);
        void this.router.navigate(['/mis-reservas'], { queryParams: { creada: 1 } });
      },
      error: (e: Error) => {
        this.enviando.set(false);
        this.error.set(e.message);
      },
    });
  }
}
