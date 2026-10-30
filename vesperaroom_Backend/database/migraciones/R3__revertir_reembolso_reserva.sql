-- =============================================================================
-- VesperaRoom - Reversión de la migración V3
-- RDBMS: MySQL 8.x
-- Descripción: quita de reserva las columnas de cancelación y reembolso y
--              vuelve al estado V2. Se pierden los montos de reembolso.
-- Ejecutar como root:  mysql -u root -p < R3__revertir_reembolso_reserva.sql
-- =============================================================================

USE vesperaroom;

ALTER TABLE reserva
	DROP CHECK ck_reserva_reembolso,
	DROP COLUMN fecha_limite_reembolso,
	DROP COLUMN monto_reembolso,
	DROP COLUMN fecha_cancelacion;
