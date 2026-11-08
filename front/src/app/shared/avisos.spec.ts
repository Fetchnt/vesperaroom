import { TestBed } from '@angular/core/testing';
import { Avisos, AvisosComponent } from './avisos';
import { SharedModule } from './shared-module';

describe('Avisos', () => {
  afterEach(() => vi.useRealTimers());

  it('resuelve la confirmación según la respuesta y descarta la pregunta anterior', async () => {
    const avisos = new Avisos();

    const primera = avisos.confirmar({ titulo: '¿Eliminar?', aceptar: 'Eliminar' });
    const segunda = avisos.confirmar({ titulo: '¿Cancelar?', aceptar: 'Cancelar' });
    await expect(primera).resolves.toBe(false);
    expect(avisos.pregunta()?.titulo).toBe('¿Cancelar?');

    avisos.responder(true);
    await expect(segunda).resolves.toBe(true);
    expect(avisos.pregunta()).toBeNull();
  });

  it('los avisos se cierran solos y no se acumulan más de cuatro', () => {
    vi.useFakeTimers();
    const avisos = new Avisos();

    for (let i = 1; i <= 5; i++) {
      avisos.exito(`Listo ${i}`);
    }
    avisos.error('Falló');
    expect(avisos.lista().map((a) => a.texto)).toEqual(['Listo 3', 'Listo 4', 'Listo 5', 'Falló']);

    vi.advanceTimersByTime(4500);
    expect(avisos.lista().map((a) => a.texto)).toEqual(['Falló']);
    vi.advanceTimersByTime(3500);
    expect(avisos.lista()).toEqual([]);
  });

  it('el diálogo se abre con la pregunta y «Volver» responde que no', async () => {
    // jsdom no implementa <dialog>: basta con que showModal/close abran y cierren
    HTMLDialogElement.prototype.showModal ??= function (this: HTMLDialogElement) {
      this.open = true;
    };
    HTMLDialogElement.prototype.close ??= function (this: HTMLDialogElement) {
      this.open = false;
      this.dispatchEvent(new Event('close'));
    };
    TestBed.configureTestingModule({ imports: [SharedModule] });
    const fixture = TestBed.createComponent(AvisosComponent);
    const avisos = TestBed.inject(Avisos);
    const dom = fixture.nativeElement as HTMLElement;

    const respuesta = avisos.confirmar({ titulo: '¿Cancelar la reserva VR-1?', aceptar: 'Cancelar reserva', peligro: true });
    fixture.detectChanges();
    const dialogo = dom.querySelector('dialog')!;
    expect(dialogo.open).toBe(true);
    expect(dialogo.classList).toContain('peligro');
    expect(dom.querySelector('h2')!.textContent).toBe('¿Cancelar la reserva VR-1?');

    [...dom.querySelectorAll('button')].find((b) => b.textContent?.trim() === 'Volver')!.click();
    await expect(respuesta).resolves.toBe(false);
    fixture.detectChanges();
    expect(dialogo.open).toBe(false);
  });
});
