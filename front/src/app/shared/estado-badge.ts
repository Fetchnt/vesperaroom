import { Component, computed, input } from '@angular/core';
import { legible, tonoDeEstado } from './etiquetas';

/** Etiqueta de color para cualquier enum de estado del backend. */
@Component({
  standalone: false,
  selector: 'app-estado',
  template: `<span class="insignia" [class]="tono()">{{ texto() }}</span>`,
  styles: `
    .insignia {
      display: inline-block;
      padding: 2px 9px;
      border-radius: 999px;
      font-size: 0.75rem;
      font-weight: 700;
      letter-spacing: 0.02em;
      white-space: nowrap;
      border: 1px solid currentColor;
    }
    .ok {
      color: var(--exito);
      background: var(--exito-tenue);
    }
    .mal {
      color: var(--error);
      background: var(--error-tenue);
    }
    .aviso {
      color: var(--aviso);
      background: var(--aviso-tenue);
    }
    .info {
      color: var(--marca-fuerte);
      background: var(--marca-tenue);
    }
    .neutro {
      color: var(--neutro);
      background: var(--neutro-tenue);
    }
  `,
})
export class EstadoBadge {
  readonly valor = input<string | null | undefined>();

  protected readonly tono = computed(() => tonoDeEstado(this.valor()));
  protected readonly texto = computed(() => legible(this.valor()));
}
