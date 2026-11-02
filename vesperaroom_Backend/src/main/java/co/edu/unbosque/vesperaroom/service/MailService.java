package co.edu.unbosque.vesperaroom.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Envía los correos de la aplicación por SMTP con Jakarta Mail. El servidor y
 * las credenciales salen de las propiedades {@code spring.mail.*}.
 */
@Service
public class MailService {

	private final JavaMailSender mailSender;
	private final String remitente;

	public MailService(JavaMailSender mailSender, @Value("${spring.mail.username:}") String remitente) {
		this.mailSender = mailSender;
		this.remitente = remitente;
	}

	/**
	 * Envía el código para restablecer la contraseña.
	 *
	 * @param correo destinatario
	 * @param codigo código de 6 dígitos, en claro
	 * @throws org.springframework.mail.MailException si el envío falla
	 */
	public void enviarCodigoRecuperacion(String correo, String codigo) {
		if (remitente.isBlank()) {
			throw new IllegalStateException("correo sin configurar: falta MAIL_USERNAME en .env.properties");
		}
		SimpleMailMessage mensaje = new SimpleMailMessage();
		mensaje.setFrom(remitente);
		mensaje.setTo(correo);
		mensaje.setSubject("VesperaRoom: código para restablecer tu contraseña");
		mensaje.setText("Tu código para restablecer la contraseña es: " + codigo + "\n\n"
				+ "Vence en 30 minutos y solo sirve una vez.\n"
				+ "Si no pediste cambiar tu contraseña, ignora este correo: tu cuenta sigue igual.");
		mailSender.send(mensaje);
	}
}
