import { Component, computed, inject } from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';
import { switchMap } from 'rxjs';
import { esDestacado } from '../../core/catalogo/filtros';
import { MotelApi } from '../../core/api/entidades';
import { ResumenApi, type MotelResumen } from '../../core/api/resumen';
import { lista } from '../../shared/recursos';

/** Cuántos moteles se muestran en la portada. */
const EN_PORTADA = 3;

@Component({
  standalone: false,
  selector: 'app-inicio',
  template: `
    <section class="portada">
      <div class="portada-texto">
        <h1>Reserva en los mejores moteles</h1>
        <p>Elige la habitación, la hora de llegada y listo. Pagas al llegar.</p>
      </div>
      <app-buscador class="buscador" />
    </section>

    <section class="bloque">
      <div class="encabezado-seccion">
        <h2>Los mejor valorados</h2>
        <a routerLink="/moteles">Ver todos los moteles →</a>
      </div>

      @if (moteles.isLoading()) {
        <div class="rejilla">
          @for (hueco of [1, 2, 3]; track hueco) {
            <div class="tarjeta esqueleto"></div>
          }
        </div>
      } @else if (moteles.error(); as e) {
        <p class="aviso mal">{{ e.message }}</p>
      } @else if (mejores().length === 0) {
        <p class="vacio">Todavía no hay moteles publicados.</p>
      } @else {
        <div class="rejilla">
          @for (m of mejores(); track m.idMotel) {
            <a class="tarjeta tarjeta-enlace motel" [routerLink]="['/motel', m.idMotel]">
              <div class="foto">
                @if (m.foto) {
                  <img [src]="m.foto" alt="" loading="lazy" (error)="$any($event.target).hidden = true" />
                }
                @if (destacado(m)) {
                  <span class="sello">Destacado</span>
                }
              </div>
              <div class="datos">
                <strong>{{ m.nombre }}</strong>
                @if (m.promedio > 0) {
                  <span class="nota">☆ {{ m.promedio.toFixed(1) }}</span>
                }
              </div>
              @if (m.desde !== null) {
                <span class="suave pequeno">desde {{ m.desde | currency: 'COP' : 'symbol-narrow' : '1.0-0' }}</span>
              }
            </a>
          }
        </div>
      }
    </section>

    <section class="duenos">
      <div>
        <h2>¿Eres dueño de un motel?</h2>
        <p>Publica tus habitaciones, tarifas y servicios, y recibe reservas desde VesperaRoom.</p>
      </div>
      <a class="boton-enlace" routerLink="/registro" [queryParams]="{ rol: 'PROPIETARIO' }">Registrar mi motel</a>
    </section>
  `,
  styleUrl: './inicio.css',
})
export class Inicio {
  private readonly motelApi = inject(MotelApi);
  private readonly resumenApi = inject(ResumenApi);

  protected readonly destacado = esDestacado;

  protected readonly moteles = rxResource<MotelResumen[], unknown>({
    stream: () => this.motelApi.activos().pipe(switchMap((lista) => this.resumenApi.resumir(lista))),
    defaultValue: [],
  });

  protected readonly mejores = computed(() =>
    [...lista(this.moteles)].sort((a, b) => b.promedio - a.promedio || b.comentarios - a.comentarios).slice(0, EN_PORTADA),
  );
}
