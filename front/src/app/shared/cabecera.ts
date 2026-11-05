import { Component, computed, input } from '@angular/core';

/**
 * Franja de título con una foto de fondo. El velo oscuro garantiza contraste
 * para el texto blanco en tema claro y oscuro. El contenido proyectado va a la
 * derecha (botones o datos breves).
 */
@Component({
  standalone: false,
  selector: 'app-cabecera',
  template: `
    <header class="cabecera" [style.background-image]="fondo()">
      <div>
        <h1>{{ titulo() }}</h1>
        @if (subtitulo()) {
          <p>{{ subtitulo() }}</p>
        }
      </div>
      <ng-content />
    </header>
  `,
  styles: `
    .cabecera {
      display: flex;
      flex-wrap: wrap;
      gap: 12px 24px;
      align-items: flex-end;
      justify-content: space-between;
      min-height: 170px;
      margin-bottom: 24px;
      padding: 24px 28px;
      border-radius: var(--r-l);
      color: #fff;
      background-size: cover;
      background-position: center;
      box-shadow: var(--sombra);
    }
    h1 {
      margin: 0;
      font-size: 1.8rem;
      text-shadow: 0 1px 8px rgb(0 0 0 / 0.5);
    }
    p {
      margin: 4px 0 0;
      max-width: 60ch;
      opacity: 0.9;
    }
    @media (max-width: 760px) {
      .cabecera {
        min-height: 130px;
        padding: 18px;
      }
    }
  `,
})
export class Cabecera {
  readonly imagen = input.required<string>();
  readonly titulo = input.required<string>();
  readonly subtitulo = input('');

  protected readonly fondo = computed(
    () =>
      `linear-gradient(180deg, rgb(18 10 16 / 0.25), rgb(18 10 16 / 0.8)), url(${this.imagen()})`,
  );
}
