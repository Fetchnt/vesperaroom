import type { Routes } from '@angular/router';
import { roleGuard } from './core/auth/guards';
import { NoEncontrada } from './features/errores/no-encontrada';
import { SinPermisos } from './features/errores/sin-permisos';

export const routes: Routes = [
  {
    path: 'panel',
    canActivate: [roleGuard('PROPIETARIO')],
    loadChildren: () => import('./features/panel/panel-module').then((m) => m.PanelModule),
  },
  {
    path: 'admin',
    canActivate: [roleGuard('ADMINISTRADOR')],
    loadChildren: () => import('./features/admin/admin-module').then((m) => m.AdminModule),
  },
  { path: 'sin-permisos', title: 'Sin permisos · VesperaRoom', component: SinPermisos },
  // El sitio va después de panel y admin: su ruta vacía recibe todo lo demás.
  { path: '', loadChildren: () => import('./features/sitio-module').then((m) => m.SitioModule) },
  { path: '**', title: 'Página no encontrada · VesperaRoom', component: NoEncontrada },
];
