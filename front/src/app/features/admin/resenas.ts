import { Component, inject } from '@angular/core';
import { ResenaApi } from '../../core/api/entidades';
import type { Resena } from '../../core/api/models';
import { type Campo, type Registro } from '../../shared/tabla-crud';

const CALIFICACIONES = [1, 2, 3, 4, 5].map((n) => ({ valor: n, texto: '★'.repeat(n) }));

@Component({
  standalone: false,
  selector: 'app-resenas-admin',
  template: `
    <h1>Reseñas</h1>
    <p class="suave">
      Todas las reseñas del sistema. Al eliminar una reseña también se elimina la respuesta del
      propietario.
    </p>

    <app-tabla-crud
      titulo="Reseñas"
      descripcion="Solo se puede reseñar una reserva finalizada que aún no tenga reseña."
      claveId="idResena"
      sinDatos="No hay reseñas registradas."
      [campos]="campos"
      [cargar]="cargar"
      [guardar]="guardar"
      [borrar]="borrar"
    />
  `,
})
export class ResenasAdmin {
  private readonly api = inject(ResenaApi);

  protected readonly campos: Campo[] = [
    { clave: 'idResena', etiqueta: 'ID', soloLectura: true },
    { clave: 'idReserva', etiqueta: 'Reserva', tipo: 'numero', requerido: true },
    {
      clave: 'calificacion',
      etiqueta: 'Calificación',
      tipo: 'select',
      opciones: CALIFICACIONES,
      requerido: true,
    },
    { clave: 'comentario', etiqueta: 'Comentario', tipo: 'area' },
    { clave: 'fecha', etiqueta: 'Fecha', tipo: 'fechaHora', soloLectura: true },
  ];

  protected readonly cargar = () => this.api.crud.getAll();
  protected readonly guardar = (id: number | string | null, dto: Registro) =>
    id === null
      ? this.api.crud.crear(dto as unknown as Resena)
      : this.api.crud.actualizar(id, dto as unknown as Resena);
  protected readonly borrar = (id: number | string) => this.api.crud.eliminar(id);
}
