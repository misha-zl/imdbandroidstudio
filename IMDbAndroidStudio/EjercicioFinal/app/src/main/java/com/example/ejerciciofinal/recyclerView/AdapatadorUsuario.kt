package com.example.ejerciciofinal.recyclerView

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ejerciciofinal.databinding.FragmentItemUsuarioBinding
import com.example.ejerciciofinal.modelo.Usuario

import android.net.Uri
import com.example.ejerciciofinal.R

class AdaptadorUsuario(
    private val listaUsuarios: List<Usuario>,
    private val onEditarUsuario: (Usuario) -> Unit,
    private val onEliminarUsuario: (Usuario) -> Unit
) : RecyclerView.Adapter<AdaptadorUsuario.ViewHolder>() {

    inner class ViewHolder(
        val binding: FragmentItemUsuarioBinding
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = FragmentItemUsuarioBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val usuario = listaUsuarios[position]

        holder.binding.tvItemNombreUsuario.text = usuario.nombreUsuario
        holder.binding.tvItemNombreCompleto.text = usuario.nombre
        holder.binding.tvItemRolUsuario.text = "Rol: ${usuario.rol}"

        holder.binding.btnEditarUsuario.setOnClickListener {
            onEditarUsuario(usuario)
        }

        holder.binding.btnEliminarUsuario.setOnClickListener {
            onEliminarUsuario(usuario)
        }

        if (usuario.imagenPerfil.isNotBlank()) {
            try {
                holder.binding.ivItemFotoUsuario.setImageURI(Uri.parse(usuario.imagenPerfil))
            } catch (e: Exception) {
                holder.binding.ivItemFotoUsuario.setImageResource(R.drawable.no_foto)
            }
        } else {
            holder.binding.ivItemFotoUsuario.setImageResource(R.drawable.no_foto)
        }
    }

    override fun getItemCount(): Int {
        return listaUsuarios.size
    }
}