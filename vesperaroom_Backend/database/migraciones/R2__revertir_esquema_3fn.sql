-- =============================================================================
-- VesperaRoom - Reversión de la migración V2
-- RDBMS: MySQL 8.x
-- Descripción: elimina las 18 tablas creadas por V2__esquema_3fn.sql y vuelve
--              al estado V1 (solo la tabla usuario, que conserva sus datos).
--              ADVERTENCIA: borra los datos de moteles, reservas, pagos, etc.
-- Ejecutar como root:  mysql -u root -p < R2__revertir_esquema_3fn.sql
-- =============================================================================

USE vesperaroom;

-- Orden inverso a las dependencias: primero las tablas hijas
DROP TABLE IF EXISTS respuesta_propietario;
DROP TABLE IF EXISTS resena;
DROP TABLE IF EXISTS pago;
DROP TABLE IF EXISTS reserva_servicio;
DROP TABLE IF EXISTS reserva;
DROP TABLE IF EXISTS servicio_adicional;
DROP TABLE IF EXISTS tarifa;
DROP TABLE IF EXISTS modalidad;
DROP TABLE IF EXISTS foto;
DROP TABLE IF EXISTS habitacion;
DROP TABLE IF EXISTS tipo_habitacion_caracteristica;
DROP TABLE IF EXISTS caracteristica;
DROP TABLE IF EXISTS tipo_habitacion;
DROP TABLE IF EXISTS politica_reserva;
DROP TABLE IF EXISTS motel;
DROP TABLE IF EXISTS perfil_propietario;
DROP TABLE IF EXISTS ciudad;
DROP TABLE IF EXISTS departamento;
