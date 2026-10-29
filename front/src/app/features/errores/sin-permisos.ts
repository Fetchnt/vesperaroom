import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-sin-permisos',
  imports: [RouterLink],
  template: `
    <div class="tarjeta pila">
      <h1>Sin permisos</h1>
      <p class="suave">Tu cuenta no tiene acceso a esta sección.</p>
      <a routerLink="/">Volver al catálogo</a>
    </div>
  `,
})
export class SinPermisos {}
