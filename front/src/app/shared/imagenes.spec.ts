import { IMAGENES, usarSinFoto } from './imagenes';

describe('usarSinFoto', () => {
  it('cambia una foto rota por la genérica una sola vez', () => {
    const img = document.createElement('img');
    img.src = 'https://cdn.ejemplo.com/rota.jpg';

    usarSinFoto({ target: img } as unknown as Event);
    expect(img.src.endsWith(IMAGENES.sinFoto)).toBe(true);

    // Si la genérica también falla, no se vuelve a asignar (evita un ciclo de error → load)
    const asignar = vi.spyOn(img, 'src', 'set');
    usarSinFoto({ target: img } as unknown as Event);
    expect(asignar).not.toHaveBeenCalled();
  });
});
