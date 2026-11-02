import { Component, inject, linkedSignal, signal } from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';
import { forkJoin, map, of, switchMap, type Observable } from 'rxjs';
import { MotelApi, ResenaApi, RespuestaApi } from '../../core/api/entidades';
import type { Resena, RespuestaPropietario } from '../../core/api/models';
import { lista } from '../../shared/recursos';

type ResenaConRespuesta = Resena & { respuesta: RespuestaPropietario | null };

@Component({
  standalone: false,
  selector: 'app-resenas-motel',
  template: `
    <h1>Reseñas de mis moteles</h1>

    @if (moteles.isLoading()) {
      <p class="suave">Cargando…</p>
    } @else if (lista(moteles).length === 0) {
      <p class="vacio">Primero publica un motel.</p>
    } @else {
      <form class="tarjeta formulario" style="margin-bottom:24px">
        <label class="campo">
          <span>Motel</span>
          <select name="motel" [ngModel]="motel()" (ngModelChange)="motel.set($event)">
            @for (m of lista(moteles); track m.idMotel) {
              <option [ngValue]="m.idMotel">{{ m.nombre }}</option>
            }
          </select>
        </label>
      </form>

      @if (mensaje(); as texto) {
        <p class="aviso ok">{{ texto }}</p>
      }
      @if (error(); as texto) {
        <p class="aviso mal">{{ texto }}</p>
      }

      @if (resenas.isLoading()) {
        <p class="suave">Cargando reseñas…</p>
      } @else if (resenas.error(); as e) {
        <p class="aviso mal">{{ e.message }}</p>
      } @else if (lista(resenas).length === 0) {
        <p class="vacio">Este motel todavía no tiene reseñas.</p>
      } @else {
        <div class="pila">
          @for (r of lista(resenas); track r.idResena) {
            <article class="tarjeta pila">
              <div class="fila entre">
                <app-estrellas [valor]="r.calificacion" [mostrarValor]="false" />
                <span class="pequeno suave">{{ r.fecha | date: 'mediumDate' }}</span>
              </div>
              @if (r.comentario) {
                <p>{{ r.comentario }}</p>
              }

              @if (editando() === r.idResena) {
                <form class="pila" (ngSubmit)="guardar(r)">
                  <label class="campo">
                    <span>Tu respuesta</span>
                    <textarea
                      name="comentario"
                      required
                      [ngModel]="borrador()"
                      (ngModelChange)="borrador.set($event)"
                    ></textarea>
                  </label>
                  <div class="fila">
                    <button type="submit" class="primario" [disabled]="trabajando()">Publicar</button>
                    <button type="button" class="plano" (click)="editando.set(null)">Cancelar</button>
                  </div>
                </form>
              } @else if (r.respuesta; as resp) {
                <div class="respuesta">
                  <strong class="pequeno">Tu respuesta</strong>
                  <p class="pequeno">{{ resp.comentario }}</p>
                </div>
                <div class="fila">
                  <button type="button" class="plano" (click)="abrir(r)">Editar respuesta</button>
                  <button type="button" class="plano peligro" [disabled]="trabajando()" (click)="borrar(r)">
                    Eliminar respuesta
                  </button>
                </div>
              } @else {
                <div class="fila">
                  <button type="button" (click)="abrir(r)">Responder</button>
                </div>
              }
            </article>
          }
        </div>
      }
    }
  `,
  styles: `
    .respuesta {
      border-left: 3px solid var(--marca);
      padding-left: 12px;
    }
    .respuesta p {
      margin: 2px 0 0;
    }
  `,
})
export class ResenasMotel {
  protected readonly lista = lista;
  private readonly motelApi = inject(MotelApi);
  private readonly resenaApi = inject(ResenaApi);
  private readonly respuestaApi = inject(RespuestaApi);

  protected readonly moteles = rxResource({
    stream: () => this.motelApi.mios(),
    defaultValue: [],
  });

  protected readonly motel = linkedSignal<number | null>(() => lista(this.moteles)[0]?.idMotel ?? null);

  protected readonly editando = signal<number | null>(null);
  protected readonly borrador = signal('');
  protected readonly trabajando = signal(false);
  protected readonly mensaje = signal('');
  protected readonly error = signal('');

  /** Las respuestas del propietario viven en su propio endpoint, una por reseña. */
  protected readonly resenas = rxResource<ResenaConRespuesta[], number | null>({
    params: () => this.motel(),
    stream: ({ params }) =>
      params === null
        ? of<ResenaConRespuesta[]>([])
        : this.resenaApi.porMotel(params).pipe(
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

  protected abrir(r: ResenaConRespuesta): void {
    this.editando.set(r.idResena ?? null);
    this.borrador.set(r.respuesta?.comentario ?? '');
    this.mensaje.set('');
    this.error.set('');
  }

  protected guardar(r: ResenaConRespuesta): void {
    if (this.trabajando()) {
      return;
    }

    const dto: RespuestaPropietario = { idResena: r.idResena!, comentario: this.borrador().trim() };
    const peticion = r.respuesta
      ? this.respuestaApi.crud.actualizar(r.idResena!, dto)
      : this.respuestaApi.crud.crear(dto);

    this.ejecutar(peticion, () => this.editando.set(null));
  }

  protected borrar(r: ResenaConRespuesta): void {
    if (this.trabajando() || !confirm('¿Eliminar tu respuesta?')) {
      return;
    }
    this.ejecutar(this.respuestaApi.crud.eliminar(r.idResena!));
  }

  private ejecutar(peticion: Observable<string>, alTerminar?: () => void): void {
    this.trabajando.set(true);
    this.mensaje.set('');
    this.error.set('');

    peticion.subscribe({
      next: (respuesta) => {
        this.trabajando.set(false);
        this.mensaje.set(respuesta);
        alTerminar?.();
        this.resenas.reload();
      },
      error: (e: Error) => {
        this.trabajando.set(false);
        this.error.set(e.message);
      },
    });
  }
}
