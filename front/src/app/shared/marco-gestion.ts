import { Component, computed, inject } from '@angular/core';
import { ENLACES } from '../core/auth/enlaces';
import { SessionStore } from '../core/auth/session';

/**
 * Marco de las áreas de gestión (/admin y /panel): barra lateral con las
 * secciones del rol y la sección actual a la derecha. En pantallas angostas
 * la barra pasa a una fila de pestañas desplazable.
 */
@Component({
  standalone: false,
  selector: 'app-marco-gestion',
  template: `
    <div class="marco">
      <nav class="lateral" aria-label="Secciones de gestión">
        <p class="rol suave pequeno">{{ titulo() }}</p>
        @for (enlace of enlaces(); track enlace.ruta) {
          <a [routerLink]="enlace.ruta" routerLinkActive="activo" ariaCurrentWhenActive="page">{{ enlace.texto }}</a>
        }
      </nav>
      <section class="contenido">
        <router-outlet />
      </section>
    </div>
  `,
  styles: `
    .marco {
      display: grid;
      grid-template-columns: 200px minmax(0, 1fr);
      gap: 32px;
      align-items: start;
    }
    .lateral {
      position: sticky;
      top: 88px;
      display: flex;
      flex-direction: column;
      gap: 2px;
      padding: 12px;
      border: 1px solid var(--borde);
      border-radius: var(--r-l);
      background: var(--superficie);
    }
    .rol {
      margin: 0 0 6px;
      padding: 0 10px;
      text-transform: uppercase;
      letter-spacing: 0.06em;
      font-weight: 600;
    }
    .lateral a {
      padding: 8px 10px;
      border-radius: var(--r-s);
      color: var(--texto);
      font-weight: 500;
      font-size: 0.92rem;
    }
    .lateral a:hover {
      text-decoration: none;
      background: var(--superficie-2);
    }
    .lateral a.activo {
      background: var(--marca-tenue);
      color: var(--marca-fuerte);
      box-shadow: inset 3px 0 0 var(--marca-solida);
    }
    @media (max-width: 760px) {
      .marco {
        grid-template-columns: minmax(0, 1fr);
        gap: 16px;
      }
      .lateral {
        position: static;
        flex-direction: row;
        overflow-x: auto;
        padding: 6px;
      }
      .rol {
        display: none;
      }
      .lateral a {
        white-space: nowrap;
      }
      .lateral a.activo {
        box-shadow: inset 0 -2px 0 var(--marca-solida);
      }
    }
  `,
})
export class MarcoGestion {
  private readonly sesion = inject(SessionStore);

  /** «Mi cuenta» vive fuera del marco, así que queda solo en el menú de usuario. */
  protected readonly enlaces = computed(() => {
    const rol = this.sesion.rol();
    return (rol ? ENLACES[rol] : []).filter((e) => e.ruta !== '/mi-cuenta');
  });
  protected readonly titulo = computed(() =>
    this.sesion.rol() === 'ADMINISTRADOR' ? 'Administración' : 'Mi negocio',
  );
}
