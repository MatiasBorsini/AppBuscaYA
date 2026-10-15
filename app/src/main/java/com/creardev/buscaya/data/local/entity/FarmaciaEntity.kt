package com.creardev.buscaya.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.creardev.buscaya.data.model.Farmacia

/**
 * Entidad de Room que representa una tabla "farmacias" en la base de datos local SQLite.
 *
 * Mantiene la separación entre el modelo de red (DTO) y el almacenamiento local.
 * Incluye la columna [fechaActualizacion] exigida para auditar cuándo se guardó localmente.
 */
@Entity(tableName = "farmacias")
data class FarmaciaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombre: String,
    val direccion: String,
    val imagen: String,
    val fechaRaw: String,
    val fechaIso: String,
    val diaSemana: String,
    // Marca de tiempo en milisegundos para registrar la fecha/hora de actualización local
    val fechaActualizacion: Long = System.currentTimeMillis()
)

// ============================================================================
// Funciones de mapeo (Mappers) para convertir entre DTO, Entidad y UI
// ============================================================================

/**
 * Convierte un DTO de red [Farmacia] a una entidad local de Room [FarmaciaEntity].
 */
fun Farmacia.toEntity(timestamp: Long = System.currentTimeMillis()): FarmaciaEntity {
    return FarmaciaEntity(
        nombre = this.nombre,
        direccion = this.direccion,
        imagen = this.imagen,
        fechaRaw = this.fechaRaw,
        fechaIso = this.fechaIso,
        diaSemana = this.diaSemana,
        fechaActualizacion = timestamp
    )
}

/**
 * Convierte una entidad local [FarmaciaEntity] al modelo de dominio/UI [Farmacia].
 */
fun FarmaciaEntity.toFarmacia(): Farmacia {
    return Farmacia(
        fechaRaw = this.fechaRaw,
        fechaIso = this.fechaIso,
        diaSemana = this.diaSemana,
        nombre = this.nombre,
        direccion = this.direccion,
        imagen = this.imagen
    )
}

/**
 * Convierte una lista de DTOs [Farmacia] a una lista de entidades [FarmaciaEntity].
 */
fun List<Farmacia>.toEntityList(timestamp: Long = System.currentTimeMillis()): List<FarmaciaEntity> {
    return map { it.toEntity(timestamp) }
}

/**
 * Convierte una lista de entidades [FarmaciaEntity] a una lista de modelos de UI [Farmacia].
 */
fun List<FarmaciaEntity>.toFarmaciaList(): List<Farmacia> {
    return map { it.toFarmacia() }
}
