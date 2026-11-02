package co.edu.unbosque.vesperaroom.dto;

/**
 * Solicitud de un código para restablecer la contraseña.
 */
public class RecuperarDTO {

	private String correo;

	public RecuperarDTO() {
	}

	public RecuperarDTO(String correo) {
		this.correo = correo;
	}

	public String getCorreo() {
		return correo;
	}

	public void setCorreo(String correo) {
		this.correo = correo;
	}
}
