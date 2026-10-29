import { Component, computed, inject } from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';
import { CiudadApi, MotelApi } from '../../core/api/entidades';
import type { Motel } from '../../core/api/models';
import { TablaCrud, type AccionFila, type Campo, type Registro } from '../../shared/tabla-crud';
import { lista } from '../../shared/recursos';

@Component({
  selector: 'app-mis-moteles',
  imports: [TablaCrud],
  template: `
    <h1>Mis moteles</h1>
    <p class="suave">
      Abre un motel para administrar su política, tipos de habitación, habitaciones, tarifas,
      servicios y fotos.
    </p>

    <app-tabla-crud
      titulo="Moteles"
      claveId="idMotel"
      textoEnlace="Gestionar"
      sinDatos="Todavía no has publicado ningún motel."
      [campos]="campos()"
      [cargar]="cargar"
      [guardar]="guardar"
      [borrar]="borrar"
      [enlace]="enlace"
      [acciones]="acciones"
    />
  `,
})
export class MisMoteles {
  protected readonly lista = lista;
  private readonly motelApi = inject(MotelApi);
  private readonly ciudadApi = inject(CiudadApi);

  private readonly ciudades = rxResource({
    stream: () => this.ciudadApi.crud.getAll(),
    defaultValue: [],
  });

  protected readonly campos = computed<Campo[]>(() => [
    { clave: 'nombre', etiqueta: 'Nombre', requerido: true },
    {
      clave: 'idCiudad',
      etiqueta: 'Ciudad',
      tipo: 'select',
      requerido: true,
      opciones: lista(this.ciudades).map((c) => ({ valor: c.idCiudad!, texto: c.nombre })),
    },
    { clave: 'calle', etiqueta: 'Calle', requerido: true },
    { clave: 'carrera', etiqueta: 'Carrera', requerido: true },
    { clave: 'descripcion', etiqueta: 'Descripción', tipo: 'area', oculto: true },
    { clave: 'latitud', etiqueta: 'Latitud', tipo: 'decimal', oculto: true },
    { clave: 'longitud', etiqueta: 'Longitud', tipo: 'decimal', oculto: true },
    { clave: 'estado', etiqueta: 'Estado', esEstado: true, soloLectura: true },
  ]);

  protected readonly cargar = () => this.motelApi.mios();

  protected readonly guardar = (id: number | string | null, dto: Registro) =>
    id === null
      ? this.motelApi.crud.crear(dto as unknown as Motel)
      : this.motelApi.crud.actualizar(id, dto as unknown as Motel);

  protected readonly borrar = (id: number | string) => this.motelApi.crud.eliminar(id);

  protected readonly enlace = (fila: Registro) => ['/panel/moteles', fila['idMotel'] as number];

  /** El estado del motel no se cambia por updatejson, sino por /motel/updateestado. */
  protected readonly acciones: AccionFila[] = [
    {
      texto: (fila) => (fila['estado'] === 'ACTIVO' ? 'Inactivar' : 'Activar'),
      ejecutar: (fila) =>
        this.motelApi.cambiarEstado(
          fila['idMotel'] as number,
          fila['estado'] === 'ACTIVO' ? 'INACTIVO' : 'ACTIVO',
        ),
    },
  ];
}
