package com.example.ejerciciofinal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ejerciciofinal.databinding.FragmentInicioBinding
import com.example.ejerciciofinal.modelo.Pelicula
import com.example.ejerciciofinal.recyclerView.AdaptadorPelicula

class InicioFragment : Fragment() {

    private var _binding: FragmentInicioBinding? = null
    private val binding get() = _binding!!

    // Aquí guardamos TODAS las películas que vienen de Room.
    // Esta lista no se toca, es la lista original.
    private var listaPeliculasCompleta: List<Pelicula> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInicioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mostrarBienvenida()
        cargarRecyclerView()
        configurarBuscador()
    }

    private fun mostrarBienvenida() {
        val usuario = (activity as MainActivity).miViewModel.usuario

        if (usuario != null) {
            binding.tvInicio.text = "Bienvenido ${usuario.nombre}\nRol: ${usuario.rol}"
        } else {
            binding.tvInicio.text = "Bienvenido"
        }
    }

    private fun cargarRecyclerView() {
        binding.rvPeliculas.layoutManager = LinearLayoutManager(requireContext())

        // Observamos la lista de películas que viene de Room.
        (activity as MainActivity).miViewModel.listaPeliculas.observe(viewLifecycleOwner) { peliculas ->

            // Guardamos la lista completa.
            listaPeliculasCompleta = peliculas

            // Aplicamos el filtro actual.
            // Si no hay nada escrito, mostrará todas.
            aplicarFiltro(binding.etBuscarInicio.text.toString())
        }
    }

    private fun configurarBuscador() {

        // Esto se ejecuta cada vez que escribes una letra en el buscador.
        binding.etBuscarInicio.addTextChangedListener { texto ->

            val textoBuscado = texto.toString()

            aplicarFiltro(textoBuscado)
        }
    }

    private fun aplicarFiltro(textoBuscado: String) {

        val listaFiltrada = if (textoBuscado.isBlank()) {

            // Si no escribes nada, mostramos todas las películas.
            listaPeliculasCompleta

        } else {

            // Si escribes algo, buscamos películas que contengan ese texto.
            listaPeliculasCompleta.filter { pelicula ->

                pelicula.nombre.contains(
                    textoBuscado,
                    ignoreCase = true
                )
            }
        }

        mostrarPeliculas(listaFiltrada)
    }

    private fun mostrarPeliculas(lista: List<Pelicula>) {

        binding.rvPeliculas.adapter = AdaptadorPelicula(lista) { peliculaPulsada ->

            // Guardamos la película pulsada en el ViewModel.
            (activity as MainActivity).miViewModel.seleccionarPelicula(peliculaPulsada)

            // Vamos al detalle de la película.
            findNavController().navigate(R.id.action_inicioFragment_to_detallePeliculaFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}