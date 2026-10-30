package co.edu.unbosque.vesperaroom.entity;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidad que representa los datos comerciales y bancarios de un propietario y
 * se corresponde con la tabla {@code perfil_propietario}. Es una relación 1:1
 * con {@link Usuario}: su identificador es el mismo {@code id_usuario} del
 * usuario dueño del perfil.
 */
public class PerfilPropietario {

	private Long idUsuario;
	private String nit;
	private String razonSocial;
	private String banco;
	private TipoCuenta tipoCuenta;
	private String numeroCuenta;
	private LocalDateTime fechaVerificacion;

	/**
	 * Define los tipos de cuenta bancaria en la que el propietario recibe sus
	 * pagos.
	 */
	public enum TipoCuenta {
		AHORROS, CORRIENTE
	}

	public PerfilPropietario() {
	}

	public PerfilPropietario(Long idUsuario, String nit, String razonSocial, String banco,
			TipoCuenta tipoCuenta, String numeroCuenta) {
		this.idUsuario = idUsuario;
		this.nit = nit;
		this.razonSocial = razonSocial;
		this.banco = banco;
		this.tipoCuenta = tipoCuenta;
		this.numeroCuenta = numeroCuenta;
	}

	@Override
	public String toString() {
		return "PerfilPropietario [idUsuario=" + idUsuario + ", nit=" + nit + ", razonSocial=" + razonSocial
				+ ", banco=" + banco + ", tipoCuenta=" + tipoCuenta + ", numeroCuenta=" + numeroCuenta
				+ ", fechaVerificacion=" + fechaVerificacion + "]";
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
		PerfilPropietario other = (PerfilPropietario) obj;
		return Objects.equals(banco, other.banco)
				&& Objects.equals(fechaVerificacion, other.fechaVerificacion)
				&& Objects.equals(idUsuario, other.idUsuario) && Objects.equals(nit, other.nit)
				&& Objects.equals(numeroCuenta, other.numeroCuenta)
				&& Objects.equals(razonSocial, other.razonSocial) && tipoCuenta == other.tipoCuenta;
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
}
