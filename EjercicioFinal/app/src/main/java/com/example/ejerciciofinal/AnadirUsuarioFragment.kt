package com.example.ejerciciofinal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.ejerciciofinal.databinding.FragmentAnadirUsuarioBinding

class AnadirUsuarioFragment : Fragment() {

    private var _binding: FragmentAnadirUsuarioBinding? = null
    private val binding get() = _binding!!

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
            esAdmin
        ) { correcto, mensaje ->

            Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show()

            if (correcto) {
                findNavController().navigate(R.id.action_anadirUsuarioFragment_to_perfilFragment)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}