import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { EstadoBadge } from './estado-badge';
import { Estrellas } from './estrellas';
import { TablaCrud } from './tabla-crud';

/** Piezas comunes y los módulos de Angular que usan todas las pantallas. */
@NgModule({
  declarations: [EstadoBadge, Estrellas, TablaCrud],
  imports: [CommonModule, FormsModule, RouterModule],
  exports: [CommonModule, FormsModule, RouterModule, EstadoBadge, Estrellas, TablaCrud],
})
export class SharedModule {}
