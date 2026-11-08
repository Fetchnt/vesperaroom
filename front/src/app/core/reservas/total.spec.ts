import { recargoPorPersonas, tarifaDelDia, totalEstimado } from './total';
import type { PoliticaReserva, ServicioAdicional, Tarifa } from '../api/models';

/** Políticas y precios tal como los carga database/02_insercion_registros.sql. */
const POLITICA_MOTEL_1: PoliticaReserva = {
  idMotel: 1,
  diasHabilesDevolucion: 5,
  porcentajeReembolso: 80,
  capacidadMaxSinRecargo: 2,
  tarifaPersonaAdicional: 25000,
  anticipacionMinimaHoras: 1,
  tiempoGraciaNoShowMin: 20,
};

const POLITICA_MOTEL_3: PoliticaReserva = {
  idMotel: 3,
  diasHabilesDevolucion: 3,
  porcentajeReembolso: 50,
  capacidadMaxSinRecargo: 4,
  tarifaPersonaAdicional: 30000,
  anticipacionMinimaHoras: 2,
  tiempoGraciaNoShowMin: 30,
};

function servicio(idServicio: number, precio: number): ServicioAdicional {
  return { idServicio, idMotel: 1, nombre: `Servicio ${idServicio}`, precio };
}

describe('recargoPorPersonas', () => {
  it('no cobra recargo hasta la capacidad sin recargo', () => {
    expect(recargoPorPersonas(POLITICA_MOTEL_1, 2)).toBe(0);
  });

  it('cobra la tarifa por cada persona que excede la capacidad', () => {
    expect(recargoPorPersonas(POLITICA_MOTEL_1, 4)).toBe(50000);
  });

  it('no cobra recargo si el motel no tiene política', () => {
    expect(recargoPorPersonas(null, 10)).toBe(0);
  });
});

describe('totalEstimado', () => {
  it('reproduce la reserva 1 del script: tarifa 110000 + champaña 90000', () => {
    const total = totalEstimado(110000, [{ servicio: servicio(1, 90000), cantidad: 1 }], 2, POLITICA_MOTEL_1);
    expect(total).toBe(200000);
  });

  it('reproduce la reserva 3 del script: tarifa 350000 + kit 30000, 4 personas sin recargo', () => {
    const total = totalEstimado(350000, [{ servicio: servicio(4, 30000), cantidad: 1 }], 4, POLITICA_MOTEL_3);
    expect(total).toBe(380000);
  });

  it('suma el recargo por personas adicionales', () => {
    // 60000 de tarifa + 2 personas extra × 25000 en el motel 1.
    expect(totalEstimado(60000, [], 4, POLITICA_MOTEL_1)).toBe(110000);
  });

  it('multiplica cada servicio por su cantidad', () => {
    const elegidos = [
      { servicio: servicio(2, 50000), cantidad: 2 },
      { servicio: servicio(3, 45000), cantidad: 1 },
    ];
    expect(totalEstimado(60000, elegidos, 2, POLITICA_MOTEL_1)).toBe(205000);
  });

  it('ignora cantidades negativas', () => {
    expect(totalEstimado(60000, [{ servicio: servicio(1, 90000), cantidad: -3 }], 2, POLITICA_MOTEL_1)).toBe(
      60000,
    );
  });
});

describe('tarifaDelDia', () => {
  // Tipo 2 (Suite Jacuzzi) del script de datos: Rato todos los días y Noche con precio por tipo de día.
  const TARIFAS: Tarifa[] = [
    { idTarifa: 3, idTipo: 2, idModalidad: 1, precio: 110000, tipoDia: 'TODOS', vigenteDesde: '2026-01-01' },
    { idTarifa: 4, idTipo: 2, idModalidad: 3, precio: 220000, tipoDia: 'ENTRE_SEMANA', vigenteDesde: '2026-01-01' },
    { idTarifa: 5, idTipo: 2, idModalidad: 3, precio: 250000, tipoDia: 'FIN_DE_SEMANA', vigenteDesde: '2026-01-01' },
    { idTarifa: 9, idTipo: 2, idModalidad: 1, precio: 90000, tipoDia: 'TODOS', vigenteDesde: '2025-01-01', vigenteHasta: '2025-12-31' },
  ];

  it('elige la tarifa del tipo de día de la entrada', () => {
    expect(tarifaDelDia(TARIFAS, 3, '2026-11-18T21:00')?.idTarifa).toBe(4); // miércoles
    expect(tarifaDelDia(TARIFAS, 3, '2026-11-21T21:00')?.idTarifa).toBe(5); // sábado
  });

  it('usa la de todos los días y descarta las que no están vigentes', () => {
    expect(tarifaDelDia(TARIFAS, 1, '2026-11-21T21:00')?.idTarifa).toBe(3);
    expect(tarifaDelDia(TARIFAS, 1, '2025-06-10T21:00')?.idTarifa).toBe(9);
  });

  it('devuelve null si la duración no tiene tarifa o no se ha elegido', () => {
    expect(tarifaDelDia(TARIFAS, 2, '2026-11-18T21:00')).toBeNull();
    expect(tarifaDelDia(TARIFAS, null, '2026-11-18T21:00')).toBeNull();
  });
});
