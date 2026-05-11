package com.example.ejerciciofinal.modelo

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "usuarios")
data class Usuario(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var nombre: String,
    var nombreUsuario: String,
    var password: String,
    var telefono: String,
    var rol: String = "NORMAL"
)