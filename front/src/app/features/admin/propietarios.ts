import { Component, inject } from '@angular/core';
import { PerfilPropietarioApi } from '../../core/api/entidades';
import { TablaCrud, type AccionFila, type Campo } from '../../shared/tabla-crud';

@Component({
  selector: 'app-propietarios',
  imports: [TablaCrud],
  template: `
    <h1>Perfiles de propietario</h1>
    <p class="suave">
      Un propietario puede publicar moteles aunque su perfil no esté verificado; la verificación es
      el visto bueno administrativo de sus datos fiscales y bancarios.
    </p>

    <app-tabla-crud
      titulo="Perfiles"
      claveId="idUsuario"
      sinDatos="No hay perfiles de propietario registrados."
      [campos]="campos"
      [cargar]="cargar"
      [borrar]="borrar"
      [acciones]="acciones"
    />
  `,
})
export class Propietarios {
  private readonly api = inject(PerfilPropietarioApi);

  protected readonly campos: Campo[] = [
    { clave: 'idUsuario', etiqueta: 'Usuario' },
    { clave: 'razonSocial', etiqueta: 'Razón social' },
    { clave: 'nit', etiqueta: 'NIT' },
    { clave: 'banco', etiqueta: 'Banco' },
    { clave: 'tipoCuenta', etiqueta: 'Tipo de cuenta' },
    { clave: 'numeroCuenta', etiqueta: 'Cuenta' },
    { clave: 'fechaVerificacion', etiqueta: 'Verificado' },
  ];

  protected readonly cargar = () => this.api.crud.getAll();
  protected readonly borrar = (id: number | string) => this.api.crud.eliminar(id);

  protected readonly acciones: AccionFila[] = [
    {
      texto: 'Verificar',
      visible: (fila) => !fila['fechaVerificacion'],
      ejecutar: (fila) => this.api.verificar(fila['idUsuario'] as number),
    },
  ];
}
