package com.example.ejerciciofinal.modelo

import androidx.room.Delete
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/*Aqui estan las consultas a la base de datos*/
@Dao
interface PeliculaDAO {

    @Query("SELECT * FROM peliculas ORDER BY nombre ASC")
    fun mostrarPeliculas(): Flow<List<Pelicula>>

    @Query("SELECT * FROM peliculas WHERE nombre and anio and director LIKE '%' || :texto || '%' ORDER BY nombre ASC")
    fun buscarPeliculas(texto: String): Flow<List<Pelicula>>



    @Query("SELECT * FROM peliculas WHERE id = :id")
    fun buscarPeliculaPorId(id: Int): Flow<Pelicula>

    @Insert
    suspend fun insertarPelicula(pelicula: Pelicula)

    @Insert
    suspend fun insertarPeliculas(peliculas: List<Pelicula>)

    @Update
    suspend fun actualizarPelicula(pelicula: Pelicula)

    @Delete
    suspend fun borrarPelicula(pelicula: Pelicula)

    @Query("SELECT COUNT(*) FROM peliculas")
    suspend fun contarPeliculas(): Int
}