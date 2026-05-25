package com.example.ejerciciofinal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.ejerciciofinal.databinding.FragmentLoginBinding

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnLogin.setOnClickListener {
            iniciarSesion()
        }

        binding.btnIrRegistro.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registroFragment)
        }
    }

    private fun iniciarSesion() {
        val nombreUsuario = binding.etLoginUsuario.text.toString().trim()
        val password = binding.etLoginPassword.text.toString().trim()

        if (nombreUsuario.isBlank() || password.isBlank()) {
            Toast.makeText(
                requireContext(),
                "Introduce usuario y contraseña",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        (activity as MainActivity).miViewModel.login(
            nombreUsuario,
            password
        ) { correcto, usuario, mensaje ->

            Toast.makeText(
                requireContext(),
                mensaje,
                Toast.LENGTH_SHORT
            ).show()

            if (correcto && usuario != null) {

                // Guardamos la sesión en SharedPreferences.
                (activity as MainActivity).guardarSesionEnSharedPreferences(usuario)

                // Navegamos a la pantalla principal.
                findNavController().navigate(R.id.action_loginFragment_to_inicioFragment)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}