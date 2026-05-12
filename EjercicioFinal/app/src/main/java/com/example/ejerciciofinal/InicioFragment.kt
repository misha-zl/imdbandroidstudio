package com.example.ejerciciofinal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ejerciciofinal.databinding.FragmentInicioBinding
import com.example.ejerciciofinal.recyclerView.AdaptadorPelicula

class InicioFragment : Fragment() {

    private var _binding: FragmentInicioBinding? = null
    private val binding get() = _binding!!

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

        (activity as MainActivity).miViewModel.listaPeliculas.observe(viewLifecycleOwner) { peliculas ->

            binding.rvPeliculas.adapter = AdaptadorPelicula(peliculas) { peliculaPulsada ->

                (activity as MainActivity).miViewModel.seleccionarPelicula(peliculaPulsada)

                findNavController().navigate(R.id.action_inicioFragment_to_detallePeliculaFragment)
            }
        }
    }

    private fun irAAnadirPelicula() {
        val puedeAñadir = (activity as MainActivity).miViewModel.puedeAñadirPelicula()

        if (puedeAñadir) {
            findNavController().navigate(R.id.action_inicioFragment_to_anadirPeliculaFragment)
        } else {
            Toast.makeText(
                requireContext(),
                "Debes iniciar sesión para añadir películas",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}