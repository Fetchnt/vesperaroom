-- =============================================================================
-- VesperaRoom - Migración V1: tabla usuario
-- RDBMS: MySQL 8.x
-- Descripción: estado inicial del proyecto (primera iteración). Crea la base y
--              la tabla usuario si no existen. Si su base ya tiene la tabla
--              usuario, este script no cambia nada y puede pasar directo a V2.
-- Ejecutar como root:  mysql -u root -p < V1__usuario.sql
-- =============================================================================

CREATE DATABASE IF NOT EXISTS vesperaroom
	CHARACTER SET utf8mb4
	COLLATE utf8mb4_spanish_ci;

USE vesperaroom;

-- -----------------------------------------------------------------------------
-- Tabla: USUARIO
-- Todo usuario se registra con correo y teléfono obligatorios y únicos.
-- La contraseña se almacena cifrada con BCrypt (60 caracteres).
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS usuario (
	id_usuario      BIGINT        NOT NULL AUTO_INCREMENT,
	nombre          VARCHAR(60)   NOT NULL,
	apellido        VARCHAR(60)   NOT NULL,
	correo          VARCHAR(120)  NOT NULL,
	telefono        VARCHAR(15)   NOT NULL,
	contrasena      VARCHAR(100)  NOT NULL,
	rol             VARCHAR(20)   NOT NULL,
	estado          VARCHAR(20)   NOT NULL DEFAULT 'ACTIVO',
	fecha_registro  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
	CONSTRAINT pk_usuario PRIMARY KEY (id_usuario),
	CONSTRAINT uq_usuario_correo UNIQUE (correo),
	CONSTRAINT uq_usuario_telefono UNIQUE (telefono),
	CONSTRAINT ck_usuario_rol CHECK (rol IN ('ADMINISTRADOR', 'CLIENTE', 'PROPIETARIO')),
	CONSTRAINT ck_usuario_estado CHECK (estado IN ('ACTIVO', 'INACTIVO')),
	CONSTRAINT ck_usuario_telefono CHECK (telefono REGEXP '^[0-9]{7,15}$'),
	INDEX ix_usuario_rol (rol)
) ENGINE = InnoDB;
