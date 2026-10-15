package com.creardev.buscaya.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.creardev.buscaya.data.local.dao.FarmaciaDao
import com.creardev.buscaya.data.local.entity.FarmaciaEntity

/**
 * Base de datos principal de Room de la aplicación.
 *
 * Utiliza el patrón Singleton ([getInstance]) para evitar crear múltiples instancias costosas
 * de la base de datos SQLite en memoria/disco.
 */
@Database(
    entities = [FarmaciaEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun farmaciaDao(): FarmaciaDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Obtiene la instancia única (Singleton) de [AppDatabase].
         */
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "buscaya_database"
                )
                    // NOTA DE DESARROLLO: fallbackToDestructiveMigration recrea la base de datos
                    // si cambia la versión del esquema. Esto es ÚNICAMENTE PARA DESARROLLO,
                    // ya que borra todos los datos guardados si se modifica la versión sin migración.
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
