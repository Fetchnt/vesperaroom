package co.edu.unbosque.vesperaroom.dto;

/**
 * Cambio de contraseña con el código recibido por correo.
 */
public class RestablecerDTO {

	private String correo;

	private String codigo;

	private String contrasena;

	public RestablecerDTO() {
	}

	public RestablecerDTO(String correo, String codigo, String contrasena) {
		this.correo = correo;
		this.codigo = codigo;
		this.contrasena = contrasena;
	}

	public String getCorreo() {
		return correo;
	}

	public void setCorreo(String correo) {
		this.correo = correo;
	}

	public String getCodigo() {
		return codigo;
	}

	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}

	public String getContrasena() {
		return contrasena;
	}

	public void setContrasena(String contrasena) {
		this.contrasena = contrasena;
	}

	@Override
	public String toString() {
		return "RestablecerDTO [correo=" + correo + "]";
	}
}
