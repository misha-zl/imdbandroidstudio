package com.example.ejerciciofinal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.ejerciciofinal.databinding.FragmentEditarPeliculaBinding
import com.example.ejerciciofinal.modelo.Pelicula

class EditarPeliculaFragment : Fragment() {

    private var _binding: FragmentEditarPeliculaBinding? = null
    private val binding get() = _binding!!

    private var peliculaActual: Pelicula? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditarPeliculaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        comprobarPermisos()
        cargarDatos()
        configurarBotonGuardar()
    }

    private fun comprobarPermisos() {
        val puedeEditar = (activity as MainActivity).miViewModel.puedeEditarEliminarPelicula()

        if (!puedeEditar) {
            Toast.makeText(
                requireContext(),
                "No tienes permisos para editar películas",
                Toast.LENGTH_SHORT
            ).show()

            findNavController().navigate(R.id.inicioFragment)
        }
    }

    private fun cargarDatos() {
        val pelicula = (activity as MainActivity).miViewModel.peliculaSeleccionada

        if (pelicula == null) {
            Toast.makeText(
                requireContext(),
                "No has seleccionado ninguna película",
                Toast.LENGTH_SHORT
            ).show()

            findNavController().navigate(R.id.inicioFragment)
            return
        }

        peliculaActual = pelicula

        binding.etEditarNombre.setText(pelicula.nombre)
        binding.etEditarDirector.setText(pelicula.director)
        binding.etEditarAnio.setText(pelicula.anio.toString())
        binding.etEditarDescripcion.setText(pelicula.descripcion)
        binding.etEditarCritica.setText(pelicula.critica)
    }

    private fun configurarBotonGuardar() {
        binding.btnGuardarCambiosPelicula.setOnClickListener {
            guardarCambios()
        }
    }

    private fun guardarCambios() {
        val pelicula = peliculaActual

        if (pelicula == null) {
            Toast.makeText(
                requireContext(),
                "No se puede editar la película",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val nombre = binding.etEditarNombre.text.toString().trim()
        val director = binding.etEditarDirector.text.toString().trim()
        val anioTexto = binding.etEditarAnio.text.toString().trim()
        val descripcion = binding.etEditarDescripcion.text.toString().trim()
        val critica = binding.etEditarCritica.text.toString().trim()

        if (
            nombre.isBlank() ||
            director.isBlank() ||
            anioTexto.isBlank() ||
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

        val anio = anioTexto.toIntOrNull()

        if (anio == null) {
            Toast.makeText(
                requireContext(),
                "El año debe ser un número",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val peliculaEditada = Pelicula(
            id = pelicula.id,
            nombre = nombre,
            director = director,
            anio = anio,
            descripcion = descripcion,
            critica = critica,
            imagen = pelicula.imagen
        )

        (activity as MainActivity).miViewModel.seleccionarPelicula(peliculaEditada)
        (activity as MainActivity).miViewModel.actualizarPelicula(peliculaEditada)

        Toast.makeText(
            requireContext(),
            "Película actualizada correctamente",
            Toast.LENGTH_SHORT
        ).show()

        findNavController().navigate(R.id.action_editarPeliculaFragment_to_inicioFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}