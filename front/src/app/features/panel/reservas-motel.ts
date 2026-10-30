import { Component, computed, inject, linkedSignal } from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { MotelApi, ReservaApi } from '../../core/api/entidades';
import type { EstadoReserva } from '../../core/api/models';
import { TablaCrud, type AccionFila, type Campo, type Registro } from '../../shared/tabla-crud';
import { lista } from '../../shared/recursos';

/**
 * Transiciones que admite el backend (ReservaService.cambiarEstado). Pasar a
 * CANCELADA desde aquí cancela con reembolso del 100 %.
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
  { estado: 'NO_SHOW', texto: 'No se presentó' },
  { estado: 'CANCELADA', texto: 'Cancelar' },
];

@Component({
  selector: 'app-reservas-motel',
  imports: [FormsModule, TablaCrud],
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
          [campos]="campos"
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

  protected readonly campos: Campo[] = [
    { clave: 'codigoReserva', etiqueta: 'Código' },
    { clave: 'fechaHoraInicio', etiqueta: 'Entrada' },
    { clave: 'fechaHoraFin', etiqueta: 'Salida' },
    { clave: 'cantidadPersonas', etiqueta: 'Personas' },
    { clave: 'total', etiqueta: 'Total', tipo: 'decimal' },
    { clave: 'estado', etiqueta: 'Estado', esEstado: true },
  ];

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
        ? '¿Cancelar esta reserva? Al cliente se le reembolsa el 100 % de lo pagado.'
        : undefined,
    ejecutar: (fila: Registro) =>
      this.reservaApi.cambiarEstado(fila['idReserva'] as number, estado),
  }));
}
