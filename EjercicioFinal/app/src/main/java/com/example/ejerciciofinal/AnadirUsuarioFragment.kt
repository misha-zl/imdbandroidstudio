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
import com.example.ejerciciofinal.databinding.FragmentAnadirUsuarioBinding

class AnadirUsuarioFragment : Fragment() {

    private var _binding: FragmentAnadirUsuarioBinding? = null
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
                binding.ivNuevoUsuarioFoto.setImageURI(uri)

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
        _binding = FragmentAnadirUsuarioBinding.inflate(inflater, container, false)
        return binding.root


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!(activity as MainActivity).miViewModel.puedeGestionarUsuarios()) {
            Toast.makeText(requireContext(), "No tienes permisos", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.perfilFragment)
            return
        }

        binding.btnGuardarNuevoUsuario.setOnClickListener {
            guardarUsuario()
        }

        binding.btnSeleccionarFotoNuevoUsuario.setOnClickListener {
            seleccionarImagen.launch(arrayOf("image/*"))
        }
    }

    private fun guardarUsuario() {
        val nombre = binding.etNuevoNombre.text.toString().trim()
        val nombreUsuario = binding.etNuevoUsuario.text.toString().trim()
        val password = binding.etNuevoPassword.text.toString().trim()
        val telefono = binding.etNuevoTelefono.text.toString().trim()
        val esAdmin = binding.cbNuevoAdmin.isChecked

        if (nombre.isBlank() || nombreUsuario.isBlank() || password.isBlank() || telefono.isBlank()) {
            Toast.makeText(requireContext(), "Rellena todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        if (nombreUsuario.contains(" ")) {
            Toast.makeText(requireContext(), "El usuario no puede tener espacios", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 4) {
            Toast.makeText(requireContext(), "La contraseña debe tener al menos 4 caracteres", Toast.LENGTH_SHORT).show()
            return
        }

        if (telefono.length != 9 || !telefono.all { it.isDigit() }) {
            Toast.makeText(requireContext(), "El teléfono debe tener 9 números", Toast.LENGTH_SHORT).show()
            return
        }

        (activity as MainActivity).miViewModel.crearUsuarioPorAdmin(
            nombre,
            nombreUsuario,
            password,
            telefono,
            esAdmin,
            imagenSeleccionada
        ) { correcto, mensaje ->

            Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show()

            if (correcto) {
                findNavController().navigate(R.id.action_anadirUsuarioFragment_to_usuariosFragment)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}