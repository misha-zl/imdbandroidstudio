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
import com.example.ejerciciofinal.databinding.FragmentPerfilBinding
import com.example.ejerciciofinal.modelo.Usuario
import com.example.ejerciciofinal.recyclerView.AdaptadorUsuario

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
        controlarZonaAdmin()

        binding.btnAnadirUsuario.setOnClickListener {
            findNavController().navigate(R.id.action_perfilFragment_to_anadirUsuarioFragment)
        }
    }

    private fun mostrarDatosUsuario() {
        val usuario = (activity as MainActivity).miViewModel.usuario

        if (usuario == null) {
            binding.tvDatosPerfil.text = "No hay usuario iniciado"
            return
        }

        binding.tvDatosPerfil.text =
            "Nombre: ${usuario.nombre}\n" +
                    "Usuario: ${usuario.nombreUsuario}\n" +
                    "Teléfono: ${usuario.telefono}\n" +
                    "Rol: ${usuario.rol}"
    }

    private fun controlarZonaAdmin() {
        val esAdmin = (activity as MainActivity).miViewModel.puedeGestionarUsuarios()

        if (esAdmin) {
            binding.btnAnadirUsuario.visibility = View.VISIBLE
            binding.tvTituloGestionUsuarios.visibility = View.VISIBLE
            binding.rvUsuarios.visibility = View.VISIBLE

            cargarRecyclerUsuarios()
        } else {
            binding.btnAnadirUsuario.visibility = View.GONE
            binding.tvTituloGestionUsuarios.visibility = View.GONE
            binding.rvUsuarios.visibility = View.GONE
        }
    }

    private fun cargarRecyclerUsuarios() {
        binding.rvUsuarios.layoutManager = LinearLayoutManager(requireContext())

        (activity as MainActivity).miViewModel.listaUsuarios.observe(viewLifecycleOwner) { usuarios ->

            binding.rvUsuarios.adapter = AdaptadorUsuario(
                usuarios,
                onEditarUsuario = { usuario ->
                    (activity as MainActivity).miViewModel.seleccionarUsuario(usuario)
                    findNavController().navigate(R.id.action_perfilFragment_to_editarUsuarioFragment)
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