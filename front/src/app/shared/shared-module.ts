import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { Buscador } from './buscador';
import { Cabecera } from './cabecera';
import { EstadoBadge } from './estado-badge';
import { Estrellas } from './estrellas';
import { MarcoGestion } from './marco-gestion';
import { TablaCrud } from './tabla-crud';
import { VistaMotel } from './vista-motel';

/** Piezas comunes y los módulos de Angular que usan todas las pantallas. */
@NgModule({
  declarations: [Buscador, Cabecera, EstadoBadge, Estrellas, MarcoGestion, TablaCrud, VistaMotel],
  imports: [CommonModule, FormsModule, RouterModule],
  exports: [
    CommonModule,
    FormsModule,
    RouterModule,
    Buscador,
    Cabecera,
    EstadoBadge,
    Estrellas,
    MarcoGestion,
    TablaCrud,
    VistaMotel,
  ],
})
export class SharedModule {}
