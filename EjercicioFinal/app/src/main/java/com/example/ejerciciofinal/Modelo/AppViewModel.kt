package com.example.ejerciciofinal.modelo

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class AppViewModel(
    private val repositorioPelicula: RepositorioPelicula,
    private val repositorioUsuario: RepositorioUsuario
) : ViewModel() {

    var usuario: Usuario? = null

    var peliculaSeleccionada: Pelicula? = null

    var usuarioSeleccionado: Usuario? = null

    val listaPeliculas: LiveData<List<Pelicula>> =
        repositorioPelicula.mostrarPeliculas().asLiveData()

    val listaUsuarios: LiveData<List<Usuario>> =
        repositorioUsuario.mostrarUsuarios().asLiveData()

    fun seleccionarPelicula(pelicula: Pelicula) {
        peliculaSeleccionada = pelicula
    }

    fun seleccionarUsuario(usuario: Usuario) {
        usuarioSeleccionado = usuario
    }

    fun cerrarSesion() {
        usuario = null
        peliculaSeleccionada = null
        usuarioSeleccionado = null
    }

    fun puedeAñadirPelicula(): Boolean {
        return usuario != null
    }

    fun puedeEditarEliminarPelicula(): Boolean {
        return usuario?.rol == "ADMIN"
    }

    fun puedeGestionarUsuarios(): Boolean {
        return usuario?.rol == "ADMIN"
    }

    fun esAdmin(): Boolean {
        return usuario?.rol == "ADMIN"
    }

    fun insertarPelicula(pelicula: Pelicula) = viewModelScope.launch {
        repositorioPelicula.insertarPelicula(pelicula)
    }

    fun actualizarPelicula(pelicula: Pelicula) = viewModelScope.launch {
        repositorioPelicula.actualizarPelicula(pelicula)
    }

    fun borrarPelicula(pelicula: Pelicula) = viewModelScope.launch {
        repositorioPelicula.borrarPelicula(pelicula)
    }

    fun registrarUsuario(
        nombre: String,
        nombreUsuario: String,
        password: String,
        telefono: String,
        onResultado: (Boolean, String) -> Unit
    ) = viewModelScope.launch {

        val usuarioExistente =
            repositorioUsuario.buscarUsuarioPorNombreUsuario(nombreUsuario)

        if (usuarioExistente != null) {
            onResultado(false, "Ya existe un usuario con ese nombre de usuario")
            return@launch
        }

        val nuevoUsuario = Usuario(
            nombre = nombre,
            nombreUsuario = nombreUsuario,
            password = password,
            telefono = telefono,
            rol = "NORMAL",
            imagenPerfil = ""
        )

        repositorioUsuario.insertarUsuario(nuevoUsuario)

        onResultado(true, "Usuario registrado correctamente")
    }

    fun login(
        nombreUsuario: String,
        password: String,
        onResultado: (Boolean, Usuario?, String) -> Unit
    ) = viewModelScope.launch {

        val usuarioEncontrado =
            repositorioUsuario.login(nombreUsuario, password)

        if (usuarioEncontrado == null) {
            onResultado(false, null, "Usuario o contraseña incorrectos")
        } else {
            usuario = usuarioEncontrado
            onResultado(true, usuarioEncontrado, "Inicio de sesión correcto")
        }
    }

    fun buscarUsuarioPorId(
        id: Int,
        onResultado: (Usuario?) -> Unit
    ) = viewModelScope.launch {

        val usuarioEncontrado =
            repositorioUsuario.buscarUsuarioPorId(id)

        if (usuarioEncontrado != null) {
            usuario = usuarioEncontrado
        }

        onResultado(usuarioEncontrado)
    }

    fun actualizarMiFotoPerfil(imagenPerfil: String) = viewModelScope.launch {
        val usu = usuario ?: return@launch

        val usuarioActualizado = Usuario(
            id = usu.id,
            nombre = usu.nombre,
            nombreUsuario = usu.nombreUsuario,
            password = usu.password,
            telefono = usu.telefono,
            rol = usu.rol,
            imagenPerfil = imagenPerfil
        )

        repositorioUsuario.actualizarUsuario(usuarioActualizado)

        usuario = usuarioActualizado
    }

    fun crearUsuarioPorAdmin(
        nombre: String,
        nombreUsuario: String,
        password: String,
        telefono: String,
        esAdmin: Boolean,
        imagenPerfil: String,
        onResultado: (Boolean, String) -> Unit
    ) = viewModelScope.launch {

        val usuarioExistente =
            repositorioUsuario.buscarUsuarioPorNombreUsuario(nombreUsuario)

        if (usuarioExistente != null) {
            onResultado(false, "Ya existe un usuario con ese nombre de usuario")
            return@launch
        }

        val rol = if (esAdmin) "ADMIN" else "NORMAL"

        val nuevoUsuario = Usuario(
            nombre = nombre,
            nombreUsuario = nombreUsuario,
            password = password,
            telefono = telefono,
            rol = rol,
            imagenPerfil = imagenPerfil
        )

        repositorioUsuario.insertarUsuario(nuevoUsuario)

        onResultado(true, "Usuario creado correctamente")
    }

    fun actualizarUsuarioPorAdmin(
        usuarioActualizado: Usuario,
        onResultado: (Boolean, String) -> Unit
    ) = viewModelScope.launch {

        val usuarioExistente =
            repositorioUsuario.buscarUsuarioPorNombreUsuario(usuarioActualizado.nombreUsuario)

        if (usuarioExistente != null && usuarioExistente.id != usuarioActualizado.id) {
            onResultado(false, "Ya existe otro usuario con ese nombre de usuario")
            return@launch
        }

        repositorioUsuario.actualizarUsuario(usuarioActualizado)

        if (usuario?.id == usuarioActualizado.id) {
            usuario = usuarioActualizado
        }

        onResultado(true, "Usuario actualizado correctamente")
    }

    fun borrarUsuarioPorAdmin(
        usuarioABorrar: Usuario,
        onResultado: (Boolean, String) -> Unit
    ) = viewModelScope.launch {

        if (usuario?.id == usuarioABorrar.id) {
            onResultado(false, "No puedes eliminar tu propio usuario")
            return@launch
        }

        repositorioUsuario.borrarUsuario(usuarioABorrar)

        onResultado(true, "Usuario eliminado correctamente")
    }

    fun insertarDatosIniciales() = viewModelScope.launch {

        val cantidadUsuarios = repositorioUsuario.contarUsuarios()

        if (cantidadUsuarios == 0) {
            val usuariosIniciales = listOf(
                Usuario(
                    nombre = "Administrador",
                    nombreUsuario = "admin",
                    password = "1234",
                    telefono = "111111111",
                    rol = "ADMIN",
                    imagenPerfil = "monoadmin"
                ),
                Usuario(
                    nombre = "Usuario Normal",
                    nombreUsuario = "usuario",
                    password = "1234",
                    telefono = "222222222",
                    rol = "NORMAL",
                    imagenPerfil = "moinkey"
                )
            )

            repositorioUsuario.insertarUsuarios(usuariosIniciales)
        }

        val cantidadPeliculas = repositorioPelicula.contarPeliculas()

        if (cantidadPeliculas == 0) {
            val peliculasIniciales = listOf(
                Pelicula(
                    nombre = "Forrest Gump",
                    director = "Robert Zemeckis",
                    anio = 1994,
                    descripcion = "La historia de Forrest, un hombre sencillo que vive grandes momentos de la historia.",
                    critica = "Película emotiva y muy conocida.",
                    imagen = "forrest"
                ),
                Pelicula(
                    nombre = "Pulp Fiction",
                    director = "Quentin Tarantino",
                    anio = 1994,
                    descripcion = "Varias historias criminales se cruzan de forma original.",
                    critica = "Clásico moderno con mucho estilo.",
                    imagen = "pulpfiction"
                ),
                Pelicula(
                    nombre = "Avatar",
                    director = "James Cameron",
                    anio = 2009,
                    descripcion = "Un marine llega al planeta Pandora y descubre una nueva forma de vida.",
                    critica = "Muy visual y espectacular.",
                    imagen = "avatar"
                )
            )

            repositorioPelicula.insertarPeliculas(peliculasIniciales)
        }
    }
}



class AppViewModelFactory(
    private val repositorioPelicula: RepositorioPelicula,
    private val repositorioUsuario: RepositorioUsuario
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        if (modelClass.isAssignableFrom(AppViewModel::class.java)) {

            @Suppress("UNCHECKED_CAST")
            return AppViewModel(
                repositorioPelicula,
                repositorioUsuario
            ) as T
        }

        throw IllegalArgumentException("ViewModel desconocido")
    }
}