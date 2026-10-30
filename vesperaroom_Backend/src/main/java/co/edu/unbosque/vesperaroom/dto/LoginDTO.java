package co.edu.unbosque.vesperaroom.dto;

/**
 * Objeto de transferencia de datos con las credenciales enviadas en el inicio
 * de sesión.
 */
public class LoginDTO {

	private String correo;

	private String contrasena;

	public LoginDTO() {
	}

	public LoginDTO(String correo, String contrasena) {
		this.correo = correo;
		this.contrasena = contrasena;
	}

	public String getCorreo() {
		return correo;
	}

	public void setCorreo(String correo) {
		this.correo = correo;
	}

	public String getContrasena() {
		return contrasena;
	}

	public void setContrasena(String contrasena) {
		this.contrasena = contrasena;
	}

	@Override
	public String toString() {
		return "LoginDTO [correo=" + correo + "]";
	}
}
