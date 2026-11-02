import { NgModule } from '@angular/core';
import { RouterModule, type Routes } from '@angular/router';
import { authGuard, guestGuard, roleGuard } from '../core/auth/guards';
import { SharedModule } from '../shared/shared-module';
import { Login } from './auth/login';
import { Registro } from './auth/registro';
import { Catalogo } from './catalogo/catalogo';
import { MiCuenta } from './cuenta/mi-cuenta';
import { MotelDetalle } from './motel/motel-detalle';
import { Reservar } from './reservar/reservar';
import { MisReservas } from './reservas/mis-reservas';

/** Lo que recorre un visitante y, con sesión, el cliente. */
const routes: Routes = [
  { path: '', title: 'Moteles · VesperaRoom', component: Catalogo },
  { path: 'motel/:id', title: 'Motel · VesperaRoom', component: MotelDetalle },
  { path: 'login', title: 'Ingresar · VesperaRoom', canActivate: [guestGuard], component: Login },
  { path: 'registro', title: 'Crear cuenta · VesperaRoom', canActivate: [guestGuard], component: Registro },
  { path: 'mi-cuenta', title: 'Mi cuenta · VesperaRoom', canActivate: [authGuard], component: MiCuenta },
  {
    path: 'reservar/:idHabitacion',
    title: 'Reservar · VesperaRoom',
    canActivate: [roleGuard('CLIENTE')],
    component: Reservar,
  },
  {
    path: 'mis-reservas',
    title: 'Mis reservas · VesperaRoom',
    canActivate: [roleGuard('CLIENTE')],
    component: MisReservas,
  },
];

@NgModule({
  declarations: [Catalogo, MotelDetalle, Login, Registro, MiCuenta, Reservar, MisReservas],
  imports: [SharedModule, RouterModule.forChild(routes)],
})
export class SitioModule {}
