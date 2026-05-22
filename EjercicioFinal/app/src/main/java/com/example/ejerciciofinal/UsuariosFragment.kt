package com.example.ejerciciofinal

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ejerciciofinal.databinding.FragmentUsuariosBinding
import com.example.ejerciciofinal.modelo.Usuario
import com.example.ejerciciofinal.recyclerView.AdaptadorUsuario

class UsuariosFragment : Fragment() {

    private var _binding: FragmentUsuariosBinding? = null
    private val binding get() = _binding!!

    private var listaUsuariosCompleta: List<Usuario> = emptyList()

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
        configurarRecyclerView()
        configurarBuscador()
        cargarUsuarios()

        binding.btnAnadirUsuario.setOnClickListener {
            findNavController().navigate(R.id.action_usuariosFragment_to_anadirUsuarioFragment)
        }
    }

    private fun comprobarPermisos() {

        // Comprobamos si el usuario es ADMIN usando el ViewModel.puedeGestionarUsuarios().
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

    private fun configurarRecyclerView() {
        binding.rvUsuarios.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun cargarUsuarios() {
        (activity as MainActivity).miViewModel.listaUsuarios.observe(viewLifecycleOwner) { usuarios ->

            listaUsuariosCompleta = usuarios

            aplicarFiltro(binding.etBuscarUsuario.text.toString())
        }
    }

    private fun configurarBuscador() {
        binding.etBuscarUsuario.addTextChangedListener { texto ->

            aplicarFiltro(texto.toString())
        }
    }

    private fun aplicarFiltro(textoBuscado: String) {
        val texto = textoBuscado.trim()

        val listaFiltrada = if (texto.isBlank()) {
            listaUsuariosCompleta
        } else {
            listaUsuariosCompleta.filter { usuario ->

                usuario.nombreUsuario.contains(
                    texto,
                    ignoreCase = true
                ) ||
                        usuario.telefono.contains(
                            texto,
                            ignoreCase = true
                        )
            }
        }

        mostrarUsuarios(listaFiltrada)
    }

    private fun mostrarUsuarios(usuarios: List<Usuario>) {
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

    private fun confirmarEliminarUsuario(usuario: Usuario) {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar usuario")
            .setMessage("¿Seguro que quieres eliminar usuario?")
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