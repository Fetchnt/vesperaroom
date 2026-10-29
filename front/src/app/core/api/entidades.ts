import { inject, Injectable } from '@angular/core';
import { forkJoin, map, of, switchMap, type Observable } from 'rxjs';
import { Api, crud, type Id } from './api';
import type {
  Caracteristica,
  Ciudad,
  Departamento,
  EstadoHabitacion,
  EstadoMotel,
  EstadoPago,
  EstadoReserva,
  EstadoUsuario,
  Foto,
  Habitacion,
  Modalidad,
  Motel,
  Pago,
  PerfilPropietario,
  PoliticaReserva,
  Resena,
  Reserva,
  RespuestaPropietario,
  ServicioAdicional,
  Tarifa,
  TipoHabitacion,
  Usuario,
} from './models';

/**
 * Un servicio por entidad. `crud` son los siete endpoints comunes y el resto
 * son los endpoints propios de cada controller del backend.
 */

@Injectable({ providedIn: 'root' })
export class DepartamentoApi {
  readonly crud = crud<Departamento>('departamento');
}

@Injectable({ providedIn: 'root' })
export class CiudadApi {
  readonly crud = crud<Ciudad>('ciudad');
}

@Injectable({ providedIn: 'root' })
export class CaracteristicaApi {
  readonly crud = crud<Caracteristica>('caracteristica');
}

@Injectable({ providedIn: 'root' })
export class ModalidadApi {
  readonly crud = crud<Modalidad>('modalidad');
}

@Injectable({ providedIn: 'root' })
export class UsuarioApi {
  private readonly api = inject(Api);
  readonly crud = crud<Usuario>('usuario');

  /** Solo admin: /getall devuelve todos, y hay un listado por cada rol. */
  administradores(): Observable<Usuario[]> {
    return this.api.lista<Usuario>('/usuario/getall/administradores');
  }

  clientes(): Observable<Usuario[]> {
    return this.api.lista<Usuario>('/usuario/getall/clientes');
  }

  propietarios(): Observable<Usuario[]> {
    return this.api.lista<Usuario>('/usuario/getall/propietarios');
  }

  porCorreo(correo: string): Observable<Usuario | null> {
    return this.api.uno<Usuario>('/usuario/getbycorreo', { correo });
  }

  cambiarEstado(id: Id, estado: EstadoUsuario): Observable<string> {
    return this.api.actualizar('/usuario/updateestado', null, { id, estado });
  }

  /** Disponible para los tres roles. */
  miCuenta(): Observable<Usuario | null> {
    return this.api.uno<Usuario>('/usuario/mi-cuenta');
  }

  actualizarMiCuenta(usuario: Usuario): Observable<string> {
    return this.api.actualizar('/usuario/mi-cuenta', usuario);
  }
}

@Injectable({ providedIn: 'root' })
export class PerfilPropietarioApi {
  private readonly api = inject(Api);
  readonly crud = crud<PerfilPropietario>('perfilpropietario');

  miPerfil(): Observable<PerfilPropietario | null> {
    return this.api.uno<PerfilPropietario>('/perfilpropietario/mi-perfil');
  }

  actualizarMiPerfil(perfil: PerfilPropietario): Observable<string> {
    return this.api.actualizar('/perfilpropietario/mi-perfil', perfil);
  }

  /** Solo admin. */
  verificar(id: Id): Observable<string> {
    return this.api.actualizar('/perfilpropietario/verificar', null, { id });
  }
}

@Injectable({ providedIn: 'root' })
export class MotelApi {
  private readonly api = inject(Api);
  readonly crud = crud<Motel>('motel');

  /** /motel/getall solo devuelve los activos; esto es el catálogo público. */
  activos(): Observable<Motel[]> {
    return this.api.lista<Motel>('/motel/getall');
  }

  /** Solo admin: incluye los inactivos. */
  todos(): Observable<Motel[]> {
    return this.api.lista<Motel>('/motel/getall/todos');
  }

  porCiudad(idCiudad: Id): Observable<Motel[]> {
    return this.api.lista<Motel>(`/motel/getbyciudad/${idCiudad}`);
  }

  mios(): Observable<Motel[]> {
    return this.api.lista<Motel>('/motel/mis-moteles');
  }

  cambiarEstado(id: Id, estado: EstadoMotel): Observable<string> {
    return this.api.actualizar('/motel/updateestado', null, { id, estado });
  }
}

@Injectable({ providedIn: 'root' })
export class PoliticaApi {
  private readonly api = inject(Api);
  /** 1:1 con motel: el id de la política es el idMotel. */
  readonly crud = crud<PoliticaReserva>('politica');

  deMotel(idMotel: Id): Observable<PoliticaReserva | null> {
    return this.api.uno<PoliticaReserva>(`/politica/getbyid/${idMotel}`);
  }
}

@Injectable({ providedIn: 'root' })
export class TipoHabitacionApi {
  private readonly api = inject(Api);
  readonly crud = crud<TipoHabitacion>('tipohabitacion');

  porMotel(idMotel: Id): Observable<TipoHabitacion[]> {
    return this.api.lista<TipoHabitacion>(`/tipohabitacion/getbymotel/${idMotel}`);
  }

  caracteristicas(idTipo: Id): Observable<Caracteristica[]> {
    return this.api.lista<Caracteristica>(`/tipohabitacion/caracteristicas/${idTipo}`);
  }

  asignarCaracteristica(idTipo: Id, idCaracteristica: Id): Observable<string> {
    return this.api.crear('/tipohabitacion/asignarcaracteristica', null, {
      idTipo,
      idCaracteristica,
    });
  }

  quitarCaracteristica(idTipo: Id, idCaracteristica: Id): Observable<string> {
    return this.api.eliminar('/tipohabitacion/quitarcaracteristica', {
      idTipo,
      idCaracteristica,
    });
  }
}

@Injectable({ providedIn: 'root' })
export class HabitacionApi {
  private readonly api = inject(Api);
  readonly crud = crud<Habitacion>('habitacion');

  porMotel(idMotel: Id): Observable<Habitacion[]> {
    return this.api.lista<Habitacion>(`/habitacion/getbymotel/${idMotel}`);
  }

  porTipo(idTipo: Id): Observable<Habitacion[]> {
    return this.api.lista<Habitacion>(`/habitacion/getbytipo/${idTipo}`);
  }

  /** Las fechas van en ISO sin zona, por ejemplo 2026-11-20T21:00:00. */
  disponibles(idMotel: Id, inicio: string, fin: string): Observable<Habitacion[]> {
    return this.api.lista<Habitacion>('/habitacion/disponibles', { idMotel, inicio, fin });
  }

  cambiarEstado(id: Id, estado: EstadoHabitacion): Observable<string> {
    return this.api.actualizar('/habitacion/updateestado', null, { id, estado });
  }
}

@Injectable({ providedIn: 'root' })
export class FotoApi {
  private readonly api = inject(Api);
  readonly crud = crud<Foto>('foto');

  porMotel(idMotel: Id): Observable<Foto[]> {
    return this.api.lista<Foto>(`/foto/getbymotel/${idMotel}`);
  }

  porHabitacion(idHabitacion: Id): Observable<Foto[]> {
    return this.api.lista<Foto>(`/foto/getbyhabitacion/${idHabitacion}`);
  }

  /** `getbymotel` solo trae las fotos generales; las de cada habitación se piden aparte. */
  todasDeMotel(idMotel: Id): Observable<Foto[]> {
    const deHabitaciones = this.api.lista<Habitacion>(`/habitacion/getbymotel/${idMotel}`).pipe(
      switchMap((habs) => (habs.length ? forkJoin(habs.map((h) => this.porHabitacion(h.idHabitacion!))) : of([]))),
      map((grupos) => grupos.flat()),
    );
    return forkJoin([this.porMotel(idMotel), deHabitaciones]).pipe(map(([motel, habs]) => [...motel, ...habs]));
  }
}

@Injectable({ providedIn: 'root' })
export class TarifaApi {
  private readonly api = inject(Api);
  readonly crud = crud<Tarifa>('tarifa');

  porTipo(idTipo: Id): Observable<Tarifa[]> {
    return this.api.lista<Tarifa>(`/tarifa/getbytipo/${idTipo}`);
  }
}

@Injectable({ providedIn: 'root' })
export class ServicioApi {
  private readonly api = inject(Api);
  readonly crud = crud<ServicioAdicional>('servicio');

  /** Solo los activos: es la vista pública. */
  porMotel(idMotel: Id): Observable<ServicioAdicional[]> {
    return this.api.lista<ServicioAdicional>(`/servicio/getbymotel/${idMotel}`);
  }

  /** Incluye los inactivos; requiere ser el propietario o admin. */
  todosPorMotel(idMotel: Id): Observable<ServicioAdicional[]> {
    return this.api.lista<ServicioAdicional>(`/servicio/getbymotel/${idMotel}/todos`);
  }
}

@Injectable({ providedIn: 'root' })
export class ReservaApi {
  private readonly api = inject(Api);
  readonly crud = crud<Reserva>('reserva');

  mias(): Observable<Reserva[]> {
    return this.api.lista<Reserva>('/reserva/mis-reservas');
  }

  porMotel(idMotel: Id): Observable<Reserva[]> {
    return this.api.lista<Reserva>(`/reserva/getbymotel/${idMotel}`);
  }

  cambiarEstado(id: Id, estado: EstadoReserva): Observable<string> {
    return this.api.actualizar('/reserva/updateestado', null, { id, estado });
  }

  /** El backend calcula el reembolso según la política del motel. */
  cancelar(id: Id): Observable<string> {
    return this.api.actualizar(`/reserva/cancelar/${id}`, null);
  }
}

@Injectable({ providedIn: 'root' })
export class PagoApi {
  private readonly api = inject(Api);
  readonly crud = crud<Pago>('pago');

  /** El backend devuelve todos los pagos de la reserva ordenados por fecha; vale el más reciente. */
  porReserva(idReserva: Id): Observable<Pago | null> {
    return this.api.lista<Pago>(`/pago/getbyreserva/${idReserva}`).pipe(map((pagos) => pagos.at(-1) ?? null));
  }

  cambiarEstado(id: Id, estado: EstadoPago): Observable<string> {
    return this.api.actualizar('/pago/updateestado', null, { id, estado });
  }
}

@Injectable({ providedIn: 'root' })
export class ResenaApi {
  private readonly api = inject(Api);
  readonly crud = crud<Resena>('resena');

  porMotel(idMotel: Id): Observable<Resena[]> {
    return this.api.lista<Resena>(`/resena/getbymotel/${idMotel}`);
  }

  /** Promedio de calificaciones del motel; 0 si todavía no tiene reseñas. */
  promedio(idMotel: Id): Observable<number> {
    return this.api.valor<number>(`/resena/promedio/${idMotel}`, 0);
  }

  mias(): Observable<Resena[]> {
    return this.api.lista<Resena>('/resena/mis-resenas');
  }
}

@Injectable({ providedIn: 'root' })
export class RespuestaApi {
  private readonly api = inject(Api);
  /** 1:1 con reseña: el id de la respuesta es el idResena. */
  readonly crud = crud<RespuestaPropietario>('respuesta');

  deResena(idResena: Id): Observable<RespuestaPropietario | null> {
    return this.api.uno<RespuestaPropietario>(`/respuesta/getbyid/${idResena}`);
  }
}
