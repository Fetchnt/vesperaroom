import { Component, computed, inject, input, linkedSignal, signal } from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';
import { forkJoin, map, of, switchMap } from 'rxjs';
import { CaracteristicaApi, FotoApi, HabitacionApi, ModalidadApi, MotelApi, PoliticaApi, ServicioApi, TarifaApi, TipoHabitacionApi } from '../../core/api/entidades';
import type {
  EstadoHabitacion,
  Foto,
  Habitacion,
  PoliticaReserva,
  ServicioAdicional,
  Tarifa,
  TipoDia,
  TipoHabitacion,
} from '../../core/api/models';
import { SessionStore } from '../../core/auth/session';
import { type AccionFila, type Campo, type Registro } from '../../shared/tabla-crud';
import { lista, valorDe } from '../../shared/recursos';

type Seccion =
  'politica' | 'tipos' | 'caracteristicas' | 'habitaciones' | 'tarifas' | 'servicios' | 'fotos';

const SECCIONES: { clave: Seccion; texto: string }[] = [
  { clave: 'politica', texto: 'Política' },
  { clave: 'tipos', texto: 'Tipos de habitación' },
  { clave: 'caracteristicas', texto: 'Características' },
  { clave: 'habitaciones', texto: 'Habitaciones' },
  { clave: 'tarifas', texto: 'Tarifas' },
  { clave: 'servicios', texto: 'Servicios' },
  { clave: 'fotos', texto: 'Fotos' },
];

const TIPOS_DIA: TipoDia[] = ['TODOS', 'ENTRE_SEMANA', 'FIN_DE_SEMANA', 'FESTIVO'];
const ESTADOS_HABITACION: EstadoHabitacion[] = ['ACTIVA', 'MANTENIMIENTO', 'FUERA_DE_SERVICIO'];

@Component({
  standalone: false,
  selector: 'app-motel-gestion',
  template: `
    <div class="encabezado-seccion">
      <div>
        <h1>{{ valorDe(motel, null)?.nombre ?? 'Motel' }}</h1>
        <p class="suave pequeno">
          @if (esAdmin()) {
            <a routerLink="/admin/moteles">← Moteles</a>
          } @else {
            <a routerLink="/panel/moteles">← Mis moteles</a>
          }
        </p>
      </div>
    </div>

    <nav class="pestanas">
      @for (s of secciones; track s.clave) {
        <button type="button" [class.activa]="seccion() === s.clave" (click)="seccion.set(s.clave)">
          {{ s.texto }}
        </button>
      }
    </nav>

    @switch (seccion()) {
      @case ('politica') {
        <app-tabla-crud
          titulo="Política de reserva"
          descripcion="Un motel tiene una sola política: define la anticipación mínima, el recargo por persona adicional y el reembolso por cancelación."
          claveId="idMotel"
          sinDatos="Este motel aún no tiene política. Agrégala para poder recibir reservas."
          [campos]="camposPolitica"
          [cargar]="cargarPolitica()"
          [guardar]="guardarPolitica"
          [borrar]="borrarPolitica"
          [porDefecto]="porDefecto()"
        />
      }

      @case ('tipos') {
        <app-tabla-crud
          titulo="Tipos de habitación"
          claveId="idTipo"
          [campos]="camposTipo"
          [cargar]="cargarTipos()"
          [guardar]="guardarTipo"
          [borrar]="borrarTipo"
          [porDefecto]="porDefecto()"
        />
      }

      @case ('caracteristicas') {
        <section class="pila">
          <h2>Características por tipo</h2>
          @if (lista(tipos).length === 0) {
            <p class="vacio">Primero crea un tipo de habitación.</p>
          } @else {
            <label class="campo" style="max-width:320px">
              <span>Tipo</span>
              <select
                name="tipo"
                [ngModel]="tipoElegido()"
                (ngModelChange)="tipoElegido.set($event)"
              >
                @for (t of lista(tipos); track t.idTipo) {
                  <option [ngValue]="t.idTipo">{{ t.nombre }}</option>
                }
              </select>
            </label>

            @if (mensaje(); as texto) {
              <p class="aviso ok">{{ texto }}</p>
            }
            @if (error(); as texto) {
              <p class="aviso mal">{{ texto }}</p>
            }

            @if (caracteristicas.isLoading() || asignadas.isLoading()) {
              <p class="suave">Cargando…</p>
            } @else {
              <ul class="casillas">
                @for (c of lista(caracteristicas); track c.idCaracteristica) {
                  <li>
                    <label>
                      <input
                        type="checkbox"
                        [checked]="estaAsignada(c.idCaracteristica!)"
                        [disabled]="trabajando()"
                        (change)="alternar(c.idCaracteristica!)"
                      />
                      {{ c.nombre }}
                    </label>
                  </li>
                }
              </ul>
            }
          }
        </section>
      }

      @case ('habitaciones') {
        <app-tabla-crud
          titulo="Habitaciones"
          descripcion="El estado se cambia con los botones; el resto con el formulario."
          claveId="idHabitacion"
          [campos]="camposHabitacion()"
          [cargar]="cargarHabitaciones()"
          [guardar]="guardarHabitacion"
          [borrar]="borrarHabitacion"
          [acciones]="accionesHabitacion"
        />
      }

      @case ('tarifas') {
        <app-tabla-crud
          titulo="Tarifas"
          descripcion="Precio por tipo de habitación y modalidad, con su vigencia."
          claveId="idTarifa"
          [campos]="camposTarifa()"
          [cargar]="cargarTarifas()"
          [guardar]="guardarTarifa"
          [borrar]="borrarTarifa"
        />
      }

      @case ('servicios') {
        <app-tabla-crud
          titulo="Servicios adicionales"
          claveId="idServicio"
          [campos]="camposServicio"
          [cargar]="cargarServicios()"
          [guardar]="guardarServicio"
          [borrar]="borrarServicio"
          [porDefecto]="porDefectoServicio()"
        />
      }

      @case ('fotos') {
        <app-tabla-crud
          titulo="Fotos"
          descripcion="Si eliges una habitación, la foto queda asociada a ella; si no, al motel."
          claveId="idFoto"
          [campos]="camposFoto()"
          [cargar]="cargarFotos()"
          [guardar]="guardarFoto"
          [borrar]="borrarFoto"
          [porDefecto]="porDefecto()"
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
    .casillas {
      display: grid;
      gap: 8px;
      grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
      list-style: none;
      margin: 0;
      padding: 0;
    }
    .casillas input {
      width: auto;
      margin-right: 6px;
    }
  `,
})
export class MotelGestion {
  protected readonly lista = lista;
  protected readonly valorDe = valorDe;
  /** :id de la ruta /panel/moteles/:id o /admin/moteles/:id. */
  readonly id = input.required<string>();

  /** El administrador entra por /admin/moteles/:id y vuelve a su listado. */
  private readonly sesion = inject(SessionStore);
  protected readonly esAdmin = computed(() => this.sesion.rol() === 'ADMINISTRADOR');

  private readonly motelApi = inject(MotelApi);
  private readonly politicaApi = inject(PoliticaApi);
  private readonly tipoApi = inject(TipoHabitacionApi);
  private readonly habitacionApi = inject(HabitacionApi);
  private readonly tarifaApi = inject(TarifaApi);
  private readonly servicioApi = inject(ServicioApi);
  private readonly fotoApi = inject(FotoApi);
  private readonly modalidadApi = inject(ModalidadApi);
  private readonly caracteristicaApi = inject(CaracteristicaApi);

  protected readonly secciones = SECCIONES;
  protected readonly seccion = signal<Seccion>('politica');

  private readonly idMotel = computed(() => Number(this.id()));
  protected readonly porDefecto = computed<Registro>(() => ({ idMotel: this.idMotel() }));
  protected readonly porDefectoServicio = computed<Registro>(() => ({
    idMotel: this.idMotel(),
    activo: true,
  }));

  protected readonly motel = rxResource({
    params: () => this.id(),
    stream: ({ params }) => this.motelApi.crud.getById(params),
  });

  protected readonly tipos = rxResource({
    params: () => this.id(),
    stream: ({ params }) => this.tipoApi.porMotel(params),
    defaultValue: [],
  });

  protected readonly modalidades = rxResource({
    stream: () => this.modalidadApi.crud.getAll(),
    defaultValue: [],
  });

  protected readonly habitaciones = rxResource({
    params: () => this.id(),
    stream: ({ params }) => this.habitacionApi.porMotel(params),
    defaultValue: [],
  });

  // ---------- Política ----------

  protected readonly camposPolitica: Campo[] = [
    {
      clave: 'anticipacionMinimaHoras',
      etiqueta: 'Anticipación mínima (h)',
      tipo: 'numero',
      requerido: true,
    },
    {
      clave: 'capacidadMaxSinRecargo',
      etiqueta: 'Capacidad sin recargo',
      tipo: 'numero',
      requerido: true,
    },
    {
      clave: 'tarifaPersonaAdicional',
      etiqueta: 'Persona adicional',
      tipo: 'decimal',
      requerido: true,
    },
    {
      clave: 'diasHabilesDevolucion',
      etiqueta: 'Días hábiles de devolución',
      tipo: 'numero',
      requerido: true,
    },
    { clave: 'porcentajeReembolso', etiqueta: 'Reembolso (%)', tipo: 'numero', requerido: true },
    {
      clave: 'tiempoGraciaNoShowMin',
      etiqueta: 'Gracia no-show (min)',
      tipo: 'numero',
      requerido: true,
    },
    { clave: 'aceptaReembolso', etiqueta: 'Acepta reembolso', soloLectura: true },
  ];

  protected readonly cargarPolitica = computed(() => {
    const id = this.idMotel();
    return () => this.politicaApi.deMotel(id).pipe(map((p) => (p ? [p] : [])));
  });

  protected readonly guardarPolitica = (idFila: number | string | null, dto: Registro) => {
    const politica = { ...dto, idMotel: this.idMotel() } as unknown as PoliticaReserva;
    return idFila === null
      ? this.politicaApi.crud.crear(politica)
      : this.politicaApi.crud.actualizar(this.idMotel(), politica);
  };

  /** Sin política el motel deja de recibir reservas nuevas. */
  protected readonly borrarPolitica = (id: number | string) => this.politicaApi.crud.eliminar(id);

  // ---------- Tipos ----------

  protected readonly camposTipo: Campo[] = [
    { clave: 'nombre', etiqueta: 'Nombre', requerido: true },
    { clave: 'capacidadPersonas', etiqueta: 'Capacidad', tipo: 'numero', requerido: true },
    { clave: 'descripcion', etiqueta: 'Descripción', tipo: 'area' },
  ];

  protected readonly cargarTipos = computed(() => {
    const id = this.idMotel();
    return () => this.tipoApi.porMotel(id);
  });

  protected readonly guardarTipo = (idFila: number | string | null, dto: Registro) => {
    const tipo = { ...dto, idMotel: this.idMotel() } as unknown as TipoHabitacion;
    return idFila === null
      ? this.tipoApi.crud.crear(tipo)
      : this.tipoApi.crud.actualizar(idFila, tipo);
  };

  protected readonly borrarTipo = (idFila: number | string) => this.tipoApi.crud.eliminar(idFila);

  // ---------- Características del tipo ----------

  protected readonly tipoElegido = linkedSignal<number | null>(
    () => lista(this.tipos)[0]?.idTipo ?? null,
  );
  protected readonly trabajando = signal(false);
  protected readonly mensaje = signal('');
  protected readonly error = signal('');

  protected readonly caracteristicas = rxResource({
    stream: () => this.caracteristicaApi.crud.getAll(),
    defaultValue: [],
  });

  protected readonly asignadas = rxResource({
    params: () => this.tipoElegido(),
    stream: ({ params }) => (params === null ? of([]) : this.tipoApi.caracteristicas(params)),
    defaultValue: [],
  });

  protected estaAsignada(idCaracteristica: number): boolean {
    return lista(this.asignadas).some((c) => c.idCaracteristica === idCaracteristica);
  }

  protected alternar(idCaracteristica: number): void {
    const idTipo = this.tipoElegido();
    if (idTipo === null || this.trabajando()) {
      return;
    }

    this.trabajando.set(true);
    this.mensaje.set('');
    this.error.set('');

    const peticion = this.estaAsignada(idCaracteristica)
      ? this.tipoApi.quitarCaracteristica(idTipo, idCaracteristica)
      : this.tipoApi.asignarCaracteristica(idTipo, idCaracteristica);

    peticion.subscribe({
      next: (respuesta) => {
        this.trabajando.set(false);
        this.mensaje.set(respuesta);
        this.asignadas.reload();
      },
      error: (e: Error) => {
        this.trabajando.set(false);
        this.error.set(e.message);
        this.asignadas.reload();
      },
    });
  }

  // ---------- Habitaciones ----------

  protected readonly camposHabitacion = computed<Campo[]>(() => [
    { clave: 'numero', etiqueta: 'Número', requerido: true },
    { clave: 'piso', etiqueta: 'Piso', tipo: 'numero' },
    {
      clave: 'idTipo',
      etiqueta: 'Tipo',
      tipo: 'select',
      requerido: true,
      opciones: lista(this.tipos).map((t) => ({ valor: t.idTipo!, texto: t.nombre })),
    },
    { clave: 'estado', etiqueta: 'Estado', esEstado: true, soloLectura: true },
  ]);

  protected readonly cargarHabitaciones = computed(() => {
    const id = this.idMotel();
    return () => this.habitacionApi.porMotel(id);
  });

  protected readonly guardarHabitacion = (idFila: number | string | null, dto: Registro) => {
    const habitacion = dto as unknown as Habitacion;
    return idFila === null
      ? this.habitacionApi.crud.crear(habitacion)
      : this.habitacionApi.crud.actualizar(idFila, habitacion);
  };

  protected readonly borrarHabitacion = (idFila: number | string) =>
    this.habitacionApi.crud.eliminar(idFila);

  /** /habitacion/updateestado: el estado no viaja por updatejson. */
  protected readonly accionesHabitacion: AccionFila[] = ESTADOS_HABITACION.map((estado) => ({
    texto:
      estado === 'ACTIVA'
        ? 'Activar'
        : estado === 'MANTENIMIENTO'
          ? 'Mantenimiento'
          : 'Fuera de servicio',
    visible: (fila: Registro) => fila['estado'] !== estado,
    peligro: estado === 'FUERA_DE_SERVICIO',
    ejecutar: (fila: Registro) =>
      this.habitacionApi.cambiarEstado(fila['idHabitacion'] as number, estado),
  }));

  // ---------- Tarifas ----------

  protected readonly camposTarifa = computed<Campo[]>(() => [
    {
      clave: 'idTipo',
      etiqueta: 'Tipo',
      tipo: 'select',
      requerido: true,
      opciones: lista(this.tipos).map((t) => ({ valor: t.idTipo!, texto: t.nombre })),
    },
    {
      clave: 'idModalidad',
      etiqueta: 'Modalidad',
      tipo: 'select',
      requerido: true,
      opciones: this.modalidades
        .value()
        .map((m) => ({ valor: m.idModalidad!, texto: `${m.nombre} (${m.duracionHoras} h)` })),
    },
    { clave: 'precio', etiqueta: 'Precio', tipo: 'decimal', requerido: true },
    {
      clave: 'tipoDia',
      etiqueta: 'Día',
      tipo: 'select',
      requerido: true,
      opciones: TIPOS_DIA.map((t) => ({ valor: t, texto: t.replaceAll('_', ' ').toLowerCase() })),
    },
    { clave: 'vigenteDesde', etiqueta: 'Vigente desde', tipo: 'fecha', requerido: true },
    { clave: 'vigenteHasta', etiqueta: 'Vigente hasta', tipo: 'fecha' },
  ]);

  /** Las tarifas se consultan por tipo, así que se piden las de todos los tipos. */
  protected readonly cargarTarifas = computed(() => {
    const id = this.idMotel();
    return () =>
      this.tipoApi
        .porMotel(id)
        .pipe(
          switchMap((tipos) =>
            tipos.length === 0
              ? of<Tarifa[]>([])
              : forkJoin(tipos.map((t) => this.tarifaApi.porTipo(t.idTipo!))).pipe(
                  map((listas) => listas.flat()),
                ),
          ),
        );
  });

  protected readonly guardarTarifa = (idFila: number | string | null, dto: Registro) => {
    const tarifa = dto as unknown as Tarifa;
    return idFila === null
      ? this.tarifaApi.crud.crear(tarifa)
      : this.tarifaApi.crud.actualizar(idFila, tarifa);
  };

  protected readonly borrarTarifa = (idFila: number | string) =>
    this.tarifaApi.crud.eliminar(idFila);

  // ---------- Servicios ----------

  protected readonly camposServicio: Campo[] = [
    { clave: 'nombre', etiqueta: 'Nombre', requerido: true },
    { clave: 'precio', etiqueta: 'Precio', tipo: 'decimal', requerido: true },
    { clave: 'descripcion', etiqueta: 'Descripción', tipo: 'area' },
    { clave: 'activo', etiqueta: 'Activo', tipo: 'booleano' },
  ];

  protected readonly cargarServicios = computed(() => {
    const id = this.idMotel();
    return () => this.servicioApi.todosPorMotel(id);
  });

  protected readonly guardarServicio = (idFila: number | string | null, dto: Registro) => {
    const servicio = { ...dto, idMotel: this.idMotel() } as unknown as ServicioAdicional;
    return idFila === null
      ? this.servicioApi.crud.crear(servicio)
      : this.servicioApi.crud.actualizar(idFila, servicio);
  };

  protected readonly borrarServicio = (idFila: number | string) =>
    this.servicioApi.crud.eliminar(idFila);

  // ---------- Fotos ----------

  protected readonly camposFoto = computed<Campo[]>(() => [
    { clave: 'uri', etiqueta: 'URL de la imagen', requerido: true },
    { clave: 'orden', etiqueta: 'Orden', tipo: 'numero' },
    {
      clave: 'idHabitacion',
      etiqueta: 'Habitación (opcional)',
      tipo: 'select',
      opciones: this.habitaciones
        .value()
        .map((h) => ({ valor: h.idHabitacion!, texto: `Habitación ${h.numero}` })),
    },
  ]);

  protected readonly cargarFotos = computed(() => {
    const id = this.idMotel();
    return () => this.fotoApi.todasDeMotel(id);
  });

  /**
   * La tabla `foto` exige que la imagen sea del motel o de una habitación, no de
   * ambos, así que se envía solo la referencia elegida.
   */
  protected readonly guardarFoto = (idFila: number | string | null, dto: Registro) => {
    const idHabitacion = (dto['idHabitacion'] as number | null) ?? null;
    const foto = {
      ...dto,
      idHabitacion,
      idMotel: idHabitacion === null ? this.idMotel() : null,
    } as unknown as Foto;

    return idFila === null
      ? this.fotoApi.crud.crear(foto)
      : this.fotoApi.crud.actualizar(idFila, foto);
  };

  protected readonly borrarFoto = (idFila: number | string) => this.fotoApi.crud.eliminar(idFila);
}
