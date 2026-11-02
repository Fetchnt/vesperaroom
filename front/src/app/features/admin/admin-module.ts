import { NgModule } from '@angular/core';
import { RouterModule, type Routes } from '@angular/router';
import { SharedModule } from '../../shared/shared-module';
import { MotelGestion } from '../panel/motel-gestion';
import { MotelGestionModule } from '../panel/motel-gestion-module';
import { Catalogos } from './catalogos';
import { MotelesAdmin } from './moteles';
import { PagosAdmin } from './pagos';
import { Propietarios } from './propietarios';
import { ResenasAdmin } from './resenas';
import { ReservasAdmin } from './reservas';
import { Usuarios } from './usuarios';

const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'usuarios' },
  { path: 'usuarios', title: 'Usuarios · VesperaRoom', component: Usuarios },
  { path: 'propietarios', title: 'Propietarios · VesperaRoom', component: Propietarios },
  { path: 'moteles', title: 'Moteles · VesperaRoom', component: MotelesAdmin },
  { path: 'moteles/:id', title: 'Gestión del motel · VesperaRoom', component: MotelGestion },
  { path: 'resenas', title: 'Reseñas · VesperaRoom', component: ResenasAdmin },
  { path: 'catalogos', title: 'Catálogos · VesperaRoom', component: Catalogos },
  { path: 'reservas', title: 'Reservas · VesperaRoom', component: ReservasAdmin },
  { path: 'pagos', title: 'Pagos · VesperaRoom', component: PagosAdmin },
];

@NgModule({
  declarations: [Usuarios, Propietarios, MotelesAdmin, ResenasAdmin, Catalogos, ReservasAdmin, PagosAdmin],
  imports: [SharedModule, MotelGestionModule, RouterModule.forChild(routes)],
})
export class AdminModule {}
