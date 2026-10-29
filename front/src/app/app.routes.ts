import type { Routes } from '@angular/router';
import { authGuard, guestGuard, roleGuard } from './core/auth/guards';

export const routes: Routes = [
  {
    path: '',
    title: 'Moteles · VesperaRoom',
    loadComponent: () => import('./features/catalogo/catalogo').then((m) => m.Catalogo),
  },
  {
    path: 'motel/:id',
    title: 'Motel · VesperaRoom',
    loadComponent: () => import('./features/motel/motel-detalle').then((m) => m.MotelDetalle),
  },
  {
    path: 'login',
    title: 'Ingresar · VesperaRoom',
    canActivate: [guestGuard],
    loadComponent: () => import('./features/auth/login').then((m) => m.Login),
  },
  {
    path: 'registro',
    title: 'Crear cuenta · VesperaRoom',
    canActivate: [guestGuard],
    loadComponent: () => import('./features/auth/registro').then((m) => m.Registro),
  },
  {
    path: 'mi-cuenta',
    title: 'Mi cuenta · VesperaRoom',
    canActivate: [authGuard],
    loadComponent: () => import('./features/cuenta/mi-cuenta').then((m) => m.MiCuenta),
  },
  {
    path: 'reservar/:idHabitacion',
    title: 'Reservar · VesperaRoom',
    canActivate: [roleGuard('CLIENTE')],
    loadComponent: () => import('./features/reservar/reservar').then((m) => m.Reservar),
  },
  {
    path: 'mis-reservas',
    title: 'Mis reservas · VesperaRoom',
    canActivate: [roleGuard('CLIENTE')],
    loadComponent: () => import('./features/reservas/mis-reservas').then((m) => m.MisReservas),
  },
  {
    path: 'panel',
    canActivate: [roleGuard('PROPIETARIO')],
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'moteles' },
      {
        path: 'moteles',
        title: 'Mis moteles · VesperaRoom',
        loadComponent: () => import('./features/panel/mis-moteles').then((m) => m.MisMoteles),
      },
      {
        path: 'moteles/:id',
        title: 'Gestión del motel · VesperaRoom',
        loadComponent: () => import('./features/panel/motel-gestion').then((m) => m.MotelGestion),
      },
      {
        path: 'reservas',
        title: 'Reservas · VesperaRoom',
        loadComponent: () => import('./features/panel/reservas-motel').then((m) => m.ReservasMotel),
      },
      {
        path: 'resenas',
        title: 'Reseñas · VesperaRoom',
        loadComponent: () => import('./features/panel/resenas-motel').then((m) => m.ResenasMotel),
      },
      {
        path: 'perfil',
        title: 'Mi perfil · VesperaRoom',
        loadComponent: () => import('./features/panel/mi-perfil').then((m) => m.MiPerfil),
      },
    ],
  },
  {
    path: 'admin',
    canActivate: [roleGuard('ADMINISTRADOR')],
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'usuarios' },
      {
        path: 'usuarios',
        title: 'Usuarios · VesperaRoom',
        loadComponent: () => import('./features/admin/usuarios').then((m) => m.Usuarios),
      },
      {
        path: 'propietarios',
        title: 'Propietarios · VesperaRoom',
        loadComponent: () => import('./features/admin/propietarios').then((m) => m.Propietarios),
      },
      {
        path: 'moteles',
        title: 'Moteles · VesperaRoom',
        loadComponent: () => import('./features/admin/moteles').then((m) => m.MotelesAdmin),
      },
      {
        path: 'catalogos',
        title: 'Catálogos · VesperaRoom',
        loadComponent: () => import('./features/admin/catalogos').then((m) => m.Catalogos),
      },
      {
        path: 'reservas',
        title: 'Reservas · VesperaRoom',
        loadComponent: () => import('./features/admin/reservas').then((m) => m.ReservasAdmin),
      },
      {
        path: 'pagos',
        title: 'Pagos · VesperaRoom',
        loadComponent: () => import('./features/admin/pagos').then((m) => m.PagosAdmin),
      },
    ],
  },
  {
    path: 'sin-permisos',
    title: 'Sin permisos · VesperaRoom',
    loadComponent: () => import('./features/errores/sin-permisos').then((m) => m.SinPermisos),
  },
  {
    path: '**',
    title: 'Página no encontrada · VesperaRoom',
    loadComponent: () => import('./features/errores/no-encontrada').then((m) => m.NoEncontrada),
  },
];
