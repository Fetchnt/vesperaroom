import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { forkJoin, map, of, switchMap, type Observable } from 'rxjs';
import { PagoApi, ResenaApi, ReservaApi } from '../../core/api/entidades';
import type { MetodoPago, Pago, Resena, Reserva } from '../../core/api/models';
import { EstadoBadge } from '../../shared/estado-badge';
import { Estrellas } from '../../shared/estrellas';
import { legible } from '../../shared/etiquetas';
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
  selector: 'app-mis-reservas',
  imports: [CurrencyPipe, DatePipe, FormsModule, EstadoBadge, Estrellas],
  template: `
    <div class="encabezado-seccion">
      <h1>Mis reservas</h1>
      <button type="button" class="plano" (click)="reservas.reload()">Actualizar</button>
    </div>

    @if (mensaje(); as texto) {
      <p class="aviso ok">{{ texto }}</p>
    }
    @if (error(); as texto) {
      <p class="aviso mal">{{ texto }}</p>
    }

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
                  {{ r.fechaHoraInicio | date: 'medium' }} → {{ r.fechaHoraFin | date: 'shortTime' }} ·
                  {{ r.cantidadPersonas }} personas
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
                @if (r.montoReembolso) {
                  Reembolso de {{ r.montoReembolso | currency: 'COP' : 'symbol-narrow' : '1.0-0' }}
                  @if (r.fechaLimiteReembolso) {
                    hasta el {{ r.fechaLimiteReembolso | date: 'mediumDate' }}
                  }
                } @else {
                  Sin reembolso.
                }
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
            } @else if (vigente(r)) {
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
                    <input name="referencia" [ngModel]="referencia()" (ngModelChange)="referencia.set($event)" />
                  </label>
                  <div class="fila">
                    <button type="submit" class="primario" [disabled]="trabajando()">Pagar</button>
                    <button type="button" class="plano" (click)="pagando.set(null)">Cancelar</button>
                  </div>
                </form>
              } @else {
                <div class="fila">
                  <button type="button" (click)="abrirPago(r)">Registrar pago</button>
                </div>
              }
            }

            <!-- Reseña -->
            @if (r.resena; as resena) {
              <div class="fila">
                <app-estrellas [valor]="resena.calificacion" [mostrarValor]="false" />
                <span class="pequeno suave">{{ resena.comentario }}</span>
              </div>
            } @else if (r.estado === 'FINALIZADA') {
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
                    <button type="submit" class="primario" [disabled]="trabajando()">Publicar reseña</button>
                    <button type="button" class="plano" (click)="resenando.set(null)">Cancelar</button>
                  </div>
                </form>
              } @else {
                <div class="fila">
                  <button type="button" (click)="abrirResena(r)">Dejar reseña</button>
                </div>
              }
            }

            @if (vigente(r)) {
              <div class="fila">
                <button type="button" class="peligro" [disabled]="trabajando()" (click)="cancelar(r)">
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
  private readonly reservaApi = inject(ReservaApi);
  private readonly pagoApi = inject(PagoApi);
  private readonly resenaApi = inject(ResenaApi);

  protected readonly legible = legible;
  protected readonly metodos = METODOS;

  protected readonly mensaje = signal('');
  protected readonly error = signal('');
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
    this.limpiarAvisos();
  }

  protected abrirResena(r: Reserva): void {
    this.resenando.set(r.idReserva ?? null);
    this.calificacion.set(5);
    this.comentario.set('');
    this.limpiarAvisos();
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
    this.ejecutar(
      this.resenaApi.crud.crear({
        idReserva: r.idReserva!,
        calificacion: Number(this.calificacion()),
        comentario: this.comentario().trim() || undefined,
      }),
      () => this.resenando.set(null),
    );
  }

  protected cancelar(r: ReservaDetallada): void {
    if (this.trabajando() || !confirm('¿Cancelar esta reserva? El reembolso depende de la política del motel.')) {
      return;
    }
    this.ejecutar(this.reservaApi.cancelar(r.idReserva!));
  }

  private ejecutar(peticion: Observable<string>, alTerminar?: () => void): void {
    this.trabajando.set(true);
    this.limpiarAvisos();

    peticion.subscribe({
      next: (respuesta) => {
        this.trabajando.set(false);
        this.mensaje.set(respuesta);
        alTerminar?.();
        this.reservas.reload();
      },
      error: (e: Error) => {
        this.trabajando.set(false);
        this.error.set(e.message);
      },
    });
  }

  private limpiarAvisos(): void {
    this.mensaje.set('');
    this.error.set('');
  }
}
