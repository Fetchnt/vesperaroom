-- =============================================================================
-- VesperaRoom - Script de inserción de registros
-- RDBMS: MySQL 8.x
-- Descripción: datos de prueba para las 19 tablas. Se insertan con
--              identificadores explícitos para que las llaves foráneas sean
--              fáciles de seguir. Ejecutar después de 01_creacion_objetos.sql.
--              Todas las cuentas de prueba usan la contraseña: Vespera2026
--              (almacenada como hash BCrypt, igual que la aplicación).
-- =============================================================================

USE motelnow;

-- -----------------------------------------------------------------------------
-- Catálogos geográficos
-- -----------------------------------------------------------------------------
INSERT INTO departamento (id_departamento, nombre) VALUES
	(1, 'Antioquia'),
	(2, 'Cundinamarca'),
	(3, 'Valle del Cauca'),
	(4, 'Bogotá D.C.');

INSERT INTO ciudad (id_ciudad, id_departamento, nombre) VALUES
	(1, 1, 'Medellín'),
	(2, 1, 'Envigado'),
	(3, 4, 'Bogotá'),
	(4, 2, 'Chía'),
	(5, 3, 'Cali');

-- -----------------------------------------------------------------------------
-- Usuarios (id 1-3 clientes, 4-5 propietarios, 6 administrador)
-- -----------------------------------------------------------------------------
INSERT INTO usuario (id_usuario, nombre, apellido, correo, telefono, contrasena, rol, estado, fecha_registro) VALUES
	(1, 'Laura',  'Gómez',     'cliente1@vesperaroom.com',     '3001112233', '$2a$10$253..OGY.ibdajjTXRy0Cuv2oBdJwjymdY4ytL5gIn1pk4VTiS.N.', 'CLIENTE',       'ACTIVO',   '2026-09-01 10:15:00'),
	(2, 'Andrés', 'Rodríguez', 'cliente2@vesperaroom.com',     '3002223344', '$2a$10$yAH2Yh0YRkdHWFsnNtAnPe7gX..GVxC8RWZRRGDhLslA0vpzKtA.2', 'CLIENTE',       'ACTIVO',   '2026-09-05 18:40:00'),
	(3, 'Camila', 'Torres',    'cliente3@vesperaroom.com',     '3003334455', '$2a$10$gmlupai8hmB4/6GkyeYqfe3Or77c8NcjFX.naTdr3b2ZVrNQHYeny', 'CLIENTE',       'INACTIVO', '2026-09-12 09:05:00'),
	(4, 'Carlos', 'Martínez',  'propietario1@vesperaroom.com', '3104445566', '$2a$10$6ClKE.1097M5Mu3tx.7ZHuEizT6T6qqPOSNqwK0OT8X5FjDIcZpZ2', 'PROPIETARIO',   'ACTIVO',   '2026-08-20 14:30:00'),
	(5, 'Sofía',  'Herrera',   'propietario2@vesperaroom.com', '3115556677', '$2a$10$d6PyBQCjfNiO2iVOpnhVuOKCRoLurnVuGR4BCrldGMhQ2a8DHk/wi', 'PROPIETARIO',   'ACTIVO',   '2026-08-25 11:00:00'),
	(6, 'Diana',  'Castro',    'soporte@vesperaroom.com',      '3126667788', '$2a$10$WeIlUa.gf0VX8mxeakzE8OFRgLb4ne2jJ3xL1E9m/DWo/E77eVOZC', 'ADMINISTRADOR', 'ACTIVO',   '2026-08-15 08:00:00');

-- -----------------------------------------------------------------------------
-- Perfiles de propietario (PK = id del usuario)
-- -----------------------------------------------------------------------------
INSERT INTO perfil_propietario (id_usuario, nit, razon_social, banco, tipo_cuenta, numero_cuenta, fecha_verificacion) VALUES
	(4, '900123456-7', 'Inversiones Luna S.A.S.', 'Bancolombia', 'CORRIENTE', '12345678901',  '2026-08-22 09:00:00'),
	(5, '901234567-1', 'Hoteles Sol S.A.S.',      'Davivienda',  'AHORROS',   '009876543210', NULL);

-- -----------------------------------------------------------------------------
-- Moteles y políticas de reserva
-- -----------------------------------------------------------------------------
INSERT INTO motel (id_motel, id_propietario, id_ciudad, nombre, descripcion, calle, carrera, latitud, longitud, estado) VALUES
	(1, 4, 1, 'Motel Luna',    'Habitaciones temáticas con jacuzzi y garaje privado.', 'Calle 10',  'Carrera 43A', 6.208800, -75.567700, 'ACTIVO'),
	(2, 4, 2, 'Motel Eclipse', 'Opción económica cerca de la autopista sur.',          'Calle 37S', 'Carrera 48',  6.169500, -75.590900, 'ACTIVO'),
	(3, 5, 3, 'Motel Sol',     'Suites amplias para grupos y celebraciones.',          'Calle 80',  'Carrera 68',  4.687400, -74.081700, 'ACTIVO');

INSERT INTO politica_reserva (id_motel, dias_habiles_devolucion, porcentaje_reembolso, capacidad_max_sin_recargo,
		tarifa_persona_adicional, anticipacion_minima_horas, tiempo_gracia_no_show_min) VALUES
	(1, 5, 80, 2, 25000.00, 1, 20),
	(2, 0,  0, 2, 15000.00, 0, 15),
	(3, 3, 50, 4, 30000.00, 2, 30);

-- -----------------------------------------------------------------------------
-- Tipos de habitación y características
-- -----------------------------------------------------------------------------
INSERT INTO tipo_habitacion (id_tipo, id_motel, nombre, capacidad_personas, descripcion) VALUES
	(1, 1, 'Estándar',           2, 'Cama doble, baño privado y TV.'),
	(2, 1, 'Suite Jacuzzi',      2, 'Suite con jacuzzi y garaje privado.'),
	(3, 2, 'Estándar',           2, 'Habitación sencilla y cómoda.'),
	(4, 3, 'Estándar',           2, 'Cama doble y aire acondicionado.'),
	(5, 3, 'Suite Presidencial', 6, 'Sala, jacuzzi, sauna y capacidad para grupos.');

INSERT INTO caracteristica (id_caracteristica, nombre, icono) VALUES
	(1, 'Jacuzzi',            'hot_tub'),
	(2, 'TV Smart',           'tv'),
	(3, 'Aire acondicionado', 'ac_unit'),
	(4, 'Sauna',              'spa'),
	(5, 'Garaje privado',     'garage'),
	(6, 'Wi-Fi',              'wifi');

INSERT INTO tipo_habitacion_caracteristica (id_tipo, id_caracteristica) VALUES
	(1, 2), (1, 3), (1, 6),
	(2, 1), (2, 2), (2, 3), (2, 5), (2, 6),
	(3, 2), (3, 6),
	(4, 2), (4, 3), (4, 6),
	(5, 1), (5, 2), (5, 3), (5, 4), (5, 5), (5, 6);

-- -----------------------------------------------------------------------------
-- Habitaciones (id_motel debe coincidir con el motel del tipo: FK compuesta)
-- -----------------------------------------------------------------------------
INSERT INTO habitacion (id_habitacion, id_motel, id_tipo, numero, piso, estado) VALUES
	(1, 1, 1, '101', 1, 'ACTIVA'),
	(2, 1, 1, '102', 1, 'ACTIVA'),
	(3, 1, 2, '201', 2, 'ACTIVA'),
	(4, 1, 2, '202', 2, 'MANTENIMIENTO'),
	(5, 2, 3, '1',   1, 'ACTIVA'),
	(6, 2, 3, '2',   1, 'ACTIVA'),
	(7, 3, 4, '301', 3, 'ACTIVA'),
	(8, 3, 4, '302', 3, 'ACTIVA'),
	(9, 3, 5, '401', 4, 'ACTIVA');

-- Fotos: las de motel llevan id_motel y las de habitación id_habitacion
INSERT INTO foto (id_foto, id_motel, id_habitacion, uri, orden) VALUES
	(1, 1,    NULL, 'https://cdn.vesperaroom.com/moteles/luna/fachada.jpg',      1),
	(2, NULL, 3,    'https://cdn.vesperaroom.com/moteles/luna/201-jacuzzi.jpg',  1),
	(3, NULL, 3,    'https://cdn.vesperaroom.com/moteles/luna/201-cama.jpg',     2),
	(4, 3,    NULL, 'https://cdn.vesperaroom.com/moteles/sol/fachada.jpg',       1),
	(5, NULL, 9,    'https://cdn.vesperaroom.com/moteles/sol/401-sala.jpg',      1);

-- -----------------------------------------------------------------------------
-- Modalidades y tarifas
-- -----------------------------------------------------------------------------
INSERT INTO modalidad (id_modalidad, nombre, duracion_horas) VALUES
	(1, 'Rato',      3),
	(2, 'Medio día', 6),
	(3, 'Noche',    12);

INSERT INTO tarifa (id_tarifa, id_tipo, id_modalidad, precio, tipo_dia, vigente_desde, vigente_hasta) VALUES
	(1,  1, 1,  60000.00, 'TODOS',         '2026-01-01', NULL),
	(2,  1, 3, 120000.00, 'TODOS',         '2026-01-01', NULL),
	(3,  2, 1, 110000.00, 'TODOS',         '2026-01-01', NULL),
	(4,  2, 3, 220000.00, 'ENTRE_SEMANA',  '2026-01-01', NULL),
	(5,  2, 3, 250000.00, 'FIN_DE_SEMANA', '2026-01-01', NULL),
	(6,  3, 1,  55000.00, 'TODOS',         '2026-01-01', NULL),
	(7,  4, 1,  70000.00, 'TODOS',         '2026-01-01', NULL),
	(8,  4, 3, 140000.00, 'TODOS',         '2026-01-01', NULL),
	(9,  5, 1, 180000.00, 'TODOS',         '2026-01-01', NULL),
	(10, 5, 3, 350000.00, 'TODOS',         '2026-01-01', NULL);

-- -----------------------------------------------------------------------------
-- Servicios adicionales
-- -----------------------------------------------------------------------------
INSERT INTO servicio_adicional (id_servicio, id_motel, nombre, descripcion, precio, activo) VALUES
	(1, 1, 'Botella de champaña',  'Botella de 750 ml con copas.',           90000.00, TRUE),
	(2, 1, 'Decoración romántica', 'Pétalos, velas y globos.',               50000.00, TRUE),
	(3, 1, 'Picada para dos',      'Tabla de carnes y quesos.',              45000.00, TRUE),
	(4, 3, 'Kit de bienvenida',    'Snacks y bebidas sin alcohol.',          30000.00, TRUE),
	(5, 3, 'Cena para dos',        'Servicio suspendido temporalmente.',    120000.00, FALSE);

-- -----------------------------------------------------------------------------
-- Reservas con su detalle de servicios, pagos, reseñas y respuestas
-- total = precio de la tarifa + recargo por personas + servicios
-- -----------------------------------------------------------------------------
INSERT INTO reserva (id_reserva, codigo_reserva, id_usuario, id_habitacion, id_tarifa, cantidad_personas,
		fecha_creacion, fecha_hora_inicio, fecha_hora_fin, estado, total) VALUES
	(1, 'VR-A1B2C3D4', 1, 3, 3,  2, '2026-10-08 12:00:00', '2026-10-10 21:00:00', '2026-10-11 00:00:00', 'FINALIZADA', 200000.00),
	(2, 'VR-E5F6G7H8', 2, 1, 2,  2, '2026-10-14 09:30:00', '2026-10-15 22:00:00', '2026-10-16 10:00:00', 'FINALIZADA', 120000.00),
	(3, 'VR-J9K8L7M6', 1, 9, 10, 4, '2026-10-25 16:10:00', '2026-11-05 21:00:00', '2026-11-06 09:00:00', 'CONFIRMADA', 380000.00),
	(4, 'VR-N5P4Q3R2', 2, 7, 7,  2, '2026-10-18 11:45:00', '2026-10-20 15:00:00', '2026-10-20 18:00:00', 'CANCELADA',   70000.00);

INSERT INTO reserva_servicio (id_reserva, id_servicio, cantidad, precio_unitario) VALUES
	(1, 1, 1, 90000.00),
	(3, 4, 1, 30000.00);

INSERT INTO pago (id_pago, id_reserva, monto, fecha_pago, metodo_pago, estado, referencia_transaccion) VALUES
	(1, 1, 200000.00, '2026-10-08 12:05:00', 'TARJETA_CREDITO', 'APROBADO', 'TX-000001'),
	(2, 2, 120000.00, '2026-10-15 22:05:00', 'EFECTIVO',        'APROBADO', NULL),
	(3, 3, 190000.00, '2026-10-25 16:15:00', 'NEQUI',           'APROBADO', 'NQ-778120');

INSERT INTO resena (id_resena, id_reserva, calificacion, comentario, fecha) VALUES
	(1, 1, 5, 'Excelente atención, la suite estaba impecable.', '2026-10-11 10:00:00'),
	(2, 2, 4, 'Buena relación precio-calidad.',                 '2026-10-16 12:30:00');

INSERT INTO respuesta_propietario (id_resena, comentario, fecha) VALUES
	(1, '¡Gracias por visitarnos, Laura! Te esperamos pronto.', '2026-10-11 15:20:00');
