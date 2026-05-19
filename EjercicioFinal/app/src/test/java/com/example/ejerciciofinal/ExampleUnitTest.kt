package com.example.ejerciciofinal

import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }
}
/*
package com.example.ejerciciofinal


================================================================================
APUNTES DEL FUNCIONAMIENTO DE LA APP IMDB
================================================================================

Este fichero es solo para apuntes. No hace nada en la aplicación.
Puedes guardarlo dentro del paquete com.example.ejerciciofinal si quieres tenerlo como referencia.

================================================================================
1. ESTRUCTURA GENERAL DE LA APP
================================================================================

La app sigue esta estructura:

Pantalla / Fragment
↓
ViewModel
↓
Repositorio
↓
DAO
↓
Room / SQLite

Idea principal:

- El Fragment NO toca directamente la base de datos.
- El Fragment habla con el ViewModel.
- El ViewModel habla con el Repositorio.
- El Repositorio habla con el DAO.
- El DAO habla con Room.
- Room guarda los datos.

================================================================================
2. QUÉ ES ROOM
================================================================================

Room es una librería de Android para trabajar con SQLite de forma más fácil.

En vez de escribir toda la base de datos a mano, usamos:

- Entity: define una tabla.
- DAO: define consultas y operaciones.
- Database: crea la base de datos.
- Repository: hace de puente.
- ViewModel: controla los datos para las pantallas.

================================================================================
3. ENTIDADES: LAS TABLAS
================================================================================

Una entidad es una clase que Room convierte en tabla.

En esta app hay dos tablas principales:

- Pelicula
- Usuario

--------------------------------------------------------------------------------
Pelicula.kt
--------------------------------------------------------------------------------

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

Esto crea la tabla peliculas con estos campos:

- id
- nombre
- director
- anio
- descripcion
- critica
- imagen

El id es la clave primaria y se genera solo.

--------------------------------------------------------------------------------
Usuario.kt
--------------------------------------------------------------------------------

@Entity(tableName = "usuarios")
data class Usuario(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var nombre: String,
    var nombreUsuario: String,
    var password: String,
    var telefono: String,
    var rol: String = "NORMAL",
    var imagenPerfil: String = ""
)

Esto crea la tabla usuarios con estos campos:

- id
- nombre
- nombreUsuario
- password
- telefono
- rol
- imagenPerfil

El rol sirve para saber qué puede hacer cada usuario:

ADMIN:
- Puede añadir películas.
- Puede editar películas.
- Puede eliminar películas.
- Puede gestionar usuarios.
- Puede añadir usuarios.
- Puede editar usuarios.
- Puede eliminar usuarios.

NORMAL:
- Puede ver películas.
- Puede buscar películas.
- Puede añadir películas.
- Puede ver su perfil.
- Puede cambiar su foto de perfil.

================================================================================
4. BBDD.kt: CREACIÓN DE LA BASE DE DATOS
================================================================================

BBDD.kt es el archivo donde se crea la base de datos Room.

Ejemplo:

@Database(
    entities = [Pelicula::class, Usuario::class],
    version = 2,
    exportSchema = false
)
abstract class BBDD : RoomDatabase() {

    abstract fun peliculaDAO(): PeliculaDAO
    abstract fun usuarioDAO(): UsuarioDAO
}

Esto significa:

- La base de datos tiene una tabla Pelicula.
- La base de datos tiene una tabla Usuario.
- Para trabajar con películas se usa PeliculaDAO.
- Para trabajar con usuarios se usa UsuarioDAO.

La base de datos se crea con:

Room.databaseBuilder(
    context.applicationContext,
    BBDD::class.java,
    "imdb_database"
)
    .fallbackToDestructiveMigration()
    .build()

Esto crea una base de datos llamada imdb_database.

fallbackToDestructiveMigration() significa:

Si cambio la estructura de la base de datos y no hago una migración formal,
Room borra la base de datos anterior y crea una nueva.

Esto en un proyecto de clase está bien, porque estamos en desarrollo.

================================================================================
5. DAO: CONSULTAS A LA BASE DE DATOS
================================================================================

DAO significa Data Access Object.

Explicado fácil:

El DAO es donde están las órdenes SQL.

Ahí se ponen operaciones como:

- SELECT
- INSERT
- UPDATE
- DELETE

--------------------------------------------------------------------------------
PeliculaDAO.kt
--------------------------------------------------------------------------------

@Query("SELECT * FROM peliculas ORDER BY nombre ASC")
fun mostrarPeliculas(): Flow<List<Pelicula>>

Significa:

Dame todas las películas ordenadas por nombre.

@Insert
suspend fun insertarPelicula(pelicula: Pelicula)

Significa:

Inserta una película en la tabla peliculas.

@Update
suspend fun actualizarPelicula(pelicula: Pelicula)

Significa:

Actualiza una película existente.

@Delete
suspend fun borrarPelicula(pelicula: Pelicula)

Significa:

Borra una película.

--------------------------------------------------------------------------------
UsuarioDAO.kt
--------------------------------------------------------------------------------

@Query("SELECT * FROM usuarios ORDER BY rol ASC, nombreUsuario ASC")
fun mostrarUsuarios(): Flow<List<Usuario>>

Significa:

Dame todos los usuarios ordenados por rol y nombre de usuario.

@Query("SELECT * FROM usuarios WHERE nombreUsuario = :nombreUsuario AND password = :password LIMIT 1")
suspend fun login(nombreUsuario: String, password: String): Usuario?

Significa:

Busca un usuario que tenga ese nombre de usuario y esa contraseña.

Si existe, devuelve el usuario.
Si no existe, devuelve null.

================================================================================
6. REPOSITORIO: EL PUENTE
================================================================================

El repositorio es un puente entre DAO y ViewModel.

Ejemplo:

class RepositorioPelicula(
    private val peliculaDAO: PeliculaDAO
) {

    fun mostrarPeliculas(): Flow<List<Pelicula>> {
        return peliculaDAO.mostrarPeliculas()
    }

    suspend fun insertarPelicula(pelicula: Pelicula) {
        peliculaDAO.insertarPelicula(pelicula)
    }

    suspend fun actualizarPelicula(pelicula: Pelicula) {
        peliculaDAO.actualizarPelicula(pelicula)
    }

    suspend fun borrarPelicula(pelicula: Pelicula) {
        peliculaDAO.borrarPelicula(pelicula)
    }
}

Explicado fácil:

- El ViewModel pide algo.
- El Repositorio se lo pasa al DAO.
- El DAO habla con Room.

El repositorio sirve para tener el código más ordenado.

================================================================================
7. VIEWMODEL: EL CENTRO DE CONTROL
================================================================================

El AppViewModel controla la lógica de la app.

Guarda datos importantes:

var usuario: Usuario? = null
var peliculaSeleccionada: Pelicula? = null
var usuarioSeleccionado: Usuario? = null

Significado:

usuario:
Usuario que ha iniciado sesión.

peliculaSeleccionada:
Película que se ha pulsado para ver detalle o editar.

usuarioSeleccionado:
Usuario que se ha pulsado para editar.

También tiene listas que vienen de Room:

val listaPeliculas: LiveData<List<Pelicula>> =
    repositorioPelicula.mostrarPeliculas().asLiveData()

val listaUsuarios: LiveData<List<Usuario>> =
    repositorioUsuario.mostrarUsuarios().asLiveData()

Esto significa:

- La lista de películas viene de Room.
- La lista de usuarios viene de Room.
- Si Room cambia, la pantalla se actualiza.

Ejemplo de insertar:

fun insertarPelicula(pelicula: Pelicula) = viewModelScope.launch {
    repositorioPelicula.insertarPelicula(pelicula)
}

viewModelScope.launch sirve para ejecutar la operación en segundo plano.
Así la app no se bloquea.

================================================================================
8. INSERTAR DATOS
================================================================================

--------------------------------------------------------------------------------
Insertar película
--------------------------------------------------------------------------------

En AnadirPeliculaFragment se leen los campos:

val nombre = binding.etAddNombre.text.toString().trim()
val director = binding.etAddDirector.text.toString().trim()
val anioTexto = binding.etAddAnio.text.toString().trim()
val descripcion = binding.etAddDescripcion.text.toString().trim()
val critica = binding.etAddCritica.text.toString().trim()

Después se crea una película:

val pelicula = Pelicula(
    nombre = nombre,
    director = director,
    anio = anio,
    descripcion = descripcion,
    critica = critica,
    imagen = imagenSeleccionada
)

Luego se manda al ViewModel:

(activity as MainActivity).miViewModel.insertarPelicula(pelicula)

Camino completo:

AnadirPeliculaFragment
↓
AppViewModel.insertarPelicula()
↓
RepositorioPelicula.insertarPelicula()
↓
PeliculaDAO.insertarPelicula()
↓
Room guarda la película

--------------------------------------------------------------------------------
Insertar usuario
--------------------------------------------------------------------------------

El admin rellena los datos del usuario:

- nombre
- nombreUsuario
- password
- telefono
- rol
- imagen

Luego llama a:

crearUsuarioPorAdmin(...)

El ViewModel comprueba si ya existe ese nombre de usuario.
Si ya existe, no lo crea.
Si no existe, crea el usuario y lo guarda.

================================================================================
9. MOSTRAR DATOS
================================================================================

--------------------------------------------------------------------------------
Mostrar películas
--------------------------------------------------------------------------------

En InicioFragment se observa la lista de películas:

miViewModel.listaPeliculas.observe(viewLifecycleOwner) { peliculas ->
    listaPeliculasCompleta = peliculas
    aplicarFiltro(binding.etBuscarInicio.text.toString())
}

Esto significa:

Cuando Room mande películas, las guardo y las muestro.

Luego se pasan al RecyclerView:

binding.rvPeliculas.adapter = AdaptadorPelicula(lista) { peliculaPulsada ->
    miViewModel.seleccionarPelicula(peliculaPulsada)
    findNavController().navigate(R.id.action_inicioFragment_to_detallePeliculaFragment)
}

Resumen:

Room manda películas.
InicioFragment las recibe.
AdaptadorPelicula las pinta en pantalla.

--------------------------------------------------------------------------------
Mostrar usuarios
--------------------------------------------------------------------------------

UsuariosFragment observa listaUsuarios:

miViewModel.listaUsuarios.observe(viewLifecycleOwner) { usuarios ->
    listaUsuariosCompleta = usuarios
    aplicarFiltro(binding.etBuscarUsuario.text.toString())
}

Luego manda la lista al AdaptadorUsuario.

================================================================================
10. EDITAR DATOS
================================================================================

--------------------------------------------------------------------------------
Editar película
--------------------------------------------------------------------------------

Cuando pulsas una película, se guarda como seleccionada:

miViewModel.seleccionarPelicula(peliculaPulsada)

Luego se abre el detalle.

Si el usuario es ADMIN, puede editar.

En EditarPeliculaFragment se carga la película seleccionada:

val pelicula = miViewModel.peliculaSeleccionada

Al guardar, se crea una película editada:

val peliculaEditada = Pelicula(
    id = pelicula.id,
    nombre = nombre,
    director = director,
    anio = anio,
    descripcion = descripcion,
    critica = critica,
    imagen = imagenSeleccionada
)

Lo más importante es:

id = pelicula.id

Porque Room necesita saber qué película modificar.

Camino:

EditarPeliculaFragment
↓
AppViewModel.actualizarPelicula()
↓
RepositorioPelicula.actualizarPelicula()
↓
PeliculaDAO.actualizarPelicula()
↓
Room modifica la película

--------------------------------------------------------------------------------
Editar usuario
--------------------------------------------------------------------------------

Igual que con película:

val usuarioEditado = Usuario(
    id = usuario.id,
    nombre = nombre,
    nombreUsuario = nombreUsuario,
    password = password,
    telefono = telefono,
    rol = rol,
    imagenPerfil = imagenSeleccionada
)

Importante:

- Mantener el mismo id.
- Mantener imagenPerfil si el usuario tenía imagen.

Si no se pone imagenPerfil, al editar el usuario se puede borrar la foto.

================================================================================
11. BORRAR DATOS
================================================================================

--------------------------------------------------------------------------------
Borrar película
--------------------------------------------------------------------------------

En DetallePeliculaFragment, si eres ADMIN puedes eliminar.

Primero se muestra un aviso:

AlertDialog.Builder(requireContext())
    .setTitle("Eliminar película")
    .setMessage("¿Seguro que quieres eliminar ${pelicula.nombre}?")

Si confirmas:

miViewModel.borrarPelicula(pelicula)

Camino:

DetallePeliculaFragment
↓
AppViewModel.borrarPelicula()
↓
RepositorioPelicula.borrarPelicula()
↓
PeliculaDAO.borrarPelicula()
↓
Room borra la película

--------------------------------------------------------------------------------
Borrar usuario
--------------------------------------------------------------------------------

El admin puede borrar usuarios.

Hay una protección:

No puedes eliminar tu propio usuario.

Esto evita que el admin se borre a sí mismo y la app quede sin administrador activo.

================================================================================
12. RECYCLERVIEW APLICADO A BASE DE DATOS
================================================================================

RecyclerView no guarda datos.

RecyclerView solo muestra listas.

Flujo:

Room tiene datos
↓
ViewModel recibe lista LiveData
↓
Fragment observa esa lista
↓
Fragment crea un Adaptador
↓
RecyclerView muestra los datos

--------------------------------------------------------------------------------
RecyclerView de películas
--------------------------------------------------------------------------------

Tabla peliculas
↓
listaPeliculas
↓
InicioFragment
↓
AdaptadorPelicula
↓
fragment_item_pelicula.xml
↓
RecyclerView en pantalla

AdaptadorPelicula recibe:

private val listaPeliculas: List<Pelicula>

Por cada película, rellena la tarjeta:

holder.binding.tvNombrePelicula.text = pelicula.nombre
holder.binding.tvDirectorPelicula.text = "Director: ${pelicula.director}"
holder.binding.tvAnioPelicula.text = "Año: ${pelicula.anio}"
holder.binding.tvCriticaPelicula.text = pelicula.critica

getItemCount() indica cuántos elementos hay:

override fun getItemCount(): Int {
    return listaPeliculas.size
}

Si hay 3 películas, muestra 3 tarjetas.
Si añades una, Room actualiza la lista y se muestran 4.

--------------------------------------------------------------------------------
RecyclerView de usuarios
--------------------------------------------------------------------------------

Tabla usuarios
↓
listaUsuarios
↓
UsuariosFragment
↓
AdaptadorUsuario
↓
fragment_item_usuario.xml
↓
RecyclerView en pantalla

El adaptador muestra:

- foto
- nombre de usuario
- nombre completo
- rol
- botón editar
- botón eliminar

================================================================================
13. BUSCADOR DE PELÍCULAS
================================================================================

El buscador de películas no busca directamente en Room.

Hace esto:

Recibe todas las películas.
Guarda la lista completa.
Cuando escribes algo, filtra esa lista.
Muestra solo las coincidencias.

Código importante:

listaPeliculasCompleta.filter { pelicula ->
    pelicula.nombre.contains(textoBuscado, ignoreCase = true)
}

Ejemplo:

Buscas "av" → aparece "Avatar".

================================================================================
14. BUSCADOR DE USUARIOS
================================================================================

El buscador de usuarios filtra por:

- nombre de usuario
- teléfono

Código:

usuario.nombreUsuario.contains(texto, ignoreCase = true) ||
usuario.telefono.contains(texto, ignoreCase = true)

Ejemplos:

Buscas "admin" → sale admin.
Buscas "111" → sale teléfono 111111111.

================================================================================
15. LOGIN Y SESIÓN
================================================================================

El login usa Room.

Consulta del DAO:

SELECT * FROM usuarios
WHERE nombreUsuario = :nombreUsuario
AND password = :password
LIMIT 1

En sencillo:

Mira si existe un usuario con ese nombre y contraseña.

Si existe:

usuario = usuarioEncontrado

También se guarda la sesión en SharedPreferences:

- logeado = true
- usuario_id = id del usuario
- nombre_usuario = nombre de usuario
- rol = ADMIN o NORMAL

Room guarda los usuarios.
SharedPreferences solo recuerda quién ha iniciado sesión.

================================================================================
16. PERMISOS ADMIN Y NORMAL
================================================================================

En AppViewModel se comprueban permisos.

Para editar y eliminar películas:

fun puedeEditarEliminarPelicula(): Boolean {
    return usuario?.rol == "ADMIN"
}

Para gestionar usuarios:

fun puedeGestionarUsuarios(): Boolean {
    return usuario?.rol == "ADMIN"
}

ADMIN puede:

- añadir películas
- editar películas
- eliminar películas
- gestionar usuarios
- añadir usuarios
- editar usuarios
- eliminar usuarios

NORMAL puede:

- ver películas
- buscar películas
- añadir películas
- ver su perfil
- cambiar foto de perfil

================================================================================
17. MENÚ INFERIOR
================================================================================

El menú inferior se crea con:

- menu_bottom.xml
- content_main.xml
- MainActivity.kt
- nav_graph.xml

--------------------------------------------------------------------------------
menu_bottom.xml
--------------------------------------------------------------------------------

Define las pestañas:

- Inicio
- Añadir
- Usuarios
- Perfil

Cada item debe tener el mismo ID que el fragment del nav_graph.

Ejemplo:

<item
    android:id="@+id/inicioFragment"
    android:title="Home" />

Debe coincidir con:

<fragment
    android:id="@+id/inicioFragment" />

--------------------------------------------------------------------------------
MainActivity.kt
--------------------------------------------------------------------------------

Conecta el menú inferior con la navegación:

bottomNavigationView.setupWithNavController(navController)

Oculta el menú en login y registro:

if (
    destination.id == R.id.loginFragment ||
    destination.id == R.id.registroFragment
) {
    View.GONE
} else {
    View.VISIBLE
}

Oculta la pestaña de usuarios si no eres admin:

bottomNavigationView.menu.findItem(R.id.usuariosFragment)?.isVisible =
    miViewModel.puedeGestionarUsuarios()

================================================================================
18. IMÁGENES
================================================================================

Las imágenes se guardan como texto.

En películas:

imagen: String = ""

En usuarios:

imagenPerfil: String = ""

Cuando eliges una imagen desde archivos, se guarda una URI:

content://...

No se guarda la imagen completa en Room.
Room guarda solo la ruta.

Ejemplo:

imagen = "content://com.android.providers..."

Luego al mostrarla:

imageView.setImageURI(Uri.parse(imagen))

Si falla, se pone una imagen por defecto:

R.drawable.ic_launcher_background

================================================================================
19. FRASE CORTA PARA EXPLICAR EN CLASE
================================================================================

Mi aplicación usa Room para gestionar una base de datos local. Las tablas son
Pelicula y Usuario. Las consultas están en los DAO, los repositorios hacen de
puente, y el ViewModel controla las operaciones de insertar, editar, borrar y
mostrar datos. Los fragments observan LiveData del ViewModel y muestran los
datos en RecyclerView mediante adaptadores. Además, el login guarda la sesión
con SharedPreferences y los permisos dependen del rol del usuario.

================================================================================
20. VERSIÓN MUY SIMPLE
================================================================================

Room guarda los datos.
El DAO hace las consultas.
El repositorio pasa los datos al ViewModel.
El ViewModel los prepara.
El Fragment los muestra.
El RecyclerView pinta las listas.


@Suppress("unused")
private object ApuntesFuncionamientoIMDB
*/

/*||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||*/
/*||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||*/
/*||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||*/
/*||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||*/
/*||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||*/
/*||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||*/
/*||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||*/
/*||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||*/
/*||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||*/
/*||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||*/
/*||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||*/
/*||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||*/
/*||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||*/
/*||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||*/
/*||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||*/
/*||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||*/
/*||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||*/
/*||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||*/
/*||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||*/
/*||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||*/
/*||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||*/
/*||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||*/
/*||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||*/
/*||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||*/
/*||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||*/
/*||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||*/
/*||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||*/
/*||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||*/

/*
================================================================================
DEFENSA PRÁCTICA DEL PROYECTO IMDB
================================================================================

Este fichero es solo para apuntes. No hace nada en la aplicación.
Está pensado para poder repasar posibles preguntas de defensa del proyecto.

================================================================================
1. EXPLICA CÓMO ESTÁ ORGANIZADA TU APP
================================================================================

Respuesta:

Mi aplicación está organizada por capas. Las pantallas están hechas con Fragments.
La base de datos está hecha con Room. Para acceder a los datos uso DAO,
Repositorio y ViewModel.

La estructura es:

Fragment
↓
ViewModel
↓
Repositorio
↓
DAO
↓
Room

El Fragment no accede directamente a la base de datos. El Fragment habla con el
ViewModel. El ViewModel habla con el Repositorio. El Repositorio habla con el
DAO. El DAO trabaja con Room.

================================================================================
2. AÑADE UN NUEVO CAMPO A PELÍCULA, POR EJEMPLO GÉNERO
================================================================================

Archivos que tocaría:

- Pelicula.kt
- fragment_anadir_pelicula.xml
- AnadirPeliculaFragment.kt
- fragment_editar_pelicula.xml
- EditarPeliculaFragment.kt
- fragment_detalle_pelicula.xml
- DetallePeliculaFragment.kt
- BBDD.kt

Solución:

Primero añadiría el campo en Pelicula.kt:

var genero: String = ""

Ejemplo:

data class Pelicula(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var nombre: String,
    var director: String,
    var anio: Int,
    var descripcion: String,
    var critica: String,
    var imagen: String = "",
    var genero: String = ""
)

Después añadiría un EditText en fragment_anadir_pelicula.xml:

<EditText
    android:id="@+id/etAddGenero"
    android:layout_width="match_parent"
    android:layout_height="48dp"
    android:hint="Género"
    android:inputType="text" />

Luego en AnadirPeliculaFragment.kt recogería el dato:

val genero = binding.etAddGenero.text.toString().trim()

Y al crear la película:

val pelicula = Pelicula(
    nombre = nombre,
    director = director,
    anio = anio,
    descripcion = descripcion,
    critica = critica,
    imagen = imagenSeleccionada,
    genero = genero
)

Como he cambiado una entidad de Room, tendría que subir la versión en BBDD.kt:

version = 3

Explicación:

La entidad define la tabla. Si añado un campo nuevo, Room necesita saber que la
base de datos ha cambiado. Por eso subo la versión.

================================================================================
3. HACER QUE EL BUSCADOR DE PELÍCULAS BUSQUE TAMBIÉN POR DIRECTOR
================================================================================

Archivo:

- InicioFragment.kt

Antes:

pelicula.nombre.contains(textoBuscado, ignoreCase = true)

Después:

pelicula.nombre.contains(textoBuscado, ignoreCase = true) ||
pelicula.director.contains(textoBuscado, ignoreCase = true)

Respuesta:

El buscador trabaja sobre la lista completa de películas. Antes solo miraba el
nombre. Si quiero buscar también por director, añado otra condición con OR.

Ejemplo:

Si busco "James", aparece Avatar porque el director es James Cameron.

================================================================================
4. HACER QUE EL BUSCADOR DE USUARIOS BUSQUE POR NOMBRE COMPLETO
================================================================================

Archivo:

- UsuariosFragment.kt

Antes:

usuario.nombreUsuario.contains(texto, ignoreCase = true) ||
usuario.telefono.contains(texto, ignoreCase = true)

Después:

usuario.nombre.contains(texto, ignoreCase = true) ||
usuario.nombreUsuario.contains(texto, ignoreCase = true) ||
usuario.telefono.contains(texto, ignoreCase = true)

Respuesta:

He ampliado el filtro. Antes buscaba por nombre de usuario y teléfono. Ahora
también busca por el nombre completo.

================================================================================
5. HACER QUE SOLO EL ADMIN PUEDA AÑADIR PELÍCULAS
================================================================================

Archivo:

- AppViewModel.kt

Antes:

fun puedeAñadirPelicula(): Boolean {
    return usuario != null
}

Después:

fun puedeAñadirPelicula(): Boolean {
    return usuario?.rol == "ADMIN"
}

Respuesta:

La función puedeAñadirPelicula controla quién puede añadir películas. Si quiero
que solo pueda el administrador, compruebo que el rol sea ADMIN.

================================================================================
6. OCULTAR LA PESTAÑA DE AÑADIR PELÍCULA AL USUARIO NORMAL
================================================================================

Archivo:

- MainActivity.kt

Código:

bottomNavigationView.menu.findItem(R.id.anadirPeliculaFragment)?.isVisible =
    miViewModel.puedeAñadirPelicula()

Respuesta:

El menú inferior se puede modificar según el usuario. Igual que oculto la pestaña
de usuarios a quien no es admin, puedo ocultar la pestaña de añadir película si
el usuario no tiene permiso.

================================================================================
7. EVITAR BORRAR UNA PELÍCULA SIN CONFIRMAR
================================================================================

Archivo:

- DetallePeliculaFragment.kt

Código:

AlertDialog.Builder(requireContext())
    .setTitle("Eliminar película")
    .setMessage("¿Seguro que quieres eliminar ${pelicula.nombre}?")
    .setPositiveButton("Eliminar") { _, _ ->
        miViewModel.borrarPelicula(pelicula)
    }
    .setNegativeButton("Cancelar", null)
    .show()

Respuesta:

Uso un AlertDialog para pedir confirmación antes de borrar. Así evito que una
película se borre por error.

================================================================================
8. EVITAR QUE EL ADMIN SE BORRE A SÍ MISMO
================================================================================

Archivo:

- AppViewModel.kt

Código:

if (usuario?.id == usuarioABorrar.id) {
    onResultado(false, "No puedes eliminar tu propio usuario")
    return@launch
}

Respuesta:

Compruebo si el usuario que se intenta borrar es el mismo que ha iniciado sesión.
Si es el mismo, no permito borrarlo. Así evito que el administrador se quede sin
su propia cuenta.

================================================================================
9. VALIDAR QUE LA CRÍTICA NO ESTÉ VACÍA
================================================================================

Archivos:

- AnadirPeliculaFragment.kt
- EditarPeliculaFragment.kt

Código:

if (critica.isBlank()) {
    Toast.makeText(requireContext(), "La crítica es obligatoria", Toast.LENGTH_SHORT).show()
    return
}

Respuesta:

Antes de guardar compruebo si el campo crítica está vacío. Si está vacío, muestro
un mensaje y hago return para parar el guardado.

================================================================================
10. VALIDAR QUE EL AÑO ESTÉ ENTRE 1900 Y 2026
================================================================================

Archivos:

- AnadirPeliculaFragment.kt
- EditarPeliculaFragment.kt

Código:

val anio = anioTexto.toIntOrNull()

if (anio == null || anio < 1900 || anio > 2026) {
    Toast.makeText(
        requireContext(),
        "El año debe estar entre 1900 y 2026",
        Toast.LENGTH_SHORT
    ).show()
    return
}

Respuesta:

Uso toIntOrNull para evitar que la app falle si escriben letras. Luego compruebo
que el año esté dentro del rango permitido.

================================================================================
11. HACER QUE AL EDITAR UNA PELÍCULA NO SE PIERDA SU IMAGEN
================================================================================

Archivo:

- EditarPeliculaFragment.kt

Al cargar datos:

imagenSeleccionada = pelicula.imagen

Al guardar:

imagen = imagenSeleccionada

Respuesta:

La imagen se guarda como texto en el campo imagen. Cuando entro a editar, guardo
la imagen actual en imagenSeleccionada. Si el usuario no elige otra imagen, se
mantiene la anterior.

================================================================================
12. HACER QUE AL EDITAR UN USUARIO NO SE PIERDA SU FOTO
================================================================================

Archivo:

- EditarUsuarioFragment.kt

Al cargar datos:

imagenSeleccionada = usuario.imagenPerfil

Al guardar:

imagenPerfil = imagenSeleccionada

Respuesta:

La foto del usuario se guarda en imagenPerfil. Si al editar no vuelvo a guardar
ese campo, la foto se perdería. Por eso mantengo la imagen anterior o la nueva si
se cambia.

================================================================================
13. AÑADIR UN USUARIO ADMIN DESDE LA APP
================================================================================

Respuesta:

Entro con un usuario administrador. Voy a la pestaña de usuarios, pulso añadir
usuario, relleno los datos y marco el checkbox de administrador.

El checkbox decide el rol:

val esAdmin = binding.cbNuevoAdmin.isChecked

En el ViewModel se convierte a texto:

val rol = if (esAdmin) "ADMIN" else "NORMAL"

Si está marcado se guarda como ADMIN. Si no está marcado se guarda como NORMAL.

================================================================================
14. IMPEDIR QUE UN USUARIO NORMAL ENTRE A GESTIÓN DE USUARIOS
================================================================================

Archivo:

- UsuariosFragment.kt

Código:

if (!esAdmin) {
    Toast.makeText(
        requireContext(),
        "No tienes permisos para gestionar usuarios",
        Toast.LENGTH_SHORT
    ).show()

    findNavController().navigate(R.id.perfilFragment)
}

Respuesta:

No basta con ocultar la pestaña. También protejo el fragment. Así, aunque un
usuario normal llegue de alguna forma, el fragment comprueba permisos y lo manda
a perfil.

================================================================================
15. EXPLICAR CÓMO SE MUESTRAN LAS PELÍCULAS
================================================================================

Respuesta:

Las películas están guardadas en Room. El DAO las obtiene con un SELECT. El
Repositorio pasa esos datos al ViewModel. El ViewModel los ofrece como LiveData.
El InicioFragment observa esa lista y se la pasa al AdaptadorPelicula. El
adaptador rellena cada item del RecyclerView.

Flujo:

Room
↓
DAO
↓
Repositorio
↓
ViewModel
↓
InicioFragment
↓
AdaptadorPelicula
↓
RecyclerView

================================================================================
16. CAMBIAR EL DISEÑO DE CADA PELÍCULA DE LA LISTA
================================================================================

Archivo:

- fragment_item_pelicula.xml

Respuesta:

El diseño de cada tarjeta de película está en fragment_item_pelicula.xml. Si
quiero cambiar cómo se ve cada película en el RecyclerView, modifico ese XML.

Puedo cambiar:

- tamaño de la imagen
- colores
- textos
- márgenes
- CardView
- distribución de los elementos

Si añado un nuevo TextView, luego tengo que rellenarlo en AdaptadorPelicula.kt.

================================================================================
17. CAMBIAR LOS DATOS QUE APARECEN EN CADA TARJETA
================================================================================

Archivo:

- AdaptadorPelicula.kt

Código:

holder.binding.tvNombrePelicula.text = pelicula.nombre
holder.binding.tvDirectorPelicula.text = "Director: ${pelicula.director}"

Respuesta:

El adaptador es quien rellena los datos de cada tarjeta. Si quiero mostrar otro
dato, primero lo pongo en el XML del item y después lo relleno en el adaptador.

================================================================================
18. EXPLICAR CÓMO FUNCIONA EL LOGIN
================================================================================

Respuesta:

El usuario escribe nombre de usuario y contraseña. LoginFragment llama al
ViewModel. El ViewModel llama al RepositorioUsuario. El repositorio llama al
UsuarioDAO. El DAO busca en la tabla usuarios si existe un usuario con ese nombre
y esa contraseña.

Si existe, se guarda en:

usuario = usuarioEncontrado

También se guarda la sesión en SharedPreferences para recordar que el usuario ha
iniciado sesión.

================================================================================
19. DIFERENCIA ENTRE ROOM Y SHAREDPREFERENCES
================================================================================

Respuesta:

Room guarda los datos importantes de la app:

- películas
- usuarios
- roles
- teléfonos
- contraseñas
- imágenes como texto

SharedPreferences guarda datos pequeños de sesión:

- si está logueado
- id del usuario
- nombre de usuario
- rol

Room es la base de datos. SharedPreferences sirve para recordar datos pequeños.

================================================================================
20. HACER QUE AL CERRAR SESIÓN VUELVA AL LOGIN
================================================================================

Archivo:

- MainActivity.kt

Código:

fun desloguearse() {
    borrarSesionDeSharedPreferences()
    miViewModel.cerrarSesion()
    navController.navigate(R.id.loginFragment)
}

Respuesta:

Cuando cierro sesión, borro SharedPreferences, limpio el usuario actual del
ViewModel y navego al login.

================================================================================
21. PONER IMAGEN POR DEFECTO SI UNA PELÍCULA NO TIENE IMAGEN
================================================================================

Archivos:

- AdaptadorPelicula.kt
- DetallePeliculaFragment.kt
- EditarPeliculaFragment.kt

Código:

if (pelicula.imagen.isBlank()) {
    imageView.setImageResource(R.drawable.ic_launcher_background)
}

Respuesta:

Compruebo si la imagen está vacía. Si no hay imagen, pongo una imagen por
defecto para que no se vea vacío y para evitar fallos.

================================================================================
22. POR QUÉ USAR TRY/CATCH AL CARGAR IMÁGENES
================================================================================

Respuesta:

Uso try/catch porque las imágenes se cargan desde una URI. Puede pasar que Android
pierda permiso o que la imagen ya no exista. Si no uso try/catch, la app podría
cerrarse. Si falla, pongo una imagen por defecto.

================================================================================
23. HACER QUE AL AÑADIR PELÍCULA VUELVA A INICIO
================================================================================

Archivo:

- AnadirPeliculaFragment.kt

Código simple:

findNavController().navigate(R.id.inicioFragment)

Con opciones:

val opciones = NavOptions.Builder()
    .setLaunchSingleTop(true)
    .build()

findNavController().navigate(R.id.inicioFragment, null, opciones)

Respuesta:

Después de insertar la película en Room, muestro un mensaje, limpio el formulario
y navego a la pantalla de inicio.

================================================================================
24. HACER QUE AL VOLVER A AÑADIR PELÍCULA NO APAREZCAN DATOS ANTERIORES
================================================================================

Archivo:

- AnadirPeliculaFragment.kt

Código:

private fun limpiarFormulario() {
    binding.etAddNombre.text.clear()
    binding.etAddDirector.text.clear()
    binding.etAddAnio.text.clear()
    binding.etAddDescripcion.text.clear()
    binding.etAddCritica.text.clear()
    imagenSeleccionada = ""
    binding.ivPreviewPelicula.setImageResource(R.drawable.ic_launcher_background)
}

Respuesta:

Después de guardar, limpio todos los campos y también borro la imagen
seleccionada. Así, al volver a entrar, el formulario está vacío.

================================================================================
25. AÑADIR UNA NUEVA PANTALLA AL MENÚ INFERIOR
================================================================================

Archivos:

- menu_bottom.xml
- nav_graph.xml
- MainActivity.kt si hay permisos especiales

En menu_bottom.xml:

<item
    android:id="@+id/nuevoFragment"
    android:icon="@drawable/ic_nuevo"
    android:title="Nuevo" />

En nav_graph.xml:

<fragment
    android:id="@+id/nuevoFragment"
    android:name="com.example.ejerciciofinal.NuevoFragment"
    android:label="Nuevo"
    tools:layout="@layout/fragment_nuevo" />

Respuesta:

El id del item del menú debe coincidir con el id del fragment en el nav_graph.
Si no coinciden, el menú inferior no sabe a qué pantalla ir.

================================================================================
26. QUÉ HACER SI ROOM DA ERROR AL CAMBIAR UNA TABLA
================================================================================

Respuesta:

Si cambio una entidad, por ejemplo añado un campo a Pelicula o Usuario, Room
detecta que la estructura de la base de datos cambió. Entonces tengo que subir la
versión en BBDD.kt.

Ejemplo:

version = 3

Como estoy en desarrollo uso:

.fallbackToDestructiveMigration()

Eso borra la base de datos anterior y crea una nueva.

También puedo desinstalar la app del emulador para borrar la base de datos vieja.

================================================================================
27. CÓMO FUNCIONA RECYCLERVIEW CON LA BASE DE DATOS
================================================================================

Respuesta:

RecyclerView no lee directamente la base de datos. Room manda una lista al
ViewModel. El Fragment observa esa lista y se la pasa al adaptador. El adaptador
rellena cada tarjeta.

Flujo:

Room
↓
DAO
↓
Repositorio
↓
ViewModel
↓
Fragment
↓
Adaptador
↓
RecyclerView

Si añado, edito o borro datos en Room, la lista cambia y el RecyclerView se
actualiza.

================================================================================
28. POR QUÉ USAR LIVEDATA
================================================================================

Respuesta:

Uso LiveData porque permite observar datos. Si la base de datos cambia, el
Fragment recibe la nueva lista automáticamente. Por ejemplo, si añado una
película, Room actualiza la lista y el InicioFragment recibe los datos nuevos.

================================================================================
29. DIFERENCIA ENTRE FRAGMENT Y ACTIVITY
================================================================================

Respuesta:

MainActivity es la pantalla principal que contiene la toolbar, el menú inferior y
el contenedor de navegación.

Los Fragments son las pantallas concretas:

- LoginFragment
- RegistroFragment
- InicioFragment
- AnadirPeliculaFragment
- DetallePeliculaFragment
- EditarPeliculaFragment
- PerfilFragment
- UsuariosFragment
- AnadirUsuarioFragment
- EditarUsuarioFragment

La Activity es el marco general. Los Fragments son las pantallas que cambian.

================================================================================
30. EXPLICAR EL PROYECTO EN POCAS LÍNEAS
================================================================================

Respuesta preparada:

Mi proyecto es una aplicación Android tipo IMDb hecha en Kotlin. Usa Room para
guardar películas y usuarios. Tiene login, registro, roles de usuario normal y
administrador, CRUD de películas, gestión de usuarios para admin, buscador,
imágenes y menú inferior. La arquitectura está separada en entidades, DAO,
repositorios, ViewModel, fragments y RecyclerView. El usuario normal puede ver y
añadir películas, y el administrador además puede editar, eliminar y gestionar
usuarios.

================================================================================
31. RESUMEN FINAL PARA DEFENSA PRÁCTICA
================================================================================

Si me piden modificar algo, pienso en este orden:

1. ¿Es un dato nuevo?
   Toco la Entity y subo versión de Room.

2. ¿Es una consulta nueva?
   Toco el DAO y el Repositorio.

3. ¿Es una acción de lógica?
   Toco el ViewModel.

4. ¿Es algo de pantalla?
   Toco el Fragment y su XML.

5. ¿Es algo de lista?
   Toco el RecyclerView Adapter y el item XML.

6. ¿Es navegación?
   Toco nav_graph.xml y quizá menu_bottom.xml.

7. ¿Es permiso de admin/normal?
   Toco AppViewModel y el Fragment correspondiente.

Frase clave:

Primero localizo si el cambio afecta a datos, lógica, pantalla, lista o
navegación. Según eso modifico Entity, DAO, Repositorio, ViewModel, Fragment,
Adapter o nav_graph.
*/