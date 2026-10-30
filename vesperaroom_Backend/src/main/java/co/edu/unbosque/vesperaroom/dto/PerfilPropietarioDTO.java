package co.edu.unbosque.vesperaroom.dto;

import java.time.LocalDateTime;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

import co.edu.unbosque.vesperaroom.entity.PerfilPropietario.TipoCuenta;

/**
 * Objeto de transferencia de datos que representa el perfil de un propietario.
 * {@code fechaVerificacion} es de solo lectura: la asigna un administrador.
 */
public class PerfilPropietarioDTO {

	private Long idUsuario;

	private String nit;

	private String razonSocial;

	private String banco;

	private TipoCuenta tipoCuenta;

	private String numeroCuenta;

	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private LocalDateTime fechaVerificacion;

	public PerfilPropietarioDTO() {
	}

	public PerfilPropietarioDTO(Long idUsuario, String nit, String razonSocial, String banco,
			TipoCuenta tipoCuenta, String numeroCuenta) {
		this.idUsuario = idUsuario;
		this.nit = nit;
		this.razonSocial = razonSocial;
		this.banco = banco;
		this.tipoCuenta = tipoCuenta;
		this.numeroCuenta = numeroCuenta;
	}

	public Long getIdUsuario() {
		return idUsuario;
	}

	public void setIdUsuario(Long idUsuario) {
		this.idUsuario = idUsuario;
	}

	public String getNit() {
		return nit;
	}

	public void setNit(String nit) {
		this.nit = nit;
	}

	public String getRazonSocial() {
		return razonSocial;
	}

	public void setRazonSocial(String razonSocial) {
		this.razonSocial = razonSocial;
	}

	public String getBanco() {
		return banco;
	}

	public void setBanco(String banco) {
		this.banco = banco;
	}

	public TipoCuenta getTipoCuenta() {
		return tipoCuenta;
	}

	public void setTipoCuenta(TipoCuenta tipoCuenta) {
		this.tipoCuenta = tipoCuenta;
	}

	public String getNumeroCuenta() {
		return numeroCuenta;
	}

	public void setNumeroCuenta(String numeroCuenta) {
		this.numeroCuenta = numeroCuenta;
	}

	public LocalDateTime getFechaVerificacion() {
		return fechaVerificacion;
	}

	public void setFechaVerificacion(LocalDateTime fechaVerificacion) {
		this.fechaVerificacion = fechaVerificacion;
	}

	@Override
	public String toString() {
		return "PerfilPropietarioDTO [idUsuario=" + idUsuario + ", nit=" + nit + ", razonSocial="
				+ razonSocial + ", banco=" + banco + ", tipoCuenta=" + tipoCuenta + ", numeroCuenta="
				+ numeroCuenta + ", fechaVerificacion=" + fechaVerificacion + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(banco, fechaVerificacion, idUsuario, nit, numeroCuenta, razonSocial, tipoCuenta);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PerfilPropietarioDTO other = (PerfilPropietarioDTO) obj;
		return Objects.equals(banco, other.banco)
				&& Objects.equals(fechaVerificacion, other.fechaVerificacion)
				&& Objects.equals(idUsuario, other.idUsuario) && Objects.equals(nit, other.nit)
				&& Objects.equals(numeroCuenta, other.numeroCuenta)
				&& Objects.equals(razonSocial, other.razonSocial) && tipoCuenta == other.tipoCuenta;
	}
}
