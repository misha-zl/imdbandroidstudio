package com.example.ejerciciofinal

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.ejerciciofinal.databinding.FragmentEditarPeliculaBinding
import com.example.ejerciciofinal.modelo.Pelicula

class EditarPeliculaFragment : Fragment() {

    private var _binding: FragmentEditarPeliculaBinding? = null
    private val binding get() = _binding!!

    private var peliculaActual: Pelicula? = null
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
                binding.ivEditarPreviewPelicula.setImageURI(uri)

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
        _binding = FragmentEditarPeliculaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        comprobarPermisos()
        cargarDatos()
        configurarBotonGuardar()

        binding.btnVolverPeliculas.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnEditarImagenPelicula.setOnClickListener {
            seleccionarImagen.launch(arrayOf("image/*"))
        }
    }

    private fun comprobarPermisos() {
        val puedeEditar =
            (activity as MainActivity).miViewModel.puedeEditarEliminarPelicula()

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
        imagenSeleccionada = pelicula.imagen

        if (pelicula.imagen.isNotBlank()) {
            binding.ivEditarPreviewPelicula.setImageURI(Uri.parse(pelicula.imagen))
        } else {
            binding.ivEditarPreviewPelicula.setImageResource(R.drawable.ic_launcher_background)
        }

        binding.etEditarNombre.setText(pelicula.nombre)
        binding.etEditarDirector.setText(pelicula.director)
        binding.etEditarAnio.setText(pelicula.anio.toString())
        binding.etEditarDescripcion.setText(pelicula.descripcion)
        binding.etEditarCritica.setText(pelicula.critica)
        //binding.etEditarGenero.setText(pelicula.genero)
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
       // val genero = binding.etEditarGenero.text.toString().trim()

        if (
            nombre.isBlank() ||
            director.isBlank() ||
            anioTexto.isBlank() ||
            descripcion.isBlank() ||
            critica.isBlank()
            // || gener.isBlank()
        ) {
            Toast.makeText(
                requireContext(),
                "Rellena todos los campos",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val anio = anioTexto.toIntOrNull()


        //si se quiere meter un rango de años se puede hacer asi
        //if (anio == null || anio < 1900 || anio > 2023) {

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
            imagen = imagenSeleccionada,
            //genero= genero
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