import { Component, computed, input } from '@angular/core';
import type { Registro } from './tabla-crud';

/**
 * Cómo se verá un motel en el catálogo mientras se crea o se edita. Copia la
 * forma de la tarjeta de features/catalogo: si esa tarjeta cambia, esta también.
 */
@Component({
  standalone: false,
  selector: 'app-vista-motel',
  template: `
    <p class="suave pequeno" style="margin:0 0 8px">Así lo verán los clientes en el catálogo:</p>
    <article class="ficha">
      <div class="imagen">
        @if (foto(); as src) {
          <img [src]="src" alt="Foto elegida para el motel" />
        } @else {
          <span class="sin-foto" aria-hidden="true">☾</span>
        }
      </div>
      <div class="cuerpo">
        <div class="titulo">
          <h2 [class.falta]="!texto('nombre')">{{ texto('nombre') || 'Nombre del motel' }}</h2>
          <p class="ubicacion suave">
            <svg viewBox="0 0 24 24" width="15" height="15" aria-hidden="true">
              <path d="M12 21s7-6.2 7-12a7 7 0 0 0-14 0c0 5.8 7 12 7 12Z" fill="none" stroke="currentColor" stroke-width="1.8" />
              <circle cx="12" cy="9" r="2.5" fill="none" stroke="currentColor" stroke-width="1.8" />
            </svg>
            {{ direccion() }}
          </p>
          @if (texto('descripcion'); as d) {
            <p class="descripcion">{{ d }}</p>
          }
        </div>
        <div class="lateral">
          <span class="nuevo">Nuevo</span>
          <span class="suave pequeno">Las tarifas se publican en «Gestionar»</span>
          <span class="boton-enlace" aria-hidden="true">Ver disponibilidad</span>
        </div>
      </div>
    </article>
    @if (!foto()) {
      <p class="suave pequeno" style="margin-top:8px">Sin foto nueva: elige una en la pestaña «Datos».</p>
    }
  `,
  styles: `
    .ficha {
      display: grid;
      grid-template-columns: 200px 1fr;
      gap: 16px;
      padding: 16px;
      background: var(--superficie);
      border: 1px solid var(--borde);
      border-radius: var(--r-l);
      box-shadow: var(--sombra);
    }
    .imagen {
      aspect-ratio: 1;
      overflow: hidden;
      border-radius: var(--r-m);
      background: var(--superficie-2);
      display: grid;
      place-items: center;
    }
    .imagen img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
    .sin-foto {
      font-size: 3rem;
      color: var(--marca);
    }
    .cuerpo {
      display: flex;
      justify-content: space-between;
      gap: 16px;
      min-width: 0;
    }
    h2 {
      font-size: 1.4rem;
      font-weight: 500;
      margin: 0 0 6px;
      overflow-wrap: anywhere;
    }
    .falta {
      color: var(--texto-suave);
      font-style: italic;
    }
    .ubicacion {
      display: flex;
      gap: 4px;
      align-items: center;
      margin: 0;
    }
    .descripcion {
      margin: 12px 0 0;
      font-size: 0.9rem;
      display: -webkit-box;
      -webkit-line-clamp: 4;
      -webkit-box-orient: vertical;
      overflow: hidden;
    }
    .lateral {
      display: flex;
      flex-direction: column;
      align-items: flex-end;
      justify-content: space-between;
      gap: 12px;
      text-align: right;
      flex-shrink: 0;
      max-width: 200px;
    }
    .nuevo {
      padding: 2px 10px;
      border-radius: 999px;
      background: var(--marca-tenue);
      color: var(--marca-fuerte);
      font-size: 0.8rem;
      font-weight: 600;
    }
    .boton-enlace {
      min-width: 180px;
      padding: 12px 20px;
    }
    @media (max-width: 760px) {
      .ficha {
        grid-template-columns: 1fr;
      }
      .imagen {
        aspect-ratio: 16 / 9;
      }
      .cuerpo {
        flex-direction: column;
      }
      .lateral {
        align-items: stretch;
        text-align: left;
        max-width: none;
      }
    }
  `,
})
export class VistaMotel {
  /** El borrador del formulario: los campos llegan tal como los escribe la persona. */
  readonly motel = input.required<Registro>();
  readonly foto = input<string | null>(null);
  readonly ciudad = input('');

  protected texto(clave: string): string {
    const valor = this.motel()[clave];
    return typeof valor === 'string' ? valor.trim() : '';
  }

  protected readonly direccion = computed(() => {
    const partes = [this.texto('calle'), this.texto('carrera')].filter(Boolean).join(' ');
    return [partes, this.ciudad()].filter(Boolean).join(', ') || 'Dirección y ciudad';
  });
}
