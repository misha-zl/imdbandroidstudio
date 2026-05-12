package com.example.ejerciciofinal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.ejerciciofinal.databinding.FragmentAddPeliculaBinding
import com.example.ejerciciofinal.modelo.Pelicula

class AddPeliculaFragment : Fragment() {

    private var _binding: FragmentAddPeliculaBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddPeliculaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnGuardarPelicula.setOnClickListener {
            guardarPelicula()
        }
    }

    private fun guardarPelicula() {
        val nombre = binding.etAddNombre.text.toString().trim()
        val director = binding.etAddDirector.text.toString().trim()
        val anioTexto = binding.etAddAnio.text.toString().trim()
        val descripcion = binding.etAddDescripcion.text.toString().trim()
        val critica = binding.etAddCritica.text.toString().trim()

        if (nombre.isBlank() || director.isBlank() || anioTexto.isBlank()
            || descripcion.isBlank() || critica.isBlank()
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

        val pelicula = Pelicula(
            nombre = nombre,
            director = director,
            anio = anio,
            descripcion = descripcion,
            critica = critica,
            imagen = ""
        )

        (activity as MainActivity).miViewModel.insertarPelicula(pelicula)

        Toast.makeText(
            requireContext(),
            "Película añadida correctamente",
            Toast.LENGTH_SHORT
        ).show()

        findNavController().navigate(R.id.action_anadirPeliculaFragment_to_inicioFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}