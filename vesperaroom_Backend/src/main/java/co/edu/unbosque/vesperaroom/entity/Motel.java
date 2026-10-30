package co.edu.unbosque.vesperaroom.entity;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Entidad que representa un motel registrado por un propietario y se
 * corresponde con la tabla {@code motel}. Guarda solo la ciudad: el
 * departamento se obtiene a través de ella (3FN).
 */
public class Motel {

	private Long idMotel;
	private Long idPropietario;
	private Long idCiudad;
	private String nombre;
	private String descripcion;
	private String calle;
	private String carrera;
	private BigDecimal latitud;
	private BigDecimal longitud;
	private Estado estado;

	/**
	 * Define los estados del motel. Solo los moteles {@code ACTIVO} aparecen en
	 * el catálogo público y admiten reservas.
	 */
	public enum Estado {
		ACTIVO, INACTIVO
	}

	/**
	 * Constructor por defecto que inicializa el motel con estado {@code ACTIVO}.
	 */
	public Motel() {
		this.estado = Estado.ACTIVO;
	}

	public Motel(Long idPropietario, Long idCiudad, String nombre, String descripcion, String calle,
			String carrera, BigDecimal latitud, BigDecimal longitud) {
		this();
		this.idPropietario = idPropietario;
		this.idCiudad = idCiudad;
		this.nombre = nombre;
		this.descripcion = descripcion;
		this.calle = calle;
		this.carrera = carrera;
		this.latitud = latitud;
		this.longitud = longitud;
	}

	@Override
	public String toString() {
		return "Motel [idMotel=" + idMotel + ", idPropietario=" + idPropietario + ", idCiudad=" + idCiudad
				+ ", nombre=" + nombre + ", descripcion=" + descripcion + ", calle=" + calle + ", carrera="
				+ carrera + ", latitud=" + latitud + ", longitud=" + longitud + ", estado=" + estado + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(calle, carrera, descripcion, estado, idCiudad, idMotel, idPropietario, latitud,
				longitud, nombre);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Motel other = (Motel) obj;
		return Objects.equals(calle, other.calle) && Objects.equals(carrera, other.carrera)
				&& Objects.equals(descripcion, other.descripcion) && estado == other.estado
				&& Objects.equals(idCiudad, other.idCiudad) && Objects.equals(idMotel, other.idMotel)
				&& Objects.equals(idPropietario, other.idPropietario)
				&& Objects.equals(latitud, other.latitud) && Objects.equals(longitud, other.longitud)
				&& Objects.equals(nombre, other.nombre);
	}

	public Long getIdMotel() {
		return idMotel;
	}

	public void setIdMotel(Long idMotel) {
		this.idMotel = idMotel;
	}

	public Long getIdPropietario() {
		return idPropietario;
	}

	public void setIdPropietario(Long idPropietario) {
		this.idPropietario = idPropietario;
	}

	public Long getIdCiudad() {
		return idCiudad;
	}

	public void setIdCiudad(Long idCiudad) {
		this.idCiudad = idCiudad;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public String getCalle() {
		return calle;
	}

	public void setCalle(String calle) {
		this.calle = calle;
	}

	public String getCarrera() {
		return carrera;
	}

	public void setCarrera(String carrera) {
		this.carrera = carrera;
	}

	public BigDecimal getLatitud() {
		return latitud;
	}

	public void setLatitud(BigDecimal latitud) {
		this.latitud = latitud;
	}

	public BigDecimal getLongitud() {
		return longitud;
	}

	public void setLongitud(BigDecimal longitud) {
		this.longitud = longitud;
	}

	public Estado getEstado() {
		return estado;
	}

	public void setEstado(Estado estado) {
		this.estado = estado;
	}
}
