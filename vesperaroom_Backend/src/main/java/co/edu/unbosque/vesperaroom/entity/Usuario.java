package co.edu.unbosque.vesperaroom.entity;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Entidad que representa un usuario registrado en VesperaRoom y se corresponde
 * con la tabla {@code usuario}. Implementa {@link UserDetails} para integrarse
 * con Spring Security, usando el correo como nombre de usuario y el estado para
 * habilitar o deshabilitar la cuenta.
 */
public class Usuario implements UserDetails {

	private static final long serialVersionUID = 4102837465192837465L;

	private Long idUsuario;
	private String nombre;
	private String apellido;
	private String correo;
	private String telefono;
	private String contrasena;
	private Rol rol;
	private Estado estado;
	private LocalDateTime fechaRegistro;

	/**
	 * Define los roles disponibles para un usuario en el sistema.
	 * {@code ADMINISTRADOR} gestiona usuarios y catálogos, {@code CLIENTE} realiza
	 * reservas y {@code PROPIETARIO} administra moteles.
	 */
	public enum Rol {
		ADMINISTRADOR, CLIENTE, PROPIETARIO
	}

	/**
	 * Define los estados posibles de la cuenta. Solo las cuentas {@code ACTIVO}
	 * pueden iniciar sesión.
	 */
	public enum Estado {
		ACTIVO, INACTIVO
	}

	/**
	 * Constructor por defecto que inicializa el usuario con estado {@code ACTIVO}.
	 */
	public Usuario() {
		this.estado = Estado.ACTIVO;
	}

	public Usuario(String nombre, String apellido, String correo, String telefono, String contrasena, Rol rol) {
		this();
		this.nombre = nombre;
		this.apellido = apellido;
		this.correo = correo;
		this.telefono = telefono;
		this.contrasena = contrasena;
		this.rol = rol;
	}

	/**
	 * Retorna el rol del usuario en el formato requerido por Spring Security,
	 * construyéndolo como {@code ROLE_<rol>}.
	 *
	 * @return colección con la autoridad asignada según el rol
	 */
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of(new SimpleGrantedAuthority("ROLE_" + rol.name()));
	}

	@Override
	public String getPassword() {
		return contrasena;
	}

	@Override
	public String getUsername() {
		return correo;
	}

	/**
	 * Indica si la cuenta está habilitada para autenticarse.
	 *
	 * @return {@code true} si el estado es {@code ACTIVO}
	 */
	@Override
	public boolean isEnabled() {
		return estado == Estado.ACTIVO;
	}

	@Override
	public String toString() {
		return "Usuario [idUsuario=" + idUsuario + ", nombre=" + nombre + ", apellido=" + apellido + ", correo="
				+ correo + ", telefono=" + telefono + ", rol=" + rol + ", estado=" + estado + ", fechaRegistro="
				+ fechaRegistro + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(apellido, correo, estado, idUsuario, nombre, rol, telefono);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Usuario other = (Usuario) obj;
		return Objects.equals(apellido, other.apellido) && Objects.equals(correo, other.correo)
				&& estado == other.estado && Objects.equals(idUsuario, other.idUsuario)
				&& Objects.equals(nombre, other.nombre) && rol == other.rol
				&& Objects.equals(telefono, other.telefono);
	}

	public Long getIdUsuario() {
		return idUsuario;
	}

	public void setIdUsuario(Long idUsuario) {
		this.idUsuario = idUsuario;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getApellido() {
		return apellido;
	}

	public void setApellido(String apellido) {
		this.apellido = apellido;
	}

	public String getCorreo() {
		return correo;
	}

	public void setCorreo(String correo) {
		this.correo = correo;
	}

	public String getTelefono() {
		return telefono;
	}

	public void setTelefono(String telefono) {
		this.telefono = telefono;
	}

	public String getContrasena() {
		return contrasena;
	}

	public void setContrasena(String contrasena) {
		this.contrasena = contrasena;
	}

	public Rol getRol() {
		return rol;
	}

	public void setRol(Rol rol) {
		this.rol = rol;
	}

	public Estado getEstado() {
		return estado;
	}

	public void setEstado(Estado estado) {
		this.estado = estado;
	}

	public LocalDateTime getFechaRegistro() {
		return fechaRegistro;
	}

	public void setFechaRegistro(LocalDateTime fechaRegistro) {
		this.fechaRegistro = fechaRegistro;
	}
}
