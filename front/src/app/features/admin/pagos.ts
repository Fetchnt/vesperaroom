import { Component, computed, inject } from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';
import { PagoApi, ReservaApi } from '../../core/api/entidades';
import type { EstadoPago } from '../../core/api/models';
import { lista } from '../../shared/recursos';
import { type AccionFila, type Campo, type Registro } from '../../shared/tabla-crud';

const ACCIONES: { estado: EstadoPago; texto: string; peligro?: boolean }[] = [
  { estado: 'APROBADO', texto: 'Aprobar' },
  { estado: 'RECHAZADO', texto: 'Rechazar', peligro: true },
];

@Component({
  standalone: false,
  selector: 'app-pagos-admin',
  template: `
    <h1>Pagos</h1>
    <p class="suave">Pagos de todas las reservas. Un pago aprobado o rechazado ya no cambia de estado.</p>

    <app-tabla-crud
      titulo="Pagos"
      claveId="idPago"
      sinDatos="No hay pagos registrados."
      [campos]="campos()"
      [cargar]="cargar"
      [acciones]="acciones"
    />
  `,
})
export class PagosAdmin {
  private readonly api = inject(PagoApi);

  private readonly reservaApi = inject(ReservaApi);
  private readonly reservas = rxResource({ stream: () => this.reservaApi.crud.getAll(), defaultValue: [] });

  /** La reserva se muestra por su código; mientras llega la lista se ve el id. */
  protected readonly campos = computed<Campo[]>(() => [
    { clave: 'idPago', etiqueta: 'ID' },
    {
      clave: 'idReserva',
      etiqueta: 'Reserva',
      tipo: 'select',
      opciones: lista(this.reservas).map((r) => ({ valor: r.idReserva!, texto: r.codigoReserva! })),
    },
    { clave: 'monto', etiqueta: 'Monto', tipo: 'decimal' },
    { clave: 'metodoPago', etiqueta: 'Método' },
    { clave: 'fechaPago', etiqueta: 'Fecha', tipo: 'fechaHora' },
    { clave: 'referenciaTransaccion', etiqueta: 'Referencia' },
    { clave: 'estado', etiqueta: 'Estado', esEstado: true },
  ]);

  protected readonly cargar = () => this.api.crud.getAll();

  protected readonly acciones: AccionFila[] = ACCIONES.map(({ estado, texto, peligro }) => ({
    texto,
    peligro,
    visible: (fila: Registro) => fila['estado'] === 'PENDIENTE',
    ejecutar: (fila: Registro) => this.api.cambiarEstado(fila['idPago'] as number, estado),
  }));
}
