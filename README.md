# VesperaRoom

Proyecto final de Bases de Datos 1 — Universidad El Bosque.

Plataforma de reserva de moteles: modelo relacional normalizado a 3FN (19 tablas) en MySQL 8,
API REST en Spring Boot con JDBC puro (sin ORM) y una aplicación web en Angular.

Integrantes: Juan Felipe Lizarazo Prieto, Felipe Andrés Zambrano Brochero y Miguel Ángel López Jiménez.

## Contenido del repositorio

| Carpeta | Qué hay |
|---------|---------|
| `vesperaroom_Backend/database/` | Scripts SQL: creación de objetos, inserción de registros, scripts varios, usuario de la aplicación y migraciones. Ver su propio README. |
| `vesperaroom_Backend/src/` | API REST en Spring Boot 4 y Java 21: controladores, servicios, repositorios JDBC y seguridad con JWT. |
| `vesperaroom_Backend/docs/uml/` | Diagrama UML del backend (draw.io). |
| `front/` | Aplicación web en Angular 22 que consume la API REST. Ver su propio README. |

## Arrancar el proyecto

Requisitos: MySQL 8, JDK 21 y Node.js con npm.

### 1. Base de datos

Desde `vesperaroom_Backend/database/`, con el usuario `root` de MySQL:

```bash
mysql -u root -p < 01_creacion_objetos.sql
mysql -u root -p < 02_insercion_registros.sql
mysql -u root -p < 03_scripts_varios.sql
mysql -u root -p < 04_script_usuariotest.sql
```

`01_creacion_objetos.sql` crea la base `vesperaroom` y **borra las tablas existentes**. Para
actualizar una base que ya tiene datos están los scripts de `migraciones/`.

### 2. Backend (puerto 8080)

En `vesperaroom_Backend/` cree el archivo `.env.properties` (está en `.gitignore`, no se sube):

```properties
DB_USER=usuario
JWT_SECRET=<clave de al menos 32 caracteres>
```

```bash
cd vesperaroom_Backend
./mvnw spring-boot:run
```

En Windows PowerShell: `.\mvnw.cmd spring-boot:run`. La documentación de la API queda en
`http://localhost:8080/swagger-ui.html`.

### 3. Front (puerto 4200)

```bash
cd front
npm install
npm run dev     # http://localhost:4200
```

Busca el backend en `http://localhost:8080`; se cambia en `front/src/environments/environment.ts`.

## Cuentas de prueba

Los datos de `02_insercion_registros.sql` traen una cuenta por rol, todas con la contraseña
`Vespera2026`:

| Rol | Correo |
|-----|--------|
| Administrador | `soporte@vesperaroom.com` |
| Propietario | `propietario1@vesperaroom.com` |
| Cliente | `cliente1@vesperaroom.com` |

Si se define `ADMIN_PASSWORD`, al arrancar también se crea `admin@vesperaroom.com`.

## Pruebas

```bash
cd vesperaroom_Backend && ./mvnw test     # pruebas del backend (JUnit y Mockito)
cd front && npm test                      # pruebas del front (Vitest)
```

## Calidad de código

El análisis estático lo hace [DeepSource](https://deepsource.com); la configuración está en
`.deepsource.toml`. Para que corra hay que activar este repositorio desde el panel de DeepSource.
