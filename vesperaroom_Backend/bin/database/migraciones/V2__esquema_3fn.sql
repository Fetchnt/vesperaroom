-- =============================================================================
-- VesperaRoom - Migración V2: esquema normalizado a 3FN
-- RDBMS: MySQL 8.x
-- Descripción: agrega las 18 tablas nuevas a una base motelnow que ya tiene la
--              tabla usuario (estado V1). No borra ni modifica datos existentes:
--              usa CREATE TABLE IF NOT EXISTS, así que puede ejecutarse dos veces.
--              También carga los catálogos base (departamentos, ciudades,
--              características y modalidades) con INSERT IGNORE.
-- Ejecutar como root:  mysql -u root -p < V2__esquema_3fn.sql
-- Revertir:            R2__revertir_esquema_3fn.sql
-- =============================================================================

USE motelnow;

-- -----------------------------------------------------------------------------
-- Tabla: DEPARTAMENTO
-- Catálogo de departamentos. Sale de la dependencia transitiva
-- id_motel -> ciudad -> departamento (3FN).
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS departamento (
	id_departamento BIGINT       NOT NULL AUTO_INCREMENT,
	nombre          VARCHAR(60)  NOT NULL,
	CONSTRAINT pk_departamento PRIMARY KEY (id_departamento),
	CONSTRAINT uq_departamento_nombre UNIQUE (nombre)
) ENGINE = InnoDB;

-- -----------------------------------------------------------------------------
-- Tabla: CIUDAD
-- El nombre solo es único dentro de su departamento (hay municipios homónimos).
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS ciudad (
	id_ciudad       BIGINT       NOT NULL AUTO_INCREMENT,
	id_departamento BIGINT       NOT NULL,
	nombre          VARCHAR(80)  NOT NULL,
	CONSTRAINT pk_ciudad PRIMARY KEY (id_ciudad),
	CONSTRAINT uq_ciudad_departamento_nombre UNIQUE (id_departamento, nombre),
	CONSTRAINT fk_ciudad_departamento FOREIGN KEY (id_departamento)
		REFERENCES departamento (id_departamento)
) ENGINE = InnoDB;

-- -----------------------------------------------------------------------------
-- Tabla: PERFIL_PROPIETARIO
-- Relación 1:1 con USUARIO: su PK es la FK al usuario dueño del perfil.
-- (banco, numero_cuenta) es una clave candidata alternativa.
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS perfil_propietario (
	id_usuario          BIGINT        NOT NULL,
	nit                 VARCHAR(15)   NOT NULL,
	razon_social        VARCHAR(120)  NOT NULL,
	banco               VARCHAR(60)   NOT NULL,
	tipo_cuenta         VARCHAR(20)   NOT NULL,
	numero_cuenta       VARCHAR(20)   NOT NULL,
	fecha_verificacion  DATETIME      NULL,
	CONSTRAINT pk_perfil_propietario PRIMARY KEY (id_usuario),
	CONSTRAINT uq_perfil_propietario_nit UNIQUE (nit),
	CONSTRAINT uq_perfil_propietario_cuenta UNIQUE (banco, numero_cuenta),
	CONSTRAINT fk_perfil_propietario_usuario FOREIGN KEY (id_usuario)
		REFERENCES usuario (id_usuario) ON DELETE CASCADE,
	CONSTRAINT ck_perfil_propietario_tipo_cuenta CHECK (tipo_cuenta IN ('AHORROS', 'CORRIENTE')),
	CONSTRAINT ck_perfil_propietario_nit CHECK (nit REGEXP '^[0-9]{9,10}(-[0-9])?$'),
	CONSTRAINT ck_perfil_propietario_numero CHECK (numero_cuenta REGEXP '^[0-9]{6,20}$')
) ENGINE = InnoDB;

-- -----------------------------------------------------------------------------
-- Tabla: MOTEL
-- Guarda solo id_ciudad; el departamento se obtiene por la ciudad (3FN).
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS motel (
	id_motel        BIGINT         NOT NULL AUTO_INCREMENT,
	id_propietario  BIGINT         NOT NULL,
	id_ciudad       BIGINT         NOT NULL,
	nombre          VARCHAR(100)   NOT NULL,
	descripcion     VARCHAR(500)   NULL,
	calle           VARCHAR(40)    NOT NULL,
	carrera         VARCHAR(40)    NOT NULL,
	latitud         DECIMAL(9, 6)  NULL,
	longitud        DECIMAL(9, 6)  NULL,
	estado          VARCHAR(20)    NOT NULL DEFAULT 'ACTIVO',
	CONSTRAINT pk_motel PRIMARY KEY (id_motel),
	CONSTRAINT fk_motel_propietario FOREIGN KEY (id_propietario)
		REFERENCES perfil_propietario (id_usuario),
	CONSTRAINT fk_motel_ciudad FOREIGN KEY (id_ciudad)
		REFERENCES ciudad (id_ciudad),
	CONSTRAINT ck_motel_estado CHECK (estado IN ('ACTIVO', 'INACTIVO')),
	CONSTRAINT ck_motel_latitud CHECK (latitud BETWEEN -90 AND 90),
	CONSTRAINT ck_motel_longitud CHECK (longitud BETWEEN -180 AND 180)
) ENGINE = InnoDB;

-- -----------------------------------------------------------------------------
-- Tabla: POLITICA_RESERVA
-- Relación 1:1 con MOTEL: su PK es id_motel. acepta_reembolso no se guarda
-- porque se deriva de porcentaje_reembolso > 0 (3FN).
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS politica_reserva (
	id_motel                   BIGINT          NOT NULL,
	dias_habiles_devolucion    INT             NOT NULL DEFAULT 0,
	porcentaje_reembolso       INT             NOT NULL DEFAULT 0,
	capacidad_max_sin_recargo  INT             NOT NULL DEFAULT 2,
	tarifa_persona_adicional   DECIMAL(10, 2)  NOT NULL DEFAULT 0,
	anticipacion_minima_horas  INT             NOT NULL DEFAULT 0,
	tiempo_gracia_no_show_min  INT             NOT NULL DEFAULT 15,
	CONSTRAINT pk_politica_reserva PRIMARY KEY (id_motel),
	CONSTRAINT fk_politica_reserva_motel FOREIGN KEY (id_motel)
		REFERENCES motel (id_motel) ON DELETE CASCADE,
	CONSTRAINT ck_politica_dias CHECK (dias_habiles_devolucion BETWEEN 0 AND 60),
	CONSTRAINT ck_politica_porcentaje CHECK (porcentaje_reembolso BETWEEN 0 AND 100),
	CONSTRAINT ck_politica_capacidad CHECK (capacidad_max_sin_recargo BETWEEN 1 AND 20),
	CONSTRAINT ck_politica_tarifa CHECK (tarifa_persona_adicional >= 0),
	CONSTRAINT ck_politica_anticipacion CHECK (anticipacion_minima_horas BETWEEN 0 AND 720),
	CONSTRAINT ck_politica_gracia CHECK (tiempo_gracia_no_show_min BETWEEN 0 AND 240)
) ENGINE = InnoDB;

-- -----------------------------------------------------------------------------
-- Tabla: TIPO_HABITACION
-- uq_tipo_habitacion_tipo_motel existe para que HABITACION pueda referenciar
-- la pareja (id_tipo, id_motel) con una FK compuesta.
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS tipo_habitacion (
	id_tipo             BIGINT        NOT NULL AUTO_INCREMENT,
	id_motel            BIGINT        NOT NULL,
	nombre              VARCHAR(60)   NOT NULL,
	capacidad_personas  INT           NOT NULL,
	descripcion         VARCHAR(500)  NULL,
	CONSTRAINT pk_tipo_habitacion PRIMARY KEY (id_tipo),
	CONSTRAINT uq_tipo_habitacion_motel_nombre UNIQUE (id_motel, nombre),
	CONSTRAINT uq_tipo_habitacion_tipo_motel UNIQUE (id_tipo, id_motel),
	CONSTRAINT fk_tipo_habitacion_motel FOREIGN KEY (id_motel)
		REFERENCES motel (id_motel),
	CONSTRAINT ck_tipo_habitacion_capacidad CHECK (capacidad_personas BETWEEN 1 AND 20)
) ENGINE = InnoDB;

-- -----------------------------------------------------------------------------
-- Tabla: CARACTERISTICA
-- Catálogo global de características (jacuzzi, sauna, TV...).
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS caracteristica (
	id_caracteristica  BIGINT       NOT NULL AUTO_INCREMENT,
	nombre             VARCHAR(60)  NOT NULL,
	icono              VARCHAR(50)  NULL,
	CONSTRAINT pk_caracteristica PRIMARY KEY (id_caracteristica),
	CONSTRAINT uq_caracteristica_nombre UNIQUE (nombre)
) ENGINE = InnoDB;

-- -----------------------------------------------------------------------------
-- Tabla: TIPO_HABITACION_CARACTERISTICA
-- Resuelve la relación N:M "dispone_de". Toda la tabla es clave (BCNF y 4FN).
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS tipo_habitacion_caracteristica (
	id_tipo            BIGINT  NOT NULL,
	id_caracteristica  BIGINT  NOT NULL,
	CONSTRAINT pk_tipo_habitacion_caracteristica PRIMARY KEY (id_tipo, id_caracteristica),
	CONSTRAINT fk_thc_tipo FOREIGN KEY (id_tipo)
		REFERENCES tipo_habitacion (id_tipo) ON DELETE CASCADE,
	CONSTRAINT fk_thc_caracteristica FOREIGN KEY (id_caracteristica)
		REFERENCES caracteristica (id_caracteristica)
) ENGINE = InnoDB;

-- -----------------------------------------------------------------------------
-- Tabla: HABITACION
-- Queda en 3FN (no en BCNF) a propósito: id_tipo -> id_motel, pero id_motel
-- forma parte de la clave candidata (id_motel, numero). La FK compuesta
-- garantiza que id_motel siempre coincida con el motel del tipo.
-- El estado solo guarda estados operativos: "ocupada" se calcula con reservas.
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS habitacion (
	id_habitacion  BIGINT       NOT NULL AUTO_INCREMENT,
	id_motel       BIGINT       NOT NULL,
	id_tipo        BIGINT       NOT NULL,
	numero         VARCHAR(10)  NOT NULL,
	piso           INT          NOT NULL,
	estado         VARCHAR(20)  NOT NULL DEFAULT 'ACTIVA',
	CONSTRAINT pk_habitacion PRIMARY KEY (id_habitacion),
	CONSTRAINT uq_habitacion_motel_numero UNIQUE (id_motel, numero),
	CONSTRAINT fk_habitacion_tipo_motel FOREIGN KEY (id_tipo, id_motel)
		REFERENCES tipo_habitacion (id_tipo, id_motel),
	CONSTRAINT ck_habitacion_estado CHECK (estado IN ('ACTIVA', 'MANTENIMIENTO', 'FUERA_DE_SERVICIO')),
	CONSTRAINT ck_habitacion_piso CHECK (piso BETWEEN -5 AND 200)
) ENGINE = InnoDB;

-- -----------------------------------------------------------------------------
-- Tabla: FOTO
-- Una foto pertenece a un motel o a una habitación, nunca a ambos ni a ninguno.
-- MySQL no permite ON DELETE CASCADE en columnas usadas por un CHECK, por eso
-- las FK quedan con la acción por defecto (RESTRICT).
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS foto (
	id_foto        BIGINT        NOT NULL AUTO_INCREMENT,
	id_motel       BIGINT        NULL,
	id_habitacion  BIGINT        NULL,
	uri            VARCHAR(500)  NOT NULL,
	orden          INT           NOT NULL DEFAULT 1,
	CONSTRAINT pk_foto PRIMARY KEY (id_foto),
	CONSTRAINT uq_foto_motel_orden UNIQUE (id_motel, orden),
	CONSTRAINT uq_foto_habitacion_orden UNIQUE (id_habitacion, orden),
	CONSTRAINT fk_foto_motel FOREIGN KEY (id_motel)
		REFERENCES motel (id_motel),
	CONSTRAINT fk_foto_habitacion FOREIGN KEY (id_habitacion)
		REFERENCES habitacion (id_habitacion),
	CONSTRAINT ck_foto_dueno CHECK ((id_motel IS NULL) <> (id_habitacion IS NULL)),
	CONSTRAINT ck_foto_orden CHECK (orden BETWEEN 1 AND 50)
) ENGINE = InnoDB;

-- -----------------------------------------------------------------------------
-- Tabla: MODALIDAD
-- Catálogo de modalidades de alquiler. Sale de la dependencia
-- nombre_modalidad -> duracion_horas (3FN).
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS modalidad (
	id_modalidad    BIGINT       NOT NULL AUTO_INCREMENT,
	nombre          VARCHAR(40)  NOT NULL,
	duracion_horas  INT          NOT NULL,
	CONSTRAINT pk_modalidad PRIMARY KEY (id_modalidad),
	CONSTRAINT uq_modalidad_nombre UNIQUE (nombre),
	CONSTRAINT ck_modalidad_duracion CHECK (duracion_horas BETWEEN 1 AND 72)
) ENGINE = InnoDB;

-- -----------------------------------------------------------------------------
-- Tabla: TARIFA
-- Precio de un tipo de habitación en una modalidad, para un tipo de día y un
-- periodo de vigencia (vigente_hasta NULL = sin fecha de fin).
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS tarifa (
	id_tarifa      BIGINT          NOT NULL AUTO_INCREMENT,
	id_tipo        BIGINT          NOT NULL,
	id_modalidad   BIGINT          NOT NULL,
	precio         DECIMAL(10, 2)  NOT NULL,
	tipo_dia       VARCHAR(20)     NOT NULL DEFAULT 'TODOS',
	vigente_desde  DATE            NOT NULL,
	vigente_hasta  DATE            NULL,
	CONSTRAINT pk_tarifa PRIMARY KEY (id_tarifa),
	CONSTRAINT uq_tarifa_vigencia UNIQUE (id_tipo, id_modalidad, tipo_dia, vigente_desde),
	CONSTRAINT fk_tarifa_tipo FOREIGN KEY (id_tipo)
		REFERENCES tipo_habitacion (id_tipo),
	CONSTRAINT fk_tarifa_modalidad FOREIGN KEY (id_modalidad)
		REFERENCES modalidad (id_modalidad),
	CONSTRAINT ck_tarifa_precio CHECK (precio > 0),
	CONSTRAINT ck_tarifa_tipo_dia CHECK (tipo_dia IN ('TODOS', 'ENTRE_SEMANA', 'FIN_DE_SEMANA', 'FESTIVO')),
	CONSTRAINT ck_tarifa_vigencia CHECK (vigente_hasta IS NULL OR vigente_hasta >= vigente_desde)
) ENGINE = InnoDB;

-- -----------------------------------------------------------------------------
-- Tabla: SERVICIO_ADICIONAL
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS servicio_adicional (
	id_servicio  BIGINT          NOT NULL AUTO_INCREMENT,
	id_motel     BIGINT          NOT NULL,
	nombre       VARCHAR(80)     NOT NULL,
	descripcion  VARCHAR(300)    NULL,
	precio       DECIMAL(10, 2)  NOT NULL,
	activo       BOOLEAN         NOT NULL DEFAULT TRUE,
	CONSTRAINT pk_servicio_adicional PRIMARY KEY (id_servicio),
	CONSTRAINT uq_servicio_adicional_motel_nombre UNIQUE (id_motel, nombre),
	CONSTRAINT fk_servicio_adicional_motel FOREIGN KEY (id_motel)
		REFERENCES motel (id_motel),
	CONSTRAINT ck_servicio_adicional_precio CHECK (precio >= 0)
) ENGINE = InnoDB;

-- -----------------------------------------------------------------------------
-- Tabla: RESERVA
-- total y fecha_hora_fin se guardan como valores históricos (lo cobrado y la
-- estadía real), no como datos derivados. El índice compuesto acelera la
-- consulta de solapamiento de horarios por habitación.
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS reserva (
	id_reserva         BIGINT          NOT NULL AUTO_INCREMENT,
	codigo_reserva     VARCHAR(12)     NOT NULL,
	id_usuario         BIGINT          NOT NULL,
	id_habitacion      BIGINT          NOT NULL,
	id_tarifa          BIGINT          NOT NULL,
	cantidad_personas  INT             NOT NULL,
	fecha_creacion     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
	fecha_hora_inicio  DATETIME        NOT NULL,
	fecha_hora_fin     DATETIME        NOT NULL,
	estado             VARCHAR(20)     NOT NULL DEFAULT 'PENDIENTE',
	total              DECIMAL(12, 2)  NOT NULL,
	CONSTRAINT pk_reserva PRIMARY KEY (id_reserva),
	CONSTRAINT uq_reserva_codigo UNIQUE (codigo_reserva),
	CONSTRAINT fk_reserva_usuario FOREIGN KEY (id_usuario)
		REFERENCES usuario (id_usuario),
	CONSTRAINT fk_reserva_habitacion FOREIGN KEY (id_habitacion)
		REFERENCES habitacion (id_habitacion),
	CONSTRAINT fk_reserva_tarifa FOREIGN KEY (id_tarifa)
		REFERENCES tarifa (id_tarifa),
	CONSTRAINT ck_reserva_personas CHECK (cantidad_personas BETWEEN 1 AND 20),
	CONSTRAINT ck_reserva_fechas CHECK (fecha_hora_fin > fecha_hora_inicio),
	CONSTRAINT ck_reserva_estado CHECK (estado IN ('PENDIENTE', 'CONFIRMADA', 'EN_CURSO', 'FINALIZADA',
		'CANCELADA', 'NO_SHOW')),
	CONSTRAINT ck_reserva_total CHECK (total >= 0),
	INDEX ix_reserva_habitacion_fechas (id_habitacion, fecha_hora_inicio, fecha_hora_fin)
) ENGINE = InnoDB;

-- -----------------------------------------------------------------------------
-- Tabla: RESERVA_SERVICIO
-- Resuelve la relación N:M "incluye". precio_unitario es el precio al momento
-- de la compra (depende de la clave completa, no viola 2FN).
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS reserva_servicio (
	id_reserva       BIGINT          NOT NULL,
	id_servicio      BIGINT          NOT NULL,
	cantidad         INT             NOT NULL,
	precio_unitario  DECIMAL(10, 2)  NOT NULL,
	CONSTRAINT pk_reserva_servicio PRIMARY KEY (id_reserva, id_servicio),
	CONSTRAINT fk_reserva_servicio_reserva FOREIGN KEY (id_reserva)
		REFERENCES reserva (id_reserva) ON DELETE CASCADE,
	CONSTRAINT fk_reserva_servicio_servicio FOREIGN KEY (id_servicio)
		REFERENCES servicio_adicional (id_servicio),
	CONSTRAINT ck_reserva_servicio_cantidad CHECK (cantidad > 0),
	CONSTRAINT ck_reserva_servicio_precio CHECK (precio_unitario >= 0)
) ENGINE = InnoDB;

-- -----------------------------------------------------------------------------
-- Tabla: PAGO
-- Una reserva puede pagarse en varias partes (relación 1:N).
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS pago (
	id_pago                 BIGINT          NOT NULL AUTO_INCREMENT,
	id_reserva              BIGINT          NOT NULL,
	monto                   DECIMAL(12, 2)  NOT NULL,
	fecha_pago              DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
	metodo_pago             VARCHAR(20)     NOT NULL,
	estado                  VARCHAR(20)     NOT NULL DEFAULT 'PENDIENTE',
	referencia_transaccion  VARCHAR(100)    NULL,
	CONSTRAINT pk_pago PRIMARY KEY (id_pago),
	CONSTRAINT uq_pago_referencia UNIQUE (referencia_transaccion),
	CONSTRAINT fk_pago_reserva FOREIGN KEY (id_reserva)
		REFERENCES reserva (id_reserva),
	CONSTRAINT ck_pago_monto CHECK (monto > 0),
	CONSTRAINT ck_pago_metodo CHECK (metodo_pago IN ('EFECTIVO', 'TARJETA_CREDITO', 'TARJETA_DEBITO', 'PSE',
		'NEQUI', 'DAVIPLATA')),
	CONSTRAINT ck_pago_estado CHECK (estado IN ('PENDIENTE', 'APROBADO', 'RECHAZADO', 'REEMBOLSADO'))
) ENGINE = InnoDB;

-- -----------------------------------------------------------------------------
-- Tabla: RESENA
-- Relación 1:1 con RESERVA (uq_resena_reserva). No guarda id_usuario ni
-- id_motel: se obtienen por la reserva (evita dependencias transitivas).
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS resena (
	id_resena     BIGINT         NOT NULL AUTO_INCREMENT,
	id_reserva    BIGINT         NOT NULL,
	calificacion  INT            NOT NULL,
	comentario    VARCHAR(1000)  NULL,
	fecha         DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
	CONSTRAINT pk_resena PRIMARY KEY (id_resena),
	CONSTRAINT uq_resena_reserva UNIQUE (id_reserva),
	CONSTRAINT fk_resena_reserva FOREIGN KEY (id_reserva)
		REFERENCES reserva (id_reserva),
	CONSTRAINT ck_resena_calificacion CHECK (calificacion BETWEEN 1 AND 5)
) ENGINE = InnoDB;

-- -----------------------------------------------------------------------------
-- Tabla: RESPUESTA_PROPIETARIO
-- Entidad débil 1:1 con RESENA: su PK es id_resena.
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS respuesta_propietario (
	id_resena   BIGINT         NOT NULL,
	comentario  VARCHAR(1000)  NOT NULL,
	fecha       DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
	CONSTRAINT pk_respuesta_propietario PRIMARY KEY (id_resena),
	CONSTRAINT fk_respuesta_propietario_resena FOREIGN KEY (id_resena)
		REFERENCES resena (id_resena) ON DELETE CASCADE
) ENGINE = InnoDB;

-- -----------------------------------------------------------------------------
-- Catálogos base. INSERT IGNORE omite las filas que violan una UNIQUE, así
-- que no duplica datos si la migración se ejecuta otra vez.
-- -----------------------------------------------------------------------------
INSERT IGNORE INTO departamento (nombre) VALUES
	('Antioquia'), ('Cundinamarca'), ('Valle del Cauca'), ('Bogotá D.C.');

INSERT IGNORE INTO ciudad (id_departamento, nombre)
SELECT d.id_departamento, c.nombre
FROM (SELECT 'Antioquia' AS dep, 'Medellín' AS nombre
	UNION ALL SELECT 'Antioquia', 'Envigado'
	UNION ALL SELECT 'Bogotá D.C.', 'Bogotá'
	UNION ALL SELECT 'Cundinamarca', 'Chía'
	UNION ALL SELECT 'Valle del Cauca', 'Cali') c
	JOIN departamento d ON d.nombre = c.dep;

INSERT IGNORE INTO caracteristica (nombre, icono) VALUES
	('Jacuzzi', 'hot_tub'), ('TV Smart', 'tv'), ('Aire acondicionado', 'ac_unit'),
	('Sauna', 'spa'), ('Garaje privado', 'garage'), ('Wi-Fi', 'wifi');

INSERT IGNORE INTO modalidad (nombre, duracion_horas) VALUES
	('Rato', 3), ('Medio día', 6), ('Noche', 12);
