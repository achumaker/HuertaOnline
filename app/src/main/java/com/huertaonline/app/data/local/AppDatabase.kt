package com.huertaonline.app.data.local

import android.content.Context
import androidx.room.*

// Se encarga de gestionar la conexión principal y la creación del archivo
// de datos de la aplicación.
@Database(entities = [CarritoItem::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    // Función necesaria para acceder a las acciones de consulta y guardado
    // que se han definido previamente.
    abstract fun carritoDao(): CarritoDao

    companion object {
        // Variable para almacenar la conexión a la base de datos y evitar
        // tener que generarla varias veces.
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Función para obtener la base de datos. Si ya está disponible, la devuelve;
        // de lo contrario, inicia el proceso para activarla.
        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                // Configuración y nombre del archivo donde se almacenará
                // la información: "huertaonline.db"
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "huertaonline.db"
                ).build().also { INSTANCE = it } // Se guarda la conexión para futuros usos.
            }
    }
}