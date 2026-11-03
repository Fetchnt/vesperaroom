import { Component, inject } from '@angular/core';
import { ReservaApi } from '../../core/api/entidades';
import type { EstadoReserva } from '../../core/api/models';
import { type AccionFila, type Campo, type Registro } from '../../shared/tabla-crud';

/**
 * Mismas transiciones que valida ReservaService.cambiarEstado. Pasar a
 * CANCELADA desde aquí cancela con reembolso del 100 %.
 */
const SIGUIENTES: Partial<Record<EstadoReserva, EstadoReserva[]>> = {
  PENDIENTE: ['CONFIRMADA', 'CANCELADA'],
  CONFIRMADA: ['EN_CURSO', 'NO_SHOW', 'CANCELADA'],
  EN_CURSO: ['FINALIZADA'],
};

const ACCIONES: { estado: EstadoReserva; texto: string }[] = [
  { estado: 'CONFIRMADA', texto: 'Confirmar' },
  { estado: 'EN_CURSO', texto: 'En curso' },
  { estado: 'FINALIZADA', texto: 'Finalizar' },
  { estado: 'NO_SHOW', texto: 'No se presentó' },
  { estado: 'CANCELADA', texto: 'Cancelar' },
];

@Component({
  standalone: false,
  selector: 'app-reservas-admin',
  template: `
    <h1>Reservas</h1>
    <p class="suave">
      Todas las reservas del sistema, con su estado y su reembolso si fueron canceladas.
    </p>

    <app-tabla-crud
      titulo="Reservas"
      claveId="idReserva"
      sinDatos="No hay reservas registradas."
      [campos]="campos"
      [cargar]="cargar"
      [borrar]="borrar"
      [acciones]="acciones"
    />
  `,
})
export class ReservasAdmin {
  private readonly api = inject(ReservaApi);

  protected readonly campos: Campo[] = [
    { clave: 'codigoReserva', etiqueta: 'Código' },
    { clave: 'idUsuario', etiqueta: 'Cliente' },
    { clave: 'idHabitacion', etiqueta: 'Habitación' },
    { clave: 'fechaHoraInicio', etiqueta: 'Entrada', tipo: 'fechaHora' },
    { clave: 'fechaHoraFin', etiqueta: 'Salida', tipo: 'fechaHora' },
    { clave: 'cantidadPersonas', etiqueta: 'Personas' },
    { clave: 'total', etiqueta: 'Total', tipo: 'decimal' },
    { clave: 'montoReembolso', etiqueta: 'Reembolso', tipo: 'decimal' },
    { clave: 'estado', etiqueta: 'Estado', esEstado: true },
  ];

  protected readonly cargar = () => this.api.crud.getAll();
  protected readonly borrar = (id: number | string) => this.api.crud.eliminar(id);

  protected readonly acciones: AccionFila[] = ACCIONES.map(({ estado, texto }) => ({
    texto,
    visible: (fila: Registro) =>
      (SIGUIENTES[fila['estado'] as EstadoReserva] ?? []).includes(estado),
    peligro: estado === 'NO_SHOW' || estado === 'CANCELADA',
    confirmar:
      estado === 'CANCELADA'
        ? '¿Cancelar esta reserva? Al cliente se le reembolsa el 100 % de lo pagado.'
        : undefined,
    ejecutar: (fila: Registro) => this.api.cambiarEstado(fila['idReserva'] as number, estado),
  }));
}
