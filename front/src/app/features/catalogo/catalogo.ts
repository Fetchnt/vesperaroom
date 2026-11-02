import { Component, computed, inject, input, signal, type WritableSignal } from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';
import { switchMap } from 'rxjs';
import { filtrar, type Orden, type Posicion } from '../../core/catalogo/filtros';
import { CaracteristicaApi, CiudadApi, ModalidadApi, MotelApi } from '../../core/api/entidades';
import { ResumenApi, type MotelResumen, type TipoResumen } from '../../core/api/resumen';
import { desdeUrl, lista } from '../../shared/recursos';

/** Cuántas opciones de una lista larga se ven antes de «Ver más». */
const LIMITE = 5;

@Component({
  standalone: false,
  selector: 'app-catalogo',
  template: `
    <app-buscador class="buscador" [donde]="donde()" [fecha]="fecha()" [hora]="hora()" />

    <nav class="miga" aria-label="Ubicación">
      <a routerLink="/">Inicio</a>
      <span aria-hidden="true">/</span>
      <span>{{ donde() || 'Todos los moteles' }}</span>
    </nav>

    <div class="disposicion">
      <button
        type="button"
        class="contorno ver-filtros"
        [attr.aria-expanded]="filtrosAbiertos()"
        (click)="filtrosAbiertos.set(!filtrosAbiertos())"
      >
        {{ filtrosAbiertos() ? 'Ocultar filtros' : 'Filtros' }}
        @if (cuantosFiltros() > 0) {
          ({{ cuantosFiltros() }})
        }
      </button>

      <aside class="filtros" [class.abiertos]="filtrosAbiertos()" aria-label="Filtros">
        <h2>Precio</h2>
        <div class="precios">
          <label class="campo">
            <span>Precio mínimo</span>
            <input type="number" min="0" step="10000" name="min" placeholder="0" [ngModel]="precioMin()" (ngModelChange)="precioMin.set($event)" />
          </label>
          <label class="campo">
            <span>Precio máximo</span>
            <input type="number" min="0" step="10000" name="max" placeholder="Sin límite" [ngModel]="precioMax()" (ngModelChange)="precioMax.set($event)" />
          </label>
        </div>

        @if (lista(modalidades).length > 0) {
          <h2>Horas</h2>
          <ul class="opciones">
            @for (m of lista(modalidades); track m.idModalidad) {
              <li>
                <label>
                  <input type="checkbox" [checked]="horas().includes(m.duracionHoras)" (change)="alternar(horas, m.duracionHoras)" />
                  {{ m.nombre }} · {{ m.duracionHoras }} {{ m.duracionHoras === 1 ? 'hora' : 'horas' }}
                </label>
              </li>
            }
          </ul>
        }

        @if (lista(caracteristicas).length > 0) {
          <h2>Servicios</h2>
          <ul class="opciones">
            @for (c of caracteristicasVisibles(); track c.idCaracteristica) {
              <li>
                <label>
                  <input type="checkbox" [checked]="elegidas().includes(c.idCaracteristica!)" (change)="alternar(elegidas, c.idCaracteristica!)" />
                  {{ c.nombre }}
                </label>
              </li>
            }
          </ul>
          @if (lista(caracteristicas).length > limite) {
            <button type="button" class="plano ver-mas" (click)="verTodas.set(!verTodas())">
              {{ verTodas() ? 'Ver menos' : 'Ver más' }}
            </button>
          }
        }

        <h2>Ordenar</h2>
        <ul class="interruptores">
          <li>
            <label>
              Los más valorados
              <input type="checkbox" role="switch" [checked]="orden() === 'valorados'" (change)="alternarOrden('valorados')" />
            </label>
          </li>
          <li>
            <label>
              Con más comentarios
              <input type="checkbox" role="switch" [checked]="orden() === 'comentarios'" (change)="alternarOrden('comentarios')" />
            </label>
          </li>
          <li>
            <label>
              Cercanos a ti
              <input type="checkbox" role="switch" [checked]="orden() === 'cercanos'" (change)="alternarCercanos()" />
            </label>
          </li>
        </ul>
        @if (avisoUbicacion(); as aviso) {
          <p class="aviso mal pequeno">{{ aviso }}</p>
        }

        <button type="button" class="contorno limpiar" (click)="limpiar()">Limpiar</button>
      </aside>

      <section class="resultados">
        <div class="filtrar-por" role="group" aria-label="Filtrar por">
          <strong class="pequeno">Filtrar por</strong>
          <button type="button" class="contorno" [class.activo]="destacados()" [attr.aria-pressed]="destacados()" (click)="destacados.set(!destacados())">
            Destacados
          </button>
          <button type="button" class="contorno" [class.activo]="nuevos()" [attr.aria-pressed]="nuevos()" (click)="nuevos.set(!nuevos())">
            Nuevos
          </button>
        </div>

        @if (moteles.isLoading()) {
          @for (hueco of [1, 2, 3]; track hueco) {
            <div class="ficha esqueleto"></div>
          }
        } @else if (moteles.error(); as e) {
          <p class="aviso mal">{{ e.message }}</p>
        } @else if (resultados().length === 0) {
          <p class="vacio">No encontramos moteles con esos filtros.</p>
        } @else {
          <p class="conteo suave pequeno">
            {{ resultados().length }} {{ resultados().length === 1 ? 'motel disponible' : 'moteles disponibles' }}
          </p>

          @for (motel of resultados(); track motel.idMotel) {
            <article class="tarjeta-motel">
              @if (motel.tipos.length > 0) {
                <div class="pestanas" role="tablist" [attr.aria-label]="'Habitaciones de ' + motel.nombre">
                  @for (t of motel.tipos; track t.idTipo) {
                    <button
                      type="button"
                      role="tab"
                      [class.activa]="t.idTipo === tipoDe(motel)?.idTipo"
                      [attr.aria-selected]="t.idTipo === tipoDe(motel)?.idTipo"
                      (click)="elegirTipo(motel, t)"
                    >
                      {{ t.nombre }}
                    </button>
                  }
                </div>
              }

              <div class="ficha">
                <a class="imagen" [routerLink]="['/motel', motel.idMotel]" [queryParams]="paramsDetalle()" tabindex="-1" aria-hidden="true">
                  @if (motel.foto) {
                    <img [src]="motel.foto" alt="" loading="lazy" (error)="$any($event.target).hidden = true" />
                  } @else {
                    <span class="sin-foto">☾</span>
                  }
                </a>

                <div class="cuerpo">
                  <div class="titulo">
                    <h2>
                      <a [routerLink]="['/motel', motel.idMotel]" [queryParams]="paramsDetalle()">{{ motel.nombre }}</a>
                    </h2>
                    <p class="ubicacion suave">
                      <svg viewBox="0 0 24 24" width="15" height="15" aria-hidden="true">
                        <path d="M12 21s7-6.2 7-12a7 7 0 0 0-14 0c0 5.8 7 12 7 12Z" fill="none" stroke="currentColor" stroke-width="1.8" />
                        <circle cx="12" cy="9" r="2.5" fill="none" stroke="currentColor" stroke-width="1.8" />
                      </svg>
                      {{ motel.calle }} {{ motel.carrera }}, {{ nombreCiudad(motel.idCiudad) }}
                    </p>
                    @if (tipoDe(motel); as t) {
                      <p class="pequeno capacidad">{{ t.nombre }} · hasta {{ t.capacidadPersonas }} personas</p>
                    }
                  </div>

                  <div class="lateral">
                    <div class="valoracion">
                      @if (motel.promedio > 0) {
                        <span class="nota" [attr.aria-label]="'Calificación ' + motel.promedio.toFixed(1) + ' de 5'">☆ {{ motel.promedio.toFixed(1) }}</span>
                      }
                      <span class="suave pequeno">
                        {{ motel.comentarios }} {{ motel.comentarios === 1 ? 'comentario' : 'comentarios' }}
                      </span>
                    </div>
                    <div class="precio">
                      @if (precioDe(motel); as p) {
                        <strong>{{ p.precio | currency: 'COP' : 'symbol-narrow' : '1.0-0' }}</strong>
                        <span class="suave">por {{ p.horas }} {{ p.horas === 1 ? 'hora' : 'horas' }}</span>
                      } @else {
                        <span class="suave pequeno">Sin tarifas publicadas</span>
                      }
                    </div>
                    <a class="boton-enlace" [routerLink]="['/motel', motel.idMotel]" [queryParams]="paramsDetalle()">Ver disponibilidad</a>
                  </div>
                </div>
              </div>
            </article>
          }
        }
      </section>
    </div>
  `,
  styleUrl: './catalogo.css',
})
export class Catalogo {
  protected readonly lista = lista;
  protected readonly limite = LIMITE;

  /** Lo que llega del buscador por la URL (?donde=&fecha=&hora=). */
  readonly donde = input('', { transform: desdeUrl });
  readonly fecha = input('', { transform: desdeUrl });
  readonly hora = input('', { transform: desdeUrl });

  private readonly motelApi = inject(MotelApi);
  private readonly ciudadApi = inject(CiudadApi);
  private readonly modalidadApi = inject(ModalidadApi);
  private readonly caracteristicaApi = inject(CaracteristicaApi);
  private readonly resumenApi = inject(ResumenApi);

  protected readonly precioMin = signal<number | null>(null);
  protected readonly precioMax = signal<number | null>(null);
  protected readonly horas = signal<number[]>([]);
  protected readonly elegidas = signal<number[]>([]);
  protected readonly destacados = signal(false);
  protected readonly nuevos = signal(false);
  protected readonly orden = signal<Orden>(null);
  protected readonly posicion = signal<Posicion | null>(null);
  protected readonly avisoUbicacion = signal('');
  protected readonly verTodas = signal(false);
  protected readonly filtrosAbiertos = signal(false);
  /** Pestaña de tipo elegida en cada ficha: idMotel → idTipo. */
  private readonly tipoElegido = signal<Record<number, number>>({});

  protected readonly ciudades = rxResource({ stream: () => this.ciudadApi.crud.getAll(), defaultValue: [] });
  protected readonly modalidades = rxResource({ stream: () => this.modalidadApi.crud.getAll(), defaultValue: [] });
  protected readonly caracteristicas = rxResource({ stream: () => this.caracteristicaApi.crud.getAll(), defaultValue: [] });

  protected readonly moteles = rxResource<MotelResumen[], unknown>({
    stream: () => this.motelApi.activos().pipe(switchMap((lista) => this.resumenApi.resumir(lista))),
    defaultValue: [],
  });

  protected readonly caracteristicasVisibles = computed(() => {
    const todas = lista(this.caracteristicas);
    return this.verTodas() ? todas : todas.slice(0, LIMITE);
  });

  private readonly duraciones = computed(
    () => new Map(lista(this.modalidades).map((m) => [m.idModalidad!, m.duracionHoras] as [number, number])),
  );

  protected readonly resultados = computed(() =>
    filtrar(
      lista(this.moteles),
      {
        texto: this.donde(),
        precioMin: this.precioMin(),
        precioMax: this.precioMax(),
        horas: this.horas(),
        caracteristicas: this.elegidas(),
        destacados: this.destacados(),
        nuevos: this.nuevos(),
        orden: this.orden(),
        posicion: this.posicion(),
      },
      this.duraciones(),
      (id) => this.nombreCiudad(id),
    ),
  );

  protected readonly cuantosFiltros = computed(
    () =>
      (this.precioMin() ? 1 : 0) +
      (this.precioMax() ? 1 : 0) +
      this.horas().length +
      this.elegidas().length +
      (this.orden() ? 1 : 0),
  );

  /** La fecha y hora del buscador viajan al detalle para no pedirlas otra vez. */
  protected readonly paramsDetalle = computed(() =>
    this.fecha() && this.hora() ? { inicio: `${this.fecha()}T${this.hora()}` } : {},
  );

  protected tipoDe(motel: MotelResumen): TipoResumen | null {
    const id = this.tipoElegido()[motel.idMotel!];
    return motel.tipos.find((t) => t.idTipo === id) ?? motel.tipos[0] ?? null;
  }

  protected elegirTipo(motel: MotelResumen, tipo: TipoResumen): void {
    this.tipoElegido.update((actual) => ({ ...actual, [motel.idMotel!]: tipo.idTipo! }));
  }

  /** La tarifa más barata del tipo elegido, con la duración de su modalidad. */
  protected precioDe(motel: MotelResumen): { precio: number; horas: number } | null {
    const tarifas = this.tipoDe(motel)?.tarifas ?? [];
    if (tarifas.length === 0) return null;
    const barata = tarifas.reduce((a, b) => (b.precio < a.precio ? b : a));
    return { precio: barata.precio, horas: this.duraciones().get(barata.idModalidad) ?? 0 };
  }

  protected alternar<T>(senal: WritableSignal<T[]>, valor: T): void {
    senal.update((actual) => (actual.includes(valor) ? actual.filter((v) => v !== valor) : [...actual, valor]));
  }

  protected alternarOrden(orden: Orden): void {
    this.orden.update((actual) => (actual === orden ? null : orden));
  }

  /** «Cercanos a ti» necesita la ubicación del navegador; si no la da, se explica por qué. */
  protected alternarCercanos(): void {
    if (this.orden() === 'cercanos') {
      this.orden.set(null);
      return;
    }
    if (!('geolocation' in navigator)) {
      this.avisoUbicacion.set('Tu navegador no permite conocer tu ubicación.');
      return;
    }
    navigator.geolocation.getCurrentPosition(
      ({ coords }) => {
        this.avisoUbicacion.set('');
        this.posicion.set({ lat: coords.latitude, lon: coords.longitude });
        this.orden.set('cercanos');
      },
      () => {
        this.avisoUbicacion.set('Activa tu ubicación para encontrar moteles cercanos a ti.');
        this.orden.set(null);
      },
    );
  }

  protected limpiar(): void {
    this.precioMin.set(null);
    this.precioMax.set(null);
    this.horas.set([]);
    this.elegidas.set([]);
    this.destacados.set(false);
    this.nuevos.set(false);
    this.orden.set(null);
    this.avisoUbicacion.set('');
  }

  protected nombreCiudad(idCiudad: number): string {
    return lista(this.ciudades).find((c) => c.idCiudad === idCiudad)?.nombre ?? '';
  }
}
