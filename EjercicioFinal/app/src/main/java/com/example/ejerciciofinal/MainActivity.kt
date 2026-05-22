package com.example.ejerciciofinal

import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.ejerciciofinal.databinding.ActivityMainBinding
import com.example.ejerciciofinal.modelo.AppViewModel
import com.example.ejerciciofinal.modelo.AppViewModelFactory
import com.example.ejerciciofinal.modelo.BBDD
import com.example.ejerciciofinal.modelo.RepositorioPelicula
import com.example.ejerciciofinal.modelo.RepositorioUsuario
import com.example.ejerciciofinal.modelo.Usuario
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    /*
    AppBarConfiguration sirve para configurar la barra superior de la app.

    Aquí le diremos cuáles son las pantallas principales.
    En las pantallas principales normalmente no aparece la flecha de volver.
    */
    private lateinit var appBarConfiguration: AppBarConfiguration

    /*
    binding nos permite acceder a los elementos del XML activity_main.xml
    sin usar findViewById todo el rato.
    */
    private lateinit var binding: ActivityMainBinding

    /*
    Creamos la base de datos Room.

    by lazy significa que la base de datos no se crea hasta que se usa por primera vez.
    Esto evita crearla antes de tiempo.
    */
    val miBBDD by lazy {
        BBDD.getDatabase(this)
    }

    /*
    Creamos el repositorio de películas.

    El repositorio es el puente entre el ViewModel y el DAO.
    Aquí le pasamos el DAO de películas que viene de la base de datos.
    */
    val repositorioPelicula by lazy {
        RepositorioPelicula(miBBDD.peliculaDAO())
    }

    /*
    Creamos el repositorio de usuarios.

    Igual que con las películas, este repositorio permite trabajar con usuarios:
    login, registro, editar, borrar, buscar, etc.
    */
    val repositorioUsuario by lazy {
        RepositorioUsuario(miBBDD.usuarioDAO())
    }

    /*
    Creamos el ViewModel principal de la aplicación.

    Como AppViewModel necesita recibir dos repositorios por constructor,
    Android no sabe crearlo solo.

    Por eso usamos AppViewModelFactory, que se encarga de crear el ViewModel
    pasándole repositorioPelicula y repositorioUsuario.
    */
    val miViewModel: AppViewModel by viewModels {
        AppViewModelFactory(
            repositorioPelicula,
            repositorioUsuario
        )
    }

    /*
    companion object sirve para crear constantes que pertenecen a la clase.

    Aquí guardamos los nombres de las claves que usamos en SharedPreferences.

    SharedPreferences se usa para recordar si el usuario ha iniciado sesión.
    */
    companion object {
        const val PREFS_NAME = "datos_usuario"
        const val KEY_LOGEADO = "logeado"
        const val KEY_USUARIO_ID = "usuario_id"
        const val KEY_NOMBRE_USUARIO = "nombre_usuario"
        const val KEY_ROL = "rol"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /*
        Inflamos el layout principal.

        ActivityMainBinding se genera automáticamente gracias a ViewBinding.
        Nos permite acceder a los elementos del XML activity_main.xml.
        */
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /*
        Configuramos la toolbar superior de la aplicación.

        binding.toolbar hace referencia al MaterialToolbar que tenemos en activity_main.xml.
        */
        setSupportActionBar(binding.toolbar)

        /*
        Buscamos el NavHostFragment.

        El NavHostFragment es el contenedor donde se van mostrando los fragments.

        Por ejemplo:
        - LoginFragment
        - InicioFragment
        - PerfilFragment
        - UsuariosFragment

        Todos se cargan dentro de este contenedor.
        */
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment

        /*
        Sacamos el NavController.

        El NavController es el encargado de movernos entre pantallas.
        Es decir, controla la navegación entre fragments.
        */
        val navController = navHostFragment.navController

        /*
        Configuramos cuáles son las pantallas principales.

        Estas pantallas forman parte del menú inferior.

        En estas pantallas no se mostrará la flecha de volver en la toolbar,
        porque se consideran pantallas principales.
        */
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.inicioFragment,
                R.id.anadirPeliculaFragment,
                R.id.perfilFragment,
                R.id.usuariosFragment
            )
        )

        /*
        Conectamos la toolbar superior con el NavController.

        Esto permite que la toolbar sepa en qué pantalla estamos
        y pueda mostrar el título o la flecha de volver cuando corresponda.
        */
        setupActionBarWithNavController(navController, appBarConfiguration)

        /*
        Buscamos el menú inferior.

        El BottomNavigationView es la barra inferior con opciones como:
        - Inicio
        - Añadir
        - Usuarios
        - Perfil
        */
        val bottomNavigationView =
            findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        /*
        Conectamos el menú inferior con el NavController.

        Esta línea es muy importante.

        Gracias a esto, cuando pulsamos una opción del menú inferior,
        Android navega automáticamente al fragment correspondiente.

        IMPORTANTE:
        Los ids de menu_bottom.xml deben coincidir con los ids del nav_graph.xml.

        Ejemplo:
        menu_bottom.xml  -> inicioFragment
        nav_graph.xml    -> inicioFragment
        */
        bottomNavigationView.setupWithNavController(navController)

        /*
        Insertamos datos iniciales.

        Esta función mete usuarios y películas de prueba si la base de datos está vacía.

        Por ejemplo:
        - Usuario admin
        - Usuario normal
        - Películas iniciales
        */
        miViewModel.insertarDatosIniciales()

        /*
        Cargamos la sesión guardada.

        Si el usuario inició sesión antes y no cerró sesión,
        la app intenta recuperar su usuario desde SharedPreferences.
        */
        cargarSesionDesdeSharedPreferences()

        /*
        Este listener se ejecuta cada vez que cambiamos de pantalla.

        Sirve para controlar cosas como:
        - Ocultar el menú inferior en Login y Registro.
        - Mostrar el menú inferior en el resto de pantallas.
        - Actualizar si se ve o no la pestaña de usuarios.
        - Actualizar el menú superior.
        */
        navController.addOnDestinationChangedListener { _, destination, _ ->

            /*
            Si estamos en Login o Registro, ocultamos el menú inferior.

            No tendría sentido mostrar Inicio, Perfil o Usuarios
            si todavía no hemos iniciado sesión.
            */
            bottomNavigationView.visibility =
                if (
                    destination.id == R.id.loginFragment ||
                    destination.id == R.id.registroFragment
                ) {
                    View.GONE
                } else {
                    View.VISIBLE
                }

            /*
            Actualizamos el menú inferior.

            Aquí se decide, por ejemplo, si la pestaña Usuarios se ve o no.
            Solo se verá si el usuario es ADMIN.
            */
            actualizarMenuInferior()

            /*
            Refrescamos el menú superior.

            Esto sirve para que la opción "Desloguearse" aparezca o desaparezca
            según haya usuario iniciado o no.
            */
            invalidateOptionsMenu()
        }
    }

    /*
    Esta función guarda la sesión del usuario en SharedPreferences.

    Se llama cuando el login es correcto.

    Guardamos:
    - Que está logueado.
    - El id del usuario.
    - El nombre de usuario.
    - El rol.
    */
    fun guardarSesionEnSharedPreferences(usuario: Usuario) {
        val datos = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        val editor = datos.edit()

        editor.putBoolean(KEY_LOGEADO, true)
        editor.putInt(KEY_USUARIO_ID, usuario.id)
        editor.putString(KEY_NOMBRE_USUARIO, usuario.nombreUsuario)
        editor.putString(KEY_ROL, usuario.rol)

        editor.apply()
    }

    /*
    Esta función intenta recuperar una sesión guardada.

    Si el usuario ya había iniciado sesión anteriormente,
    buscamos su id en SharedPreferences.

    Después buscamos ese usuario en la base de datos Room.
    */
    private fun cargarSesionDesdeSharedPreferences() {
        val datos = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        val logeado = datos.getBoolean(KEY_LOGEADO, false)

        /*
        Si logeado es true, significa que había una sesión guardada.
        */
        if (logeado) {
            val usuarioId = datos.getInt(KEY_USUARIO_ID, -1)

            /*
            Si el id es distinto de -1, intentamos buscar el usuario en Room.
            */
            if (usuarioId != -1) {
                miViewModel.buscarUsuarioPorId(usuarioId) { usuarioEncontrado ->

                    /*
                    Si no encontramos el usuario, borramos la sesión.

                    Esto puede pasar si el usuario fue eliminado de la base de datos.
                    */
                    if (usuarioEncontrado == null) {
                        borrarSesionDeSharedPreferences()
                    }
                }
            }
        }
    }

    /*
    Esta función borra la sesión guardada en SharedPreferences.

    Se usa cuando cerramos sesión o cuando la sesión guardada ya no es válida.
    */
    fun borrarSesionDeSharedPreferences() {
        val datos = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        val editor = datos.edit()

        editor.clear()

        editor.apply()
    }

    /*
    Esta función cierra sesión.

    Hace tres cosas:
    1. Borra SharedPreferences.
    2. Limpia los datos del ViewModel.
    3. Vuelve a la pantalla de Login.
    */
    fun desloguearse() {
        borrarSesionDeSharedPreferences()

        miViewModel.cerrarSesion()

        Toast.makeText(
            this,
            "Sesión cerrada correctamente",
            Toast.LENGTH_SHORT
        ).show()

        /*
        Volvemos a obtener el NavController para navegar al Login.
        */
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment

        val navController = navHostFragment.navController

        navController.navigate(R.id.loginFragment)
    }

    /*
    Creamos el menú superior.

    Aquí cargamos menu_main.xml.

    En tu app contiene la opción de desloguearse.
    */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    /*
    Esta función se ejecuta antes de mostrar el menú superior.

    Sirve para decidir qué opciones se ven y cuáles no.
    */
    override fun onPrepareOptionsMenu(menu: Menu): Boolean {

        /*
        La opción "Desloguearse" solo se muestra si hay un usuario iniciado.

        Si miViewModel.usuario es null, significa que no hay sesión activa.
        */
        menu.findItem(R.id.action_desloguearse)?.isVisible =
            miViewModel.usuario != null

        return super.onPrepareOptionsMenu(menu)
    }

    /*
    Esta función detecta qué opción del menú superior ha pulsado el usuario.
    */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {

            /*
            Si pulsa "Desloguearse", llamamos a la función desloguearse().
            */
            R.id.action_desloguearse -> {
                desloguearse()
                true
            }

            /*
            Si pulsa otra cosa, dejamos que Android lo gestione normalmente.
            */
            else -> super.onOptionsItemSelected(item)
        }
    }

    /*
    Esta función controla la flecha de volver de la toolbar.

    Cuando estamos en una pantalla secundaria, como editar o detalle,
    permite volver a la pantalla anterior.
    */
    override fun onSupportNavigateUp(): Boolean {

        /*
        Buscamos otra vez el NavHostFragment.
        */
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment

        /*
        Sacamos el NavController.
        */
        val navController = navHostFragment.navController

        /*
        Intentamos navegar hacia arriba.

        Si navController.navigateUp() no puede hacerlo,
        usamos el comportamiento normal de Android.
        */
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    /*
    Esta función actualiza el menú inferior.

    En concreto, controla si se ve o no la pestaña de Usuarios.
    */
    private fun actualizarMenuInferior() {

        /*
        Buscamos el BottomNavigationView.
        */
        val bottomNavigationView =
            findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(
                R.id.bottomNavigationView
            )

        /*
        Buscamos la opción usuariosFragment del menú inferior.

        Solo será visible si puedeGestionarUsuarios() devuelve true.

        En tu ViewModel, puedeGestionarUsuarios() devuelve true solo si el usuario es ADMIN.

        Resultado:
        - ADMIN ve la pestaña Usuarios.
        - NORMAL no ve la pestaña Usuarios.
        */
        bottomNavigationView.menu.findItem(R.id.usuariosFragment)?.isVisible =
            miViewModel.puedeGestionarUsuarios()
    }
}