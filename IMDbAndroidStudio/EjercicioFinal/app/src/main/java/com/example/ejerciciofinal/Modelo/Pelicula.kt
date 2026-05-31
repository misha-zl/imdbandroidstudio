package com.example.ejerciciofinal.modelo

import androidx.room.Entity
import androidx.room.PrimaryKey
/*Se crea las tablas en las entidades, se indican las columnas de la tabla*/

@Entity(tableName = "peliculas")
data class Pelicula(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var nombre: String,
    var director: String,
    var anio: Int,
    var descripcion: String,
    var critica: String,
    var imagen: String = "",
    /* se aáde esta columna para poder buscar por genero o tener favoritos */
    /*ve a fragment_anadir_pelicula.xml,fragment_editar_pelicula.xml,fragmen_item_pelicula.xml */
    /*var genero: String = ""
    var favorita: Boolean = false*/
)