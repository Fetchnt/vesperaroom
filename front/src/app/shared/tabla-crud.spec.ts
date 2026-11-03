import { Component, NgModule } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { SharedModule } from './shared-module';
import { type AccionFila, type Campo, type Registro, TablaCrud, textoCelda } from './tabla-crud';

const CAMPOS: Campo[] = [
  { clave: 'nombre', etiqueta: 'Nombre' },
  { clave: 'estado', etiqueta: 'Estado', esEstado: true },
];

const FILAS: Registro[] = [
  { id: 1, nombre: 'Motel Sánchez', estado: 'PENDIENTE' },
  { id: 2, nombre: 'Motel Luna', estado: 'CONFIRMADA' },
  { id: 3, nombre: 'Motel Sol', estado: 'PENDIENTE' },
];

@Component({
  standalone: false,
  template: `
    <app-tabla-crud claveId="id" [campos]="campos" [cargar]="cargar" [guardar]="guardar" [vistaPrevia]="vista" />
    <ng-template #vista let-m><p class="previa">Previa: {{ m['nombre'] }}</p></ng-template>
  `,
})
class Anfitrion {
  campos = CAMPOS;
  cargar = () => of(FILAS);
  guardar = () => of('ok');
}

@NgModule({ declarations: [Anfitrion], imports: [SharedModule] })
class ModuloAnfitrion {}

function montar(acciones: AccionFila[] = []) {
  // jsdom no implementa <dialog>: basta con que showModal/close abran y cierren
  HTMLDialogElement.prototype.showModal ??= function (this: HTMLDialogElement) {
    this.open = true;
  };
  HTMLDialogElement.prototype.close ??= function (this: HTMLDialogElement) {
    this.open = false;
    this.dispatchEvent(new Event('close'));
  };

  TestBed.configureTestingModule({ imports: [SharedModule], providers: [provideRouter([])] });
  const fixture = TestBed.createComponent(TablaCrud);
  fixture.componentRef.setInput('campos', CAMPOS);
  fixture.componentRef.setInput('claveId', 'id');
  fixture.componentRef.setInput('cargar', () => of(FILAS));
  fixture.componentRef.setInput('acciones', acciones);
  fixture.detectChanges();
  const dom = fixture.nativeElement as HTMLElement;
  const filas = () => dom.querySelectorAll('tbody tr').length;
  const boton = (texto: string) =>
    [...dom.querySelectorAll('button')].find((b) => b.textContent?.trim().startsWith(texto))!;
  return { fixture, dom, filas, boton };
}

describe('textoCelda', () => {
  it('lee las fechas sin zona como hora local y no cambia de día', () => {
    expect(textoCelda('2026-11-02', { clave: 'f', etiqueta: 'F', tipo: 'fecha' })).toContain('2');
    expect(textoCelda('2026-11-02', { clave: 'f', etiqueta: 'F', tipo: 'fecha' })).toContain('2026');
    const hora = textoCelda('2026-11-02T14:30:00', { clave: 'f', etiqueta: 'F', tipo: 'fechaHora' });
    expect(hora).not.toContain('T14');
    expect(hora).toContain('30');
  });

  it('muestra el texto de la opción y no el id', () => {
    const campo: Campo = { clave: 'u', etiqueta: 'U', tipo: 'select', opciones: [{ valor: 5, texto: 'Ana Ruiz' }] };
    expect(textoCelda(5, campo)).toBe('Ana Ruiz');
    expect(textoCelda(null, campo)).toBe('—');
  });
});

describe('TablaCrud', () => {
  it('filtra por texto sin importar tildes ni mayúsculas', () => {
    const { fixture, dom, filas } = montar();
    expect(filas()).toBe(3);

    const buscar = dom.querySelector<HTMLInputElement>('input[type=search]')!;
    buscar.value = 'sanchez';
    buscar.dispatchEvent(new Event('input'));
    fixture.detectChanges();

    expect(filas()).toBe(1);
    expect(dom.textContent).toContain('1 de 3 registros');
  });

  it('filtra por estado con su conteo', () => {
    const { fixture, filas, boton } = montar();
    expect(boton('Pendiente').textContent).toContain('2');

    boton('Pendiente').click();
    fixture.detectChanges();
    expect(filas()).toBe(2);

    boton('Todos').click();
    fixture.detectChanges();
    expect(filas()).toBe(3);
  });

  it('la pestaña Vista previa recibe el borrador y los datos se conservan', async () => {
    TestBed.configureTestingModule({ imports: [ModuloAnfitrion], providers: [provideRouter([])] });
    const fixture = TestBed.createComponent(Anfitrion);
    fixture.detectChanges();
    const dom = fixture.nativeElement as HTMLElement;
    const boton = (texto: string) => [...dom.querySelectorAll('button')].find((b) => b.textContent?.trim() === texto)!;

    boton('Agregar').click();
    fixture.detectChanges();
    // ngModel escribe su valor inicial en una microtarea: hay que esperarla antes de teclear
    await fixture.whenStable();
    const nombre = dom.querySelector<HTMLInputElement>('.formulario input')!;
    nombre.value = 'Motel Aurora';
    nombre.dispatchEvent(new Event('input'));

    boton('Vista previa').click();
    fixture.detectChanges();
    expect(dom.querySelector('.previa')?.textContent).toContain('Motel Aurora');
    expect(dom.querySelector('.formulario')!.hasAttribute('hidden')).toBe(true);

    boton('Datos').click();
    fixture.detectChanges();
    expect(dom.querySelector('.previa')).toBeNull();
    expect(dom.querySelector<HTMLInputElement>('.formulario input')!.value).toBe('Motel Aurora');
  });

  it('solo ejecuta la acción si se confirma en el diálogo', () => {
    const ejecutar = vi.fn(() => of('Listo'));
    const { fixture, dom, boton } = montar([{ texto: 'Cancelar reserva', confirmar: '¿Seguro?', ejecutar }]);

    boton('Cancelar reserva').click();
    fixture.detectChanges();
    expect(dom.querySelector('dialog')!.open).toBe(true);
    boton('Volver').click();
    fixture.detectChanges();
    expect(ejecutar).not.toHaveBeenCalled();

    boton('Cancelar reserva').click();
    fixture.detectChanges();
    [...dom.querySelectorAll('dialog button')].at(-1)!.dispatchEvent(new Event('click'));
    fixture.detectChanges();
    expect(ejecutar).toHaveBeenCalledTimes(1);
  });
});
