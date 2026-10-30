package co.edu.unbosque.vesperaroom.dto;

/**
 * Objeto de transferencia de datos devuelto tras un inicio de sesión exitoso,
 * con el token JWT y la información básica del usuario autenticado.
 */
public class AuthResponseDTO {

	private String token;
	private Long idUsuario;
	private String nombre;
	private String correo;
	private String rol;

	public AuthResponseDTO() {
	}

	public AuthResponseDTO(String token, Long idUsuario, String nombre, String correo, String rol) {
		this.token = token;
		this.idUsuario = idUsuario;
		this.nombre = nombre;
		this.correo = correo;
		this.rol = rol;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
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

	public String getCorreo() {
		return correo;
	}

	public void setCorreo(String correo) {
		this.correo = correo;
	}

	public String getRol() {
		return rol;
	}

	public void setRol(String rol) {
		this.rol = rol;
	}
}
