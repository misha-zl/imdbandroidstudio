package com.example.ejerciciofinal

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.ejerciciofinal.databinding.FragmentAnadirPeliculaBinding
import com.example.ejerciciofinal.modelo.Pelicula
import androidx.navigation.NavOptions

class AnadirPeliculaFragment : Fragment() {

    private var _binding: FragmentAnadirPeliculaBinding? = null
    private val binding get() = _binding!!

    private var imagenSeleccionada: String = ""

    private val seleccionarImagen = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->

        if (uri != null) {
            try {
                requireContext().contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )

                imagenSeleccionada = uri.toString()
                binding.ivPreviewPelicula.setImageURI(uri)

            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "No se pudo cargar la imagen",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnadirPeliculaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSeleccionarImagenPelicula.setOnClickListener {
            seleccionarImagen.launch(arrayOf("image/*"))
        }

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


        //Si alguno de los campos esta vacio muestra un mensaje de error
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

        //aniotexto lo convierte en int en caso de no ser un numero devuelve null
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
            imagen = imagenSeleccionada
        )

        (activity as MainActivity).miViewModel.insertarPelicula(pelicula)

        Toast.makeText(
            requireContext(),
            "Película añadida correctamente",
            Toast.LENGTH_SHORT
        ).show()

        limpiarFormulario()

        val opciones = NavOptions.Builder()
            .setPopUpTo(R.id.inicioFragment, false)
            .setLaunchSingleTop(true)
            .build()
        findNavController().popBackStack()
        findNavController().navigate(R.id.inicioFragment, null, opciones)
    }

    private fun limpiarFormulario() {
        binding.etAddNombre.text.clear()
        binding.etAddDirector.text.clear()
        binding.etAddAnio.text.clear()
        binding.etAddDescripcion.text.clear()
        binding.etAddCritica.text.clear()

        imagenSeleccionada = ""

        binding.ivPreviewPelicula.setImageResource(R.drawable.ic_launcher_background)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}