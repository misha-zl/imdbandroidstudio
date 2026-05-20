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

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    val miBBDD by lazy {
        BBDD.getDatabase(this)
    }

    val repositorioPelicula by lazy {
        RepositorioPelicula(miBBDD.peliculaDAO())
    }

    val repositorioUsuario by lazy {
        RepositorioUsuario(miBBDD.usuarioDAO())
    }

    val miViewModel: AppViewModel by viewModels {
        AppViewModelFactory(
            repositorioPelicula,
            repositorioUsuario
        )
    }

    companion object {
        const val PREFS_NAME = "datos_usuario"
        const val KEY_LOGEADO = "logeado"
        const val KEY_USUARIO_ID = "usuario_id"
        const val KEY_NOMBRE_USUARIO = "nombre_usuario"
        const val KEY_ROL = "rol"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Cargamos el layout principal de la Activity.
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configuramos la toolbar superior.
        setSupportActionBar(binding.toolbar)

        // Buscamos el NavHostFragment de forma segura.
        // Este fragment es el contenedor donde se van mostrando las pantallas.
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment

        // Sacamos el NavController desde el NavHostFragment.
        // El NavController sirve para navegar entre fragments.
        val navController = navHostFragment.navController

        // Aquí indicamos cuáles son las pantallas principales.
        // En estas pantallas no aparece la flecha de volver arriba.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.inicioFragment,
                R.id.anadirPeliculaFragment,
                R.id.perfilFragment,
                R.id.usuariosFragment
            )
        )

        // Unimos la toolbar con el sistema de navegación.
        setupActionBarWithNavController(navController, appBarConfiguration)

        // Buscamos el menú inferior.
        val bottomNavigationView =
            findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        // Conectamos el menú inferior con el nav_graph.
        // IMPORTANTE: los ids de menu_bottom.xml deben coincidir con los ids del nav_graph.xml.
        bottomNavigationView.setupWithNavController(navController)

        // Inserta usuarios y películas iniciales si la base de datos está vacía.
        // Por ejemplo: admin, usuario normal y películas iniciales.
        miViewModel.insertarDatosIniciales()

        // Carga la sesión guardada si el usuario ya había iniciado sesión antes.
        cargarSesionDesdeSharedPreferences()

        // Cada vez que cambiamos de pantalla, decidimos si se ve o no el menú inferior.
        navController.addOnDestinationChangedListener { _, destination, _ ->

            bottomNavigationView.visibility =
                if (
                    destination.id == R.id.loginFragment ||
                    destination.id == R.id.registroFragment
                ) {
                    View.GONE
                } else {
                    View.VISIBLE
                }

            actualizarMenuInferior()
            invalidateOptionsMenu()
        }
    }

    fun guardarSesionEnSharedPreferences(usuario: Usuario) {
        val datos = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        val editor = datos.edit()

        editor.putBoolean(KEY_LOGEADO, true)
        editor.putInt(KEY_USUARIO_ID, usuario.id)
        editor.putString(KEY_NOMBRE_USUARIO, usuario.nombreUsuario)
        editor.putString(KEY_ROL, usuario.rol)

        editor.apply()
    }

    private fun cargarSesionDesdeSharedPreferences() {
        val datos = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        val logeado = datos.getBoolean(KEY_LOGEADO, false)

        if (logeado) {
            val usuarioId = datos.getInt(KEY_USUARIO_ID, -1)

            if (usuarioId != -1) {
                miViewModel.buscarUsuarioPorId(usuarioId) { usuarioEncontrado ->

                    if (usuarioEncontrado == null) {
                        borrarSesionDeSharedPreferences()
                    }
                }
            }
        }
    }

    fun borrarSesionDeSharedPreferences() {
        val datos = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        val editor = datos.edit()

        editor.clear()

        editor.apply()
    }

    fun desloguearse() {
        borrarSesionDeSharedPreferences()

        miViewModel.cerrarSesion()

        Toast.makeText(
            this,
            "Sesión cerrada correctamente",
            Toast.LENGTH_SHORT
        ).show()

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment

        val navController = navHostFragment.navController

        navController.navigate(R.id.loginFragment)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {

        // La opción de desloguearse solo aparece si hay usuario iniciado.
        menu.findItem(R.id.action_desloguearse)?.isVisible =
            miViewModel.usuario != null

        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {

            R.id.action_desloguearse -> {
                desloguearse()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {

        /*Busco el contenedor de fragments.
        Saco de ahí el NavController.*/
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment

        val navController = navHostFragment.navController

        /*El navcontroller es el que controla la navegación.*/
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    private fun actualizarMenuInferior() {

        /*Menú inferior, cuando pulse un botón, usa este navController para cambiar de pantalla.*/

        val bottomNavigationView =
            findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(
                R.id.bottomNavigationView
            )

        bottomNavigationView.menu.findItem(R.id.usuariosFragment)?.isVisible =
            miViewModel.puedeGestionarUsuarios()
    }
}