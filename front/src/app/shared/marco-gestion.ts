import { Component, computed, inject } from '@angular/core';
import { ENLACES } from '../core/auth/enlaces';
import { SessionStore } from '../core/auth/session';

/**
 * Marco de las áreas de gestión (/admin y /panel): barra de pestañas arriba con
 * las secciones del rol y la sección actual debajo, a todo el ancho. Si no caben,
 * las pestañas se desplazan de lado.
 */
@Component({
  standalone: false,
  selector: 'app-marco-gestion',
  template: `
    <nav class="secciones" aria-label="Secciones de gestión">
      <span class="area">{{ titulo() }}</span>
      <div class="pestanas">
        @for (enlace of enlaces(); track enlace.ruta) {
          <a [routerLink]="enlace.ruta" routerLinkActive="activo" ariaCurrentWhenActive="page">{{ enlace.texto }}</a>
        }
      </div>
    </nav>
    <section class="contenido">
      <router-outlet />
    </section>
  `,
  styles: `
    .secciones {
      display: flex;
      align-items: center;
      gap: 8px 20px;
      margin-bottom: 24px;
      padding: 6px 8px 6px 16px;
      border: 1px solid var(--borde);
      border-radius: var(--r-l);
      background: var(--superficie);
      box-shadow: var(--sombra);
    }
    .area {
      flex-shrink: 0;
      color: var(--texto-suave);
      font-size: 0.75rem;
      font-weight: 600;
      letter-spacing: 0.06em;
      text-transform: uppercase;
    }
    .pestanas {
      display: flex;
      gap: 4px;
      min-width: 0;
      overflow-x: auto;
      scrollbar-width: none;
    }
    .pestanas::-webkit-scrollbar {
      display: none;
    }
    .pestanas a {
      position: relative;
      padding: 8px 14px;
      border-radius: var(--r-m);
      color: var(--texto-suave);
      font-size: 0.92rem;
      font-weight: 500;
      white-space: nowrap;
      transition:
        background-color 0.15s ease,
        color 0.15s ease;
    }
    .pestanas a::after {
      content: '';
      position: absolute;
      left: 14px;
      right: 14px;
      bottom: 2px;
      height: 2px;
      border-radius: 2px;
      background: var(--marca-solida);
      transform: scaleX(0);
      transition: transform 0.2s ease;
    }
    .pestanas a:hover {
      text-decoration: none;
      color: var(--texto);
      background: var(--superficie-2);
    }
    .pestanas a.activo {
      color: var(--marca-fuerte);
      background: var(--marca-tenue);
    }
    .pestanas a.activo::after {
      transform: scaleX(1);
    }
    @media (prefers-reduced-motion: reduce) {
      .pestanas a,
      .pestanas a::after {
        transition: none;
      }
    }
    @media (max-width: 760px) {
      .area {
        display: none;
      }
      .secciones {
        padding: 6px;
      }
    }
  `,
})
export class MarcoGestion {
  private readonly sesion = inject(SessionStore);

  /** roleGuard ya garantiza el rol; un cliente que llegara aquí vería la barra vacía. */
  protected readonly enlaces = computed(() => {
    const rol = this.sesion.rol();
    return rol && rol !== 'CLIENTE' ? ENLACES[rol] : [];
  });
  protected readonly titulo = computed(() =>
    this.sesion.rol() === 'ADMINISTRADOR' ? 'Administración' : 'Mi negocio',
  );
}
