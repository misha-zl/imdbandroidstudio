package com.example.ejerciciofinal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.ejerciciofinal.databinding.FragmentPerfilBinding

class PerfilFragment : Fragment() {

    private var _binding: FragmentPerfilBinding? = null
    private val binding get() = _binding!!

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

        binding.btnDesloguearsePerfil.setOnClickListener {
            (activity as MainActivity).desloguearse()
        }
    }

    private fun mostrarDatosUsuario() {
        val usuario = (activity as MainActivity).miViewModel.usuario

        if (usuario == null) {
            binding.tvAvatarPerfil.text = "?"
            binding.tvDatosPerfil.text = "No hay usuario iniciado"
            return
        }

        binding.tvAvatarPerfil.text =
            usuario.nombre.firstOrNull()?.toString()?.uppercase() ?: "U"

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