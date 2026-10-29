import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, computed, inject, input, signal } from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { forkJoin, map, of, switchMap } from 'rxjs';
import {
  CiudadApi,
  FotoApi,
  HabitacionApi,
  ModalidadApi,
  MotelApi,
  PoliticaApi,
  ResenaApi,
  RespuestaApi,
  ServicioApi,
  TarifaApi,
  TipoHabitacionApi,
} from '../../core/api/entidades';
import { ResumenApi } from '../../core/api/resumen';
import type { Caracteristica, Resena, RespuestaPropietario, Tarifa, TipoHabitacion } from '../../core/api/models';
import { SessionStore } from '../../core/auth/session';
import { Estrellas } from '../../shared/estrellas';
import { legible } from '../../shared/etiquetas';
import { lista, valorDe } from '../../shared/recursos';

type TipoDetallado = TipoHabitacion & {
  caracteristicas: Caracteristica[];
  tarifas: Tarifa[];
};

type ResenaConRespuesta = Resena & { respuesta: RespuestaPropietario | null };

/** Normaliza lo que entrega un input datetime-local al ISO que espera el backend. */
function aIso(valor: string): string {
  return valor.length === 16 ? `${valor}:00` : valor;
}

@Component({
  selector: 'app-motel-detalle',
  imports: [CurrencyPipe, DatePipe, FormsModule, RouterLink, Estrellas],
  template: `
    @if (motel.isLoading()) {
      <div class="cargando">
        <div class="esqueleto alto"></div>
        <div class="esqueleto bajo"></div>
      </div>
    } @else if (motel.error(); as e) {
      <p class="aviso mal">{{ e.message }}</p>
    } @else if (valorDe(motel, null); as m) {
      <article class="pila">
        <!-- Galería -->
        @if (lista(fotos).length > 0) {
          <section class="galeria">
            <!-- Una URL rota se oculta; al cambiar de foto, load la vuelve a mostrar. -->
            <img class="principal" [src]="fotoActiva() ?? lista(fotos)[0].uri" [alt]="m.nombre" (error)="$any($event.target).hidden = true" (load)="$any($event.target).hidden = false" />
            @if (lista(fotos).length > 1) {
              <div class="miniaturas">
                @for (foto of lista(fotos); track foto.idFoto) {
                  <button
                    type="button"
                    [class.activa]="(fotoActiva() ?? lista(fotos)[0].uri) === foto.uri"
                    (click)="fotoActiva.set(foto.uri)"
                  >
                    <img [src]="foto.uri" [alt]="m.nombre" loading="lazy" (error)="$any($event.target).parentElement.hidden = true" />
                  </button>
                }
              </div>
            }
          </section>
        }

        <!-- Cabecera -->
        <header class="cabecera">
          <div>
            <h1>{{ m.nombre }}</h1>
            <p class="suave">{{ nombreCiudad() }} · {{ m.calle }} {{ m.carrera }}</p>
            @if (m.descripcion) {
              <p class="descripcion">{{ m.descripcion }}</p>
            }
          </div>

          <div class="resumen">
            @if (valorDe(promedio, 0) > 0) {
              <app-estrellas [valor]="valorDe(promedio, 0)" />
              <span class="pequeno suave">
                {{ lista(resenas).length }}
                {{ lista(resenas).length === 1 ? 'reseña' : 'reseñas' }}
              </span>
            } @else {
              <span class="pequeno suave">Sin reseñas todavía</span>
            }
            @if (valorDe(desde, null); as precio) {
              <span class="desde">
                <small class="suave">desde</small>
                <strong>{{ precio | currency: 'COP' : 'symbol-narrow' : '1.0-0' }}</strong>
              </span>
            }
          </div>
        </header>

        @if (lista(servicios).length > 0) {
          <ul class="etiquetas">
            @for (s of lista(servicios); track s.idServicio) {
              <li>
                {{ s.nombre }}
                <small>{{ s.precio | currency: 'COP' : 'symbol-narrow' : '1.0-0' }}</small>
              </li>
            }
          </ul>
        }

        <!-- Disponibilidad -->
        <section class="reservar" id="disponibilidad">
          <h2>¿Cuándo llegas?</h2>
          <div class="formulario">
            <label class="campo">
              <span>Entrada</span>
              <input type="datetime-local" name="inicio" [ngModel]="inicio()" (ngModelChange)="inicio.set($event)" />
            </label>
            <label class="campo">
              <span>Salida</span>
              <input type="datetime-local" name="fin" [ngModel]="fin()" (ngModelChange)="fin.set($event)" />
            </label>
          </div>

          @if (rangoInvalido()) {
            <p class="aviso mal">La salida debe ser posterior a la entrada.</p>
          } @else if (!rango()) {
            <p class="suave pequeno">
              Elige el horario y te mostramos las habitaciones libres. Pagas en el motel.
            </p>
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
                    <span class="suave pequeno">
                      {{ nombreTipo(h.idTipo) }} · {{ capacidadTipo(h.idTipo) }}
                      @if (h.piso) {
                        · piso {{ h.piso }}
                      }
                    </span>
                  </div>
                  @if (esCliente()) {
                    <a
                      class="boton-enlace"
                      [routerLink]="['/reservar', h.idHabitacion]"
                      [queryParams]="{ inicio: rango()?.inicio }"
                    >
                      Reservar
                    </a>
                  } @else {
                    <a routerLink="/login" class="pequeno">Ingresa para reservar</a>
                  }
                </li>
              }
            </ul>
          }
        </section>

        <!-- Habitaciones y tarifas -->
        <section class="pila">
          <h2>Habitaciones y tarifas</h2>
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
                    <span class="pequeno suave">{{ tipo.capacidadPersonas }} personas</span>
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

        <!-- Política -->
        @if (valorDe(politica, null); as p) {
          <section class="politica">
            <h2>Antes de reservar</h2>
            <ul>
              <li><strong>{{ p.anticipacionMinimaHoras }} h</strong> de anticipación mínima</li>
              <li><strong>{{ p.capacidadMaxSinRecargo }} personas</strong> sin recargo; cada persona de más,
                {{ p.tarifaPersonaAdicional | currency: 'COP' : 'symbol-narrow' : '1.0-0' }}</li>
              <li><strong>{{ p.tiempoGraciaNoShowMin }} min</strong> de gracia si te demoras</li>
              <li>
                @if (p.aceptaReembolso) {
                  Reembolso del <strong>{{ p.porcentajeReembolso }}%</strong> cancelando con
                  {{ p.diasHabilesDevolucion }} días hábiles de antelación
                } @else {
                  <strong>Sin reembolso</strong> por cancelación
                }
              </li>
            </ul>
          </section>
        }

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
      </article>
    } @else {
      <p class="vacio">Ese motel no existe o fue retirado.</p>
    }
  `,
  styleUrl: './motel-detalle.css',
})
export class MotelDetalle {
  protected readonly lista = lista;
  protected readonly valorDe = valorDe;
  /** Viene del parámetro de ruta :id. */
  readonly id = input.required<string>();

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

  protected readonly legible = legible;
  protected readonly esCliente = computed(() => this.sesion.rol() === 'CLIENTE');

  /** Foto grande de la galería; null significa «la primera». */
  protected readonly fotoActiva = signal<string | null>(null);

  protected readonly inicio = signal('');
  protected readonly fin = signal('');

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

  protected readonly promedio = rxResource({
    params: () => this.id(),
    stream: ({ params }) => this.resenaApi.promedio(params),
    defaultValue: 0,
  });

  /** Tarifa más barata del motel, para el «desde» de la cabecera. */
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
