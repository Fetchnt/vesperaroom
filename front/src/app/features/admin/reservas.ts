import { Component, computed, inject } from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';
import { HabitacionApi, MotelApi, ReservaApi, UsuarioApi } from '../../core/api/entidades';
import type { EstadoReserva } from '../../core/api/models';
import { lista } from '../../shared/recursos';
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
      [campos]="campos()"
      [cargar]="cargar"
      [borrar]="borrar"
      [acciones]="acciones"
    />
  `,
})
export class ReservasAdmin {
  private readonly api = inject(ReservaApi);

  private readonly usuarioApi = inject(UsuarioApi);
  private readonly habitacionApi = inject(HabitacionApi);
  private readonly motelApi = inject(MotelApi);
  private readonly usuarios = rxResource({ stream: () => this.usuarioApi.crud.getAll(), defaultValue: [] });
  private readonly habitaciones = rxResource({ stream: () => this.habitacionApi.crud.getAll(), defaultValue: [] });
  private readonly moteles = rxResource({ stream: () => this.motelApi.todos(), defaultValue: [] });

  /** Cliente y habitación con nombre; mientras llegan las listas se ve el id. */
  protected readonly campos = computed<Campo[]>(() => {
    const motel = new Map(lista(this.moteles).map((m) => [m.idMotel, m.nombre]));
    return [
      { clave: 'codigoReserva', etiqueta: 'Código' },
      {
        clave: 'idUsuario',
        etiqueta: 'Cliente',
        tipo: 'select',
        opciones: lista(this.usuarios).map((u) => ({ valor: u.idUsuario!, texto: `${u.nombre} ${u.apellido}` })),
      },
      {
        clave: 'idHabitacion',
        etiqueta: 'Habitación',
        tipo: 'select',
        opciones: lista(this.habitaciones).map((h) => ({
          valor: h.idHabitacion!,
          texto: `${h.numero} · ${motel.get(h.idMotel) ?? 'Motel ' + h.idMotel}`,
        })),
      },
      { clave: 'fechaHoraInicio', etiqueta: 'Entrada', tipo: 'fechaHora' },
      { clave: 'fechaHoraFin', etiqueta: 'Salida', tipo: 'fechaHora' },
      { clave: 'cantidadPersonas', etiqueta: 'Personas', tipo: 'numero' },
      { clave: 'total', etiqueta: 'Total', tipo: 'decimal' },
      { clave: 'montoReembolso', etiqueta: 'Reembolso', tipo: 'decimal' },
      { clave: 'estado', etiqueta: 'Estado', esEstado: true },
    ];
  });

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
