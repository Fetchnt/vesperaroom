import type { Rol } from '../api/models';

export interface Enlace {
  ruta: string;
  texto: string;
}

/** Secciones de cada rol: las usan el menú de usuario y la barra lateral de gestión. */
export const ENLACES: Record<Rol, Enlace[]> = {
  CLIENTE: [
    { ruta: '/mis-reservas', texto: 'Mis reservas' },
    { ruta: '/mi-cuenta', texto: 'Mi cuenta' },
  ],
  PROPIETARIO: [
    { ruta: '/panel/moteles', texto: 'Mis moteles' },
    { ruta: '/panel/reservas', texto: 'Reservas' },
    { ruta: '/panel/resenas', texto: 'Reseñas' },
    { ruta: '/panel/perfil', texto: 'Mi perfil' },
    { ruta: '/mi-cuenta', texto: 'Mi cuenta' },
  ],
  ADMINISTRADOR: [
    { ruta: '/admin/inicio', texto: 'Resumen' },
    { ruta: '/admin/usuarios', texto: 'Usuarios' },
    { ruta: '/admin/propietarios', texto: 'Propietarios' },
    { ruta: '/admin/moteles', texto: 'Moteles' },
    { ruta: '/admin/catalogos', texto: 'Catálogos' },
    { ruta: '/admin/reservas', texto: 'Reservas' },
    { ruta: '/admin/pagos', texto: 'Pagos' },
    { ruta: '/admin/resenas', texto: 'Reseñas' },
    { ruta: '/mi-cuenta', texto: 'Mi cuenta' },
  ],
};
