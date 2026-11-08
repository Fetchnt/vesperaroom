import { Component, ElementRef, Injectable, effect, inject, signal, viewChild } from '@angular/core';

/** Pregunta del diálogo de confirmación. */
export interface Pregunta {
  titulo: string;
  texto?: string;
  /** Texto del botón que confirma, por ejemplo «Cancelar reserva». */
  aceptar: string;
  /** Texto del botón que desiste; por defecto «Volver». */
  cancelar?: string;
  /** Pinta la confirmación en rojo: acciones que borran o no se pueden deshacer. */
  peligro?: boolean;
}

export type Tono = 'exito' | 'error' | 'info';

export interface Aviso {
  id: number;
  tono: Tono;
  texto: string;
}

const DURACION: Record<Tono, number> = { exito: 4500, info: 4500, error: 8000 };
const MAXIMO = 4;

/**
 * Diálogo de confirmación y avisos flotantes de toda la aplicación, en lugar de
 * confirm() y alert() del navegador. El componente <app-avisos> los pinta.
 */
@Injectable({ providedIn: 'root' })
export class Avisos {
  private siguiente = 0;
  private respuesta: ((si: boolean) => void) | null = null;

  readonly pregunta = signal<Pregunta | null>(null);
  readonly lista = signal<Aviso[]>([]);

  /** Muestra la pregunta y resuelve true solo si la persona confirma. */
  confirmar(pregunta: Pregunta): Promise<boolean> {
    this.respuesta?.(false); // una pregunta nueva descarta la anterior
    this.pregunta.set(pregunta);
    return new Promise((resolver) => (this.respuesta = resolver));
  }

  responder(si: boolean): void {
    const resolver = this.respuesta;
    if (!resolver) {
      return;
    }
    this.respuesta = null;
    this.pregunta.set(null);
    resolver(si);
  }

  exito(texto: string): void {
    this.mostrar('exito', texto);
  }

  error(texto: string): void {
    this.mostrar('error', texto);
  }

  info(texto: string): void {
    this.mostrar('info', texto);
  }

  cerrar(id: number): void {
    this.lista.update((l) => l.filter((a) => a.id !== id));
  }

  private mostrar(tono: Tono, texto: string): void {
    const id = ++this.siguiente;
    this.lista.update((l) => [...l.slice(-(MAXIMO - 1)), { id, tono, texto }]);
    setTimeout(() => this.cerrar(id), DURACION[tono]);
  }
}

@Component({
  standalone: false,
  selector: 'app-avisos',
  template: `
    <dialog
      #dialogo
      class="confirmacion"
      [class.peligro]="avisos.pregunta()?.peligro"
      aria-labelledby="confirmacion-titulo"
      aria-describedby="confirmacion-texto"
      (close)="avisos.responder(false)"
      (click)="fuera($event)"
    >
      @if (avisos.pregunta(); as p) {
        <div class="icono" aria-hidden="true">
          @if (p.peligro) {
            <svg viewBox="0 0 24 24"><path d="M12 9v4m0 4h.01M10.3 3.9 1.8 18a2 2 0 0 0 1.7 3h17a2 2 0 0 0 1.7-3L13.7 3.9a2 2 0 0 0-3.4 0Z" /></svg>
          } @else {
            <svg viewBox="0 0 24 24"><circle cx="12" cy="12" r="10" /><path d="M9.1 9a3 3 0 0 1 5.8 1c0 2-3 3-3 3m.1 4h.01" /></svg>
          }
        </div>
        <h2 id="confirmacion-titulo">{{ p.titulo }}</h2>
        @if (p.texto) {
          <p id="confirmacion-texto">{{ p.texto }}</p>
        }
        <div class="botones">
          <button type="button" class="plano" (click)="avisos.responder(false)">{{ p.cancelar ?? 'Volver' }}</button>
          <button type="button" [class.peligro]="p.peligro" [class.primario]="!p.peligro" (click)="avisos.responder(true)">
            {{ p.aceptar }}
          </button>
        </div>
      }
    </dialog>

    <section class="avisos" aria-live="polite" aria-label="Avisos">
      @for (a of avisos.lista(); track a.id) {
        <div class="aviso-flotante" [class]="a.tono" [attr.role]="a.tono === 'error' ? 'alert' : 'status'">
          <span class="marca" aria-hidden="true">
            <svg viewBox="0 0 24 24">
              @switch (a.tono) {
                @case ('exito') {
                  <path d="m5 12.5 4.5 4.5L19 7.5" />
                }
                @case ('error') {
                  <path d="M12 7v6m0 4h.01" />
                }
                @default {
                  <path d="M12 11v6m0-10h.01" />
                }
              }
            </svg>
          </span>
          <p>{{ a.texto }}</p>
          <button type="button" class="cerrar" aria-label="Cerrar aviso" (click)="avisos.cerrar(a.id)">
            <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M6 6l12 12M18 6 6 18" /></svg>
          </button>
          <span class="tiempo" aria-hidden="true"></span>
        </div>
      }
    </section>
  `,
  styles: `
    svg {
      width: 100%;
      height: 100%;
      fill: none;
      stroke: currentColor;
      stroke-width: 2;
      stroke-linecap: round;
      stroke-linejoin: round;
    }

    /* Diálogo de confirmación */
    .confirmacion {
      --tono: var(--marca);
      --tono-tenue: var(--marca-tenue);
      border: 1px solid var(--borde);
      border-radius: var(--r-l);
      background: var(--superficie);
      color: var(--texto);
      box-shadow: 0 24px 60px rgb(0 0 0 / 0.35);
      padding: 28px 24px 20px;
      width: min(420px, calc(100% - 32px));
      text-align: center;
    }
    .confirmacion.peligro {
      --tono: var(--error);
      --tono-tenue: var(--error-tenue);
    }
    .confirmacion[open] {
      animation: entrar 0.18s ease-out;
    }
    .confirmacion::backdrop {
      background: rgb(10 6 14 / 0.55);
      backdrop-filter: blur(3px);
    }
    .icono {
      width: 56px;
      height: 56px;
      margin: 0 auto 14px;
      padding: 13px;
      border-radius: 50%;
      background: var(--tono-tenue);
      color: var(--tono);
    }
    h2 {
      margin: 0 0 8px;
      font-size: 1.2rem;
    }
    .confirmacion p {
      margin: 0;
      color: var(--texto-suave);
      line-height: 1.5;
    }
    .botones {
      display: flex;
      gap: 10px;
      justify-content: center;
      margin-top: 22px;
    }
    .botones button {
      flex: 1;
      max-width: 180px;
    }
    .botones button.peligro {
      background: var(--error);
      border-color: var(--error);
      color: #fff;
    }
    .botones button.peligro:hover {
      filter: brightness(1.08);
    }

    /* Avisos flotantes */
    .avisos {
      position: fixed;
      right: 20px;
      bottom: 20px;
      z-index: 1000;
      display: flex;
      flex-direction: column;
      gap: 10px;
      width: min(380px, calc(100% - 32px));
      pointer-events: none;
    }
    .aviso-flotante {
      --tono: var(--marca);
      --tono-tenue: var(--marca-tenue);
      position: relative;
      overflow: hidden;
      display: flex;
      align-items: flex-start;
      gap: 12px;
      padding: 14px 12px 16px 14px;
      border: 1px solid var(--borde);
      border-left: 4px solid var(--tono);
      border-radius: var(--r-m);
      background: var(--superficie);
      color: var(--texto);
      box-shadow: var(--sombra), 0 10px 30px rgb(0 0 0 / 0.18);
      pointer-events: auto;
      animation: deslizar 0.22s ease-out;
    }
    .aviso-flotante.exito {
      --tono: var(--exito);
      --tono-tenue: var(--exito-tenue);
      --duracion: 4.5s;
    }
    .aviso-flotante.error {
      --tono: var(--error);
      --tono-tenue: var(--error-tenue);
      --duracion: 8s;
    }
    .aviso-flotante.info {
      --duracion: 4.5s;
    }
    .marca {
      flex-shrink: 0;
      width: 28px;
      height: 28px;
      padding: 5px;
      border-radius: 50%;
      background: var(--tono-tenue);
      color: var(--tono);
    }
    .aviso-flotante p {
      flex: 1;
      margin: 3px 0 0;
      line-height: 1.45;
    }
    .cerrar {
      flex-shrink: 0;
      width: 28px;
      height: 28px;
      padding: 6px;
      border: none;
      border-radius: var(--r-s);
      background: transparent;
      color: var(--texto-suave);
      cursor: pointer;
    }
    .cerrar:hover {
      background: var(--superficie-2);
      color: var(--texto);
    }
    .tiempo {
      position: absolute;
      left: 0;
      bottom: 0;
      height: 3px;
      width: 100%;
      background: var(--tono);
      opacity: 0.35;
      transform-origin: left;
      animation: consumir var(--duracion, 4.5s) linear forwards;
    }

    @keyframes entrar {
      from {
        opacity: 0;
        transform: translateY(8px) scale(0.97);
      }
    }
    @keyframes deslizar {
      from {
        opacity: 0;
        transform: translateX(24px);
      }
    }
    @keyframes consumir {
      to {
        transform: scaleX(0);
      }
    }
    @media (max-width: 600px) {
      .avisos {
        right: 16px;
        left: 16px;
        bottom: 16px;
        width: auto;
      }
      .botones {
        flex-direction: column-reverse;
      }
      .botones button {
        max-width: none;
      }
    }
    @media (prefers-reduced-motion: reduce) {
      .confirmacion[open],
      .aviso-flotante,
      .tiempo {
        animation: none;
      }
    }
  `,
})
export class AvisosComponent {
  protected readonly avisos = inject(Avisos);
  private readonly dialogo = viewChild.required<ElementRef<HTMLDialogElement>>('dialogo');

  constructor() {
    // El diálogo modal sigue a la pregunta pendiente: Esc y «Volver» responden que no.
    effect(() => {
      const dialogo = this.dialogo().nativeElement;
      if (this.avisos.pregunta() && !dialogo.open) {
        dialogo.showModal();
      } else if (!this.avisos.pregunta() && dialogo.open) {
        dialogo.close();
      }
    });
  }

  /** Un clic en el fondo oscuro, fuera de la tarjeta, equivale a «Volver». */
  protected fuera(evento: MouseEvent): void {
    const dialogo = this.dialogo().nativeElement;
    if (evento.target !== dialogo) {
      return;
    }
    // El relleno de la tarjeta también es el <dialog>: solo cuenta si el clic cae fuera del recuadro.
    const r = dialogo.getBoundingClientRect();
    const dentro = evento.clientX >= r.left && evento.clientX <= r.right && evento.clientY >= r.top && evento.clientY <= r.bottom;
    if (!dentro) {
      this.avisos.responder(false);
    }
  }
}
