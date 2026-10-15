package com.creardev.buscaya.data.repository

import androidx.room.withTransaction
import com.creardev.buscaya.data.local.AppDatabase
import com.creardev.buscaya.data.local.dao.FarmaciaDao
import com.creardev.buscaya.data.local.entity.FarmaciaEntity
import com.creardev.buscaya.data.local.entity.toEntityList
import com.creardev.buscaya.data.local.entity.toFarmaciaList
import com.creardev.buscaya.data.model.Farmacia
import com.creardev.buscaya.data.remote.ApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Repositorio de datos de Farmacias.
 *
 * Implementa el patrón "Single Source of Truth" (Única Fuente de Verdad):
 * La interfaz de usuario (UI) SOLO observa la base de datos local (Room) a través de un [Flow].
 * La API remota solo se consulta para refrescar los datos locales guardados.
 */
class FarmaciasRepository(
    private val farmaciaDao: FarmaciaDao,
    private val apiService: ApiService,
    private val database: AppDatabase,
) {

    /**
     * Observa las farmacias guardadas en Room.
     * Mapea automáticamente las entidades [FarmaciaEntity] a modelos de UI [Farmacia].
     */
    val farmacias: Flow<List<Farmacia>> = farmaciaDao.obtenerTodos().map { entities ->
        entities.toFarmaciaList()
    }

    /**
     * Busca farmacias en la base de datos local Room por nombre usando una consulta SQL con LIKE.
     * Devuelve un [Flow] que reacciona en tiempo real a los cambios en la base de datos.
     * NUNCA filtra en memoria; la consulta se realiza directo en la base de datos local SQLite.
     */
    fun buscarPorNombre(query: String): Flow<List<Farmacia>> {
        return farmaciaDao.buscarPorNombre(query).map { entities ->
            entities.toFarmaciaList()
        }
    }

    /**
     * Refresca los datos desde la API si la base de datos local está vacía o si [forzar] es true.
     *
     * Si la llamada a la red tiene éxito, utiliza [withTransaction] para borrar los datos
     * anteriores e insertar los nuevos atómicamente.
     *
     * Si falla (por ejemplo, sin conexión a internet), NO borra la base de datos local
     * y devuelve [Result.failure] para que la UI informe del problema mostrando los datos guardados.
     */
    suspend fun refrescar(forzar: Boolean = false): Result<Unit> {
        val estaVacia = farmaciaDao.contar() == 0

        if (estaVacia || forzar) {
            return try {
                // 1. Obtener datos actualizados desde la API remota
                val respuestaApi = apiService.getFarmacias()
                val nuevasEntidades = respuestaApi.farmacias.toEntityList()

                // 2. Guardar en Room dentro de una transacción atómica (borra y reinserta)
                database.withTransaction {
                    farmaciaDao.borrarTodos()
                    farmaciaDao.insertarTodos(nuevasEntidades)
                }

                Result.success(Unit)
            } catch (e: Exception) {
                // Si falla la red o no hay conexión, se preserva el contenido local de Room
                Result.failure(e)
            }
        }

        return Result.success(Unit)
    }
}
