package co.edu.unbosque.vesperaroom.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

import co.edu.unbosque.vesperaroom.entity.Reserva.Estado;

/**
 * Objeto de transferencia de datos que representa una reserva. El cliente
 * envía la habitación, la tarifa, la fecha de inicio, la cantidad de personas y
 * los servicios; el código, las fechas calculadas, el estado y el total son de
 * solo lectura porque los calcula el servidor. Los datos de cancelación y
 * reembolso también son de solo lectura y solo vienen llenos en reservas
 * canceladas.
 */
public class ReservaDTO {

	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private Long idReserva;

	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private String codigoReserva;

	private Long idUsuario;

	private Long idHabitacion;

	private Long idTarifa;

	private Integer cantidadPersonas;

	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private LocalDateTime fechaCreacion;

	private LocalDateTime fechaHoraInicio;

	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private LocalDateTime fechaHoraFin;

	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private Estado estado;

	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private BigDecimal total;

	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private LocalDateTime fechaCancelacion;

	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private BigDecimal montoReembolso;

	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private LocalDate fechaLimiteReembolso;

	private List<ReservaServicioDTO> servicios;

	public ReservaDTO() {
	}

	public ReservaDTO(Long idUsuario, Long idHabitacion, Long idTarifa, Integer cantidadPersonas,
			LocalDateTime fechaHoraInicio) {
		this.idUsuario = idUsuario;
		this.idHabitacion = idHabitacion;
		this.idTarifa = idTarifa;
		this.cantidadPersonas = cantidadPersonas;
		this.fechaHoraInicio = fechaHoraInicio;
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

	public List<ReservaServicioDTO> getServicios() {
		return servicios;
	}

	public void setServicios(List<ReservaServicioDTO> servicios) {
		this.servicios = servicios;
	}

	@Override
	public String toString() {
		return "ReservaDTO [idReserva=" + idReserva + ", codigoReserva=" + codigoReserva + ", idUsuario="
				+ idUsuario + ", idHabitacion=" + idHabitacion + ", idTarifa=" + idTarifa
				+ ", cantidadPersonas=" + cantidadPersonas + ", fechaCreacion=" + fechaCreacion
				+ ", fechaHoraInicio=" + fechaHoraInicio + ", fechaHoraFin=" + fechaHoraFin + ", estado="
				+ estado + ", total=" + total + ", fechaCancelacion=" + fechaCancelacion + ", montoReembolso="
				+ montoReembolso + ", fechaLimiteReembolso=" + fechaLimiteReembolso + ", servicios=" + servicios
				+ "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(cantidadPersonas, codigoReserva, estado, fechaCancelacion, fechaCreacion,
				fechaHoraFin, fechaHoraInicio, fechaLimiteReembolso, idHabitacion, idReserva, idTarifa, idUsuario,
				montoReembolso, servicios, total);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ReservaDTO other = (ReservaDTO) obj;
		return Objects.equals(cantidadPersonas, other.cantidadPersonas)
				&& Objects.equals(codigoReserva, other.codigoReserva) && estado == other.estado
				&& Objects.equals(fechaCreacion, other.fechaCreacion)
				&& Objects.equals(fechaHoraFin, other.fechaHoraFin)
				&& Objects.equals(fechaHoraInicio, other.fechaHoraInicio)
				&& Objects.equals(idHabitacion, other.idHabitacion)
				&& Objects.equals(idReserva, other.idReserva) && Objects.equals(idTarifa, other.idTarifa)
				&& Objects.equals(idUsuario, other.idUsuario) && Objects.equals(servicios, other.servicios)
				&& Objects.equals(total, other.total) && Objects.equals(fechaCancelacion, other.fechaCancelacion)
				&& Objects.equals(montoReembolso, other.montoReembolso)
				&& Objects.equals(fechaLimiteReembolso, other.fechaLimiteReembolso);
	}
}
