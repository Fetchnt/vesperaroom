import { ENLACES, MENU, type RolGestion } from './enlaces';

describe('enlaces', () => {
  it('todos los roles tienen «Mi cuenta» en el menú del avatar', () => {
    for (const enlaces of Object.values(MENU)) {
      expect(enlaces.map((e) => e.ruta)).toContain('/mi-cuenta');
    }
  });

  it('el menú no repite las secciones de la barra lateral', () => {
    for (const rol of Object.keys(ENLACES) as RolGestion[]) {
      const lateral = ENLACES[rol].map((e) => e.ruta);
      expect(MENU[rol].filter((e) => lateral.includes(e.ruta))).toEqual([]);
    }
  });
});
