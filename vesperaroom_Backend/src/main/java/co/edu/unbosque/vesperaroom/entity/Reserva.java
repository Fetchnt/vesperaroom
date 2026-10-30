package co.edu.unbosque.vesperaroom.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidad que representa la reserva de una habitación por parte de un cliente
 * y se corresponde con la tabla {@code reserva}. {@code total} y
 * {@code fechaHoraFin} se guardan como valores históricos (lo cobrado y la
 * estadía real), no como datos derivados. Al cancelar se registran la fecha,
 * el monto a devolver y la fecha límite de devolución; quedan nulos mientras la
 * reserva no se cancele.
 */
public class Reserva {

	private Long idReserva;
	private String codigoReserva;
	private Long idUsuario;
	private Long idHabitacion;
	private Long idTarifa;
	private Integer cantidadPersonas;
	private LocalDateTime fechaCreacion;
	private LocalDateTime fechaHoraInicio;
	private LocalDateTime fechaHoraFin;
	private Estado estado;
	private BigDecimal total;
	private LocalDateTime fechaCancelacion;
	private BigDecimal montoReembolso;
	private LocalDate fechaLimiteReembolso;

	/**
	 * Define el ciclo de vida de una reserva. Las reservas {@code PENDIENTE},
	 * {@code CONFIRMADA} y {@code EN_CURSO} ocupan la habitación en su horario.
	 */
	public enum Estado {
		PENDIENTE, CONFIRMADA, EN_CURSO, FINALIZADA, CANCELADA, NO_SHOW
	}

	/**
	 * Constructor por defecto que inicializa la reserva con estado {@code PENDIENTE}.
	 */
	public Reserva() {
		this.estado = Estado.PENDIENTE;
	}

	public Reserva(Long idUsuario, Long idHabitacion, Long idTarifa, Integer cantidadPersonas,
			LocalDateTime fechaHoraInicio) {
		this();
		this.idUsuario = idUsuario;
		this.idHabitacion = idHabitacion;
		this.idTarifa = idTarifa;
		this.cantidadPersonas = cantidadPersonas;
		this.fechaHoraInicio = fechaHoraInicio;
	}

	@Override
	public String toString() {
		return "Reserva [idReserva=" + idReserva + ", codigoReserva=" + codigoReserva + ", idUsuario="
				+ idUsuario + ", idHabitacion=" + idHabitacion + ", idTarifa=" + idTarifa
				+ ", cantidadPersonas=" + cantidadPersonas + ", fechaCreacion=" + fechaCreacion
				+ ", fechaHoraInicio=" + fechaHoraInicio + ", fechaHoraFin=" + fechaHoraFin + ", estado="
				+ estado + ", total=" + total + ", fechaCancelacion=" + fechaCancelacion + ", montoReembolso="
				+ montoReembolso + ", fechaLimiteReembolso=" + fechaLimiteReembolso + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(cantidadPersonas, codigoReserva, estado, fechaCancelacion, fechaCreacion,
				fechaHoraFin, fechaHoraInicio, fechaLimiteReembolso, idHabitacion, idReserva, idTarifa, idUsuario,
				montoReembolso, total);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Reserva other = (Reserva) obj;
		return Objects.equals(cantidadPersonas, other.cantidadPersonas)
				&& Objects.equals(codigoReserva, other.codigoReserva) && estado == other.estado
				&& Objects.equals(fechaCreacion, other.fechaCreacion)
				&& Objects.equals(fechaHoraFin, other.fechaHoraFin)
				&& Objects.equals(fechaHoraInicio, other.fechaHoraInicio)
				&& Objects.equals(idHabitacion, other.idHabitacion)
				&& Objects.equals(idReserva, other.idReserva) && Objects.equals(idTarifa, other.idTarifa)
				&& Objects.equals(idUsuario, other.idUsuario) && Objects.equals(total, other.total)
				&& Objects.equals(fechaCancelacion, other.fechaCancelacion)
				&& Objects.equals(montoReembolso, other.montoReembolso)
				&& Objects.equals(fechaLimiteReembolso, other.fechaLimiteReembolso);
	}

	public Long getIdReserva() {
		return idReserva;
	}

	public void setIdReserva(Long idReserva) {
		this.idReserva = idReserva;
	}

	public String getCodigoReserva() {
		return codigoReserva;
	}

	public void setCodigoReserva(String codigoReserva) {
		this.codigoReserva = codigoReserva;
	}

	public Long getIdUsuario() {
		return idUsuario;
	}

	public void setIdUsuario(Long idUsuario) {
		this.idUsuario = idUsuario;
	}

	public Long getIdHabitacion() {
		return idHabitacion;
	}

	public void setIdHabitacion(Long idHabitacion) {
		this.idHabitacion = idHabitacion;
	}

	public Long getIdTarifa() {
		return idTarifa;
	}

	public void setIdTarifa(Long idTarifa) {
		this.idTarifa = idTarifa;
	}

	public Integer getCantidadPersonas() {
		return cantidadPersonas;
	}

	public void setCantidadPersonas(Integer cantidadPersonas) {
		this.cantidadPersonas = cantidadPersonas;
	}

	public LocalDateTime getFechaCreacion() {
		return fechaCreacion;
	}

	public void setFechaCreacion(LocalDateTime fechaCreacion) {
		this.fechaCreacion = fechaCreacion;
	}

	public LocalDateTime getFechaHoraInicio() {
		return fechaHoraInicio;
	}

	public void setFechaHoraInicio(LocalDateTime fechaHoraInicio) {
		this.fechaHoraInicio = fechaHoraInicio;
	}

	public LocalDateTime getFechaHoraFin() {
		return fechaHoraFin;
	}

	public void setFechaHoraFin(LocalDateTime fechaHoraFin) {
		this.fechaHoraFin = fechaHoraFin;
	}

	public Estado getEstado() {
		return estado;
	}

	public void setEstado(Estado estado) {
		this.estado = estado;
	}

	public BigDecimal getTotal() {
		return total;
	}

	public void setTotal(BigDecimal total) {
		this.total = total;
	}

	public LocalDateTime getFechaCancelacion() {
		return fechaCancelacion;
	}

	public void setFechaCancelacion(LocalDateTime fechaCancelacion) {
		this.fechaCancelacion = fechaCancelacion;
	}

	public BigDecimal getMontoReembolso() {
		return montoReembolso;
	}

	public void setMontoReembolso(BigDecimal montoReembolso) {
		this.montoReembolso = montoReembolso;
	}

	public LocalDate getFechaLimiteReembolso() {
		return fechaLimiteReembolso;
	}

	public void setFechaLimiteReembolso(LocalDate fechaLimiteReembolso) {
		this.fechaLimiteReembolso = fechaLimiteReembolso;
	}
}
