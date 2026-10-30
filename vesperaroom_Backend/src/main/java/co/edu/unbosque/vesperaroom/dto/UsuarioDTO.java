package co.edu.unbosque.vesperaroom.dto;

import java.time.LocalDateTime;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

import co.edu.unbosque.vesperaroom.entity.Usuario.Estado;
import co.edu.unbosque.vesperaroom.entity.Usuario.Rol;

/**
 * Objeto de transferencia de datos que representa un usuario en el sistema,
 * incluyendo sus datos personales, credenciales, rol y estado. Los campos
 * {@code idUsuario} y {@code fechaRegistro} son de solo lectura y
 * {@code contrasena} es de solo escritura en la serialización JSON, por lo que
 * nunca se devuelve al cliente.
 */
public class UsuarioDTO {

	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private Long idUsuario;

	private String nombre;

	private String apellido;

	private String correo;

	private String telefono;

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private String contrasena;

	private Rol rol;

	private Estado estado;

	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private LocalDateTime fechaRegistro;

	public UsuarioDTO() {
	}

	public UsuarioDTO(String nombre, String apellido, String correo, String telefono, String contrasena, Rol rol) {
		this.nombre = nombre;
		this.apellido = apellido;
		this.correo = correo;
		this.telefono = telefono;
		this.contrasena = contrasena;
		this.rol = rol;
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

	@Override
	public String toString() {
		return "UsuarioDTO [idUsuario=" + idUsuario + ", nombre=" + nombre + ", apellido=" + apellido + ", correo="
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
		UsuarioDTO other = (UsuarioDTO) obj;
		return Objects.equals(apellido, other.apellido) && Objects.equals(correo, other.correo)
				&& estado == other.estado && Objects.equals(idUsuario, other.idUsuario)
				&& Objects.equals(nombre, other.nombre) && rol == other.rol
				&& Objects.equals(telefono, other.telefono);
	}
}
