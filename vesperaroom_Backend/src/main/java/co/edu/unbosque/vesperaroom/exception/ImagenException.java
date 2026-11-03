package co.edu.unbosque.vesperaroom.exception;

/**
 * Excepción lanzada cuando el archivo subido como foto no es una imagen JPG, PNG
 * o WEBP, llega vacío o supera el tamaño permitido.
 */
public class ImagenException extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructor que inicializa la excepción con su mensaje por defecto.
	 */
	public ImagenException() {
		super("La foto debe ser una imagen JPG, PNG o WEBP de máximo 5 MB.");
	}
}
