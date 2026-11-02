-- =============================================================================
-- VesperaRoom - Reversión de la migración V4
-- RDBMS: MySQL 8.x
-- Descripción: borra codigo_recuperacion y vuelve al estado V3. Los códigos
--              pendientes dejan de valer; las cuentas no cambian.
-- Ejecutar como root:  mysql -u root -p < R4__revertir_codigo_recuperacion.sql
-- =============================================================================

USE vesperaroom;

DROP TABLE IF EXISTS codigo_recuperacion;
