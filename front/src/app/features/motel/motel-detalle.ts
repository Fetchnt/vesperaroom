import { Component, computed, inject, input, linkedSignal, signal } from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';
import { DomSanitizer, type SafeResourceUrl } from '@angular/platform-browser';
import { forkJoin, map, of, switchMap } from 'rxjs';
import { CiudadApi, FotoApi, HabitacionApi, ModalidadApi, MotelApi, PoliticaApi, ResenaApi, RespuestaApi, ServicioApi, TarifaApi, TipoHabitacionApi } from '../../core/api/entidades';
import { ResumenApi } from '../../core/api/resumen';
import type { Caracteristica, Resena, RespuestaPropietario, Tarifa, TipoHabitacion } from '../../core/api/models';
import { SessionStore } from '../../core/auth/session';
import { legible } from '../../shared/etiquetas';
import { IMAGENES, usarSinFoto } from '../../shared/imagenes';
import { desdeUrl, lista, valorDe } from '../../shared/recursos';

type TipoDetallado = TipoHabitacion & {
  caracteristicas: Caracteristica[];
  tarifas: Tarifa[];
};

type ResenaConRespuesta = Resena & { respuesta: RespuestaPropietario | null };

/** Normaliza lo que entrega un input datetime-local al ISO que espera el backend. */
function aIso(valor: string): string {
  return valor.length === 16 ? `${valor}:00` : valor;
}

/** Suma horas a un valor de datetime-local («2026-11-20T21:00») sin pasar por UTC. */
function sumarHoras(valor: string, horas: number): string {
  const [fecha, hora] = valor.split('T');
  const [a, m, d] = fecha.split('-').map(Number);
  const [hh, mm] = hora.split(':').map(Number);
  const t = new Date(a, m - 1, d, hh + horas, mm);
  const dos = (n: number) => String(n).padStart(2, '0');
  return `${t.getFullYear()}-${dos(t.getMonth() + 1)}-${dos(t.getDate())}T${dos(t.getHours())}:${dos(t.getMinutes())}`;
}

@Component({
  standalone: false,
  selector: 'app-motel-detalle',
  template: `
    @if (motel.isLoading()) {
      <div class="cargando">
        <div class="esqueleto alto"></div>
        <div class="esqueleto bajo"></div>
      </div>
    } @else if (motel.error(); as e) {
      <p class="aviso mal">{{ e.message }}</p>
    } @else if (valorDe(motel, null); as m) {
      <nav class="miga" aria-label="Ubicación">
        <a routerLink="/">Inicio</a>
        <span aria-hidden="true">/</span>
        <a routerLink="/moteles" [queryParams]="{ donde: nombreCiudad() }">{{ nombreCiudad() }}</a>
        <span aria-hidden="true">/</span>
        <span>{{ m.nombre }}</span>
      </nav>

      <!-- Galería -->
      @if (lista(fotos).length === 0) {
        <section class="galeria">
          <img class="principal" [src]="imagenes.sinFoto" alt="" />
        </section>
      } @else {
        <section class="galeria">
          <!-- Una URL rota se cambia por la foto genérica. -->
          <img class="principal" [src]="fotoActiva() ?? lista(fotos)[0].uri" [alt]="m.nombre" (error)="usarSinFoto($event)" />
          @if (lista(fotos).length > 1) {
            <div class="miniaturas">
              @for (foto of lista(fotos); track foto.idFoto) {
                <button
                  type="button"
                  [attr.aria-label]="'Ver foto ' + ($index + 1)"
                  [class.activa]="(fotoActiva() ?? lista(fotos)[0].uri) === foto.uri"
                  (click)="fotoActiva.set(foto.uri)"
                >
                  <img [src]="foto.uri" alt="" loading="lazy" (error)="$any($event.target).parentElement.hidden = true" />
                </button>
              }
            </div>
          }
        </section>
      }

      <div class="detalle">
        <div class="columna">
          <header class="cabecera">
            <div>
              <h1>{{ m.nombre }}</h1>
              <p class="ubicacion suave">
                <svg viewBox="0 0 24 24" width="15" height="15" aria-hidden="true">
                  <path d="M12 21s7-6.2 7-12a7 7 0 0 0-14 0c0 5.8 7 12 7 12Z" fill="none" stroke="currentColor" stroke-width="1.8" />
                  <circle cx="12" cy="9" r="2.5" fill="none" stroke="currentColor" stroke-width="1.8" />
                </svg>
                {{ m.calle }} {{ m.carrera }}, {{ nombreCiudad() }}
              </p>
            </div>
            <div class="resumen">
              @if (valorDe(promedio, 0) > 0) {
                <app-estrellas [valor]="valorDe(promedio, 0)" />
                <span class="pequeno suave">
                  {{ lista(resenas).length }} {{ lista(resenas).length === 1 ? 'comentario' : 'comentarios' }}
                </span>
              } @else {
                <span class="pequeno suave">Sin reseñas todavía</span>
              }
            </div>
          </header>

          <!-- Habitaciones y tarifas -->
          <section class="pila">
            <h2>Habitaciones</h2>
            @if (tipos.isLoading()) {
              <p class="suave">Cargando…</p>
            } @else if (lista(tipos).length === 0) {
              <p class="vacio">Este motel todavía no publicó sus habitaciones.</p>
            } @else {
              <div class="pestanas" role="tablist" aria-label="Tipos de habitación">
                @for (tipo of lista(tipos); track tipo.idTipo) {
                  <button
                    type="button"
                    role="tab"
                    [id]="'pestana-' + tipo.idTipo"
                    [class.activa]="tipo.idTipo === tipoActivo()?.idTipo"
                    [attr.aria-selected]="tipo.idTipo === tipoActivo()?.idTipo"
                    (click)="idTipoActivo.set(tipo.idTipo!)"
                  >
                    {{ tipo.nombre }}
                  </button>
                }
              </div>
              @if (tipoActivo(); as tipo) {
                <div class="tipo" role="tabpanel" [attr.aria-labelledby]="'pestana-' + tipo.idTipo">
                  <div class="fila entre">
                    <h3>{{ tipo.nombre }}</h3>
                    <span class="pequeno suave">Hasta {{ tipo.capacidadPersonas }} personas</span>
                  </div>
                  @if (tipo.descripcion) {
                    <p class="pequeno">{{ tipo.descripcion }}</p>
                  }
                  @if (tipo.caracteristicas.length > 0) {
                    <ul class="etiquetas tenues">
                      @for (c of tipo.caracteristicas; track c.idCaracteristica) {
                        <li>{{ c.nombre }}</li>
                      }
                    </ul>
                  }
                  @if (tipo.tarifas.length > 0) {
                    <ul class="tarifas">
                      @for (t of tipo.tarifas; track t.idTarifa) {
                        <li>
                          <span>
                            {{ nombreModalidad(t.idModalidad) }}
                            <small class="suave">{{ legible(t.tipoDia) }}</small>
                          </span>
                          <strong>{{ t.precio | currency: 'COP' : 'symbol-narrow' : '1.0-0' }}</strong>
                        </li>
                      }
                    </ul>
                  } @else {
                    <p class="pequeno suave">Sin tarifas publicadas.</p>
                  }
                </div>
              }
            }
          </section>

          <!-- Lo que tiene el motel -->
          @if (caracteristicas().length > 0) {
            <section class="pila">
              <h2>Servicios</h2>
              <ul class="baldosas">
                @for (c of caracteristicas(); track c.idCaracteristica) {
                  <li>{{ c.nombre }}</li>
                }
              </ul>
            </section>
          }

          @if (lista(servicios).length > 0) {
            <section class="pila">
              <h2>Para agregar a tu reserva</h2>
              <ul class="etiquetas">
                @for (s of lista(servicios); track s.idServicio) {
                  <li>
                    {{ s.nombre }}
                    <small>{{ s.precio | currency: 'COP' : 'symbol-narrow' : '1.0-0' }}</small>
                  </li>
                }
              </ul>
            </section>
          }

          @if (m.descripcion) {
            <p class="descripcion">{{ m.descripcion }}</p>
          }

          <!-- Ubicación -->
          <section class="pila">
            <h2>Ubicación</h2>
            <p class="suave">{{ m.calle }} {{ m.carrera }}, {{ nombreCiudad() }}</p>
            @if (mapa(); as url) {
              <iframe class="mapa" [src]="url" title="Mapa con la ubicación del motel" loading="lazy"></iframe>
            }
          </section>

          <!-- Reseñas -->
          <section class="pila">
            <h2>Lo que dicen quienes estuvieron</h2>
            @if (lista(resenas).length === 0) {
              <p class="vacio">Este motel todavía no tiene reseñas.</p>
            } @else {
              <div class="rejilla">
                @for (r of lista(resenas); track r.idResena) {
                  <div class="resena">
                    <div class="fila entre">
                      <app-estrellas [valor]="r.calificacion" [mostrarValor]="false" />
                      <span class="pequeno suave">{{ r.fecha | date: 'mediumDate' }}</span>
                    </div>
                    @if (r.comentario) {
                      <p>{{ r.comentario }}</p>
                    }
                    @if (r.respuesta; as resp) {
                      <div class="respuesta">
                        <strong class="pequeno">Respuesta del motel</strong>
                        <p class="pequeno">{{ resp.comentario }}</p>
                      </div>
                    }
                  </div>
                }
              </div>
            }
          </section>
        </div>

        <!-- Tarjeta de reserva -->
        <aside class="reserva-col">
          <section class="reservar" id="disponibilidad">
            @if (valorDe(desde, null); as precio) {
              <p class="desde">
                <small class="suave">desde</small>
                <strong>{{ precio | currency: 'COP' : 'symbol-narrow' : '1.0-0' }}</strong>
              </p>
            }
            <h2>¿Cuándo llegas?</h2>
            <label class="campo">
              <span>Entrada</span>
              <input type="datetime-local" name="inicio" [ngModel]="inicio()" (ngModelChange)="cambiarInicio($event)" />
            </label>
            <label class="campo">
              <span>Salida</span>
              <input type="datetime-local" name="fin" [ngModel]="fin()" (ngModelChange)="fin.set($event)" />
            </label>

            @if (rangoInvalido()) {
              <p class="aviso mal">La salida debe ser posterior a la entrada.</p>
            } @else if (!rango()) {
              <p class="suave pequeno">Elige el horario y te mostramos las habitaciones libres.</p>
            } @else if (disponibles.isLoading()) {
              <p class="suave">Buscando habitaciones…</p>
            } @else if (disponibles.error(); as e) {
              <p class="aviso mal">{{ e.message }}</p>
            } @else if (lista(disponibles).length === 0) {
              <p class="vacio">No hay habitaciones libres en ese horario.</p>
            } @else {
              <ul class="habitaciones">
                @for (h of lista(disponibles); track h.idHabitacion) {
                  <li>
                    <div>
                      <strong>Habitación {{ h.numero }}</strong>
                      <span class="suave pequeno">{{ nombreTipo(h.idTipo) }} · {{ capacidadTipo(h.idTipo) }}</span>
                    </div>
                    @if (esCliente()) {
                      <a class="boton-enlace" [routerLink]="['/reservar', h.idHabitacion]" [queryParams]="{ inicio: rango()?.inicio }">
                        Solicitar
                      </a>
                    } @else {
                      <a routerLink="/login" class="pequeno">Ingresa para reservar</a>
                    }
                  </li>
                }
              </ul>
            }

            @if (valorDe(politica, null); as p) {
              <p class="nota-reserva">
                *Después de confirmada tu reserva, el motel guardará tu habitación por
                <strong>{{ p.tiempoGraciaNoShowMin }} minutos</strong>. Al llegar, presenta tu código de
                reserva y listo. Pagas en el motel.
              </p>
              <details class="cambios">
                <summary>Cambios y cancelaciones</summary>
                <ul>
                  <li>Reserva con al menos <strong>{{ p.anticipacionMinimaHoras }} h</strong> de anticipación.</li>
                  <li>
                    Hasta <strong>{{ p.capacidadMaxSinRecargo }} personas</strong> sin recargo; cada persona de más
                    cuesta {{ p.tarifaPersonaAdicional | currency: 'COP' : 'symbol-narrow' : '1.0-0' }}.
                  </li>
                  <li>
                    @if (p.aceptaReembolso) {
                      Si cancelas antes de la hora de entrada te devolvemos el
                      <strong>{{ p.porcentajeReembolso }}%</strong> de lo pagado, en un máximo de
                      {{ p.diasHabilesDevolucion }} días hábiles.
                    } @else {
                      Este motel <strong>no hace reembolsos</strong> por cancelación.
                    }
                  </li>
                </ul>
              </details>
            }
          </section>
        </aside>
      </div>
    } @else {
      <p class="vacio">Ese motel no existe o fue retirado.</p>
    }
  `,
  styleUrl: './motel-detalle.css',
})
export class MotelDetalle {
  protected readonly lista = lista;
  protected readonly imagenes = IMAGENES;
  protected readonly usarSinFoto = usarSinFoto;
  protected readonly valorDe = valorDe;
  /** Viene del parámetro de ruta :id. */
  readonly id = input.required<string>();
  /** Entrada que trae el buscador (?inicio=2026-11-20T21:00), para no pedirla dos veces. */
  readonly inicioSugerido = input('', { alias: 'inicio', transform: desdeUrl });

  private readonly motelApi = inject(MotelApi);
  private readonly ciudadApi = inject(CiudadApi);
  private readonly fotoApi = inject(FotoApi);
  private readonly politicaApi = inject(PoliticaApi);
  private readonly tipoApi = inject(TipoHabitacionApi);
  private readonly tarifaApi = inject(TarifaApi);
  private readonly servicioApi = inject(ServicioApi);
  private readonly resenaApi = inject(ResenaApi);
  private readonly respuestaApi = inject(RespuestaApi);
  private readonly habitacionApi = inject(HabitacionApi);
  private readonly modalidadApi = inject(ModalidadApi);
  private readonly resumenApi = inject(ResumenApi);
  private readonly sesion = inject(SessionStore);
  private readonly sanitizer = inject(DomSanitizer);

  protected readonly legible = legible;
  protected readonly esCliente = computed(() => this.sesion.rol() === 'CLIENTE');

  /** Foto grande de la galería; null significa «la primera». */
  protected readonly fotoActiva = signal<string | null>(null);

  protected readonly inicio = linkedSignal(() => this.inicioSugerido().slice(0, 16));
  /** Con una entrada sugerida se propone la modalidad más corta (3 h) como salida. */
  protected readonly fin = linkedSignal(() => (this.inicio() ? sumarHoras(this.inicio(), 3) : ''));

  protected cambiarInicio(valor: string): void {
    this.inicio.set(valor);
  }

  protected readonly rangoInvalido = computed(() => {
    const [a, b] = [this.inicio(), this.fin()];
    return a !== '' && b !== '' && b <= a;
  });

  /** undefined mientras falte una fecha: así el recurso no dispara la búsqueda. */
  protected readonly rango = computed(() => {
    const [a, b] = [this.inicio(), this.fin()];
    return a !== '' && b !== '' && b > a ? { inicio: aIso(a), fin: aIso(b) } : undefined;
  });

  protected readonly motel = rxResource({
    params: () => this.id(),
    stream: ({ params }) => this.motelApi.crud.getById(params),
  });

  /** Mapa de OpenStreetMap: no necesita clave. Solo entran números, así que la URL es segura. */
  protected readonly mapa = computed<SafeResourceUrl | null>(() => {
    const m = valorDe(this.motel, null);
    const [lat, lon] = [Number(m?.latitud), Number(m?.longitud)];
    if (!m || !Number.isFinite(lat) || !Number.isFinite(lon) || (lat === 0 && lon === 0)) {
      return null;
    }
    const d = 0.006;
    const url =
      `https://www.openstreetmap.org/export/embed.html?bbox=${lon - d},${lat - d},${lon + d},${lat + d}` +
      `&layer=mapnik&marker=${lat},${lon}`;
    return this.sanitizer.bypassSecurityTrustResourceUrl(url);
  });

  protected readonly promedio = rxResource({
    params: () => this.id(),
    stream: ({ params }) => this.resenaApi.promedio(params),
    defaultValue: 0,
  });

  /** Tarifa más barata del motel, para el «desde» de la tarjeta de reserva. */
  protected readonly desde = rxResource({
    params: () => Number(this.id()),
    stream: ({ params }) => this.resumenApi.precioDesde(params),
  });

  protected readonly fotos = rxResource({
    params: () => this.id(),
    stream: ({ params }) => this.fotoApi.todasDeMotel(params),
    defaultValue: [],
  });

  protected readonly politica = rxResource({
    params: () => this.id(),
    stream: ({ params }) => this.politicaApi.deMotel(params),
  });

  /** El backend no acepta reservas en un motel sin política, así que ni se ofrece buscar. */
  protected readonly sinPolitica = computed(() => this.politica.hasValue() && this.politica.value() === null);

  protected readonly servicios = rxResource({
    params: () => this.id(),
    stream: ({ params }) => this.servicioApi.porMotel(params),
    defaultValue: [],
  });

  protected readonly modalidades = rxResource({
    stream: () => this.modalidadApi.crud.getAll(),
    defaultValue: [],
  });

  protected readonly ciudades = rxResource({
    stream: () => this.ciudadApi.crud.getAll(),
    defaultValue: [],
  });

  /** Cada tipo con sus características y sus tarifas, que son endpoints aparte. */
  protected readonly idTipoActivo = signal<number | null>(null);
  protected readonly tipoActivo = computed(
    () => lista(this.tipos).find((t) => t.idTipo === this.idTipoActivo()) ?? lista(this.tipos)[0] ?? null,
  );

  protected readonly tipos = rxResource<TipoDetallado[], string>({
    params: () => this.id(),
    stream: ({ params }) =>
      this.tipoApi.porMotel(params).pipe(
        switchMap((lista) =>
          lista.length === 0
            ? of<TipoDetallado[]>([])
            : forkJoin(
                lista.map((tipo) =>
                  forkJoin({
                    caracteristicas: this.tipoApi.caracteristicas(tipo.idTipo!),
                    tarifas: this.tarifaApi.porTipo(tipo.idTipo!),
                  }).pipe(map((extra) => ({ ...tipo, ...extra }))),
                ),
              ),
        ),
      ),
    defaultValue: [],
  });

  /** Todo lo que ofrece alguna de sus habitaciones, sin repetir. */
  protected readonly caracteristicas = computed(() => {
    const vistas = new Map<number, Caracteristica>();
    for (const t of lista(this.tipos)) {
      for (const c of t.caracteristicas) vistas.set(c.idCaracteristica!, c);
    }
    return [...vistas.values()].sort((a, b) => a.nombre.localeCompare(b.nombre));
  });

  /** Reseñas con la respuesta del propietario, que vive en otro endpoint. */
  protected readonly resenas = rxResource<ResenaConRespuesta[], string>({
    params: () => this.id(),
    stream: ({ params }) =>
      this.resenaApi.porMotel(params).pipe(
        switchMap((lista) =>
          lista.length === 0
            ? of<ResenaConRespuesta[]>([])
            : forkJoin(
                lista.map((resena) =>
                  this.respuestaApi
                    .deResena(resena.idResena!)
                    .pipe(map((respuesta) => ({ ...resena, respuesta }))),
                ),
              ),
        ),
      ),
    defaultValue: [],
  });

  protected readonly disponibles = rxResource({
    params: () => {
      const rango = this.rango();
      return rango ? { id: this.id(), ...rango } : undefined;
    },
    stream: ({ params }) => this.habitacionApi.disponibles(params.id, params.inicio, params.fin),
    defaultValue: [],
  });

  protected nombreCiudad(): string {
    const idCiudad = valorDe(this.motel, null)?.idCiudad;
    return lista(this.ciudades).find((c) => c.idCiudad === idCiudad)?.nombre ?? '';
  }

  protected nombreModalidad(idModalidad: number): string {
    const modalidad = lista(this.modalidades).find((m) => m.idModalidad === idModalidad);
    return modalidad ? `${modalidad.nombre} (${modalidad.duracionHoras} h)` : `Modalidad ${idModalidad}`;
  }

  protected nombreTipo(idTipo: number): string {
    return lista(this.tipos).find((t) => t.idTipo === idTipo)?.nombre ?? '—';
  }

  protected capacidadTipo(idTipo: number): string {
    const tipo = lista(this.tipos).find((t) => t.idTipo === idTipo);
    return tipo ? `${tipo.capacidadPersonas} personas` : '—';
  }
}
