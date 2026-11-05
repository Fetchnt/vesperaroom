import { Component } from '@angular/core';

@Component({
  standalone: false,
  selector: 'app-sin-permisos',
  template: `
    <div class="tarjeta pila error-ilustrado">
      <img src="/imagenes/puerta.webp" alt="" />
      <h1>Sin permisos</h1>
      <p class="suave">Tu cuenta no tiene acceso a esta sección.</p>
      <a routerLink="/">Volver al catálogo</a>
    </div>
  `,
})
export class SinPermisos {}
