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
    var imagen: String = ""
)