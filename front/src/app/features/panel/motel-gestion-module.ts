import { NgModule } from '@angular/core';
import { SharedModule } from '../../shared/shared-module';
import { MotelGestion } from './motel-gestion';

/** La gestión de un motel la usan el propietario y el administrador. */
@NgModule({
  declarations: [MotelGestion],
  imports: [SharedModule],
  exports: [MotelGestion],
})
export class MotelGestionModule {}
