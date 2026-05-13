package com.example.ejerciciofinal.modelo

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDAO {

    @Query("SELECT * FROM usuarios ORDER BY rol ASC, nombreUsuario ASC")
    fun mostrarUsuarios(): Flow<List<Usuario>>

    @Query("SELECT * FROM usuarios WHERE nombreUsuario = :nombreUsuario AND password = :password LIMIT 1")
    suspend fun login(nombreUsuario: String, password: String): Usuario?

    @Query("SELECT * FROM usuarios WHERE nombreUsuario = :nombreUsuario LIMIT 1")
    suspend fun buscarUsuarioPorNombreUsuario(nombreUsuario: String): Usuario?

    @Query("SELECT * FROM usuarios WHERE id = :id LIMIT 1")
    suspend fun buscarUsuarioPorId(id: Int): Usuario?

    @Insert
    suspend fun insertarUsuario(usuario: Usuario)

    @Insert
    suspend fun insertarUsuarios(usuarios: List<Usuario>)

    @Update
    suspend fun actualizarUsuario(usuario: Usuario)

    @Delete
    suspend fun borrarUsuario(usuario: Usuario)

    @Query("SELECT COUNT(*) FROM usuarios")
    suspend fun contarUsuarios(): Int
}