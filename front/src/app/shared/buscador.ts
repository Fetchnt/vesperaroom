import { Component, computed, inject, input, linkedSignal } from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';
import { Router } from '@angular/router';
import { CiudadApi, MotelApi } from '../core/api/entidades';
import { lista } from './recursos';

/**
 * Buscador «Dónde · Cuándo · Hora de llegada». Lleva al listado con la
 * búsqueda en la URL (?donde=&fecha=&hora=), así se puede compartir o recargar.
 */
@Component({
  standalone: false,
  selector: 'app-buscador',
  template: `
    <form class="buscador" role="search" (ngSubmit)="buscar()">
      <label class="campo donde">
        <span>Dónde</span>
        <input
          name="donde"
          list="sugerencias-donde"
          autocomplete="off"
          placeholder="Motel o ciudad"
          [ngModel]="donde()"
          (ngModelChange)="donde.set($event)"
        />
        <datalist id="sugerencias-donde">
          @for (s of sugerencias(); track s) {
            <option [value]="s"></option>
          }
        </datalist>
      </label>
      <label class="campo">
        <span>Cuándo</span>
        <input type="date" name="fecha" [min]="hoy" [ngModel]="fecha()" (ngModelChange)="fecha.set($event)" />
      </label>
      <label class="campo">
        <span>Hora de llegada</span>
        <input type="time" name="hora" step="900" [ngModel]="hora()" (ngModelChange)="hora.set($event)" />
      </label>
      <button type="submit" class="primario">Buscar</button>
    </form>
  `,
  styles: `
    .buscador {
      display: grid;
      grid-template-columns: 2fr 1fr 1fr auto;
      align-items: stretch;
      background: var(--superficie);
      border-radius: var(--r-m);
      box-shadow: var(--sombra);
      overflow: hidden;
    }
    .campo {
      padding: 10px 16px;
      border-right: 1px solid var(--borde);
      gap: 2px;
    }
    .campo > span {
      color: var(--texto);
      text-transform: none;
      letter-spacing: 0;
      font-size: 0.85rem;
    }
    input {
      border: none;
      padding: 2px 0;
      background: transparent;
    }
    button {
      margin: 10px;
      padding: 0 32px;
      border-radius: var(--r-m);
    }
    @media (max-width: 720px) {
      .buscador {
        grid-template-columns: 1fr 1fr;
      }
      .donde {
        grid-column: 1 / -1;
        border-right: none;
        border-bottom: 1px solid var(--borde);
      }
      button {
        grid-column: 1 / -1;
        height: 44px;
      }
    }
  `,
})
export class Buscador {
  private readonly router = inject(Router);
  private readonly ciudadApi = inject(CiudadApi);
  private readonly motelApi = inject(MotelApi);

  readonly dondeInicial = input('', { alias: 'donde' });
  readonly fechaInicial = input('', { alias: 'fecha' });
  readonly horaInicial = input('', { alias: 'hora' });

  protected readonly donde = linkedSignal(() => this.dondeInicial());
  protected readonly fecha = linkedSignal(() => this.fechaInicial());
  protected readonly hora = linkedSignal(() => this.horaInicial());

  /** Fecha de hoy en formato yyyy-mm-dd y en hora local, para el mínimo del calendario. */
  protected readonly hoy = new Date(Date.now() - new Date().getTimezoneOffset() * 60_000).toISOString().slice(0, 10);

  private readonly ciudades = rxResource({ stream: () => this.ciudadApi.crud.getAll(), defaultValue: [] });
  private readonly moteles = rxResource({ stream: () => this.motelApi.activos(), defaultValue: [] });

  protected readonly sugerencias = computed(() => [
    ...lista(this.ciudades).map((c) => c.nombre),
    ...lista(this.moteles).map((m) => m.nombre),
  ]);

  protected buscar(): void {
    const queryParams = {
      donde: this.donde().trim() || null,
      fecha: this.fecha() || null,
      hora: this.hora() || null,
    };
    void this.router.navigate(['/moteles'], { queryParams });
  }
}
