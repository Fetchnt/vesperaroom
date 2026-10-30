# Scripts de base de datos — VesperaRoom

RDBMS: **MySQL 8.x**. Base de datos: `motelnow`. Modelo normalizado a **3FN** con **19 tablas**.

## Base nueva (desde cero)

| Orden | Script | Contenido |
|-------|--------|-----------|
| 1 | `01_creacion_objetos.sql` | Crea la base y las 19 tablas con sus PK, FK, UNIQUE, CHECK e índices. **Borra las tablas existentes.** |
| 2 | `02_insercion_registros.sql` | Datos de prueba de todas las tablas, con ids explícitos. |
| 3 | `03_scripts_varios.sql` | Usuario de aplicación (comentado), vista `v_reserva_detalle` y consultas de apoyo. |

```bash
mysql -u root -p < 01_creacion_objetos.sql
mysql -u root -p < 02_insercion_registros.sql
mysql -u root -p < 03_scripts_varios.sql
```

## Base existente (migración sin perder datos)

Para una base que ya tiene la tabla `usuario` de la primera iteración:

| Script | Qué hace |
|--------|----------|
| `migraciones/V1__usuario.sql` | Estado inicial: crea `usuario` solo si no existe. |
| `migraciones/V2__esquema_3fn.sql` | Agrega las 18 tablas nuevas con `CREATE TABLE IF NOT EXISTS` y carga los catálogos base con `INSERT IGNORE`. No toca los usuarios existentes. |
| `migraciones/R2__revertir_esquema_3fn.sql` | Revierte V2: borra las 18 tablas y deja solo `usuario`. |
| `migraciones/V3__reembolso_reserva.sql` | Agrega a `reserva` las columnas `fecha_cancelacion`, `monto_reembolso` y `fecha_limite_reembolso`. |
| `migraciones/R3__revertir_reembolso_reserva.sql` | Revierte V3: quita esas tres columnas. |

```bash
mysql -u root -p < migraciones/V2__esquema_3fn.sql
mysql -u root -p < migraciones/V3__reembolso_reserva.sql
```

Si la base ya se creó con una versión de `01_creacion_objetos.sql` sin las columnas de reembolso, basta con ejecutar V3.

Los nombres siguen la convención `V<versión>__<descripción>.sql` (la misma de Flyway), pero se ejecutan a mano: el proyecto no usa herramientas de migración automática.

Las migraciones crean tablas, así que deben ejecutarse con `root`. El usuario de la aplicación (`usuario`) solo tiene permisos `SELECT, INSERT, UPDATE, DELETE`.

## Datos de prueba

Todas las cuentas usan la contraseña `Vespera2026`.

| id | Correo | Rol | Datos asociados |
|----|--------|-----|-----------------|
| 1 | cliente1@vesperaroom.com | CLIENTE | Reservas 1 (finalizada, con reseña) y 3 (confirmada) |
| 2 | cliente2@vesperaroom.com | CLIENTE | Reservas 2 (finalizada, con reseña) y 4 (cancelada) |
| 3 | cliente3@vesperaroom.com | CLIENTE (INACTIVO) | — |
| 4 | propietario1@vesperaroom.com | PROPIETARIO | Perfil verificado; moteles 1 (Luna) y 2 (Eclipse) |
| 5 | propietario2@vesperaroom.com | PROPIETARIO | Perfil sin verificar; motel 3 (Sol) |
| 6 | soporte@vesperaroom.com | ADMINISTRADOR | — |

## Tablas

| Tabla | PK | Relaciones principales |
|-------|----|------------------------|
| departamento | id_departamento | — |
| ciudad | id_ciudad | departamento |
| usuario | id_usuario | — |
| perfil_propietario | id_usuario | usuario (1:1) |
| motel | id_motel | perfil_propietario, ciudad |
| politica_reserva | id_motel | motel (1:1) |
| tipo_habitacion | id_tipo | motel |
| caracteristica | id_caracteristica | — |
| tipo_habitacion_caracteristica | (id_tipo, id_caracteristica) | N:M tipo – característica |
| habitacion | id_habitacion | FK compuesta (id_tipo, id_motel) → tipo_habitacion |
| foto | id_foto | motel **o** habitación (CHECK) |
| modalidad | id_modalidad | — |
| tarifa | id_tarifa | tipo_habitacion, modalidad |
| servicio_adicional | id_servicio | motel |
| reserva | id_reserva | usuario, habitacion, tarifa |
| reserva_servicio | (id_reserva, id_servicio) | N:M reserva – servicio |
| pago | id_pago | reserva |
| resena | id_resena | reserva (1:1) |
| respuesta_propietario | id_resena | resena (1:1, entidad débil) |

La justificación de cada tabla está en el documento *VesperaRoom — Normalización del modelo de datos*.
