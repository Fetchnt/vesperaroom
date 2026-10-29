import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-no-encontrada',
  imports: [RouterLink],
  template: `
    <div class="tarjeta pila">
      <h1>Página no encontrada</h1>
      <p class="suave">La dirección que abriste no existe.</p>
      <a routerLink="/">Volver al catálogo</a>
    </div>
  `,
})
export class NoEncontrada {}
