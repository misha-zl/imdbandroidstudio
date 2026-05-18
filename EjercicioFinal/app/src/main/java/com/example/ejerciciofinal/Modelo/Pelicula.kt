package com.example.ejerciciofinal.modelo

import androidx.room.Entity
import androidx.room.PrimaryKey

/* aqui se representa las tablas de la base de datos */


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