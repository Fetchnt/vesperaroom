import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { registerLocaleData } from '@angular/common';
import localeEsCo from '@angular/common/locales/es-CO';
import { LOCALE_ID, NgModule, provideBrowserGlobalErrorListeners, provideZonelessChangeDetection } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { RouterModule } from '@angular/router';
import { App } from './app';
import { routes } from './app.routes';
import { authInterceptor } from './core/auth/interceptor';
import { NoEncontrada } from './features/errores/no-encontrada';
import { SinPermisos } from './features/errores/sin-permisos';
import { SharedModule } from './shared/shared-module';

registerLocaleData(localeEsCo);

@NgModule({
  declarations: [App, NoEncontrada, SinPermisos],
  imports: [
    BrowserModule,
    SharedModule,
    RouterModule.forRoot(routes, { bindToComponentInputs: true, scrollPositionRestoration: 'top' }),
  ],
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideZonelessChangeDetection(),
    { provide: LOCALE_ID, useValue: 'es-CO' },
    provideHttpClient(withInterceptors([authInterceptor])),
  ],
  bootstrap: [App],
})
export class AppModule {}
