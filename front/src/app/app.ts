import { Component, computed, inject } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from './core/auth/auth';
import { ENLACES, type Enlace } from './core/auth/enlaces';
import { SessionStore } from './core/auth/session';

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
    const rol = this.sesion.rol();
    return rol ? ENLACES[rol] : [];
  });

  protected salir(): void {
    this.auth.salir();
    void this.router.navigate(['/']);
  }
}
