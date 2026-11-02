import { Component, computed, inject } from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';
import { PerfilPropietarioApi, UsuarioApi } from '../../core/api/entidades';
import type { PerfilPropietario, TipoCuenta } from '../../core/api/models';
import { type AccionFila, type Campo, type Registro } from '../../shared/tabla-crud';
import { lista } from '../../shared/recursos';

const TIPOS_CUENTA: TipoCuenta[] = ['AHORROS', 'CORRIENTE'];

@Component({
  standalone: false,
  selector: 'app-propietarios',
  template: `
    <h1>Perfiles de propietario</h1>
    <p class="suave">
      Un propietario puede publicar moteles aunque su perfil no esté verificado; la verificación es
      el visto bueno administrativo de sus datos fiscales y bancarios.
    </p>

    <app-tabla-crud
      titulo="Perfiles"
      descripcion="Cada usuario con rol propietario tiene como máximo un perfil."
      claveId="idUsuario"
      sinDatos="No hay perfiles de propietario registrados."
      [campos]="campos()"
      [cargar]="cargar"
      [guardar]="guardar"
      [borrar]="borrar"
      [acciones]="acciones"
    />
  `,
})
export class Propietarios {
  private readonly api = inject(PerfilPropietarioApi);
  private readonly usuarioApi = inject(UsuarioApi);

  private readonly propietarios = rxResource({
    stream: () => this.usuarioApi.propietarios(),
    defaultValue: [],
  });

  protected readonly campos = computed<Campo[]>(() => [
    {
      clave: 'idUsuario',
      etiqueta: 'Usuario',
      tipo: 'select',
      requerido: true,
      opciones: lista(this.propietarios).map((u) => ({
        valor: u.idUsuario!,
        texto: `${u.nombre} ${u.apellido} (${u.correo})`,
      })),
    },
    { clave: 'razonSocial', etiqueta: 'Razón social', requerido: true },
    { clave: 'nit', etiqueta: 'NIT', requerido: true },
    { clave: 'banco', etiqueta: 'Banco', requerido: true },
    {
      clave: 'tipoCuenta',
      etiqueta: 'Tipo de cuenta',
      tipo: 'select',
      requerido: true,
      opciones: TIPOS_CUENTA.map((t) => ({
        valor: t,
        texto: t === 'AHORROS' ? 'Ahorros' : 'Corriente',
      })),
    },
    { clave: 'numeroCuenta', etiqueta: 'Cuenta', requerido: true },
    { clave: 'fechaVerificacion', etiqueta: 'Verificado', soloLectura: true },
  ]);

  protected readonly cargar = () => this.api.crud.getAll();
  protected readonly guardar = (id: number | string | null, dto: Registro) =>
    id === null
      ? this.api.crud.crear(dto as unknown as PerfilPropietario)
      : this.api.crud.actualizar(id, dto as unknown as PerfilPropietario);
  protected readonly borrar = (id: number | string) => this.api.crud.eliminar(id);

  protected readonly acciones: AccionFila[] = [
    {
      texto: 'Verificar',
      visible: (fila) => !fila['fechaVerificacion'],
      ejecutar: (fila) => this.api.verificar(fila['idUsuario'] as number),
    },
  ];
}
