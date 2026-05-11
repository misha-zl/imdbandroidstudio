package com.example.ejerciciofinal.modelo

import androidx.annotation.WorkerThread

class RepositorioUsuario(
    private val usuarioDAO: UsuarioDAO
) {

    @WorkerThread
    suspend fun login(nombreUsuario: String, password: String): Usuario? {
        return usuarioDAO.login(nombreUsuario, password)
    }

    @WorkerThread
    suspend fun buscarUsuarioPorNombreUsuario(nombreUsuario: String): Usuario? {
        return usuarioDAO.buscarUsuarioPorNombreUsuario(nombreUsuario)
    }

    @WorkerThread
    suspend fun buscarUsuarioPorId(id: Int): Usuario? {
        return usuarioDAO.buscarUsuarioPorId(id)
    }

    @WorkerThread
    suspend fun insertarUsuario(usuario: Usuario) {
        usuarioDAO.insertarUsuario(usuario)
    }

    @WorkerThread
    suspend fun insertarUsuarios(usuarios: List<Usuario>) {
        usuarioDAO.insertarUsuarios(usuarios)
    }

    @WorkerThread
    suspend fun actualizarUsuario(usuario: Usuario) {
        usuarioDAO.actualizarUsuario(usuario)
    }

    @WorkerThread
    suspend fun contarUsuarios(): Int {
        return usuarioDAO.contarUsuarios()
    }
}