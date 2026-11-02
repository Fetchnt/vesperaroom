import { Component, inject } from '@angular/core';
import { PagoApi } from '../../core/api/entidades';
import type { EstadoPago } from '../../core/api/models';
import { type AccionFila, type Campo, type Registro } from '../../shared/tabla-crud';

const ACCIONES: { estado: EstadoPago; texto: string; peligro?: boolean }[] = [
  { estado: 'APROBADO', texto: 'Aprobar' },
  { estado: 'RECHAZADO', texto: 'Rechazar', peligro: true },
  { estado: 'REEMBOLSADO', texto: 'Reembolsar' },
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
      [campos]="campos"
      [cargar]="cargar"
      [acciones]="acciones"
    />
  `,
})
export class PagosAdmin {
  private readonly api = inject(PagoApi);

  protected readonly campos: Campo[] = [
    { clave: 'idPago', etiqueta: 'ID' },
    { clave: 'idReserva', etiqueta: 'Reserva' },
    { clave: 'monto', etiqueta: 'Monto', tipo: 'decimal' },
    { clave: 'metodoPago', etiqueta: 'Método' },
    { clave: 'fechaPago', etiqueta: 'Fecha' },
    { clave: 'referenciaTransaccion', etiqueta: 'Referencia' },
    { clave: 'estado', etiqueta: 'Estado', esEstado: true },
  ];

  protected readonly cargar = () => this.api.crud.getAll();

  protected readonly acciones: AccionFila[] = ACCIONES.map(({ estado, texto, peligro }) => ({
    texto,
    peligro,
    visible: (fila: Registro) => fila['estado'] === 'PENDIENTE',
    ejecutar: (fila: Registro) => this.api.cambiarEstado(fila['idPago'] as number, estado),
  }));
}
