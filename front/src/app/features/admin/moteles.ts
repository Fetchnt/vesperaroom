import { Component, computed, inject } from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';
import { CiudadApi, MotelApi } from '../../core/api/entidades';
import { TablaCrud, type AccionFila, type Campo } from '../../shared/tabla-crud';
import { lista } from '../../shared/recursos';

@Component({
  selector: 'app-moteles-admin',
  imports: [TablaCrud],
  template: `
    <h1>Moteles</h1>
    <p class="suave">Incluye los moteles inactivos, que no aparecen en el catálogo público.</p>

    <app-tabla-crud
      titulo="Todos los moteles"
      claveId="idMotel"
      sinDatos="No hay moteles registrados."
      [campos]="campos()"
      [cargar]="cargar"
      [borrar]="borrar"
      [acciones]="acciones"
    />
  `,
})
export class MotelesAdmin {
  protected readonly lista = lista;
  private readonly api = inject(MotelApi);
  private readonly ciudadApi = inject(CiudadApi);

  private readonly ciudades = rxResource({
    stream: () => this.ciudadApi.crud.getAll(),
    defaultValue: [],
  });

  protected readonly campos = computed<Campo[]>(() => [
    { clave: 'idMotel', etiqueta: 'ID' },
    { clave: 'nombre', etiqueta: 'Nombre' },
    {
      clave: 'idCiudad',
      etiqueta: 'Ciudad',
      tipo: 'select',
      opciones: lista(this.ciudades).map((c) => ({ valor: c.idCiudad!, texto: c.nombre })),
    },
    { clave: 'calle', etiqueta: 'Calle' },
    { clave: 'carrera', etiqueta: 'Carrera' },
    { clave: 'idPropietario', etiqueta: 'Propietario' },
    { clave: 'estado', etiqueta: 'Estado', esEstado: true },
  ]);

  protected readonly cargar = () => this.api.todos();
  protected readonly borrar = (id: number | string) => this.api.crud.eliminar(id);

  protected readonly acciones: AccionFila[] = [
    {
      texto: (fila) => (fila['estado'] === 'ACTIVO' ? 'Inactivar' : 'Activar'),
      ejecutar: (fila) =>
        this.api.cambiarEstado(
          fila['idMotel'] as number,
          fila['estado'] === 'ACTIVO' ? 'INACTIVO' : 'ACTIVO',
        ),
    },
  ];
}
