import { Component, computed, input } from '@angular/core';

/** Calificación de 1 a 5 en estrellas, con el valor numérico al lado. */
@Component({
  standalone: false,
  selector: 'app-estrellas',
  template: `
    <span class="estrellas" [attr.aria-label]="etiqueta()">
      <span aria-hidden="true">{{ dibujo() }}</span>
      @if (mostrarValor()) {
        <small class="suave">{{ valor().toFixed(1) }}</small>
      }
    </span>
  `,
  styles: `
    .estrellas {
      display: inline-flex;
      gap: 6px;
      align-items: baseline;
      white-space: nowrap;
    }
    span[aria-hidden] {
      color: var(--marca);
      letter-spacing: 1px;
    }
  `,
})
export class Estrellas {
  readonly valor = input.required<number>();
  readonly mostrarValor = input(true);

  private readonly llenas = computed(() => Math.round(Math.min(5, Math.max(0, this.valor()))));

  protected readonly dibujo = computed(() => '★'.repeat(this.llenas()) + '☆'.repeat(5 - this.llenas()));
  protected readonly etiqueta = computed(() => `${this.valor().toFixed(1)} de 5`);
}
