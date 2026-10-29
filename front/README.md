# VesperaRoom — front

SPA en Angular 22 (standalone + signals, sin librerías de UI ni de estado) sobre la API REST de
`../src/main/java`.

## Arrancar

```bash
npm install
npm run dev     # http://localhost:4200
```

El backend debe estar en `http://localhost:8080` (su `app.cors.allowed-origins` ya permite el 4200).
Si corre en otra dirección, cámbiala en `src/environments/environment.ts`.

```bash
npm run build       # compilación de producción
npm run typecheck   # tsc sobre app y pruebas
npm test            # vitest
```

## Cómo está organizado

| Carpeta | Qué hay |
|---------|---------|
| `core/api/api.ts` | `Api`, el cliente HTTP, y `crud()`, los siete endpoints que todos los controllers repiten. |
| `core/api/entidades.ts` | Un servicio por entidad: su `crud` más sus endpoints propios. |
| `core/api/models.ts` | Los DTO del backend como tipos de TypeScript. |
| `core/auth/` | Login, sesión en `localStorage`, interceptor del JWT y guards por rol. |
| `core/reservas/total.ts` | Total estimado de una reserva (misma fórmula que `ReservaService`). |
| `shared/tabla-crud.ts` | Tabla con formulario que usan las pantallas cuyo CRUD es el genérico. |
| `features/` | Una carpeta por pantalla; todas se cargan de forma perezosa. |

## Convenciones del backend que resuelve `core/api/api.ts`

El backend no usa los códigos HTTP habituales, así que la capa HTTP los traduce una sola vez:

- las listas responden **202**, y **204 sin cuerpo** cuando están vacías → se normalizan a `[]`;
- `getbyid` responde **404** cuando no encuentra → se traduce a `null`, no a un error;
- `exists` responde **404** para decir «false»;
- `count` responde **204** cuando el total es cero;
- los errores llegan en **text/plain**, no en JSON;
- `updatejson` recibe el id por **query** (`?id=`) y el resto en el cuerpo.

## Rutas

| Ruta | Acceso |
|------|--------|
| `/`, `/motel/:id` | Público. Catálogo, detalle y búsqueda de disponibilidad. |
| `/login`, `/registro` | Público (solo sin sesión). |
| `/reservar/:idHabitacion`, `/mis-reservas` | CLIENTE. |
| `/mi-cuenta` | Cualquier rol. |
| `/panel/...` | PROPIETARIO. Moteles, política, tipos, habitaciones, tarifas, servicios, fotos, reservas, reseñas y perfil. |
| `/admin/...` | ADMINISTRADOR. Usuarios, propietarios, moteles, catálogos, reservas y pagos. |

## Cuentas de prueba

Las del script `../database/02_insercion_registros.sql`, todas con la contraseña `Vespera2026`:
`cliente1@vesperaroom.com`, `propietario1@vesperaroom.com`, `soporte@vesperaroom.com`.
