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

    val listaPeliculas: LiveData<List<Pelicula>> =
        repositorioPelicula.mostrarPeliculas().asLiveData()

    fun seleccionarPelicula(pelicula: Pelicula) {
        peliculaSeleccionada = pelicula
    }

    fun cerrarSesion() {
        usuario = null
        peliculaSeleccionada = null
    }


    fun puedeAñadirPelicula(): Boolean {
        return usuario != null
    }

    fun puedeEditarEliminarPelicula(): Boolean {
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
            rol = "NORMAL"
        )

        repositorioUsuario.insertarUsuario(nuevoUsuario)

        onResultado(true, "Usuario registrado correctamente")
    }

    fun login(
        nombreUsuario: String,
        password: String,
        onResultado: (Boolean, String, Usuario?) -> Unit
    ) = viewModelScope.launch {

        val usuarioEncontrado =
            repositorioUsuario.login(nombreUsuario, password)

        if (usuarioEncontrado == null) {
            onResultado(false, "Nombre de usuario o contraseña incorrectos", null)
        } else {
            usuario = usuarioEncontrado
            onResultado(true, "Login correcto", usuarioEncontrado)
        }
    }

    fun actualizarUsuario(
        usuarioActualizado: Usuario,
        onResultado: (Boolean, String) -> Unit
    ) = viewModelScope.launch {

        repositorioUsuario.actualizarUsuario(usuarioActualizado)

        usuario = usuarioActualizado

        onResultado(true, "Usuario actualizado correctamente")
    }

    fun buscarUsuarioPorId(
        id: Int,
        onResultado: (Usuario?) -> Unit
    ) = viewModelScope.launch {

        val usuarioEncontrado = repositorioUsuario.buscarUsuarioPorId(id)

        usuario = usuarioEncontrado

        onResultado(usuarioEncontrado)
    }

    fun insertarDatosIniciales() = viewModelScope.launch {

        val cantidadUsuarios = repositorioUsuario.contarUsuarios()

        if (cantidadUsuarios == 0) {

            val usuariosIniciales = listOf(
                Usuario(
                    nombre = "Administrador",
                    nombreUsuario = "admin",
                    password = "1234",
                    telefono = "000000000",
                    rol = "ADMIN"
                ),
                Usuario(
                    nombre = "Usuario Normal",
                    nombreUsuario = "usuario",
                    password = "1234",
                    telefono = "111111111",
                    rol = "NORMAL"
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
                    descripcion = "Historia de Forrest, un hombre sencillo que vive momentos históricos.",
                    critica = "Película emotiva y muy recomendable.",
                    imagen = ""
                ),
                Pelicula(
                    nombre = "Pulp Fiction",
                    director = "Quentin Tarantino",
                    anio = 1994,
                    descripcion = "Historias criminales conectadas entre sí.",
                    critica = "Muy original y con diálogos memorables.",
                    imagen = ""
                ),
                Pelicula(
                    nombre = "Avatar",
                    director = "James Cameron",
                    anio = 2009,
                    descripcion = "Aventura de ciencia ficción en Pandora.",
                    critica = "Visualmente espectacular.",
                    imagen = ""
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