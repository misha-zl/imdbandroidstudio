package com.example.ejerciciofinal

import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
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

import androidx.navigation.fragment.NavHostFragment

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

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        // Cogemos el NavHostFragment de forma segura.
        // El NavHostFragment es el contenedor donde se cargan los fragments.
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment

        // De ese NavHostFragment sacamos el NavController.
        // El NavController es el que mueve la app entre pantallas.
        val navController = navHostFragment.navController

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.inicioFragment,
                R.id.anadirPeliculaFragment
            )
        )

        setupActionBarWithNavController(navController, appBarConfiguration)

        val bottomNavigationView =
            findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        bottomNavigationView.setupWithNavController(navController)

        miViewModel.insertarDatosIniciales()

        cargarSesionDesdeSharedPreferences()

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



    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {

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

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)

        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}