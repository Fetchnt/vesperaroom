# VesperaRoom

Proyecto final de Bases de Datos 1 — Universidad El Bosque.

Plataforma de reserva de moteles: modelo relacional normalizado a 3FN (19 tablas),
API REST en Spring Boot sobre MySQL con JDBC puro, y una aplicación web en Angular.

## Contenido del repositorio

| Carpeta | Qué hay |
|---------|---------|
| `front/` | Aplicación web en Angular 22 que consume la API REST. Ver su propio README. |

Pendientes de subir: el backend de Spring Boot y los scripts de base de datos
(`database/`), que el enunciado también exige en el repositorio.

## Calidad de código

El análisis estático lo hace [DeepSource](https://deepsource.com); la configuración está en
`.deepsource.toml`. Para que corra hay que activar este repositorio desde el panel de DeepSource.

## Arrancar el front

```bash
cd front
npm install
npm run dev     # http://localhost:4200
```

Necesita el backend en `http://localhost:8080`; se cambia en `front/src/environments/environment.ts`.
