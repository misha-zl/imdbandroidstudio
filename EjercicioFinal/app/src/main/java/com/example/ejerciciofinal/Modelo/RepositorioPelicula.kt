package com.example.ejerciciofinal.modelo

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow

class RepositorioPelicula(
    private val peliculaDAO: PeliculaDAO
) {

    fun mostrarPeliculas(): Flow<List<Pelicula>> {
        return peliculaDAO.mostrarPeliculas()
    }

    fun buscarPeliculas(texto: String): Flow<List<Pelicula>> {
        return peliculaDAO.buscarPeliculas(texto)
    }

    fun buscarPeliculaPorId(id: Int): Flow<Pelicula> {
        return peliculaDAO.buscarPeliculaPorId(id)
    }

    @WorkerThread
    suspend fun insertarPelicula(pelicula: Pelicula) {
        peliculaDAO.insertarPelicula(pelicula)
    }

    @WorkerThread
    suspend fun insertarPeliculas(peliculas: List<Pelicula>) {
        peliculaDAO.insertarPeliculas(peliculas)
    }

    @WorkerThread
    suspend fun actualizarPelicula(pelicula: Pelicula) {
        peliculaDAO.actualizarPelicula(pelicula)
    }

    @WorkerThread
    suspend fun borrarPelicula(pelicula: Pelicula) {
        peliculaDAO.borrarPelicula(pelicula)
    }

    @WorkerThread
    suspend fun contarPeliculas(): Int {
        return peliculaDAO.contarPeliculas()
    }
}