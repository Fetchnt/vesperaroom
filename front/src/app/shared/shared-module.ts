import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { Buscador } from './buscador';
import { EstadoBadge } from './estado-badge';
import { Estrellas } from './estrellas';
import { MarcoGestion } from './marco-gestion';
import { TablaCrud } from './tabla-crud';

/** Piezas comunes y los módulos de Angular que usan todas las pantallas. */
@NgModule({
  declarations: [Buscador, EstadoBadge, Estrellas, MarcoGestion, TablaCrud],
  imports: [CommonModule, FormsModule, RouterModule],
  exports: [CommonModule, FormsModule, RouterModule, Buscador, EstadoBadge, Estrellas, MarcoGestion, TablaCrud],
})
export class SharedModule {}
