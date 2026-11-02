-- =============================================================================
-- VesperaRoom - Migración V4: códigos de recuperación de contraseña
-- RDBMS: MySQL 8.x
-- Descripción: crea codigo_recuperacion, 1:1 con usuario. Guarda el hash del
--              código que se envía por correo, cuándo vence y cuántos intentos
--              fallidos lleva. No toca datos existentes.
-- Requiere: V3__reembolso_reserva.sql (o 01_creacion_objetos.sql anterior a V4).
-- Ejecutar como root:  mysql -u root -p < V4__codigo_recuperacion.sql
-- =============================================================================

USE vesperaroom;

CREATE TABLE IF NOT EXISTS codigo_recuperacion (
	id_usuario        BIGINT        NOT NULL,
	codigo_hash       VARCHAR(100)  NOT NULL,
	fecha_creacion    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
	fecha_expiracion  DATETIME      NOT NULL,
	intentos          INT           NOT NULL DEFAULT 0,
	CONSTRAINT pk_codigo_recuperacion PRIMARY KEY (id_usuario),
	CONSTRAINT fk_codigo_recuperacion_usuario FOREIGN KEY (id_usuario)
		REFERENCES usuario (id_usuario) ON DELETE CASCADE,
	CONSTRAINT ck_codigo_recuperacion_intentos CHECK (intentos >= 0),
	CONSTRAINT ck_codigo_recuperacion_fechas CHECK (fecha_expiracion > fecha_creacion)
) ENGINE = InnoDB;
