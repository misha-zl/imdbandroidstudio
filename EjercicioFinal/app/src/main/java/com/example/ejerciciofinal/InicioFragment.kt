package com.example.ejerciciofinal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.ejerciciofinal.databinding.FragmentInicioBinding

class InicioFragment : Fragment() {

    private var _binding: FragmentInicioBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentInicioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val usuario = (activity as MainActivity).miViewModel.usuario

        if (usuario != null) {
            binding.tvInicio.text =
                "Bienvenido ${usuario.nombre}\nRol: ${usuario.rol}"
        } else {
            binding.tvInicio.text = "Bienvenido"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}