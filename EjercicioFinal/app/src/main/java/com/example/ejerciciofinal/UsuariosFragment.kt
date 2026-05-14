package com.example.ejerciciofinal

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ejerciciofinal.databinding.FragmentUsuariosBinding
import com.example.ejerciciofinal.modelo.Usuario
import com.example.ejerciciofinal.recyclerView.AdaptadorUsuario

class UsuariosFragment : Fragment() {

    private var _binding: FragmentUsuariosBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUsuariosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        comprobarPermisos()
        cargarUsuarios()

        binding.btnAnadirUsuario.setOnClickListener {
            findNavController().navigate(R.id.action_usuariosFragment_to_anadirUsuarioFragment)
        }
    }

    private fun comprobarPermisos() {
        val esAdmin = (activity as MainActivity).miViewModel.puedeGestionarUsuarios()

        if (!esAdmin) {
            Toast.makeText(
                requireContext(),
                "No tienes permisos para gestionar usuarios",
                Toast.LENGTH_SHORT
            ).show()

            findNavController().navigate(R.id.perfilFragment)
        }
    }

    private fun cargarUsuarios() {
        binding.rvUsuarios.layoutManager = LinearLayoutManager(requireContext())

        (activity as MainActivity).miViewModel.listaUsuarios.observe(viewLifecycleOwner) { usuarios ->

            binding.rvUsuarios.adapter = AdaptadorUsuario(
                usuarios,
                onEditarUsuario = { usuario ->
                    (activity as MainActivity).miViewModel.seleccionarUsuario(usuario)
                    findNavController().navigate(R.id.action_usuariosFragment_to_editarUsuarioFragment)
                },
                onEliminarUsuario = { usuario ->
                    confirmarEliminarUsuario(usuario)
                }
            )
        }
    }

    private fun confirmarEliminarUsuario(usuario: Usuario) {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar usuario")
            .setMessage("¿Seguro que quieres eliminar a ${usuario.nombreUsuario}?")
            .setPositiveButton("Eliminar") { _, _ ->

                (activity as MainActivity).miViewModel.borrarUsuarioPorAdmin(usuario) { _, mensaje ->

                    Toast.makeText(
                        requireContext(),
                        mensaje,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}