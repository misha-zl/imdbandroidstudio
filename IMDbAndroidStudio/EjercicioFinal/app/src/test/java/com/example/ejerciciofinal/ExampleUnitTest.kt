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


/*1. El usuario pulsa Perfil en la barra inferior.

2. El item del menú tiene id perfilFragment.

3. setupWithNavController manda ese id al NavController.

4. El NavController busca perfilFragment en nav_graph.xml.

5. Encuentra:
   com.example.ejerciciofinal.PerfilFragment

6. Carga PerfilFragment dentro de nav_host_fragment_content_main.

7. La pantalla cambia y se ve Mi Perfil. */




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


//Con esto permite añadir el menu en la parte de arriba en los 3 puntos, activity_main.xml
/*

En el XML se tiene que ver asi
<item
android:id="@+id/prestadosFragment"
android:title="Prestados"
app:showAsAction="never" />


se le indica que coje el menu_main

override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.menu_main, menu)
    return true
}

// lo que hace cada opcion

override fun onOptionsItemSelected(item: MenuItem): Boolean {

    val navHostFragment =
        supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment

    val navController = navHostFragment.navController

    return when (item.itemId) {

        R.id.inicioFragment -> {
            navController.navigate(R.id.inicioFragment)
            true
        }

        R.id.anadirLibroFragment -> {
            navController.navigate(R.id.anadirLibroFragment)
            true
        }

        R.id.disponiblesFragment -> {
            navController.navigate(R.id.disponiblesFragment)
            true
        }

        R.id.prestadosFragment -> {
            navController.navigate(R.id.prestadosFragment)
            true
        }

        else -> super.onOptionsItemSelected(item)
    }
}

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



/*
# Apuntes prácticos — Modificaciones rápidas para la demo del proyecto IMDb

Proyecto Android/Kotlin: `com.example.ejerciciofinal`

Estos apuntes están pensados para una demostración práctica de 20–30 minutos. La idea es saber **qué archivo tocar**, **en qué punto**, **qué código cambiar** y **qué hace cada cambio**.

---

## Regla rápida para saber dónde tocar

```text
Dato nuevo                → Pelicula.kt / Usuario.kt + BBDD.kt
Consulta a base de datos  → DAO
Paso intermedio           → Repositorio
Lógica de la app          → AppViewModel
Pantalla                  → Fragment + XML
Lista                     → Adapter + item XML
Menú                      → menu_main.xml / menu_bottom.xml + MainActivity
Navegación                → nav_graph.xml
Sesión                    → SharedPreferences en MainActivity
```

---

# 1. Validación nueva en añadir / editar película

## 1A. Validar año entre 1900 y 2026

### Archivo: `AnadirPeliculaFragment.kt`

Ve a la función:

```kotlin
private fun guardarPelicula()
```

Busca esta parte:

```kotlin
if (anio == null) {
    ```

    Cámbiala por:

    ```kotlin
// Archivo: AnadirPeliculaFragment.kt
// Punto: dentro de guardarPelicula(), después de convertir anioTexto con toIntOrNull()
// Qué hace: evita guardar años inválidos o textos que no sean número.

    if (anio == null || anio < 1900 || anio > 2026) {
        Toast.makeText(
            requireContext(),
            "El año debe estar entre 1900 y 2026",
            Toast.LENGTH_SHORT
        ).show()
        return
    }
    ```

    ---

    ### Archivo: `EditarPeliculaFragment.kt`

    Ve a la función:

    ```kotlin
    private fun guardarCambios()
    ```

    Busca:

    ```kotlin
    if (anio == null) {
        ```

        Cámbialo por:

        ```kotlin
// Archivo: EditarPeliculaFragment.kt
// Punto: dentro de guardarCambios(), después de val anio = anioTexto.toIntOrNull()
// Qué hace: impide editar una película con un año absurdo.

        if (anio == null || anio < 1900 || anio > 2026) {
            Toast.makeText(
                requireContext(),
                "El año debe estar entre 1900 y 2026",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        ```

        ---

        ## 1B. Validar crítica mínima

        ### Archivo: `AnadirPeliculaFragment.kt`

        Dentro de `guardarPelicula()`, después de comprobar que los campos no están vacíos, añade:

        ```kotlin
// Archivo: AnadirPeliculaFragment.kt
// Punto: después de comprobar que los campos no están vacíos
// Qué hace: evita guardar críticas demasiado cortas.

        if (critica.length < 10) {
            Toast.makeText(
                requireContext(),
                "La crítica debe tener al menos 10 caracteres",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        ```

        ### Archivo: `EditarPeliculaFragment.kt`

        Haz lo mismo dentro de:

        ```kotlin
        private fun guardarCambios()
        ```

        Añade:

        ```kotlin
// Archivo: EditarPeliculaFragment.kt
// Punto: después de comprobar que los campos no están vacíos
// Qué hace: evita editar una película dejando una crítica demasiado corta.

        if (critica.length < 10) {
            Toast.makeText(
                requireContext(),
                "La crítica debe tener al menos 10 caracteres",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        ```

        ---

        # 2. Añadir campo `genero`

        Este cambio toca Room. Cuando se cambia una entidad, hay que tocar también la versión de la base de datos.

        ---

        ## Archivo: `Pelicula.kt`

        Añade el campo:

        ```kotlin
        var genero: String = ""
        ```

        La clase quedaría así:

        ```kotlin
// Archivo: Pelicula.kt
// Punto: dentro del data class Pelicula
// Qué hace: añade una nueva columna llamada genero a la tabla peliculas.

        package com.example.ejerciciofinal.modelo

        import androidx.room.Entity
                import androidx.room.PrimaryKey

                @Entity(tableName = "peliculas")
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
        ```

        ---

        ## Archivo: `BBDD.kt`

        Cambia la versión de Room.

        Si ahora tienes:

        ```kotlin
        version = 4
        ```

        Cámbialo a:

        ```kotlin
// Archivo: BBDD.kt
// Punto: anotación @Database
// Qué hace: avisa a Room de que ha cambiado la estructura de la tabla.

        version = 5
        ```

        Quedaría:

        ```kotlin
        @Database(
            entities = [Pelicula::class, Usuario::class],
            version = 5,
            exportSchema = false
        )
        ```

        Como tienes:

        ```kotlin
        .fallbackToDestructiveMigration(true)
        ```

        Room puede borrar y reconstruir la base de datos durante el desarrollo.

        ---

        # 3. Mostrar género en tarjeta y detalle

                ---

        ## 3A. Añadir género al formulario de añadir película

        ### Archivo: `fragment_anadir_pelicula.xml`

        Ve donde están estos campos:

        ```text
        etAddNombre
        etAddDirector
        etAddAnio
        ```

        Debajo de `etAddAnio`, añade:

        ```xml
        <!-- Archivo: fragment_anadir_pelicula.xml -->
        <!-- Punto: debajo del campo etAddAnio -->
        <!-- Qué hace: permite escribir el género al añadir película -->

        <EditText
        android:id="@+id/etAddGenero"
        android:layout_width="match_parent"
        android:layout_height="48dp"
        android:hint="Género"
        android:inputType="text"
        android:layout_marginBottom="12dp" />
        ```

        ---

        ### Archivo: `AnadirPeliculaFragment.kt`

        Dentro de `guardarPelicula()`, donde lees los campos, añade:

        ```kotlin
// Archivo: AnadirPeliculaFragment.kt
// Punto: dentro de guardarPelicula(), junto al resto de campos
// Qué hace: recoge el género escrito en el formulario.

        val genero = binding.etAddGenero.text.toString().trim()
        ```

        La zona quedaría así:

        ```kotlin
        val nombre = binding.etAddNombre.text.toString().trim()
        val director = binding.etAddDirector.text.toString().trim()
        val anioTexto = binding.etAddAnio.text.toString().trim()
        val genero = binding.etAddGenero.text.toString().trim()
        val descripcion = binding.etAddDescripcion.text.toString().trim()
        val critica = binding.etAddCritica.text.toString().trim()
        ```

        En la validación de campos vacíos, añade `genero.isBlank()`:

        ```kotlin
// Archivo: AnadirPeliculaFragment.kt
// Punto: validación de campos vacíos
// Qué hace: obliga a rellenar también el género.

        if (
            nombre.isBlank() ||
            director.isBlank() ||
            anioTexto.isBlank() ||
            genero.isBlank() ||
            descripcion.isBlank() ||
            critica.isBlank()
        ) {
            Toast.makeText(
                requireContext(),
                "Rellena todos los campos",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        ```

        Al crear la película, añade `genero = genero`:

        ```kotlin
// Archivo: AnadirPeliculaFragment.kt
// Punto: creación del objeto Pelicula
// Qué hace: guarda el género dentro del objeto que va a Room.

        val pelicula = Pelicula(
            nombre = nombre,
            director = director,
            anio = anio,
            descripcion = descripcion,
            critica = critica,
            imagen = imagenSeleccionada,
            genero = genero
        )
        ```

        ---

        ## 3B. Mostrar género en detalle

        ### Archivo: `fragment_detalle_pelicula.xml`

        Debajo del `TextView` del año:

        ```xml
        <TextView
        android:id="@+id/tvDetalleAnio"
        ```

        añade:

        ```xml
        <!-- Archivo: fragment_detalle_pelicula.xml -->
        <!-- Punto: debajo del TextView tvDetalleAnio -->
        <!-- Qué hace: muestra el género en la pantalla de detalle -->

        <TextView
        android:id="@+id/tvDetalleGenero"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Género"
        android:textColor="#777777"
        android:textSize="16sp"
        android:layout_marginBottom="20dp" />
        ```

        ---

        ### Archivo: `DetallePeliculaFragment.kt`

        Dentro de `cargarDatosPelicula()`, junto a:

        ```kotlin
        binding.tvDetalleAnio.text = "Año: ${pelicula.anio}"
        ```

        añade:

        ```kotlin
// Archivo: DetallePeliculaFragment.kt
// Punto: dentro de cargarDatosPelicula()
// Qué hace: rellena el TextView del género con el dato de la película seleccionada.

        binding.tvDetalleGenero.text = "Género: ${pelicula.genero}"
        ```

        ---

        ## 3C. Mostrar género en tarjeta del RecyclerView

        ### Archivo: `fragment_item_pelicula.xml`

        Debajo de `tvAnioPelicula`, añade:

        ```xml
        <!-- Archivo: fragment_item_pelicula.xml -->
        <!-- Punto: debajo de tvAnioPelicula -->
        <!-- Qué hace: añade una línea de género en cada tarjeta del RecyclerView -->

        <TextView
        android:id="@+id/tvGeneroPelicula"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="4dp"
        android:text="Género"
        android:textColor="#777777"
        android:textSize="14sp" />
        ```

        ---

        ### Archivo: `AdaptadorPelicula.kt`

        Dentro de `onBindViewHolder`, debajo de:

        ```kotlin
        holder.binding.tvAnioPelicula.text = "Año: ${pelicula.anio}"
        ```

        añade:

        ```kotlin
// Archivo: AdaptadorPelicula.kt
// Punto: dentro de onBindViewHolder()
// Qué hace: pone el género en cada tarjeta de película.

        holder.binding.tvGeneroPelicula.text = "Género: ${pelicula.genero}"
        ```

        ---

        ## 3D. Editar género

        ### Archivo: `fragment_editar_pelicula.xml`

        Debajo de `etEditarAnio`, añade:

        ```xml
        <!-- Archivo: fragment_editar_pelicula.xml -->
        <!-- Punto: debajo de etEditarAnio -->
        <!-- Qué hace: permite modificar el género al editar película -->

        <EditText
        android:id="@+id/etEditarGenero"
        android:layout_width="match_parent"
        android:layout_height="48dp"
        android:hint="Género"
        android:inputType="text"
        android:backgroundTint="#DDDDDD"
        android:paddingStart="12dp"
        android:paddingEnd="12dp"
        android:layout_marginBottom="12dp" />
        ```

        ---

        ### Archivo: `EditarPeliculaFragment.kt`

        En `cargarDatos()` añade:

        ```kotlin
// Archivo: EditarPeliculaFragment.kt
// Punto: dentro de cargarDatos()
// Qué hace: al abrir editar, carga el género actual en el campo.

        binding.etEditarGenero.setText(pelicula.genero)
        ```

        En `guardarCambios()` añade:

        ```kotlin
// Archivo: EditarPeliculaFragment.kt
// Punto: dentro de guardarCambios(), junto al resto de campos
// Qué hace: lee el género escrito al editar.

        val genero = binding.etEditarGenero.text.toString().trim()
        ```

        En la validación de campos vacíos, añade:

        ```kotlin
        genero.isBlank() ||
        ```

        Al crear `peliculaEditada`, añade `genero = genero`:

        ```kotlin
// Archivo: EditarPeliculaFragment.kt
// Punto: creación de peliculaEditada
// Qué hace: guarda el género editado sin perder el id de la película.

        val peliculaEditada = Pelicula(
            id = pelicula.id,
            nombre = nombre,
            director = director,
            anio = anio,
            descripcion = descripcion,
            critica = critica,
            imagen = imagenSeleccionada,
            genero = genero
        )
        ```

        ---

        # 4. Ocultar añadir película si no es admin

        ---

        ## 4A. Bloquear pantalla

        ### Archivo: `AnadirPeliculaFragment.kt`

        En `onViewCreated()`, al principio, añade o descomenta:

        ```kotlin
// Archivo: AnadirPeliculaFragment.kt
// Punto: al principio de onViewCreated()
// Qué hace: si el usuario no es ADMIN, no le deja entrar a añadir película.

        if (!(activity as MainActivity).miViewModel.esAdmin()) {
            Toast.makeText(
                requireContext(),
                "Solo el administrador puede añadir películas",
                Toast.LENGTH_SHORT
            ).show()

            findNavController().navigate(R.id.inicioFragment)
            return
        }
        ```

        ---

        ## 4B. Ocultar pestaña del menú inferior

        ### Archivo: `MainActivity.kt`

        Ve a:

        ```kotlin
        private fun actualizarMenuInferior()
        ```

        Debajo de donde ocultas `usuariosFragment`, añade:

        ```kotlin
// Archivo: MainActivity.kt
// Punto: dentro de actualizarMenuInferior()
// Qué hace: oculta la pestaña Añadir si el usuario no es administrador.

        bottomNavigationView.menu.findItem(R.id.anadirPeliculaFragment)?.isVisible =
            miViewModel.esAdmin()
        ```

        ---

        # 5. Restaurar sesión con SharedPreferences

        ### Archivo: `MainActivity.kt`

        Ve a:

        ```kotlin
        private fun cargarSesionDesdeSharedPreferences(navController: NavController)
        ```

        Busca la parte donde se comprueba `usuarioEncontrado`.

        Déjalo así:

        ```kotlin
// Archivo: MainActivity.kt
// Punto: dentro de cargarSesionDesdeSharedPreferences()
// Qué hace: si había sesión guardada, recupera el usuario y entra directamente a Inicio.

        if (usuarioEncontrado == null) {
            borrarSesionDeSharedPreferences()
        } else {
            miViewModel.usuario = usuarioEncontrado
            navController.navigate(R.id.inicioFragment)
        }
        ```

        Esto sirve para que, si cierras y abres la app, entre directamente si el usuario ya estaba logueado.

        ---

        # 6. Añadir opción “Acerca de” al menú superior

        ---

        ## Archivo: `menu_main.xml`

        Ahora tienes `action_desloguearse`. Añade debajo:

        ```xml
        <!-- Archivo: menu_main.xml -->
        <!-- Punto: dentro de <menu> -->
        <!-- Qué hace: añade una opción nueva en los tres puntitos superiores -->

        <item
        android:id="@+id/action_acerca_de"
        android:title="Acerca de"
        app:showAsAction="never" />
        ```

        ---

        ## Archivo: `MainActivity.kt`

        En `onOptionsItemSelected`, añade un nuevo caso:

        ```kotlin
// Archivo: MainActivity.kt
// Punto: dentro de onOptionsItemSelected()
// Qué hace: cuando se pulsa "Acerca de", muestra un mensaje informativo.

        R.id.action_acerca_de -> {
            Toast.makeText(
                this,
                "IMDb Android - Proyecto Kotlin con Room, ViewModel y RecyclerView",
                Toast.LENGTH_LONG
            ).show()
            true
        }
        ```

        El `when` quedaría parecido a esto:

        ```kotlin
        return when (item.itemId) {

            R.id.action_desloguearse -> {
                desloguearse()
                true
            }

            R.id.action_acerca_de -> {
                Toast.makeText(
                    this,
                    "IMDb Android - Proyecto Kotlin con Room, ViewModel y RecyclerView",
                    Toast.LENGTH_LONG
                ).show()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
        ```

        ---

        # 7. Cambiar diseño del item de película

                ---

        ## 7A. Quitar crítica de la tarjeta

        ### Archivo: `AdaptadorPelicula.kt`

        Arriba añade:

        ```kotlin
        import android.view.View
        ```

        En `onBindViewHolder`, busca:

        ```kotlin
        holder.binding.tvCriticaPelicula.text = pelicula.critica
        ```

        Añade debajo:

        ```kotlin
// Archivo: AdaptadorPelicula.kt
// Punto: dentro de onBindViewHolder()
// Qué hace: oculta la crítica en la tarjeta, pero sigue existiendo en detalle.

        holder.binding.tvCriticaPelicula.visibility = View.GONE
        ```

        ---

        ## 7B. Mostrar año junto al nombre

        ### Archivo: `AdaptadorPelicula.kt`

        Cambia:

        ```kotlin
        holder.binding.tvNombrePelicula.text = pelicula.nombre
        ```

        por:

        ```kotlin
// Archivo: AdaptadorPelicula.kt
// Punto: dentro de onBindViewHolder()
// Qué hace: muestra el año al lado del título.

        holder.binding.tvNombrePelicula.text = "${pelicula.nombre} (${pelicula.anio})"
        ```

        ---

        ## 7C. Cambiar color de fondo de la tarjeta

        ### Archivo: `AdaptadorPelicula.kt`

        Dentro de `onBindViewHolder`, añade:

        ```kotlin
// Archivo: AdaptadorPelicula.kt
// Punto: dentro de onBindViewHolder()
// Qué hace: cambia visualmente la tarjeta según el año.

        if (pelicula.anio >= 2000) {
            holder.binding.itemPelicula.setCardBackgroundColor(
                android.graphics.Color.parseColor("#FFF8D6")
            )
        } else {
            holder.binding.itemPelicula.setCardBackgroundColor(
                android.graphics.Color.WHITE
            )
        }
        ```

        ---

        # 8. Añadir mensaje “No hay resultados”

        ---

        ## Archivo: `fragment_inicio.xml`

        Debajo de `tvSeccionPeliculas` y antes del `RecyclerView`, añade:

        ```xml
        <!-- Archivo: fragment_inicio.xml -->
        <!-- Punto: debajo del TextView tvSeccionPeliculas -->
        <!-- Qué hace: muestra un mensaje cuando el buscador no encuentra películas -->

        <TextView
        android:id="@+id/tvSinResultados"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="No hay resultados"
        android:textColor="#000000"
        android:textSize="18sp"
        android:textStyle="bold"
        android:visibility="gone"
        app:layout_constraintTop_toBottomOf="@id/tvSeccionPeliculas"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent" />
        ```

        No lo pongas dentro de `barraSuperiorInicio`, porque se metería en la cabecera amarilla.

        ---

        ## Archivo: `InicioFragment.kt`

        En:

        ```kotlin
        private fun mostrarPeliculas(lista: List<Pelicula>)
        ```

        déjalo así:

        ```kotlin
// Archivo: InicioFragment.kt
// Punto: dentro de mostrarPeliculas()
// Qué hace: si la lista filtrada está vacía, oculta el RecyclerView y muestra mensaje.

        private fun mostrarPeliculas(lista: List<Pelicula>) {

            if (lista.isEmpty()) {
                binding.tvSinResultados.visibility = View.VISIBLE
                binding.rvPeliculas.visibility = View.GONE
            } else {
                binding.tvSinResultados.visibility = View.GONE
                binding.rvPeliculas.visibility = View.VISIBLE
            }

            binding.rvPeliculas.adapter = AdaptadorPelicula(lista) { peliculaPulsada ->

                (activity as MainActivity).miViewModel.seleccionarPelicula(peliculaPulsada)

                findNavController().navigate(R.id.action_inicioFragment_to_detallePeliculaFragment)
            }
        }
        ```

        Asegúrate de tener este import:

        ```kotlin
        import android.view.View
        ```

        ---

        # 9. Buscar usuarios por nombre completo

        ### Archivo: `UsuariosFragment.kt`

        Ve a:

        ```kotlin
        private fun aplicarFiltro(textoBuscado: String)
        ```

        Busca la parte donde filtras por `nombreUsuario` y `telefono`.

        Déjalo así:

        ```kotlin
// Archivo: UsuariosFragment.kt
// Punto: dentro de aplicarFiltro()
// Qué hace: permite buscar por usuario, teléfono o nombre completo.

        usuario.nombreUsuario.contains(
            texto,
            ignoreCase = true
        ) ||
                usuario.telefono.contains(
                    texto,
                    ignoreCase = true
                ) ||
                usuario.nombre.contains(
                    texto,
                    ignoreCase = true
                )
        ```

        ---

        # 10. Ordenar películas por año desde DAO

        ### Archivo: `PeliculaDAO.kt`

        Ahora seguramente tienes:

        ```kotlin
        @Query("SELECT * FROM peliculas ORDER BY nombre ASC")
        fun mostrarPeliculas(): Flow<List<Pelicula>>
        ```

        Cámbialo por:

        ```kotlin
// Archivo: PeliculaDAO.kt
// Punto: consulta mostrarPeliculas()
// Qué hace: muestra primero las películas más nuevas.

        @Query("SELECT * FROM peliculas ORDER BY anio DESC")
        fun mostrarPeliculas(): Flow<List<Pelicula>>
        ```

        Si quieres las más antiguas primero:

        ```kotlin
        @Query("SELECT * FROM peliculas ORDER BY anio ASC")
        fun mostrarPeliculas(): Flow<List<Pelicula>>
        ```

        ---

        # 11. Evitar películas duplicadas

        Este cambio toca DAO, Repositorio, ViewModel y Fragment.

        ---

        ## 11A. DAO

        ### Archivo: `PeliculaDAO.kt`

        Añade esta consulta dentro de `interface PeliculaDAO`:

        ```kotlin
// Archivo: PeliculaDAO.kt
// Punto: dentro de interface PeliculaDAO
// Qué hace: busca si ya existe una película con ese nombre.

        @Query("SELECT * FROM peliculas WHERE LOWER(nombre) = LOWER(:nombre) LIMIT 1")
        suspend fun buscarPeliculaPorNombre(nombre: String): Pelicula?
        ```

        ---

        ## 11B. Repositorio

        ### Archivo: `RepositorioPelicula.kt`

        Añade dentro de `class RepositorioPelicula`:

        ```kotlin
// Archivo: RepositorioPelicula.kt
// Punto: dentro de class RepositorioPelicula
// Qué hace: permite al ViewModel preguntar si ya existe una película.

        @WorkerThread
        suspend fun buscarPeliculaPorNombre(nombre: String): Pelicula? {
            return peliculaDAO.buscarPeliculaPorNombre(nombre)
        }
        ```

        ---

        ## 11C. ViewModel

        ### Archivo: `AppViewModel.kt`

        Añade esta función dentro de `class AppViewModel`:

        ```kotlin
// Archivo: AppViewModel.kt
// Punto: dentro de class AppViewModel
// Qué hace: antes de insertar, comprueba si ya existe una película con el mismo nombre.

        fun insertarPeliculaSinDuplicar(
            pelicula: Pelicula,
            onResultado: (Boolean, String) -> Unit
        ) = viewModelScope.launch {

            val existente = repositorioPelicula.buscarPeliculaPorNombre(pelicula.nombre)

            if (existente != null) {
                onResultado(false, "Ya existe una película con ese nombre")
                return@launch
            }

            repositorioPelicula.insertarPelicula(pelicula)
            onResultado(true, "Película añadida correctamente")
        }
        ```

        ---

        ## 11D. Fragment

        ### Archivo: `AnadirPeliculaFragment.kt`

        Busca:

        ```kotlin
        (activity as MainActivity).miViewModel.insertarPelicula(pelicula)

        Toast.makeText(
            requireContext(),
            "Película añadida correctamente",
            Toast.LENGTH_SHORT
        ).show()

        limpiarFormulario()
        ```

        Cámbialo por:

        ```kotlin
// Archivo: AnadirPeliculaFragment.kt
// Punto: después de crear val pelicula = Pelicula(...)
// Qué hace: llama al ViewModel para insertar solo si no existe otra con el mismo nombre.

        (activity as MainActivity).miViewModel.insertarPeliculaSinDuplicar(pelicula) { correcto, mensaje ->

            Toast.makeText(
                requireContext(),
                mensaje,
                Toast.LENGTH_SHORT
            ).show()

            if (correcto) {
                limpiarFormulario()

                val opciones = NavOptions.Builder()
                    .setPopUpTo(R.id.inicioFragment, false)
                    .setLaunchSingleTop(true)
                    .build()

                findNavController().popBackStack()
                findNavController().navigate(R.id.inicioFragment, null, opciones)
            }
        }
        ```

        Importante: elimina o comenta la navegación antigua para que no se ejecute dos veces.

        ---

        # 12. Añadir favoritos

                Este cambio toca entidad, BBDD, detalle y tarjeta.

        ---

        ## 12A. Añadir campo favorito

        ### Archivo: `Pelicula.kt`

        Añade:

        ```kotlin
// Archivo: Pelicula.kt
// Punto: dentro del data class Pelicula
// Qué hace: permite marcar una película como favorita o no.

        var favorita: Boolean = false
        ```

        Si ya añadiste `genero`, quedaría así:

        ```kotlin
        @Entity(tableName = "peliculas")
        data class Pelicula(
            @PrimaryKey(autoGenerate = true)
            var id: Int = 0,
            var nombre: String,
            var director: String,
            var anio: Int,
            var descripcion: String,
            var critica: String,
            var imagen: String = "",
            var genero: String = "",
            var favorita: Boolean = false
        )
        ```

        ---

        ## 12B. Subir versión de Room

        ### Archivo: `BBDD.kt`

        Si ya estabas en `version = 5` por género, sube a:

        ```kotlin
// Archivo: BBDD.kt
// Punto: @Database
// Qué hace: Room detecta que hay una nueva columna favorita.

        version = 6
        ```

        Si haces género y favorita a la vez desde `version = 4`, puedes subir directamente a `version = 5`.

        ---

        ## 12C. Botón favorito en detalle

        ### Archivo: `fragment_detalle_pelicula.xml`

        Antes del botón editar, añade:

        ```xml
        <!-- Archivo: fragment_detalle_pelicula.xml -->
        <!-- Punto: antes de btnEditarPelicula -->
        <!-- Qué hace: botón para marcar o desmarcar una película favorita -->

        <Button
        android:id="@+id/btnFavorita"
        android:layout_width="match_parent"
        android:layout_height="48dp"
        android:text="Marcar como favorita"
        android:textColor="#000000"
        android:textStyle="bold"
        android:backgroundTint="#F5C518"
        android:layout_marginBottom="12dp" />
        ```

        ---

        ## 12D. Lógica en detalle

        ### Archivo: `DetallePeliculaFragment.kt`

        En `configurarBotones()`, añade:

        ```kotlin
// Archivo: DetallePeliculaFragment.kt
// Punto: dentro de configurarBotones()
// Qué hace: al pulsar el botón, cambia favorita de true a false o de false a true.

        binding.btnFavorita.setOnClickListener {
            cambiarFavorita()
        }
        ```

        Ahora crea esta función:

        ```kotlin
// Archivo: DetallePeliculaFragment.kt
// Punto: debajo de configurarBotones()
// Qué hace: actualiza la película seleccionada y la guarda en Room.

        private fun cambiarFavorita() {
            val pelicula = (activity as MainActivity).miViewModel.peliculaSeleccionada

            if (pelicula == null) {
                Toast.makeText(
                    requireContext(),
                    "No hay película seleccionada",
                    Toast.LENGTH_SHORT
                ).show()
                return
            }

            val peliculaActualizada = pelicula.copy(
                favorita = !pelicula.favorita
            )

            (activity as MainActivity).miViewModel.seleccionarPelicula(peliculaActualizada)
            (activity as MainActivity).miViewModel.actualizarPelicula(peliculaActualizada)

            Toast.makeText(
                requireContext(),
                "Favorito actualizado",
                Toast.LENGTH_SHORT
            ).show()

            actualizarTextoFavorita(peliculaActualizada)
        }
        ```

        Añade también:

        ```kotlin
// Archivo: DetallePeliculaFragment.kt
// Punto: debajo de cambiarFavorita()
// Qué hace: cambia el texto del botón según si es favorita o no.

        private fun actualizarTextoFavorita(pelicula: Pelicula) {
            binding.btnFavorita.text =
                if (pelicula.favorita) {
                    "Quitar de favoritas"
                } else {
                    "Marcar como favorita"
                }
        }
        ```

        Arriba importa:

        ```kotlin
        import com.example.ejerciciofinal.modelo.Pelicula
        ```

        Dentro de `cargarDatosPelicula()`, al final, añade:

        ```kotlin
// Archivo: DetallePeliculaFragment.kt
// Punto: final de cargarDatosPelicula()
// Qué hace: pone el texto correcto del botón al abrir el detalle.

        actualizarTextoFavorita(pelicula)
        ```

        ---

        ## 12E. Mostrar estrella en la tarjeta

        ### Archivo: `AdaptadorPelicula.kt`

        Cambia:

        ```kotlin
        holder.binding.tvNombrePelicula.text = pelicula.nombre
        ```

        por:

        ```kotlin
// Archivo: AdaptadorPelicula.kt
// Punto: dentro de onBindViewHolder()
// Qué hace: si la película es favorita, muestra una estrella en el título.

        holder.binding.tvNombrePelicula.text =
            if (pelicula.favorita) {
                "⭐ ${pelicula.nombre}"
            } else {
                pelicula.nombre
            }
        ```

        ---

        # Resumen final

        ```text
        1. Validación nueva
        → AnadirPeliculaFragment.kt / EditarPeliculaFragment.kt

        2. Añadir género
        → Pelicula.kt + BBDD.kt

        3. Mostrar género
        → XML de añadir, editar, detalle, item + Fragment/Adapter

        4. Ocultar añadir si no es admin
        → AnadirPeliculaFragment.kt + MainActivity.kt

        5. Restaurar sesión
        → MainActivity.kt

        6. Opción Acerca de
        → menu_main.xml + MainActivity.kt

        7. Cambiar diseño item película
        → fragment_item_pelicula.xml + AdaptadorPelicula.kt

        8. No hay resultados
        → fragment_inicio.xml + InicioFragment.kt

        9. Buscar usuarios por nombre completo
        → UsuariosFragment.kt

        10. Ordenar por año
        → PeliculaDAO.kt

        11. Evitar duplicados
        → PeliculaDAO.kt + RepositorioPelicula.kt + AppViewModel.kt + AnadirPeliculaFragment.kt

        12. Favoritos
        → Pelicula.kt + BBDD.kt + fragment_detalle_pelicula.xml + DetallePeliculaFragment.kt + AdaptadorPelicula.kt
        ```

        ---

        # Frase para explicar en la demo

        ```text
        Cuando quiero añadir una funcionalidad, primero miro si es un dato nuevo, una consulta, una lógica o un cambio visual.
        Si es un dato nuevo, voy a la entidad y subo la versión de Room.
        Si es una consulta, voy al DAO.
        Si es lógica, la pongo en el ViewModel.
        Si es pantalla, cambio el XML y el Fragment.
        Si es una lista, cambio el Adapter y el XML del item.
        ```
*/

/*/*||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||*/
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

Te los dejo aquí directamente, sin generar fichero.

APUNTES — COSAS QUE PUEDO AÑADIR A MI APP IMDb

Estos apuntes son para practicar cambios que se puedan hacer en 20-30 minutos y poder explicarlos bien en clase o en una defensa práctica.

La idea es saber:

Qué quiero añadir
Qué archivos tengo que tocar
Qué código puede hacer falta
Por qué se hace así
Cómo lo explico
1. Regla rápida: ¿dónde tengo que tocar?

Antes de tocar código, piensa esto:

Si añado un dato nuevo       → Pelicula.kt / Usuario.kt + BBDD.kt
Si añado una consulta        → DAO
Si paso datos entre capas    → Repositorio
Si añado lógica              → AppViewModel
Si cambio una pantalla       → Fragment + XML
Si cambio una lista          → Adapter + item XML
Si cambio navegación         → nav_graph.xml
Si cambio menú inferior      → menu_bottom.xml + MainActivity.kt
Si cambio permisos           → AppViewModel + Fragment/MainActivity

Frase importante:

El Fragment no debe hablar directamente con Room.
El Fragment llama al ViewModel.
El ViewModel llama al Repositorio.
El Repositorio llama al DAO.
El DAO trabaja con Room.
2. Añadir favoritas a las películas
Qué añado

Permitir que una película se pueda marcar como favorita.

Ejemplo:

Avatar → favorita
Titanic → no favorita
Archivos que tocaría
Pelicula.kt
BBDD.kt
DetallePeliculaFragment.kt
fragment_detalle_pelicula.xml
AdaptadorPelicula.kt
InicioFragment.kt
fragment_inicio.xml
Paso 1: añadir campo en Pelicula.kt
var favorita: Boolean = false

Ejemplo:

@Entity(tableName = "peliculas")
data class Pelicula(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,

    var nombre: String,
    var director: String,
    var anio: Int,
    var descripcion: String,
    var critica: String,
    var imagen: String = "",

    var favorita: Boolean = false
)
Paso 2: subir versión en BBDD.kt

Si antes tienes:

version = 4

lo cambias a:

version = 5

Porque has cambiado la tabla peliculas.

Paso 3: añadir botón en detalle

En fragment_detalle_pelicula.xml:

<Button
    android:id="@+id/btnFavorita"
    android:layout_width="match_parent"
    android:layout_height="48dp"
    android:text="Marcar como favorita"
    android:textColor="#000000"
    android:textStyle="bold"
    android:backgroundTint="#F5C518"
    android:layout_marginBottom="12dp" />
Paso 4: lógica en DetallePeliculaFragment.kt
private fun cambiarFavorita() {
    val pelicula = (activity as MainActivity).miViewModel.peliculaSeleccionada ?: return

    val peliculaActualizada = pelicula.copy(
        favorita = !pelicula.favorita
    )

    (activity as MainActivity).miViewModel.seleccionarPelicula(peliculaActualizada)
    (activity as MainActivity).miViewModel.actualizarPelicula(peliculaActualizada)

    actualizarTextoFavorita(peliculaActualizada)

    Toast.makeText(requireContext(), "Favorita actualizada", Toast.LENGTH_SHORT).show()
}

Y esta función:

private fun actualizarTextoFavorita(pelicula: Pelicula) {
    binding.btnFavorita.text =
        if (pelicula.favorita) {
            "Quitar de favoritas"
        } else {
            "Marcar como favorita"
        }
}

En configurarBotones():

binding.btnFavorita.setOnClickListener {
    cambiarFavorita()
}
Paso 5: mostrar estrella en el RecyclerView

En AdaptadorPelicula.kt:

holder.binding.tvNombrePelicula.text =
    if (pelicula.favorita) {
        "⭐ ${pelicula.nombre}"
    } else {
        pelicula.nombre
    }
Cómo lo explico
He añadido una funcionalidad de favoritas.
Para eso he añadido un campo Boolean en la entidad Pelicula.
Desde el detalle cambio ese valor con copy, actualizo la película en Room mediante el ViewModel y luego muestro una estrella en el RecyclerView si la película es favorita.
3. Añadir género a las películas
Qué añado

Un nuevo campo para guardar el género:

Acción
Drama
Comedia
Ciencia ficción
Terror
Animación
Archivos que tocaría
Pelicula.kt
BBDD.kt
fragment_anadir_pelicula.xml
AnadirPeliculaFragment.kt
fragment_editar_pelicula.xml
EditarPeliculaFragment.kt
fragment_detalle_pelicula.xml
DetallePeliculaFragment.kt
fragment_item_pelicula.xml
AdaptadorPelicula.kt
Paso 1: Pelicula.kt
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
Paso 2: BBDD.kt

Subes la versión:

version = 5
Paso 3: añadir campo en formulario de añadir

En fragment_anadir_pelicula.xml:

<EditText
    android:id="@+id/etAddGenero"
    android:layout_width="match_parent"
    android:layout_height="48dp"
    android:hint="Género"
    android:inputType="text"
    android:layout_marginBottom="12dp" />
Paso 4: leer género en AnadirPeliculaFragment.kt
val genero = binding.etAddGenero.text.toString().trim()

En la validación:

if (
    nombre.isBlank() ||
    director.isBlank() ||
    anioTexto.isBlank() ||
    genero.isBlank() ||
    descripcion.isBlank() ||
    critica.isBlank()
) {
    Toast.makeText(requireContext(), "Rellena todos los campos", Toast.LENGTH_SHORT).show()
    return
}

Al crear la película:

val pelicula = Pelicula(
    nombre = nombre,
    director = director,
    anio = anio,
    descripcion = descripcion,
    critica = critica,
    imagen = imagenSeleccionada,
    genero = genero
)
Cómo lo explico
He añadido un dato nuevo a la película: el género.
Como Pelicula es una entidad de Room, al añadir un campo nuevo cambia la tabla.
Por eso subo la versión de la base de datos en BBDD.kt.
Después añado el campo en los formularios y lo muestro en detalle y en el RecyclerView.
4. Añadir puntuación a las películas
Qué añado

Una nota para cada película, por ejemplo de 0 a 10.

Avatar → 8
Titanic → 9
Joker → 7
Archivos que tocaría
Pelicula.kt
BBDD.kt
fragment_anadir_pelicula.xml
AnadirPeliculaFragment.kt
fragment_editar_pelicula.xml
EditarPeliculaFragment.kt
fragment_detalle_pelicula.xml
DetallePeliculaFragment.kt
AdaptadorPelicula.kt
En Pelicula.kt
var puntuacion: Int = 0
Validación

En añadir o editar:

val puntuacionTexto = binding.etAddPuntuacion.text.toString().trim()
val puntuacion = puntuacionTexto.toIntOrNull()

if (puntuacion == null || puntuacion < 0 || puntuacion > 10) {
    Toast.makeText(
        requireContext(),
        "La puntuación debe estar entre 0 y 10",
        Toast.LENGTH_SHORT
    ).show()
    return
}
Cómo lo explico
He añadido una puntuación a cada película.
Es un campo Int dentro de la entidad Pelicula.
Valido que esté entre 0 y 10 para evitar datos incorrectos.
Después lo guardo en Room y lo muestro en la tarjeta o en el detalle.
5. Añadir filtro por género
Qué añado

Un botón o spinner para filtrar películas por género.

Ejemplo:

Ver solo acción
Ver solo drama
Ver solo ciencia ficción
Archivos que tocaría
fragment_inicio.xml
InicioFragment.kt
Forma sencilla con botones

En fragment_inicio.xml:

<Button
    android:id="@+id/btnAccion"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:text="Ver acción" />

<Button
    android:id="@+id/btnDrama"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:text="Ver drama" />

En InicioFragment.kt:

binding.btnAccion.setOnClickListener {
    val listaAccion = listaPeliculasCompleta.filter { pelicula ->
        pelicula.genero.equals("Acción", ignoreCase = true)
    }

    mostrarPeliculas(listaAccion)
}
Cómo lo explico
Uso la lista completa de películas que ya tengo cargada desde el ViewModel.
Cuando pulso el botón, filtro esa lista por el campo género y mando el resultado al RecyclerView.
6. Añadir ordenar por año
Qué añado

Un botón para ordenar las películas por año.

Archivos que tocaría
fragment_inicio.xml
InicioFragment.kt
XML
<Button
    android:id="@+id/btnOrdenarAnio"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:text="Ordenar por año" />
Kotlin
binding.btnOrdenarAnio.setOnClickListener {
    val ordenadas = listaPeliculasCompleta.sortedByDescending { pelicula ->
        pelicula.anio
    }

    mostrarPeliculas(ordenadas)
}
Cómo lo explico
Ordeno la lista en memoria usando sortedByDescending.
No necesito consultar de nuevo Room porque ya tengo todas las películas cargadas en listaPeliculasCompleta.
7. Añadir contador de películas
Qué añado

Mostrar cuántas películas hay.

Ejemplo:

Películas: 6
Archivos que tocaría
fragment_inicio.xml
InicioFragment.kt
XML
<TextView
    android:id="@+id/tvContadorPeliculas"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="Películas: 0"
    android:textSize="16sp"
    android:textStyle="bold" />
Kotlin

Dentro de mostrarPeliculas():

binding.tvContadorPeliculas.text = "Películas: ${lista.size}"
Cómo lo explico
Cada vez que muestro una lista en el RecyclerView, actualizo también un TextView con el tamaño de esa lista.
Así el usuario sabe cuántas películas está viendo.
8. Añadir mensaje “No hay resultados”
Qué añado

Cuando el buscador no encuentra nada, aparece un mensaje.

Archivos que tocaría
fragment_inicio.xml
InicioFragment.kt
XML
<TextView
    android:id="@+id/tvSinResultados"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="No hay resultados"
    android:textSize="18sp"
    android:textStyle="bold"
    android:visibility="gone" />
Kotlin
private fun mostrarPeliculas(lista: List<Pelicula>) {

    if (lista.isEmpty()) {
        binding.tvSinResultados.visibility = View.VISIBLE
        binding.rvPeliculas.visibility = View.GONE
    } else {
        binding.tvSinResultados.visibility = View.GONE
        binding.rvPeliculas.visibility = View.VISIBLE
    }

    binding.rvPeliculas.adapter = AdaptadorPelicula(lista) { pelicula ->
        (activity as MainActivity).miViewModel.seleccionarPelicula(pelicula)
        findNavController().navigate(R.id.action_inicioFragment_to_detallePeliculaFragment)
    }
}
Cómo lo explico
Compruebo si la lista está vacía.
Si está vacía, oculto el RecyclerView y muestro un mensaje.
Si tiene datos, muestro el RecyclerView.
9. Añadir evitar películas duplicadas
Qué añado

Evitar guardar dos películas con el mismo nombre.

Archivos que tocaría
PeliculaDAO.kt
RepositorioPelicula.kt
AppViewModel.kt
AnadirPeliculaFragment.kt
DAO
@Query("SELECT * FROM peliculas WHERE LOWER(nombre) = LOWER(:nombre) LIMIT 1")
suspend fun buscarPeliculaPorNombre(nombre: String): Pelicula?
Repositorio
suspend fun buscarPeliculaPorNombre(nombre: String): Pelicula? {
    return peliculaDAO.buscarPeliculaPorNombre(nombre)
}
ViewModel
fun insertarPelicula(
    pelicula: Pelicula,
    onResultado: (Boolean, String) -> Unit
) = viewModelScope.launch(Dispatchers.IO) {

    val existente = repositorioPelicula.buscarPeliculaPorNombre(pelicula.nombre)

    if (existente != null) {
        withContext(Dispatchers.Main) {
            onResultado(false, "Ya existe una película con ese nombre")
        }
        return@launch
    }

    repositorioPelicula.insertarPelicula(pelicula)

    withContext(Dispatchers.Main) {
        onResultado(true, "Película añadida correctamente")
    }
}
Fragment
(activity as MainActivity).miViewModel.insertarPelicula(pelicula) { correcto, mensaje ->

    Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show()

    if (correcto) {
        findNavController().navigate(R.id.action_anadirPeliculaFragment_to_inicioFragment)
    }
}
Cómo lo explico
Antes de insertar una película, el ViewModel consulta en Room si ya existe una película con el mismo nombre.
Si existe, no la inserta y devuelve un mensaje.
Si no existe, la guarda normalmente.
10. Añadir datos iniciales
Qué añado

Que la app cree películas de prueba al arrancar si la base de datos está vacía.

Archivos que tocaría
PeliculaDAO.kt
RepositorioPelicula.kt
AppViewModel.kt
MainActivity.kt
DAO
@Query("SELECT COUNT(*) FROM peliculas")
suspend fun contarPeliculas(): Int

@Insert
suspend fun insertarPeliculas(peliculas: List<Pelicula>)
Repositorio
suspend fun contarPeliculas(): Int {
    return peliculaDAO.contarPeliculas()
}

suspend fun insertarPeliculas(peliculas: List<Pelicula>) {
    peliculaDAO.insertarPeliculas(peliculas)
}
ViewModel
fun insertarDatosIniciales() = viewModelScope.launch(Dispatchers.IO) {

    val cantidad = repositorioPelicula.contarPeliculas()

    if (cantidad > 0) {
        return@launch
    }

    val peliculasIniciales = listOf(
        Pelicula(
            nombre = "Avatar",
            director = "James Cameron",
            anio = 2009,
            descripcion = "Un marine viaja al planeta Pandora.",
            critica = "Película visualmente espectacular.",
            imagen = ""
        ),
        Pelicula(
            nombre = "Titanic",
            director = "James Cameron",
            anio = 1997,
            descripcion = "Historia de amor durante el hundimiento del Titanic.",
            critica = "Drama romántico muy conocido.",
            imagen = ""
        )
    )

    repositorioPelicula.insertarPeliculas(peliculasIniciales)
}
MainActivity

Dentro de onCreate():

miViewModel.insertarDatosIniciales()
Cómo lo explico
Al arrancar la app compruebo cuántas películas hay en Room.
Si hay 0, inserto una lista de películas iniciales.
Si ya hay películas, no hago nada para no duplicarlas.
11. Añadir búsqueda de usuarios por nombre completo
Qué añado

Que el buscador de usuarios busque también por nombre real.

Archivos que tocaría
UsuariosFragment.kt
Código
val listaFiltrada = listaUsuariosCompleta.filter { usuario ->

    usuario.nombre.contains(textoBuscado, ignoreCase = true) ||
    usuario.nombreUsuario.contains(textoBuscado, ignoreCase = true) ||
    usuario.telefono.contains(textoBuscado, ignoreCase = true)
}
Cómo lo explico
Antes el buscador solo miraba el nombre de usuario y el teléfono.
Ahora también comprueba el nombre real del usuario.
Así es más fácil encontrar usuarios en la lista.
12. Añadir confirmación al cerrar sesión
Qué añado

Antes de cerrar sesión, preguntar si está seguro.

Archivos que tocaría
PerfilFragment.kt
Código
binding.btnDesloguearsePerfil.setOnClickListener {

    AlertDialog.Builder(requireContext())
        .setTitle("Cerrar sesión")
        .setMessage("¿Seguro que quieres cerrar sesión?")
        .setPositiveButton("Sí") { _, _ ->
            (activity as MainActivity).desloguearse()
        }
        .setNegativeButton("Cancelar", null)
        .show()
}
Cómo lo explico
He añadido un AlertDialog para confirmar el cierre de sesión.
Así evito que el usuario cierre sesión por error.
13. Añadir protección extra en pantallas de admin
Qué añado

Aunque ocultes la pestaña de usuarios, también proteges el Fragment.

Archivos que tocaría
UsuariosFragment.kt
AnadirUsuarioFragment.kt
EditarUsuarioFragment.kt
Código
if (!(activity as MainActivity).miViewModel.puedeGestionarUsuarios()) {
    Toast.makeText(
        requireContext(),
        "No tienes permiso para gestionar usuarios",
        Toast.LENGTH_SHORT
    ).show()

    findNavController().navigate(R.id.perfilFragment)
    return
}
Cómo lo explico
No basta con ocultar la pestaña.
También protejo la pantalla por si un usuario normal llega de alguna forma.
Si no tiene permiso, lo mando al perfil.
14. Añadir opción “Acerca de”
Qué añado

Una opción en los tres puntitos superiores con información de la app.

Archivos que tocaría
menu_main.xml
MainActivity.kt
menu_main.xml
<item
    android:id="@+id/action_acerca_de"
    android:title="Acerca de"
    app:showAsAction="never" />
MainActivity.kt
R.id.action_acerca_de -> {
    Toast.makeText(
        this,
        "IMDb App creada con Kotlin, Room, ViewModel y RecyclerView",
        Toast.LENGTH_LONG
    ).show()
    true
}
Cómo lo explico
El menú superior se infla en MainActivity con onCreateOptionsMenu.
Luego uso onOptionsItemSelected para saber qué opción ha pulsado el usuario.
Si pulsa Acerca de, muestro un Toast con información de la app.
15. Añadir pestaña de estadísticas
Qué añado

Una pantalla nueva con datos como:

Total de películas
Total de usuarios
Películas favoritas
Películas por género
Archivos que tocaría
EstadisticasFragment.kt
fragment_estadisticas.xml
nav_graph.xml
menu_bottom.xml
AppViewModel.kt
Qué haría

Crear un nuevo Fragment:

EstadisticasFragment

Añadirlo al nav_graph.xml.

Añadirlo al menú inferior:

<item
    android:id="@+id/estadisticasFragment"
    android:icon="@drawable/ic_stats"
    android:title="Stats" />
Cómo lo explico
He añadido una pantalla nueva de estadísticas.
Para que funcione con el menú inferior, el id del item del menu_bottom tiene que coincidir con el id del fragment en nav_graph.
MainActivity conecta el BottomNavigationView con el NavController usando setupWithNavController.

Esta es más larga. Mejor practicarla solo si tienes tiempo.

16. Añadir imagen por defecto más controlada
Qué añado

Si una película no tiene imagen o falla la URI, pongo una imagen por defecto.

Archivos que tocaría
AdaptadorPelicula.kt
DetallePeliculaFragment.kt
EditarPeliculaFragment.kt
PerfilFragment.kt
Código típico
try {
    if (pelicula.imagen.isNotBlank()) {
        holder.binding.ivPosterPelicula.setImageURI(Uri.parse(pelicula.imagen))
    } else {
        holder.binding.ivPosterPelicula.setImageResource(R.drawable.no_foto)
    }
} catch (e: Exception) {
    holder.binding.ivPosterPelicula.setImageResource(R.drawable.no_foto)
}
Cómo lo explico
Las imágenes se guardan como URI en Room, no como imagen completa.
Puede pasar que la URI falle o que no haya imagen.
Por eso uso try/catch y una imagen por defecto para evitar que la app se cierre.
17. Añadir botón “limpiar buscador”
Qué añado

Un botón para borrar el texto del buscador.

Archivos que tocaría
fragment_inicio.xml
InicioFragment.kt
XML
<Button
    android:id="@+id/btnLimpiarBusqueda"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:text="Limpiar búsqueda" />
Kotlin
binding.btnLimpiarBusqueda.setOnClickListener {
    binding.etBuscarInicio.text.clear()
    mostrarPeliculas(listaPeliculasCompleta)
}
Cómo lo explico
El botón limpia el EditText del buscador y vuelve a mostrar la lista completa de películas.
18. Añadir contador de usuarios
Qué añado

Mostrar cuántos usuarios hay en la pantalla de usuarios.

Archivos que tocaría
fragment_usuarios.xml
UsuariosFragment.kt
Kotlin
binding.tvContadorUsuarios.text = "Usuarios: ${lista.size}"
Cómo lo explico
Cada vez que muestro una lista de usuarios, actualizo el contador con el tamaño de la lista.
19. Añadir validación de teléfono
Qué añado

Que el teléfono tenga 9 dígitos.

Archivos que tocaría
RegistroFragment.kt
AnadirUsuarioFragment.kt
EditarUsuarioFragment.kt
Código
if (telefono.length != 9 || !telefono.all { it.isDigit() }) {
    Toast.makeText(
        requireContext(),
        "El teléfono debe tener 9 números",
        Toast.LENGTH_SHORT
    ).show()
    return
}
Cómo lo explico
Antes de guardar el usuario valido que el teléfono tenga 9 caracteres y que todos sean números.
Si no cumple, muestro un Toast y corto la función con return.
20. Añadir validación de contraseña
Qué añado

Que la contraseña tenga mínimo 4 caracteres.

Archivos que tocaría
RegistroFragment.kt
AnadirUsuarioFragment.kt
EditarUsuarioFragment.kt
Código
if (password.length < 4) {
    Toast.makeText(
        requireContext(),
        "La contraseña debe tener al menos 4 caracteres",
        Toast.LENGTH_SHORT
    ).show()
    return
}
Cómo lo explico
Compruebo la longitud de la contraseña antes de guardar.
Si es demasiado corta, aviso al usuario y no creo la cuenta.
21. Añadir ocultar botón añadir película a usuario normal
Qué añado

Que solo el admin vea la pestaña de añadir película.

Archivos que tocaría
AppViewModel.kt
MainActivity.kt
AnadirPeliculaFragment.kt
ViewModel
fun puedeAñadirPelicula(): Boolean {
    return usuario?.rol == "ADMIN"
}
MainActivity
bottomNavigationView.menu.findItem(R.id.anadirPeliculaFragment)?.isVisible =
    miViewModel.puedeAñadirPelicula()
Fragment
if (!(activity as MainActivity).miViewModel.puedeAñadirPelicula()) {
    Toast.makeText(requireContext(), "No tienes permiso", Toast.LENGTH_SHORT).show()
    findNavController().navigate(R.id.perfilFragment)
    return
}
Cómo lo explico
El menú inferior se adapta al rol del usuario.
Si el usuario no es administrador, oculto la pestaña de añadir película.
Además protejo el Fragment por seguridad.
22. Añadir pregunta de confirmación al borrar película
Qué añado

Que al borrar salga una alerta antes.

Archivos que tocaría
DetallePeliculaFragment.kt
Código
AlertDialog.Builder(requireContext())
    .setTitle("Eliminar película")
    .setMessage("¿Seguro que quieres eliminar esta película?")
    .setPositiveButton("Eliminar") { _, _ ->
        (activity as MainActivity).miViewModel.borrarPelicula(pelicula)
        findNavController().navigate(R.id.inicioFragment)
    }
    .setNegativeButton("Cancelar", null)
    .show()
Cómo lo explico
Uso un AlertDialog para pedir confirmación antes de borrar.
Así evito borrados accidentales.
23. Añadir orden recomendado para hacer cualquier práctica

Cuando te pidan añadir algo, sigue este orden:

1. Pienso si afecta a datos, pantalla, lista, menú o navegación.
2. Si afecta a datos, miro Entity y BBDD.
3. Si necesita consulta, miro DAO.
4. Si necesita lógica, miro ViewModel.
5. Si se ve en pantalla, miro XML y Fragment.
6. Si aparece en lista, miro Adapter y item XML.
7. Si cambia de pantalla, miro nav_graph.
8. Si es menú, miro menu_bottom/menu_main y MainActivity.
9. Pruebo.
24. Ideas rápidas para hacer en 5-10 minutos

Estas son las más fáciles:

Añadir Toast informativo.
Añadir mensaje “No hay resultados”.
Añadir contador de películas.
Añadir validación de año.
Añadir validación de contraseña.
Añadir validación de teléfono.
Añadir opción “Acerca de”.
Eliminar FloatingActionButton.
Cambiar color de botones.
Cambiar texto de una pestaña.
25. Ideas buenas para 20-30 minutos

Estas son las mejores para practicar:

Añadir favoritas.
Añadir género.
Añadir puntuación.
Añadir filtro por género.
Añadir ordenar por año.
Añadir datos iniciales.
Evitar duplicados.
Ocultar añadir película a usuario normal.
Proteger pantalla de usuarios.
Añadir confirmación al cerrar sesión.
26. Ideas más peligrosas

Estas pueden salir, pero hay que practicarlas bien:

Añadir una tabla nueva.
Añadir relación entre tablas.
Eliminar campos de Room.
Cambiar muchos IDs XML.
Cambiar mucho el nav_graph.
Cambiar login y sesión.
Añadir una pestaña nueva completa.

¿Por qué son peligrosas?

Porque si tocas Room, navegación o IDs XML y te equivocas, puede dejar de compilar.
27. Frase final para defender cualquier añadido
He añadido esta funcionalidad siguiendo la arquitectura de la app.
El Fragment recoge los datos de la pantalla y llama al ViewModel.
El ViewModel contiene la lógica y llama al repositorio.
El repositorio llama al DAO.
El DAO trabaja con Room.
Si el cambio afecta a una lista, el Fragment observa LiveData y el RecyclerView se actualiza mediante el Adapter.
28. La mejor práctica completa para entrenar

La mejor práctica para entrenar en 30 minutos es:

Añadir favoritas

Porque toca muchas partes importantes:

Pelicula.kt
BBDD.kt
DetallePeliculaFragment.kt
fragment_detalle_pelicula.xml
AdaptadorPelicula.kt
InicioFragment.kt
fragment_inicio.xml

Y puedes defenderla diciendo:

He añadido un campo favorita a la entidad Pelicula.
Desde el detalle puedo cambiar ese valor.
Uso copy para crear una película igual pero cambiando favorita.
Actualizo la película en Room mediante el ViewModel.
En el RecyclerView muestro una estrella si es favorita.
Y en Inicio puedo filtrar la lista para ver solo las favoritas.

Esa práctica demuestra que entiendes:

Room
Entity
ViewModel
Fragment
RecyclerView
Adapter
Filtros
Actualización de datos

 */
