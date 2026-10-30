import { Component, computed, inject } from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';
import { CiudadApi, MotelApi, UsuarioApi } from '../../core/api/entidades';
import type { Motel } from '../../core/api/models';
import { TablaCrud, type AccionFila, type Campo, type Registro } from '../../shared/tabla-crud';
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
      [guardar]="guardar"
      [borrar]="borrar"
      [acciones]="acciones"
      [enlace]="gestionar"
      textoEnlace="Gestionar"
    />
  `,
})
export class MotelesAdmin {
  protected readonly lista = lista;
  private readonly api = inject(MotelApi);
  private readonly ciudadApi = inject(CiudadApi);
  private readonly usuarioApi = inject(UsuarioApi);

  private readonly ciudades = rxResource({
    stream: () => this.ciudadApi.crud.getAll(),
    defaultValue: [],
  });

  private readonly propietarios = rxResource({
    stream: () => this.usuarioApi.propietarios(),
    defaultValue: [],
  });

  /** Los mismos campos que el propietario edita en su panel, más quién es el dueño. */
  protected readonly campos = computed<Campo[]>(() => [
    { clave: 'idMotel', etiqueta: 'ID', soloLectura: true },
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
    {
      clave: 'idPropietario',
      etiqueta: 'Propietario',
      tipo: 'select',
      requerido: true,
      opciones: lista(this.propietarios).map((u) => ({
        valor: u.idUsuario!,
        texto: `${u.nombre} ${u.apellido}`,
      })),
    },
    { clave: 'descripcion', etiqueta: 'Descripción', tipo: 'area', oculto: true },
    { clave: 'latitud', etiqueta: 'Latitud', tipo: 'decimal', oculto: true },
    { clave: 'longitud', etiqueta: 'Longitud', tipo: 'decimal', oculto: true },
    { clave: 'estado', etiqueta: 'Estado', esEstado: true, soloLectura: true },
  ]);

  protected readonly cargar = () => this.api.todos();
  protected readonly guardar = (id: number | string | null, dto: Registro) =>
    id === null
      ? this.api.crud.crear(dto as unknown as Motel)
      : this.api.crud.actualizar(id, dto as unknown as Motel);
  protected readonly borrar = (id: number | string) => this.api.crud.eliminar(id);
  /** Política, tipos, habitaciones, tarifas, servicios y fotos de cualquier motel. */
  protected readonly gestionar = (fila: Registro) => ['/admin/moteles', fila['idMotel'] as number];

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
