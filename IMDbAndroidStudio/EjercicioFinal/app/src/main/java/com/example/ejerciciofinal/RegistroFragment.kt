package com.example.ejerciciofinal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.ejerciciofinal.databinding.FragmentRegistroBinding

class RegistroFragment : Fragment() {

    private var _binding: FragmentRegistroBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentRegistroBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnRegistrar.setOnClickListener {
            registrarUsuario()
        }

        binding.btnVolverLogin.setOnClickListener {
            findNavController().navigate(R.id.action_registroFragment_to_loginFragment)
        }
    }

    private fun registrarUsuario() {
        val nombre = binding.etRegistroNombre.text.toString().trim()
        val nombreUsuario = binding.etRegistroUsuario.text.toString().trim()
        val password = binding.etRegistroPassword.text.toString().trim()
        val telefono = binding.etRegistroTelefono.text.toString().trim()

        if (nombre.isBlank()) {
            Toast.makeText(
                requireContext(),
                "El nombre es obligatorio",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (nombreUsuario.isBlank()) {
            Toast.makeText(
                requireContext(),
                "El nombre de usuario es obligatorio",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (nombreUsuario.contains(" ")) {
            Toast.makeText(
                requireContext(),
                "El nombre de usuario no puede tener espacios",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (password.length < 4) {
            Toast.makeText(
                requireContext(),
                "La contraseña debe tener al menos 4 caracteres",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (telefono.length != 9 || !telefono.all { it.isDigit() }) {
            Toast.makeText(
                requireContext(),
                "El teléfono debe tener 9 números",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        (activity as MainActivity).miViewModel.registrarUsuario(
            nombre,
            nombreUsuario,
            password,
            telefono
        ) { correcto, mensaje ->

            Toast.makeText(
                requireContext(),
                mensaje,
                Toast.LENGTH_SHORT
            ).show()

            if (correcto) {
                findNavController().navigate(R.id.action_registroFragment_to_loginFragment)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}