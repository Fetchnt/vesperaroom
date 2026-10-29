import { Component, computed, inject, signal } from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';
import { CaracteristicaApi, CiudadApi, DepartamentoApi, ModalidadApi } from '../../core/api/entidades';
import type { Caracteristica, Ciudad, Departamento, Modalidad } from '../../core/api/models';
import { TablaCrud, type Campo, type Registro } from '../../shared/tabla-crud';
import { lista } from '../../shared/recursos';

type Catalogo = 'departamentos' | 'ciudades' | 'caracteristicas' | 'modalidades';

const PESTANAS: { clave: Catalogo; texto: string }[] = [
  { clave: 'departamentos', texto: 'Departamentos' },
  { clave: 'ciudades', texto: 'Ciudades' },
  { clave: 'caracteristicas', texto: 'Características' },
  { clave: 'modalidades', texto: 'Modalidades' },
];

@Component({
  selector: 'app-catalogos',
  imports: [TablaCrud],
  template: `
    <h1>Catálogos</h1>
    <p class="suave">
      Tablas base del modelo: alimentan los selectores de moteles, tarifas y tipos de habitación.
    </p>

    <nav class="pestanas">
      @for (p of pestanas; track p.clave) {
        <button type="button" [class.activa]="actual() === p.clave" (click)="actual.set(p.clave)">
          {{ p.texto }}
        </button>
      }
    </nav>

    @switch (actual()) {
      @case ('departamentos') {
        <app-tabla-crud
          titulo="Departamentos"
          claveId="idDepartamento"
          [campos]="camposDepartamento"
          [cargar]="cargarDepartamentos"
          [guardar]="guardarDepartamento"
          [borrar]="borrarDepartamento"
        />
      }
      @case ('ciudades') {
        <app-tabla-crud
          titulo="Ciudades"
          claveId="idCiudad"
          [campos]="camposCiudad()"
          [cargar]="cargarCiudades"
          [guardar]="guardarCiudad"
          [borrar]="borrarCiudad"
        />
      }
      @case ('caracteristicas') {
        <app-tabla-crud
          titulo="Características"
          descripcion="El icono es un nombre libre que el front puede usar para representarla."
          claveId="idCaracteristica"
          [campos]="camposCaracteristica"
          [cargar]="cargarCaracteristicas"
          [guardar]="guardarCaracteristica"
          [borrar]="borrarCaracteristica"
        />
      }
      @case ('modalidades') {
        <app-tabla-crud
          titulo="Modalidades"
          descripcion="La duración en horas define la hora de salida de cada reserva."
          claveId="idModalidad"
          [campos]="camposModalidad"
          [cargar]="cargarModalidades"
          [guardar]="guardarModalidad"
          [borrar]="borrarModalidad"
        />
      }
    }
  `,
  styles: `
    .pestanas {
      display: flex;
      flex-wrap: wrap;
      gap: 4px;
      margin-bottom: 24px;
      border-bottom: 1px solid var(--borde);
      padding-bottom: 8px;
    }
    .pestanas button {
      background: transparent;
      border-color: transparent;
    }
    .pestanas button.activa {
      background: var(--marca-tenue);
      border-color: var(--marca);
      color: var(--marca-fuerte);
    }
  `,
})
export class Catalogos {
  protected readonly lista = lista;
  private readonly departamentoApi = inject(DepartamentoApi);
  private readonly ciudadApi = inject(CiudadApi);
  private readonly caracteristicaApi = inject(CaracteristicaApi);
  private readonly modalidadApi = inject(ModalidadApi);

  protected readonly pestanas = PESTANAS;
  protected readonly actual = signal<Catalogo>('departamentos');

  private readonly departamentos = rxResource({
    stream: () => this.departamentoApi.crud.getAll(),
    defaultValue: [],
  });

  protected readonly camposDepartamento: Campo[] = [
    { clave: 'idDepartamento', etiqueta: 'ID', soloLectura: true },
    { clave: 'nombre', etiqueta: 'Nombre', requerido: true },
  ];

  protected readonly cargarDepartamentos = () => this.departamentoApi.crud.getAll();
  protected readonly guardarDepartamento = (id: number | string | null, dto: Registro) =>
    id === null
      ? this.departamentoApi.crud.crear(dto as unknown as Departamento)
      : this.departamentoApi.crud.actualizar(id, dto as unknown as Departamento);
  protected readonly borrarDepartamento = (id: number | string) => this.departamentoApi.crud.eliminar(id);

  protected readonly camposCiudad = computed<Campo[]>(() => [
    { clave: 'idCiudad', etiqueta: 'ID', soloLectura: true },
    { clave: 'nombre', etiqueta: 'Nombre', requerido: true },
    {
      clave: 'idDepartamento',
      etiqueta: 'Departamento',
      tipo: 'select',
      requerido: true,
      opciones: lista(this.departamentos).map((d) => ({ valor: d.idDepartamento!, texto: d.nombre })),
    },
  ]);

  protected readonly cargarCiudades = () => this.ciudadApi.crud.getAll();
  protected readonly guardarCiudad = (id: number | string | null, dto: Registro) =>
    id === null
      ? this.ciudadApi.crud.crear(dto as unknown as Ciudad)
      : this.ciudadApi.crud.actualizar(id, dto as unknown as Ciudad);
  protected readonly borrarCiudad = (id: number | string) => this.ciudadApi.crud.eliminar(id);

  protected readonly camposCaracteristica: Campo[] = [
    { clave: 'idCaracteristica', etiqueta: 'ID', soloLectura: true },
    { clave: 'nombre', etiqueta: 'Nombre', requerido: true },
    { clave: 'icono', etiqueta: 'Icono' },
  ];

  protected readonly cargarCaracteristicas = () => this.caracteristicaApi.crud.getAll();
  protected readonly guardarCaracteristica = (id: number | string | null, dto: Registro) =>
    id === null
      ? this.caracteristicaApi.crud.crear(dto as unknown as Caracteristica)
      : this.caracteristicaApi.crud.actualizar(id, dto as unknown as Caracteristica);
  protected readonly borrarCaracteristica = (id: number | string) => this.caracteristicaApi.crud.eliminar(id);

  protected readonly camposModalidad: Campo[] = [
    { clave: 'idModalidad', etiqueta: 'ID', soloLectura: true },
    { clave: 'nombre', etiqueta: 'Nombre', requerido: true },
    { clave: 'duracionHoras', etiqueta: 'Duración (h)', tipo: 'numero', requerido: true },
  ];

  protected readonly cargarModalidades = () => this.modalidadApi.crud.getAll();
  protected readonly guardarModalidad = (id: number | string | null, dto: Registro) =>
    id === null
      ? this.modalidadApi.crud.crear(dto as unknown as Modalidad)
      : this.modalidadApi.crud.actualizar(id, dto as unknown as Modalidad);
  protected readonly borrarModalidad = (id: number | string) => this.modalidadApi.crud.eliminar(id);
}
