package com.example.ejerciciofinal.modelo

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Pelicula::class, Usuario::class],
    version = 1,
    exportSchema = false
)
abstract class BBDD : RoomDatabase() {

    abstract fun peliculaDAO(): PeliculaDAO

    abstract fun usuarioDAO(): UsuarioDAO

    companion object {

        @Volatile
        private var INSTANCE: BBDD? = null

        fun getDatabase(context: Context): BBDD {
            return INSTANCE ?: synchronized(this) {

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BBDD::class.java,
                    "imdb_database"
                ).build()

                INSTANCE = instance

                instance
            }
        }
    }
}