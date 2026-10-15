package com.creardev.buscaya.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.creardev.buscaya.data.local.entity.FarmaciaEntity
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz Data Access Object (DAO) para operaciones CRUD en la tabla "farmacias".
 *
 * Las consultas que devuelven [Flow] observan automáticamente los cambios en la base de datos
 * y emiten un nuevo valor cada vez que la tabla se modifica.
 *
 * Todas las operaciones de escritura (insertar, actualizar, eliminar) son [suspend]
 * para garantizar que se ejecuten fuera del hilo principal (Main Thread).
 */
@Dao
interface FarmaciaDao {

    /**
     * Obtiene todas las farmacias ordenadas por nombre.
     * Devuelve un [Flow] para reactividad en tiempo real.
     */
    @Query("SELECT * FROM farmacias ORDER BY nombre ASC")
    fun obtenerTodos(): Flow<List<FarmaciaEntity>>

    /**
     * Inserta una lista de farmacias en la base de datos local.
     * En caso de conflicto de clave primaria, reemplaza el registro existente.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodos(farmacias: List<FarmaciaEntity>)

    /**
     * Actualiza los datos de una farmacia existente.
     */
    @Update
    suspend fun actualizar(farmacia: FarmaciaEntity)

    /**
     * Elimina un registro específico de farmacia.
     */
    @Delete
    suspend fun eliminar(farmacia: FarmaciaEntity)

    /**
     * Busca farmacias por nombre utilizando el operador LIKE con parámetros seguros de SQLite.
     * NUNCA concatenar texto directamente en las sentencias SQL para prevenir inyección SQL.
     */
    @Query("SELECT * FROM farmacias WHERE nombre LIKE '%' || :query || '%' ORDER BY nombre ASC")
    fun buscarPorNombre(query: String): Flow<List<FarmaciaEntity>>

    /**
     * Cuenta el total de registros en la tabla de farmacias.
     * Útil para comprobar si la base de datos local está vacía.
     */
    @Query("SELECT COUNT(*) FROM farmacias")
    suspend fun contar(): Int

    /**
     * Elimina todos los registros de la tabla de farmacias.
     */
    @Query("DELETE FROM farmacias")
    suspend fun borrarTodos()
}
