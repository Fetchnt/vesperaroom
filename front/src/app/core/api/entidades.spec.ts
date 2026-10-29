import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../../environments/environment';
import { FotoApi, PagoApi } from './entidades';
import type { Foto, Pago } from './models';

describe('FotoApi.todasDeMotel', () => {
  let api: FotoApi;
  let http: HttpTestingController;
  const url = (ruta: string) => environment.apiUrl + ruta;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    api = TestBed.inject(FotoApi);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('junta las fotos del motel con las de cada habitación', () => {
    let fotos: Foto[] = [];
    api.todasDeMotel(1).subscribe((f) => (fotos = f));

    http.expectOne(url('/foto/getbymotel/1')).flush([{ idFoto: 1, idMotel: 1, uri: 'a' }]);
    http.expectOne(url('/habitacion/getbymotel/1')).flush([{ idHabitacion: 5 }, { idHabitacion: 6 }]);
    http.expectOne(url('/foto/getbyhabitacion/5')).flush([{ idFoto: 2, idHabitacion: 5, uri: 'b' }]);
    http.expectOne(url('/foto/getbyhabitacion/6')).flush(null, { status: 204, statusText: 'No Content' });

    expect(fotos.map((f) => f.uri)).toEqual(['a', 'b']);
  });

  it('no se queda esperando cuando el motel no tiene habitaciones', () => {
    let fotos: Foto[] | undefined;
    api.todasDeMotel(1).subscribe((f) => (fotos = f));

    http.expectOne(url('/foto/getbymotel/1')).flush(null, { status: 204, statusText: 'No Content' });
    http.expectOne(url('/habitacion/getbymotel/1')).flush(null, { status: 204, statusText: 'No Content' });

    expect(fotos).toEqual([]);
  });
});

describe('PagoApi.porReserva', () => {
  let api: PagoApi;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    api = TestBed.inject(PagoApi);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('toma el pago más reciente de la lista que devuelve el backend', () => {
    let pago: Pago | null | undefined;
    api.porReserva(1).subscribe((p) => (pago = p));

    http.expectOne(environment.apiUrl + '/pago/getbyreserva/1').flush([
      { idPago: 1, idReserva: 1, monto: 100, metodoPago: 'PSE', estado: 'RECHAZADO' },
      { idPago: 2, idReserva: 1, monto: 100, metodoPago: 'NEQUI', estado: 'APROBADO' },
    ]);

    expect(pago?.idPago).toBe(2);
  });

  it('devuelve null cuando la reserva no tiene pagos (204)', () => {
    let pago: Pago | null | undefined;
    api.porReserva(1).subscribe((p) => (pago = p));

    http.expectOne(environment.apiUrl + '/pago/getbyreserva/1').flush(null, { status: 204, statusText: 'No Content' });

    expect(pago).toBeNull();
  });
});
