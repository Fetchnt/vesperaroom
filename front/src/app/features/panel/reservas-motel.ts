import { Component, computed, inject, linkedSignal } from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';
import { HabitacionApi, MotelApi, ReservaApi } from '../../core/api/entidades';
import type { EstadoReserva } from '../../core/api/models';
import { type AccionFila, type Campo, type Registro } from '../../shared/tabla-crud';
import { lista } from '../../shared/recursos';

/**
 * Transiciones que admite el backend (ReservaService.cambiarEstado).
 */
const SIGUIENTES: Partial<Record<EstadoReserva, EstadoReserva[]>> = {
  PENDIENTE: ['CONFIRMADA', 'CANCELADA'],
  CONFIRMADA: ['EN_CURSO', 'NO_SHOW', 'CANCELADA'],
  EN_CURSO: ['FINALIZADA'],
};

const ACCIONES: { estado: EstadoReserva; texto: string }[] = [
  { estado: 'CONFIRMADA', texto: 'Confirmar' },
  { estado: 'EN_CURSO', texto: 'Marcar en curso' },
  { estado: 'FINALIZADA', texto: 'Finalizar' },
  { estado: 'NO_SHOW', texto: 'Marcar inasistencia' },
  { estado: 'CANCELADA', texto: 'Cancelar' },
];

@Component({
  standalone: false,
  selector: 'app-reservas-motel',
  template: `
    <h1>Reservas de mis moteles</h1>

    @if (moteles.isLoading()) {
      <p class="suave">Cargando…</p>
    } @else if (lista(moteles).length === 0) {
      <p class="vacio">Primero publica un motel.</p>
    } @else {
      <form class="tarjeta formulario" style="margin-bottom:24px">
        <label class="campo">
          <span>Motel</span>
          <select name="motel" [ngModel]="motel()" (ngModelChange)="motel.set($event)">
            @for (m of lista(moteles); track m.idMotel) {
              <option [ngValue]="m.idMotel">{{ m.nombre }}</option>
            }
          </select>
        </label>
      </form>

      @if (motel(); as id) {
        <app-tabla-crud
          titulo="Reservas"
          claveId="idReserva"
          sinDatos="Este motel no tiene reservas."
          [campos]="campos()"
          [cargar]="cargar()"
          [acciones]="acciones"
        />
      }
    }
  `,
})
export class ReservasMotel {
  protected readonly lista = lista;
  private readonly motelApi = inject(MotelApi);
  private readonly reservaApi = inject(ReservaApi);

  protected readonly moteles = rxResource({
    stream: () => this.motelApi.mios(),
    defaultValue: [],
  });

  /** Se preselecciona el primer motel en cuanto llega la lista. */
  protected readonly motel = linkedSignal<number | null>(
    () => lista(this.moteles)[0]?.idMotel ?? null,
  );

  private readonly habitacionApi = inject(HabitacionApi);
  private readonly habitaciones = rxResource({
    params: () => this.motel() ?? undefined,
    stream: ({ params }) => this.habitacionApi.porMotel(params),
    defaultValue: [],
  });

  protected readonly campos = computed<Campo[]>(() => [
    { clave: 'codigoReserva', etiqueta: 'Código' },
    {
      clave: 'idHabitacion',
      etiqueta: 'Habitación',
      tipo: 'select',
      opciones: lista(this.habitaciones).map((h) => ({ valor: h.idHabitacion!, texto: `Hab. ${h.numero}` })),
    },
    { clave: 'fechaHoraInicio', etiqueta: 'Entrada', tipo: 'fechaHora' },
    { clave: 'fechaHoraFin', etiqueta: 'Salida', tipo: 'fechaHora' },
    { clave: 'cantidadPersonas', etiqueta: 'Personas', tipo: 'numero' },
    { clave: 'total', etiqueta: 'Total', tipo: 'decimal' },
    { clave: 'estado', etiqueta: 'Estado', esEstado: true },
  ]);

  /** La función se recrea al cambiar de motel, y eso hace recargar la tabla. */
  protected readonly cargar = computed(() => {
    const id = this.motel();
    return () => this.reservaApi.porMotel(id!);
  });

  protected readonly acciones: AccionFila[] = ACCIONES.map(({ estado, texto }) => ({
    texto,
    visible: (fila: Registro) =>
      (SIGUIENTES[fila['estado'] as EstadoReserva] ?? []).includes(estado),
    peligro: estado === 'NO_SHOW' || estado === 'CANCELADA',
    confirmar:
      estado === 'CANCELADA'
        ? '¿Cancelar esta reserva? Esta acción no se puede deshacer.'
        : undefined,
    ejecutar: (fila: Registro) =>
      this.reservaApi.cambiarEstado(fila['idReserva'] as number, estado),
  }));
}
