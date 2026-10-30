-- =============================================================================
-- VesperaRoom - Migración V3: datos de cancelación y reembolso en reserva
-- RDBMS: MySQL 8.x
-- Descripción: agrega a reserva la fecha de cancelación, el monto que se le
--              devuelve al cliente y la fecha límite de devolución. Las tres
--              columnas son opcionales: solo se llenan cuando la reserva se
--              cancela. Siguen en 3FN porque dependen solo de id_reserva.
-- Requiere: V2__esquema_3fn.sql (o 01_creacion_objetos.sql anterior a V3).
-- Ejecutar como root:  mysql -u root -p < V3__reembolso_reserva.sql
-- =============================================================================

USE motelnow;

ALTER TABLE reserva
	ADD COLUMN fecha_cancelacion       DATETIME        NULL AFTER total,
	ADD COLUMN monto_reembolso         DECIMAL(12, 2)  NULL AFTER fecha_cancelacion,
	ADD COLUMN fecha_limite_reembolso  DATE            NULL AFTER monto_reembolso,
	ADD CONSTRAINT ck_reserva_reembolso CHECK (monto_reembolso IS NULL OR monto_reembolso BETWEEN 0 AND total);

-- Las reservas que ya estaban canceladas quedan sin reembolso registrado
UPDATE reserva SET fecha_cancelacion = fecha_creacion, monto_reembolso = 0.00
WHERE estado = 'CANCELADA' AND fecha_cancelacion IS NULL;
