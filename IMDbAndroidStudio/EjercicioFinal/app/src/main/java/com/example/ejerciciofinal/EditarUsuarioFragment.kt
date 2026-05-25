package com.example.ejerciciofinal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.ejerciciofinal.databinding.FragmentEditarUsuarioBinding
import com.example.ejerciciofinal.modelo.Usuario

import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts

class EditarUsuarioFragment : Fragment() {

    private var _binding: FragmentEditarUsuarioBinding? = null
    private val binding get() = _binding!!

    private var usuarioActual: Usuario? = null

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
                binding.ivEditarUsuarioFoto.setImageURI(uri)

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
        _binding = FragmentEditarUsuarioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!(activity as MainActivity).miViewModel.puedeGestionarUsuarios()) {
            Toast.makeText(requireContext(), "No tienes permisos", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.perfilFragment)
            return
        }

        cargarDatos()

        binding.btnGuardarUsuarioEditado.setOnClickListener {
            guardarCambios()
        }

        binding.btnVolverListaUsuarios.setOnClickListener {
            findNavController().navigate(R.id.action_editarUsuarioFragment_to_usuariosFragment)
        }

        binding.btnSeleccionarFotoEditarUsuario.setOnClickListener {
            seleccionarImagen.launch(arrayOf("image/*"))
        }
    }

    private fun cargarDatos() {
        val usuario = (activity as MainActivity).miViewModel.usuarioSeleccionado

        if (usuario == null) {
            Toast.makeText(requireContext(), "No has seleccionado ningún usuario", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.perfilFragment)
            return
        }

        usuarioActual = usuario

        binding.etEditarUsuarioNombre.setText(usuario.nombre)
        binding.etEditarUsuarioNombreUsuario.setText(usuario.nombreUsuario)
        binding.etEditarUsuarioPassword.setText(usuario.password)
        binding.etEditarUsuarioTelefono.setText(usuario.telefono)
        binding.cbEditarUsuarioAdmin.isChecked = usuario.rol == "ADMIN"

        imagenSeleccionada = usuario.imagenPerfil

        if (usuario.imagenPerfil.isNotBlank()) {
            try {
                binding.ivEditarUsuarioFoto.setImageURI(Uri.parse(usuario.imagenPerfil))
            } catch (e: Exception) {
                binding.ivEditarUsuarioFoto.setImageResource(R.drawable.no_foto)
            }
        } else {
            binding.ivEditarUsuarioFoto.setImageResource(R.drawable.no_foto)
        }
    }

    private fun guardarCambios() {
        val usuario = usuarioActual ?: return

        val nombre = binding.etEditarUsuarioNombre.text.toString().trim()
        val nombreUsuario = binding.etEditarUsuarioNombreUsuario.text.toString().trim()
        val password = binding.etEditarUsuarioPassword.text.toString().trim()
        val telefono = binding.etEditarUsuarioTelefono.text.toString().trim()
        val rol = if (binding.cbEditarUsuarioAdmin.isChecked) "ADMIN" else "NORMAL"

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

        val usuarioEditado = Usuario(
            id = usuario.id,
            nombre = nombre,
            nombreUsuario = nombreUsuario,
            password = password,
            telefono = telefono,
            rol = rol,
            imagenPerfil = imagenSeleccionada
        )

        (activity as MainActivity).miViewModel.actualizarUsuarioPorAdmin(usuarioEditado) { correcto, mensaje ->

            Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show()

            if (correcto) {
                findNavController().navigate(R.id.action_editarUsuarioFragment_to_usuariosFragment)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}