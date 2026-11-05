import type { Rol } from '../api/models';

export interface Enlace {
  ruta: string;
  texto: string;
}

/** Roles con área de gestión propia (/admin y /panel). */
export type RolGestion = Exclude<Rol, 'CLIENTE'>;

/** Secciones de la barra lateral de cada área de gestión. */
export const ENLACES: Record<RolGestion, Enlace[]> = {
  PROPIETARIO: [
    { ruta: '/panel/moteles', texto: 'Mis moteles' },
    { ruta: '/panel/reservas', texto: 'Reservas' },
    { ruta: '/panel/resenas', texto: 'Reseñas' },
    { ruta: '/panel/perfil', texto: 'Mi perfil' },
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
  ],
};

/**
 * Menú del avatar: la entrada al área propia de cada rol y la cuenta. Las
 * secciones de gestión no se repiten aquí porque ya están en la barra lateral.
 */
export const MENU: Record<Rol, Enlace[]> = {
  CLIENTE: [
    { ruta: '/mis-reservas', texto: 'Mis reservas' },
    { ruta: '/mi-cuenta', texto: 'Mi cuenta' },
  ],
  PROPIETARIO: [
    { ruta: '/panel', texto: 'Mi negocio' },
    { ruta: '/mi-cuenta', texto: 'Mi cuenta' },
  ],
  ADMINISTRADOR: [
    { ruta: '/admin', texto: 'Administración' },
    { ruta: '/mi-cuenta', texto: 'Mi cuenta' },
  ],
};
