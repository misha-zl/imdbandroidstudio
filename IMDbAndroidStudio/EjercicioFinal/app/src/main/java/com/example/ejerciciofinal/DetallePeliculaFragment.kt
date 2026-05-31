package com.example.ejerciciofinal

import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.ejerciciofinal.databinding.FragmentDetallePeliculaBinding
import com.example.ejerciciofinal.modelo.Pelicula

class DetallePeliculaFragment : Fragment() {

    private var _binding: FragmentDetallePeliculaBinding? = null
    private val binding get() = _binding!!

    private var peliculaActual: Pelicula? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetallePeliculaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cargarDatosPelicula()
        controlarBotonesAdmin()
        configurarBotones()

        binding.btnVolverPeliculas.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun cargarDatosPelicula() {
        val pelicula = (activity as MainActivity).miViewModel.peliculaSeleccionada

        peliculaActual = pelicula

        if (pelicula == null) {
            Toast.makeText(
                requireContext(),
                "No has seleccionado ninguna película",
                Toast.LENGTH_SHORT
            ).show()

            findNavController().navigate(R.id.inicioFragment)
            return
        }


        if (pelicula.imagen.isNotBlank()) {
            try {
                binding.ivDetallePoster.setImageURI(Uri.parse(pelicula.imagen))
            } catch (e: Exception) {
                binding.ivDetallePoster.setImageResource(R.drawable.no_foto)
            }
        } else {
            binding.ivDetallePoster.setImageResource(R.drawable.no_foto)
        }

        binding.tvDetalleNombre.text = pelicula.nombre
        binding.tvDetalleDirector.text = "Director: ${pelicula.director}"
        binding.tvDetalleAnio.text = "Año: ${pelicula.anio}"
        binding.tvDetalleDescripcion.text = pelicula.descripcion
        binding.tvDetalleCritica.text = pelicula.critica
        // binding,tvDetalleGenero.text = pelicula.genero

       // actualizarTextoFavorita(pelicula)
    }

    private fun controlarBotonesAdmin() {
        val puedeEditarEliminar =
            (activity as MainActivity).miViewModel.puedeEditarEliminarPelicula()

        if (puedeEditarEliminar) {
            binding.btnEditarPelicula.visibility = View.VISIBLE
            binding.btnEliminarPelicula.visibility = View.VISIBLE
        } else {
            binding.btnEditarPelicula.visibility = View.GONE
            binding.btnEliminarPelicula.visibility = View.GONE
        }
    }

    private fun configurarBotones() {
        binding.btnEditarPelicula.setOnClickListener {
            findNavController().navigate(R.id.action_detallePeliculaFragment_to_editarPeliculaFragment)
        }

        binding.btnEliminarPelicula.setOnClickListener {
            confirmarEliminarPelicula()
        }

      /*  binding.btnFavorita.setOnClickListener {
            cambiarFavorita()
        }*/
    }

    private fun confirmarEliminarPelicula() {
        val pelicula = (activity as MainActivity).miViewModel.peliculaSeleccionada

        if (pelicula == null) {
            Toast.makeText(
                requireContext(),
                "No se puede eliminar la película",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar película")
            .setMessage("¿Seguro que quieres eliminar la película?")
            .setPositiveButton("Eliminar") { _, _ ->

                (activity as MainActivity).miViewModel.borrarPelicula(pelicula)

                Toast.makeText(
                    requireContext(),
                    "Película eliminada correctamente",
                    Toast.LENGTH_SHORT
                ).show()


                findNavController().navigate(R.id.action_detallePeliculaFragment_to_inicioFragment)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

/*
    private fun cambiarFavorita() {
        val pelicula = peliculaActual ?: return
        val peliculaActualizada = pelicula.copy(favorita = !pelicula.favorita)

        peliculaActual = peliculaActualizada


        (activity as MainActivity).miViewModel.seleccionarPelicula(peliculaActualizada)
        (activity as MainActivity).miViewModel.actualizarPelicula(peliculaActualizada)

        actualizarTextoFavorita(peliculaActualizada)


        Toast.makeText(requireContext(), "Favorita actualizada", Toast.LENGTH_SHORT).show()


    }

    private fun actualizarTextoFavorita(pelicula: Pelicula) {
        binding.btnFavorita.text =
            if (pelicula.favorita) {
                "Quitar de favoritas"
            } else {
                "Marcar como favorita"
            }
    } */



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}