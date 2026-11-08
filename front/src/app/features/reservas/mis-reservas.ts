import { Component, inject, signal } from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';
import { forkJoin, map, of, switchMap, type Observable } from 'rxjs';
import { PagoApi, ResenaApi, ReservaApi } from '../../core/api/entidades';
import type { MetodoPago, Pago, Resena, Reserva } from '../../core/api/models';
import { Avisos } from '../../shared/avisos';
import { legible } from '../../shared/etiquetas';
import { IMAGENES } from '../../shared/imagenes';
import { lista } from '../../shared/recursos';

const METODOS: MetodoPago[] = [
  'EFECTIVO',
  'TARJETA_CREDITO',
  'TARJETA_DEBITO',
  'PSE',
  'NEQUI',
  'DAVIPLATA',
];

type ReservaDetallada = Reserva & {
  pago: Pago | null;
  resena: Resena | null;
};

@Component({
  standalone: false,
  selector: 'app-mis-reservas',
  template: `
    <app-cabecera [imagen]="imagenes.habitacion" titulo="Mis reservas" subtitulo="Tus noches reservadas, sus pagos y reseñas.">
      <button type="button" (click)="reservas.reload()">Actualizar</button>
    </app-cabecera>

    @if (reservas.isLoading()) {
      <p class="suave">Cargando…</p>
    } @else if (reservas.error(); as e) {
      <p class="aviso mal">{{ e.message }}</p>
    } @else if (lista(reservas).length === 0) {
      <p class="vacio">Todavía no has hecho ninguna reserva.</p>
    } @else {
      <div class="pila">
        @for (r of lista(reservas); track r.idReserva) {
          <article class="tarjeta pila">
            <div class="fila entre">
              <div>
                <h2>{{ r.codigoReserva }}</h2>
                <p class="suave pequeno">
                  {{ r.fechaHoraInicio | date: 'medium' }} →
                  {{ r.fechaHoraFin | date: 'shortTime' }} · {{ r.cantidadPersonas }} personas
                </p>
              </div>
              <div class="derecha">
                <app-estado [valor]="r.estado" />
                <strong>{{ r.total | currency: 'COP' : 'symbol-narrow' : '1.0-0' }}</strong>
              </div>
            </div>

            @if (r.estado === 'CANCELADA') {
              <p class="pequeno suave">
                Cancelada el {{ r.fechaCancelacion | date: 'medium' }}.
              </p>
            }

            <!-- Pago -->
            @if (r.pago; as pago) {
              <p class="pequeno">
                Pago: {{ pago.monto | currency: 'COP' : 'symbol-narrow' : '1.0-0' }} ·
                {{ legible(pago.metodoPago) }} · <app-estado [valor]="pago.estado" />
                @if (pago.referenciaTransaccion) {
                  <span class="suave"> · ref. {{ pago.referenciaTransaccion }}</span>
                }
              </p>
            }
            <!-- Un pago rechazado se muestra, pero no impide volver a pagar. -->
            @if (vigente(r) && (!r.pago || r.pago.estado === 'RECHAZADO')) {
              @if (pagando() === r.idReserva) {
                <form class="formulario" (ngSubmit)="registrarPago(r)">
                  <label class="campo">
                    <span>Método</span>
                    <select name="metodo" [ngModel]="metodo()" (ngModelChange)="metodo.set($event)">
                      @for (m of metodos; track m) {
                        <option [ngValue]="m">{{ legible(m) }}</option>
                      }
                    </select>
                  </label>
                  <label class="campo">
                    <span>Referencia (opcional)</span>
                    <input
                      name="referencia"
                      [ngModel]="referencia()"
                      (ngModelChange)="referencia.set($event)"
                    />
                  </label>
                  <div class="fila">
                    <button type="submit" class="primario" [disabled]="trabajando()">Pagar</button>
                    <button type="button" class="plano" (click)="pagando.set(null)">
                      Cancelar
                    </button>
                  </div>
                </form>
              } @else {
                <div class="fila">
                  <button type="button" (click)="abrirPago(r)">Registrar pago</button>
                </div>
              }
            }

            <!-- Reseña -->
            @if (resenando() === r.idReserva) {
              <form class="pila" (ngSubmit)="dejarResena(r)">
                <div class="formulario">
                  <label class="campo">
                    <span>Calificación</span>
                    <select
                      name="calificacion"
                      [ngModel]="calificacion()"
                      (ngModelChange)="calificacion.set($event)"
                    >
                      @for (n of [1, 2, 3, 4, 5]; track n) {
                        <option [ngValue]="n">{{ n }}</option>
                      }
                    </select>
                  </label>
                </div>
                <label class="campo">
                  <span>Comentario</span>
                  <textarea
                    name="comentario"
                    [ngModel]="comentario()"
                    (ngModelChange)="comentario.set($event)"
                  ></textarea>
                </label>
                <div class="fila">
                  <button type="submit" class="primario" [disabled]="trabajando()">
                    {{ r.resena ? 'Guardar cambios' : 'Publicar reseña' }}
                  </button>
                  <button type="button" class="plano" (click)="resenando.set(null)">
                    Cancelar
                  </button>
                </div>
              </form>
            } @else if (r.resena; as resena) {
              <div class="fila">
                <app-estrellas [valor]="resena.calificacion" [mostrarValor]="false" />
                <span class="pequeno suave">{{ resena.comentario }}</span>
                <button type="button" class="plano" (click)="abrirResena(r)">Editar</button>
                <button
                  type="button"
                  class="plano peligro"
                  [disabled]="trabajando()"
                  (click)="borrarResena(resena)"
                >
                  Eliminar
                </button>
              </div>
            } @else if (r.estado === 'FINALIZADA') {
              <div class="fila">
                <button type="button" (click)="abrirResena(r)">Dejar reseña</button>
              </div>
            }

            @if (vigente(r)) {
              <div class="fila">
                <button
                  type="button"
                  class="peligro"
                  [disabled]="trabajando()"
                  (click)="cancelar(r)"
                >
                  Cancelar reserva
                </button>
              </div>
            }
          </article>
        }
      </div>
    }
  `,
  styles: `
    .derecha {
      text-align: right;
    }
    .derecha strong {
      display: block;
    }
  `,
})
export class MisReservas {
  protected readonly lista = lista;
  protected readonly imagenes = IMAGENES;
  private readonly reservaApi = inject(ReservaApi);
  private readonly pagoApi = inject(PagoApi);
  private readonly resenaApi = inject(ResenaApi);
  private readonly avisos = inject(Avisos);

  protected readonly legible = legible;
  protected readonly metodos = METODOS;

  protected readonly trabajando = signal(false);

  protected readonly pagando = signal<number | null>(null);
  protected readonly metodo = signal<MetodoPago>('EFECTIVO');
  protected readonly referencia = signal('');

  protected readonly resenando = signal<number | null>(null);
  protected readonly calificacion = signal(5);
  protected readonly comentario = signal('');

  /** Cada reserva con su pago y su reseña, que viven en endpoints aparte. */
  protected readonly reservas = rxResource<ReservaDetallada[], unknown>({
    stream: () =>
      forkJoin({ reservas: this.reservaApi.mias(), resenas: this.resenaApi.mias() }).pipe(
        switchMap(({ reservas, resenas }) =>
          reservas.length === 0
            ? of<ReservaDetallada[]>([])
            : forkJoin(
                reservas.map((reserva) =>
                  this.pagoApi.porReserva(reserva.idReserva!).pipe(
                    map((pago) => ({
                      ...reserva,
                      pago,
                      resena: resenas.find((r) => r.idReserva === reserva.idReserva) ?? null,
                    })),
                  ),
                ),
              ),
        ),
      ),
    defaultValue: [],
  });

  /** Solo una reserva vigente admite pago o cancelación. */
  protected vigente(r: Reserva): boolean {
    return r.estado === 'PENDIENTE' || r.estado === 'CONFIRMADA';
  }

  protected abrirPago(r: Reserva): void {
    this.pagando.set(r.idReserva ?? null);
    this.metodo.set('EFECTIVO');
    this.referencia.set('');
  }

  /** Abre el formulario vacío, o con la reseña actual si ya existe. */
  protected abrirResena(r: ReservaDetallada): void {
    this.resenando.set(r.idReserva ?? null);
    this.calificacion.set(r.resena?.calificacion ?? 5);
    this.comentario.set(r.resena?.comentario ?? '');
  }

  protected registrarPago(r: ReservaDetallada): void {
    if (this.trabajando()) {
      return;
    }
    this.ejecutar(
      this.pagoApi.crud.crear({
        idReserva: r.idReserva!,
        monto: r.total ?? 0,
        metodoPago: this.metodo(),
        referenciaTransaccion: this.referencia().trim() || undefined,
      }),
      () => this.pagando.set(null),
    );
  }

  protected dejarResena(r: ReservaDetallada): void {
    if (this.trabajando()) {
      return;
    }
    const resena: Resena = {
      idReserva: r.idReserva!,
      calificacion: Number(this.calificacion()),
      comentario: this.comentario().trim() || undefined,
    };
    this.ejecutar(
      r.resena
        ? this.resenaApi.crud.actualizar(r.resena.idResena!, resena)
        : this.resenaApi.crud.crear(resena),
      () => this.resenando.set(null),
    );
  }

  protected async borrarResena(resena: Resena): Promise<void> {
    if (this.trabajando()) {
      return;
    }
    const si = await this.avisos.confirmar({
      titulo: '¿Eliminar tu reseña?',
      texto: 'También se borra la respuesta del motel, si la hay.',
      aceptar: 'Eliminar reseña',
      cancelar: 'Conservarla',
      peligro: true,
    });
    if (si) {
      this.ejecutar(this.resenaApi.crud.eliminar(resena.idResena!));
    }
  }

  protected async cancelar(r: ReservaDetallada): Promise<void> {
    if (this.trabajando()) {
      return;
    }
    const si = await this.avisos.confirmar({
      titulo: `¿Cancelar la reserva ${r.codigoReserva}?`,
      texto: 'Esta acción no se puede deshacer.',
      aceptar: 'Cancelar reserva',
      cancelar: 'Mantenerla',
      peligro: true,
    });
    if (si) {
      this.ejecutar(this.reservaApi.cancelar(r.idReserva!));
    }
  }

  private ejecutar(peticion: Observable<string>, alTerminar?: () => void): void {
    this.trabajando.set(true);

    peticion.subscribe({
      next: (respuesta) => {
        this.trabajando.set(false);
        this.avisos.exito(respuesta);
        alTerminar?.();
        this.reservas.reload();
      },
      error: (e: Error) => {
        this.trabajando.set(false);
        this.avisos.error(e.message);
      },
    });
  }
}
