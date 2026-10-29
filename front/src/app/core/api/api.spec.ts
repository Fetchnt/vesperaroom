import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../../environments/environment';
import { Api } from './api';

/**
 * El backend no usa los códigos habituales: las listas y los conteos vacíos
 * llegan como 204 sin cuerpo, `exists` responde 404 para decir «false» y los
 * errores son texto plano. Estas pruebas fijan esa traducción.
 */
describe('Api', () => {
  let api: Api;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    api = TestBed.inject(Api);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('convierte un 204 de lista vacía en un arreglo vacío', () => {
    let recibido: unknown[] | undefined;
    api.lista('/motel/getall').subscribe((datos) => (recibido = datos));

    http
      .expectOne(environment.apiUrl + '/motel/getall')
      .flush(null, { status: 204, statusText: 'No Content' });

    expect(recibido).toEqual([]);
  });

  it('entrega la lista cuando el backend responde 202', () => {
    let recibido: unknown[] | undefined;
    api.lista<{ idMotel: number }>('/motel/getall').subscribe((datos) => (recibido = datos));

    http
      .expectOne(environment.apiUrl + '/motel/getall')
      .flush([{ idMotel: 1 }], { status: 202, statusText: 'Accepted' });

    expect(recibido).toEqual([{ idMotel: 1 }]);
  });

  it('traduce un 404 de getbyid a null, no a un error', () => {
    let recibido: unknown = 'sin asignar';
    api.uno('/motel/getbyid/99').subscribe((dato) => (recibido = dato));

    http
      .expectOne(environment.apiUrl + '/motel/getbyid/99')
      .flush('Motel no encontrado', { status: 404, statusText: 'Not Found' });

    expect(recibido).toBeNull();
  });

  it('traduce el 404 de exists a false', () => {
    let recibido: boolean | undefined;
    api.existe('/motel/exists/99').subscribe((hay) => (recibido = hay));

    http
      .expectOne(environment.apiUrl + '/motel/exists/99')
      .flush(false, { status: 404, statusText: 'Not Found' });

    expect(recibido).toBe(false);
  });

  it('convierte el 204 de count en cero', () => {
    let recibido: number | undefined;
    api.conteo('/motel/count').subscribe((total) => (recibido = total));

    http.expectOne(environment.apiUrl + '/motel/count').flush(null, { status: 204, statusText: 'No Content' });

    expect(recibido).toBe(0);
  });

  it('usa el texto plano del backend como mensaje de error', () => {
    let mensaje = '';
    api.crear('/motel/createjson', {}).subscribe({ error: (e: Error) => (mensaje = e.message) });

    http
      .expectOne(environment.apiUrl + '/motel/createjson')
      .flush('El nombre del motel ya existe', { status: 409, statusText: 'Conflict' });

    expect(mensaje).toBe('El nombre del motel ya existe');
  });

  it('explica el fallo de red cuando no hay servidor', () => {
    let mensaje = '';
    api.lista('/motel/getall').subscribe({ error: (e: Error) => (mensaje = e.message) });

    http.expectOne(environment.apiUrl + '/motel/getall').error(new ProgressEvent('error'), { status: 0 });

    expect(mensaje).toContain('No pudimos conectarnos');
  });

  it('manda el id de updatejson por query, como espera el controller', () => {
    api.actualizar('/motel/updatejson', { nombre: 'Luna' }, { id: 7 }).subscribe();

    const peticion = http.expectOne(
      (r) => r.url === environment.apiUrl + '/motel/updatejson' && r.params.get('id') === '7',
    );
    expect(peticion.request.method).toBe('PUT');
    peticion.flush('Motel actualizado', { status: 202, statusText: 'Accepted' });
  });
});
