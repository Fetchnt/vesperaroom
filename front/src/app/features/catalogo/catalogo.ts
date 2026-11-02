import { Component, computed, inject, signal } from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';
import { map, switchMap } from 'rxjs';
import { CiudadApi, DepartamentoApi, MotelApi } from '../../core/api/entidades';
import { ResumenApi, type MotelResumen } from '../../core/api/resumen';
import type { Motel } from '../../core/api/models';
import { lista } from '../../shared/recursos';

@Component({
  standalone: false,
  selector: 'app-catalogo',
  template: `
    <form class="buscador" role="search" (ngSubmit)="$event.preventDefault()">
      <label class="campo">
        <span>Departamento</span>
        <select name="departamento" [ngModel]="departamento()" (ngModelChange)="cambiarDepartamento($event)">
          <option [ngValue]="null">Todo el país</option>
          @for (d of lista(departamentos); track d.idDepartamento) {
            <option [ngValue]="d.idDepartamento">{{ d.nombre }}</option>
          }
        </select>
      </label>

      <label class="campo">
        <span>Ciudad</span>
        <select name="ciudad" [ngModel]="ciudad()" (ngModelChange)="ciudad.set($event)" [disabled]="!departamento()">
          <option [ngValue]="null">Todas</option>
          @for (c of ciudadesDelDepartamento(); track c.idCiudad) {
            <option [ngValue]="c.idCiudad">{{ c.nombre }}</option>
          }
        </select>
      </label>

      <label class="campo crece">
        <span>Buscar</span>
        <input
          name="texto"
          type="search"
          placeholder="Nombre del motel o dirección"
          [ngModel]="texto()"
          (ngModelChange)="texto.set($event)"
        />
      </label>

      <button type="submit" class="primario lupa" aria-label="Buscar">
        <svg viewBox="0 0 24 24" width="22" height="22" aria-hidden="true">
          <circle cx="11" cy="11" r="7" fill="none" stroke="currentColor" stroke-width="2" />
          <path d="m20 20-4-4" stroke="currentColor" stroke-width="2" stroke-linecap="round" />
        </svg>
      </button>
    </form>

    <nav class="miga" aria-label="Ubicación">
      <a href="" (click)="$event.preventDefault(); cambiarDepartamento(null)">Inicio</a>
      @if (nombreDepartamento(); as d) {
        <span aria-hidden="true">/</span>
        <a href="" (click)="$event.preventDefault(); ciudad.set(null)">{{ d }}</a>
      }
      @if (ciudad(); as c) {
        <span aria-hidden="true">/</span>
        <span>{{ nombreCiudad(c) }}</span>
      }
    </nav>

    <div class="disposicion">
      <aside class="filtros" aria-label="Filtros">
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

        @if (servicios().length > 0) {
          <h2>Servicios</h2>
          <ul class="opciones">
            @for (s of servicios(); track s) {
              <li>
                <label>
                  <input type="checkbox" [checked]="elegidos().includes(s)" (change)="alternarServicio(s)" />
                  {{ s }}
                </label>
              </li>
            }
          </ul>
          @if (elegidos().length > 0) {
            <button type="button" class="plano limpiar" (click)="elegidos.set([])">Quitar filtros</button>
          }
        }
      </aside>

      <section class="resultados">
        <div class="ordenar" role="group" aria-label="Ordenar por">
          <strong class="pequeno">Ordenar por</strong>
          <button type="button" class="contorno" [class.activo]="orden() === 'calificacion'" [attr.aria-pressed]="orden() === 'calificacion'" (click)="alternarOrden('calificacion')">
            Mejor calificados
          </button>
          <button type="button" class="contorno" [class.activo]="orden() === 'precio'" [attr.aria-pressed]="orden() === 'precio'" (click)="alternarOrden('precio')">
            Menor precio
          </button>
        </div>

        @if (moteles.isLoading()) {
          @for (hueco of [1, 2, 3]; track hueco) {
            <div class="ficha esqueleto"></div>
          }
        } @else if (moteles.error(); as e) {
          <p class="aviso mal">{{ e.message }}</p>
        } @else if (visibles().length === 0) {
          <p class="vacio">No encontramos moteles con esos filtros.</p>
        } @else {
          <p class="conteo suave pequeno">
            {{ visibles().length }} {{ visibles().length === 1 ? 'motel disponible' : 'moteles disponibles' }}
          </p>

          @for (motel of visibles(); track motel.idMotel) {
            <a class="ficha" [routerLink]="['/motel', motel.idMotel]">
              <div class="imagen">
                @if (motel.foto) {
                  <img [src]="motel.foto" [alt]="motel.nombre" loading="lazy" (error)="$any($event.target).hidden = true" />
                } @else {
                  <span class="sin-foto" aria-hidden="true">☾</span>
                }
              </div>

              <div class="cuerpo">
                <div class="titulo">
                  <h2>{{ motel.nombre }}</h2>
                  <p class="ubicacion suave">
                    <svg viewBox="0 0 24 24" width="15" height="15" aria-hidden="true">
                      <path d="M12 21s7-6.2 7-12a7 7 0 0 0-14 0c0 5.8 7 12 7 12Z" fill="none" stroke="currentColor" stroke-width="1.8" />
                      <circle cx="12" cy="9" r="2.5" fill="none" stroke="currentColor" stroke-width="1.8" />
                    </svg>
                    {{ motel.calle }} {{ motel.carrera }}, {{ nombreCiudad(motel.idCiudad) }}
                  </p>
                  @if (motel.servicios.length > 0) {
                    <ul class="etiquetas">
                      @for (s of motel.servicios.slice(0, 3); track s.idServicio) {
                        <li>{{ s.nombre }}</li>
                      }
                      @if (motel.servicios.length > 3) {
                        <li>+{{ motel.servicios.length - 3 }}</li>
                      }
                    </ul>
                  }
                </div>

                <div class="lateral">
                  @if (motel.promedio > 0) {
                    <span class="nota" [attr.aria-label]="'Calificación ' + motel.promedio.toFixed(1) + ' de 5'">☆ {{ motel.promedio.toFixed(1) }}</span>
                  } @else {
                    <span class="suave pequeno">Sin reseñas</span>
                  }
                  <div class="precio">
                    @if (motel.desde !== null) {
                      <strong>{{ motel.desde | currency: 'COP' : 'symbol-narrow' : '1.0-0' }}</strong>
                      <span class="suave">desde</span>
                    } @else {
                      <span class="suave pequeno">Sin tarifas publicadas</span>
                    }
                  </div>
                  <span class="boton-enlace">Ver disponibilidad</span>
                </div>
              </div>
            </a>
          }
        }
      </section>
    </div>
  `,
  styleUrl: './catalogo.css',
})
export class Catalogo {
  protected readonly lista = lista;

  private readonly motelApi = inject(MotelApi);
  private readonly ciudadApi = inject(CiudadApi);
  private readonly departamentoApi = inject(DepartamentoApi);
  private readonly resumenApi = inject(ResumenApi);

  protected readonly departamento = signal<number | null>(null);
  protected readonly ciudad = signal<number | null>(null);
  protected readonly texto = signal('');
  protected readonly elegidos = signal<string[]>([]);
  protected readonly precioMin = signal<number | null>(null);
  protected readonly precioMax = signal<number | null>(null);
  protected readonly orden = signal<'calificacion' | 'precio' | null>(null);

  protected readonly departamentos = rxResource({
    stream: () => this.departamentoApi.crud.getAll(),
    defaultValue: [],
  });

  protected readonly ciudades = rxResource({
    stream: () => this.ciudadApi.crud.getAll(),
    defaultValue: [],
  });

  protected readonly ciudadesDelDepartamento = computed(() => {
    const id = this.departamento();
    return id === null ? [] : lista(this.ciudades).filter((c) => c.idDepartamento === id);
  });

  /** El catálogo se pide al servidor por ciudad; el resto se filtra en el cliente. */
  protected readonly moteles = rxResource<MotelResumen[], { ciudad: number | null; departamento: number | null }>({
    params: () => ({ ciudad: this.ciudad(), departamento: this.departamento() }),
    stream: ({ params }) =>
      (params.ciudad === null ? this.motelApi.activos() : this.motelApi.porCiudad(params.ciudad)).pipe(
        map((lista) => this.filtrarPorDepartamento(lista, params.departamento, params.ciudad)),
        switchMap((lista) => this.resumenApi.resumir(lista)),
      ),
    defaultValue: [],
  });

  /** Todos los servicios que ofrecen los moteles cargados, para los chips. */
  protected readonly servicios = computed(() =>
    [...new Set(lista(this.moteles).flatMap((m) => m.servicios.map((s) => s.nombre)))].sort(),
  );

  protected readonly visibles = computed(() => {
    const busqueda = this.texto().trim().toLowerCase();
    const requeridos = this.elegidos();

    const filtrados = lista(this.moteles).filter((motel) => {
      const coincide =
        busqueda === '' ||
        `${motel.nombre} ${motel.calle} ${motel.carrera}`.toLowerCase().includes(busqueda);
      const tieneServicios = requeridos.every((nombre) =>
        motel.servicios.some((s) => s.nombre === nombre),
      );
      const [min, max] = [this.precioMin(), this.precioMax()];
      // Sin tarifas no hay precio que comparar: solo aparece si no se filtra por precio.
      const enRango =
        motel.desde === null ? !min && !max : (!min || motel.desde >= min) && (!max || motel.desde <= max);
      return coincide && tieneServicios && enRango;
    });

    const orden = this.orden();
    if (orden === 'calificacion') {
      return [...filtrados].sort((a, b) => b.promedio - a.promedio);
    }
    if (orden === 'precio') {
      // Los moteles sin tarifas van al final.
      return [...filtrados].sort((a, b) => (a.desde ?? Infinity) - (b.desde ?? Infinity));
    }
    return filtrados;
  });

  private filtrarPorDepartamento(moteles: Motel[], departamento: number | null, ciudad: number | null): Motel[] {
    if (departamento === null || ciudad !== null) {
      return moteles;
    }
    const delDepartamento = new Set(
      lista(this.ciudades)
        .filter((c) => c.idDepartamento === departamento)
        .map((c) => c.idCiudad),
    );
    return moteles.filter((m) => delDepartamento.has(m.idCiudad));
  }

  protected cambiarDepartamento(id: number | null): void {
    this.departamento.set(id);
    this.ciudad.set(null);
  }

  protected alternarServicio(nombre: string): void {
    this.elegidos.update((actual) =>
      actual.includes(nombre) ? actual.filter((s) => s !== nombre) : [...actual, nombre],
    );
  }

  protected alternarOrden(orden: 'calificacion' | 'precio'): void {
    this.orden.update((actual) => (actual === orden ? null : orden));
  }

  protected readonly nombreDepartamento = computed(
    () => lista(this.departamentos).find((d) => d.idDepartamento === this.departamento())?.nombre ?? null,
  );

  protected nombreCiudad(idCiudad: number): string {
    return lista(this.ciudades).find((c) => c.idCiudad === idCiudad)?.nombre ?? '';
  }
}
