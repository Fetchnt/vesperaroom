import { Component, computed, inject, signal } from '@angular/core';
import { map, type OperatorFunction } from 'rxjs';
import { UsuarioApi } from '../../core/api/entidades';
import type { Rol, Usuario } from '../../core/api/models';
import { type AccionFila, type Campo, type Registro } from '../../shared/tabla-crud';

type Filtro = 'TODOS' | Rol;

/** La búsqueda por correo devuelve un usuario o nada; la tabla espera una lista. */
const mapAUno: OperatorFunction<Usuario | null, Usuario[]> = map((usuario) => (usuario ? [usuario] : []));

const FILTROS: { valor: Filtro; texto: string }[] = [
  { valor: 'TODOS', texto: 'Todos' },
  { valor: 'CLIENTE', texto: 'Clientes' },
  { valor: 'PROPIETARIO', texto: 'Propietarios' },
  { valor: 'ADMINISTRADOR', texto: 'Administradores' },
];

@Component({
  standalone: false,
  selector: 'app-usuarios',
  template: `
    <h1>Usuarios</h1>

    <form class="tarjeta formulario" style="margin-bottom:24px" (ngSubmit)="buscar()">
      <label class="campo">
        <span>Rol</span>
        <select name="filtro" [ngModel]="filtro()" (ngModelChange)="cambiarFiltro($event)">
          @for (f of filtros; track f.valor) {
            <option [ngValue]="f.valor">{{ f.texto }}</option>
          }
        </select>
      </label>
      <label class="campo">
        <span>Buscar por correo</span>
        <input name="correo" type="email" [ngModel]="correo()" (ngModelChange)="correo.set($event)" />
      </label>
      <div class="fila">
        <button type="submit">Buscar</button>
        @if (busqueda()) {
          <button type="button" class="plano" (click)="limpiar()">Ver todos</button>
        }
      </div>
    </form>

    <app-tabla-crud
      titulo="Cuentas"
      claveId="idUsuario"
      sinDatos="No hay usuarios para ese filtro."
      [campos]="campos"
      [cargar]="cargar()"
      [guardar]="guardar"
      [borrar]="borrar"
      [acciones]="acciones"
    />
  `,
})
export class Usuarios {
  private readonly api = inject(UsuarioApi);

  protected readonly filtros = FILTROS;
  protected readonly filtro = signal<Filtro>('TODOS');
  protected readonly correo = signal('');
  protected readonly busqueda = signal('');

  protected readonly campos: Campo[] = [
    { clave: 'nombre', etiqueta: 'Nombre', requerido: true },
    { clave: 'apellido', etiqueta: 'Apellido', requerido: true },
    { clave: 'correo', etiqueta: 'Correo', requerido: true },
    { clave: 'telefono', etiqueta: 'Teléfono', requerido: true },
    {
      clave: 'rol',
      etiqueta: 'Rol',
      tipo: 'select',
      requerido: true,
      opciones: [
        { valor: 'CLIENTE', texto: 'Cliente' },
        { valor: 'PROPIETARIO', texto: 'Propietario' },
        { valor: 'ADMINISTRADOR', texto: 'Administrador' },
      ],
    },
    { clave: 'contrasena', etiqueta: 'Contraseña', oculto: true },
    { clave: 'estado', etiqueta: 'Estado', esEstado: true, soloLectura: true },
    { clave: 'fechaRegistro', etiqueta: 'Registro', soloLectura: true },
  ];

  protected readonly cargar = computed(() => {
    const [filtro, busqueda] = [this.filtro(), this.busqueda()];

    if (busqueda) {
      return () => this.api.porCorreo(busqueda).pipe(mapAUno);
    }
    switch (filtro) {
      case 'CLIENTE':
        return () => this.api.clientes();
      case 'PROPIETARIO':
        return () => this.api.propietarios();
      case 'ADMINISTRADOR':
        return () => this.api.administradores();
      default:
        return () => this.api.crud.getAll();
    }
  });

  protected readonly guardar = (id: number | string | null, dto: Registro) => {
    const usuario = { ...dto } as unknown as Usuario;
    if (!usuario.contrasena) {
      delete usuario.contrasena;
    }
    return id === null ? this.api.crud.crear(usuario) : this.api.crud.actualizar(id, usuario);
  };

  protected readonly borrar = (id: number | string) => this.api.crud.eliminar(id);

  /** El estado se cambia con /usuario/updateestado, no con updatejson. */
  protected readonly acciones: AccionFila[] = [
    {
      texto: (fila) => (fila['estado'] === 'ACTIVO' ? 'Inactivar' : 'Activar'),
      ejecutar: (fila) =>
        this.api.cambiarEstado(
          fila['idUsuario'] as number,
          fila['estado'] === 'ACTIVO' ? 'INACTIVO' : 'ACTIVO',
        ),
    },
  ];

  protected cambiarFiltro(filtro: Filtro): void {
    this.filtro.set(filtro);
    this.busqueda.set('');
  }

  protected buscar(): void {
    this.busqueda.set(this.correo().trim());
  }

  protected limpiar(): void {
    this.correo.set('');
    this.busqueda.set('');
  }
}
