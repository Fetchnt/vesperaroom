package co.edu.unbosque.vesperaroom.service;

import java.util.List;

/**
 * Interfaz genérica que define las operaciones CRUD básicas para un servicio.
 * Establece el contrato de creación, actualización, eliminación, consulta,
 * verificación de existencia y conteo de entidades.
 */
public interface CRUDOperation<T> {

	/**
	 * Crea un nuevo registro a partir de los datos proporcionados.
	 *
	 * @param data objeto con los datos a persistir
	 * @return código de resultado de la operación
	 */
	public int create(T data);

	/**
	 * Actualiza un registro existente identificado por su ID.
	 *
	 * @param id   identificador del registro a actualizar
	 * @param data objeto con los nuevos datos
	 * @return código de resultado de la operación
	 */
	public int updateById(Long id, T data);

	/**
	 * Elimina un registro existente identificado por su ID.
	 *
	 * @param id identificador del registro a eliminar
	 * @return código de resultado de la operación
	 */
	public int deleteById(Long id);

	/**
	 * Retorna la lista de todos los registros disponibles.
	 *
	 * @return lista con todos los registros
	 */
	public List<T> getAll();

	/**
	 * Verifica si existe un registro con el identificador dado.
	 *
	 * @param id identificador a verificar
	 * @return true si el registro existe, false en caso contrario
	 */
	public boolean exist(Long id);

	/**
	 * Retorna el total de registros existentes.
	 *
	 * @return cantidad total de registros
	 */
	public long count();
}
