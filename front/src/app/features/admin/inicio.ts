import { Component, computed, inject } from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';
import { MotelApi, PagoApi, PerfilPropietarioApi, ReservaApi, UsuarioApi } from '../../core/api/entidades';
import { IMAGENES } from '../../shared/imagenes';
import { lista, valorDe } from '../../shared/recursos';

/** Portada del administrador: totales del sistema y lo que espera una decisión suya. */
@Component({
  standalone: false,
  selector: 'app-inicio-admin',
  template: `
    <app-cabecera [imagen]="imagenes.ciudad" titulo="Resumen" subtitulo="Estado general de VesperaRoom." />

    <div class="totales">
      @for (t of totales(); track t.texto) {
        <a class="tarjeta tarjeta-enlace total" [routerLink]="t.ruta">
          <span class="cifra">{{ t.cifra ?? '…' }}</span>
          <span class="suave">{{ t.texto }}</span>
        </a>
      }
    </div>

    <h2 style="margin-top:32px">Por atender</h2>
    <div class="totales">
      <a class="tarjeta tarjeta-enlace total" routerLink="/admin/pagos" [class.alerta]="pagosPendientes() > 0">
        <span class="cifra">{{ pagos.isLoading() ? '…' : pagosPendientes() }}</span>
        <span class="suave">Pagos pendientes de aprobar</span>
      </a>
      <a
        class="tarjeta tarjeta-enlace total"
        routerLink="/admin/propietarios"
        [class.alerta]="sinVerificar() > 0"
      >
        <span class="cifra">{{ perfiles.isLoading() ? '…' : sinVerificar() }}</span>
        <span class="suave">Propietarios sin verificar</span>
      </a>
    </div>

    @if (error()) {
      <p class="aviso mal" role="alert">{{ error() }}</p>
    }
  `,
  styles: `
    .totales {
      display: grid;
      gap: 16px;
      grid-template-columns: repeat(auto-fill, minmax(190px, 1fr));
      margin-top: 16px;
    }
    .total {
      display: flex;
      flex-direction: column;
      gap: 4px;
    }
    .cifra {
      font-size: 2rem;
      font-weight: 600;
      line-height: 1.1;
      font-variant-numeric: tabular-nums;
    }
    .alerta {
      border-color: var(--aviso);
      background: var(--aviso-tenue);
    }
    .alerta .cifra {
      color: var(--aviso);
    }
  `,
})
export class InicioAdmin {
  protected readonly imagenes = IMAGENES;
  private readonly usuarioApi = inject(UsuarioApi);
  private readonly motelApi = inject(MotelApi);
  private readonly reservaApi = inject(ReservaApi);
  private readonly pagoApi = inject(PagoApi);
  private readonly perfilApi = inject(PerfilPropietarioApi);

  private readonly usuarios = rxResource({ stream: () => this.usuarioApi.crud.contar() });
  private readonly moteles = rxResource({ stream: () => this.motelApi.crud.contar() });
  private readonly reservas = rxResource({ stream: () => this.reservaApi.crud.contar() });
  protected readonly pagos = rxResource({ stream: () => this.pagoApi.crud.getAll(), defaultValue: [] });
  protected readonly perfiles = rxResource({ stream: () => this.perfilApi.crud.getAll(), defaultValue: [] });

  protected readonly totales = computed(() => [
    { texto: 'Usuarios', ruta: '/admin/usuarios', cifra: valorDe(this.usuarios, undefined) },
    { texto: 'Moteles', ruta: '/admin/moteles', cifra: valorDe(this.moteles, undefined) },
    { texto: 'Reservas', ruta: '/admin/reservas', cifra: valorDe(this.reservas, undefined) },
    { texto: 'Pagos', ruta: '/admin/pagos', cifra: this.pagos.isLoading() ? undefined : lista(this.pagos).length },
  ]);

  protected readonly pagosPendientes = computed(
    () => lista(this.pagos).filter((p) => p.estado === 'PENDIENTE').length,
  );
  protected readonly sinVerificar = computed(
    () => lista(this.perfiles).filter((p) => !p.fechaVerificacion).length,
  );

  protected readonly error = computed(() => {
    const fallido = [this.usuarios, this.moteles, this.reservas, this.pagos, this.perfiles].find((r) => r.error());
    return fallido ? (fallido.error() as Error).message : '';
  });
}
