import { NgModule } from '@angular/core';
import { RouterModule, type Routes } from '@angular/router';
import { SharedModule } from '../../shared/shared-module';
import { MiPerfil } from './mi-perfil';
import { MisMoteles } from './mis-moteles';
import { MotelGestion } from './motel-gestion';
import { MotelGestionModule } from './motel-gestion-module';
import { ResenasMotel } from './resenas-motel';
import { ReservasMotel } from './reservas-motel';

const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'moteles' },
  { path: 'moteles', title: 'Mis moteles · VesperaRoom', component: MisMoteles },
  { path: 'moteles/:id', title: 'Gestión del motel · VesperaRoom', component: MotelGestion },
  { path: 'reservas', title: 'Reservas · VesperaRoom', component: ReservasMotel },
  { path: 'resenas', title: 'Reseñas · VesperaRoom', component: ResenasMotel },
  { path: 'perfil', title: 'Mi perfil · VesperaRoom', component: MiPerfil },
];

@NgModule({
  declarations: [MisMoteles, ReservasMotel, ResenasMotel, MiPerfil],
  imports: [SharedModule, MotelGestionModule, RouterModule.forChild(routes)],
})
export class PanelModule {}
