
-- VesperaRoom - Scripts varios
-- RDBMS: MySQL 8.x
-- Descripción: usuario de aplicación, vista de apoyo y consultas que usa o
--              puede usar el backend (disponibilidad, ingresos, calificaciones).

-- -----------------------------------------------------------------------------
-- Usuario de base de datos para la aplicación (ejecutar como root).
-- Reemplazar <contrasena_segura> antes de ejecutar; debe coincidir con
-- DB_PASSWORD en .env.properties. Solo recibe permisos de datos (DML).
-- -----------------------------------------------------------------------------
-- CREATE USER IF NOT EXISTS 'usuario'@'%' IDENTIFIED BY '<contrasena_segura>';
-- GRANT SELECT, INSERT, UPDATE, DELETE ON vesperaroom.* TO 'usuario'@'%';
-- FLUSH PRIVILEGES;

USE vesperaroom;

-- -----------------------------------------------------------------------------
-- Vista: detalle legible de cada reserva (une 6 tablas). Las vistas no guardan
-- datos, así que no rompen la normalización.
-- -----------------------------------------------------------------------------
CREATE OR REPLACE VIEW v_reserva_detalle AS
SELECT r.id_reserva,
	r.codigo_reserva,
	CONCAT(u.nombre, ' ', u.apellido) AS cliente,
	m.nombre AS motel,
	c.nombre AS ciudad,
	h.numero AS habitacion,
	t.nombre AS tipo_habitacion,
	mo.nombre AS modalidad,
	r.fecha_hora_inicio,
	r.fecha_hora_fin,
	r.estado,
	r.total
FROM reserva r
	JOIN usuario u ON u.id_usuario = r.id_usuario
	JOIN habitacion h ON h.id_habitacion = r.id_habitacion
	JOIN tipo_habitacion t ON t.id_tipo = h.id_tipo
	JOIN motel m ON m.id_motel = h.id_motel
	JOIN ciudad c ON c.id_ciudad = m.id_ciudad
	JOIN tarifa ta ON ta.id_tarifa = r.id_tarifa
	JOIN modalidad mo ON mo.id_modalidad = ta.id_modalidad;

SELECT * FROM v_reserva_detalle ORDER BY fecha_hora_inicio DESC;

-- -----------------------------------------------------------------------------
-- Disponibilidad: habitaciones activas del motel 1 sin reservas que se crucen
-- con el rango pedido. Dos rangos se cruzan si inicio1 < fin2 y fin1 > inicio2.
-- Es la misma consulta de HabitacionRepository.findDisponibles.
-- -----------------------------------------------------------------------------
SELECT h.id_habitacion, h.numero, t.nombre AS tipo
FROM habitacion h
	JOIN tipo_habitacion t ON t.id_tipo = h.id_tipo
WHERE h.id_motel = 1
	AND h.estado = 'ACTIVA'
	AND NOT EXISTS (
		SELECT 1
		FROM reserva r
		WHERE r.id_habitacion = h.id_habitacion
			AND r.estado IN ('PENDIENTE', 'CONFIRMADA', 'EN_CURSO')
			AND r.fecha_hora_inicio < '2026-10-10 23:00:00'
			AND r.fecha_hora_fin > '2026-10-10 20:00:00')
ORDER BY h.numero;

-- -----------------------------------------------------------------------------
-- Ingresos por motel: suma de pagos aprobados (agregación + JOIN de 4 tablas)
-- -----------------------------------------------------------------------------
SELECT m.id_motel, m.nombre, COALESCE(SUM(p.monto), 0) AS ingresos
FROM motel m
	LEFT JOIN habitacion h ON h.id_motel = m.id_motel
	LEFT JOIN reserva r ON r.id_habitacion = h.id_habitacion
	LEFT JOIN pago p ON p.id_reserva = r.id_reserva AND p.estado = 'APROBADO'
GROUP BY m.id_motel, m.nombre
ORDER BY ingresos DESC;

-- -----------------------------------------------------------------------------
-- Calificación promedio y cantidad de reseñas por motel
-- -----------------------------------------------------------------------------
SELECT m.nombre, ROUND(AVG(re.calificacion), 2) AS promedio, COUNT(re.id_resena) AS resenas
FROM motel m
	JOIN habitacion h ON h.id_motel = m.id_motel
	JOIN reserva r ON r.id_habitacion = h.id_habitacion
	JOIN resena re ON re.id_reserva = r.id_reserva
GROUP BY m.id_motel, m.nombre
ORDER BY promedio DESC;

-- -----------------------------------------------------------------------------
-- Saldo pendiente por reserva: total menos pagos aprobados o pendientes
-- -----------------------------------------------------------------------------
SELECT r.codigo_reserva, r.total,
	COALESCE(SUM(p.monto), 0) AS pagado,
	r.total - COALESCE(SUM(p.monto), 0) AS saldo
FROM reserva r
	LEFT JOIN pago p ON p.id_reserva = r.id_reserva AND p.estado IN ('APROBADO', 'PENDIENTE')
WHERE r.estado NOT IN ('CANCELADA', 'NO_SHOW')
GROUP BY r.id_reserva, r.codigo_reserva, r.total
HAVING saldo > 0;

-- -----------------------------------------------------------------------------
-- Política de reembolso con el atributo derivado acepta_reembolso
-- (se calcula, no se guarda: ver normalización 3FN)
-- -----------------------------------------------------------------------------
SELECT m.nombre, p.porcentaje_reembolso, (p.porcentaje_reembolso > 0) AS acepta_reembolso
FROM politica_reserva p
	JOIN motel m ON m.id_motel = p.id_motel;

-- -----------------------------------------------------------------------------
-- Moteles con su ciudad y departamento (reconstrucción por JOIN tras 3FN)
-- -----------------------------------------------------------------------------
SELECT m.nombre AS motel, c.nombre AS ciudad, d.nombre AS departamento
FROM motel m
	JOIN ciudad c ON c.id_ciudad = m.id_ciudad
	JOIN departamento d ON d.id_departamento = c.id_departamento
ORDER BY d.nombre, c.nombre, m.nombre;

-- -----------------------------------------------------------------------------
-- Servicios adicionales más vendidos
-- -----------------------------------------------------------------------------
SELECT s.nombre, SUM(rs.cantidad) AS unidades, SUM(rs.cantidad * rs.precio_unitario) AS vendido
FROM reserva_servicio rs
	JOIN servicio_adicional s ON s.id_servicio = rs.id_servicio
GROUP BY s.id_servicio, s.nombre
ORDER BY unidades DESC;

-- -----------------------------------------------------------------------------
-- Cantidad de usuarios por rol y estado
-- -----------------------------------------------------------------------------
SELECT rol, estado, COUNT(*) AS total
FROM usuario
GROUP BY rol, estado
ORDER BY rol, estado;
