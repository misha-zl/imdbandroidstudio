package com.example.ejerciciofinal.modelo

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase


/*Aqui se crea las bases de datos*/


/*La base de datos tiene 2 tablas usuarios y peliculas*/
@Database(
    entities = [Pelicula::class, Usuario::class],
    version = 4,
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
                /*Se crea la base de datos llamada imdb_database*/
                /*la bd no se ve como un archivo normal visible*/


                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BBDD::class.java,
                    "imdb_database"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()

                INSTANCE = instance

                instance
            }
        }
    }
}