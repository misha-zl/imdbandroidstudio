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
import com.example.ejerciciofinal.databinding.FragmentPerfilBinding

class PerfilFragment : Fragment() {

    private var _binding: FragmentPerfilBinding? = null
    private val binding get() = _binding!!

    private val seleccionarFotoPerfil = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->

        if (uri != null) {
            try {
                requireContext().contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )

                binding.ivFotoPerfil.setImageURI(uri)

                (activity as MainActivity).miViewModel.actualizarMiFotoPerfil(
                    uri.toString()
                )

                Toast.makeText(
                    requireContext(),
                    "Foto de perfil actualizada",
                    Toast.LENGTH_SHORT
                ).show()

            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "No se pudo cargar la foto",
                    Toast.LENGTH_SHORT
                ).show()

                binding.ivFotoPerfil.setImageResource(R.drawable.ic_launcher_background)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPerfilBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mostrarDatosUsuario()

        binding.btnCambiarFotoPerfil.setOnClickListener {
            seleccionarFotoPerfil.launch(arrayOf("image/*"))
        }

        binding.btnDesloguearsePerfil.setOnClickListener {
            (activity as MainActivity).desloguearse()
        }
    }

    private fun mostrarDatosUsuario() {
        val usuario = (activity as MainActivity).miViewModel.usuario

        if (usuario == null) {
            binding.ivFotoPerfil.setImageResource(R.drawable.ic_launcher_background)
            binding.tvDatosPerfil.text = "No hay usuario iniciado"
            return
        }

        if (usuario.imagenPerfil.isNotBlank()) {
            try {
                binding.ivFotoPerfil.setImageURI(Uri.parse(usuario.imagenPerfil))
            } catch (e: Exception) {
                binding.ivFotoPerfil.setImageResource(R.drawable.ic_launcher_background)
            }
        } else {
            binding.ivFotoPerfil.setImageResource(R.drawable.ic_launcher_background)
        }

        binding.tvDatosPerfil.text =
            "Nombre: ${usuario.nombre}\n" +
                    "Usuario: ${usuario.nombreUsuario}\n" +
                    "Teléfono: ${usuario.telefono}\n" +
                    "Rol: ${usuario.rol}"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}