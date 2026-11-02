import { Component, computed, inject } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from './core/auth/auth';
import { SessionStore } from './core/auth/session';

interface Enlace {
  ruta: string;
  texto: string;
}

@Component({
  standalone: false,
  selector: 'app-root',
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  protected readonly sesion = inject(SessionStore);

  /** El llamado a dueños sobra para quien ya es propietario o administra. */
  protected readonly invitarDuenos = computed(() => {
    const rol = this.sesion.rol();
    return rol !== 'PROPIETARIO' && rol !== 'ADMINISTRADOR';
  });

  /** Lo que va en el menú de usuario, según el rol. */
  protected readonly enlaces = computed<Enlace[]>(() => {
    switch (this.sesion.rol()) {
      case 'CLIENTE':
        return [
          { ruta: '/mis-reservas', texto: 'Mis reservas' },
          { ruta: '/mi-cuenta', texto: 'Mi cuenta' },
        ];
      case 'PROPIETARIO':
        return [
          { ruta: '/panel/moteles', texto: 'Mis moteles' },
          { ruta: '/panel/reservas', texto: 'Reservas' },
          { ruta: '/panel/resenas', texto: 'Reseñas' },
          { ruta: '/panel/perfil', texto: 'Mi perfil' },
          { ruta: '/mi-cuenta', texto: 'Mi cuenta' },
        ];
      case 'ADMINISTRADOR':
        return [
          { ruta: '/admin/usuarios', texto: 'Usuarios' },
          { ruta: '/admin/propietarios', texto: 'Propietarios' },
          { ruta: '/admin/moteles', texto: 'Moteles' },
          { ruta: '/admin/catalogos', texto: 'Catálogos' },
          { ruta: '/admin/reservas', texto: 'Reservas' },
          { ruta: '/admin/pagos', texto: 'Pagos' },
          { ruta: '/admin/resenas', texto: 'Reseñas' },
          { ruta: '/mi-cuenta', texto: 'Mi cuenta' },
        ];
      default:
        return [];
    }
  });

  protected salir(): void {
    this.auth.salir();
    void this.router.navigate(['/']);
  }
}
