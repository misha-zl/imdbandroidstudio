package com.example.ejerciciofinal.recyclerView

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ejerciciofinal.R
import com.example.ejerciciofinal.databinding.FragmentItemPeliculaBinding
import com.example.ejerciciofinal.modelo.Pelicula

class AdaptadorPelicula(
    private val listaPeliculas: List<Pelicula>,
    private val onClickPelicula: (Pelicula) -> Unit
) : RecyclerView.Adapter<AdaptadorPelicula.ViewHolder>() {

    inner class ViewHolder(
        val binding: FragmentItemPeliculaBinding
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = FragmentItemPeliculaBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val pelicula = listaPeliculas[position]

        holder.binding.tvNombrePelicula.text = pelicula.nombre
        holder.binding.tvDirectorPelicula.text = "Director: ${pelicula.director}"
        holder.binding.tvAnioPelicula.text = "Año: ${pelicula.anio}"
        holder.binding.tvCriticaPelicula.text = pelicula.critica

        if (pelicula.imagen.isNotBlank()) {
            try {
                holder.binding.ivPosterPelicula.setImageURI(Uri.parse(pelicula.imagen))
            } catch (e: Exception) {
                holder.binding.ivPosterPelicula.setImageResource(R.drawable.ic_launcher_background)
            }
        } else {
            holder.binding.ivPosterPelicula.setImageResource(R.drawable.ic_launcher_background)
        }

        holder.binding.itemPelicula.setOnClickListener {
            onClickPelicula(pelicula)
        }
    }

    override fun getItemCount(): Int {
        return listaPeliculas.size
    }
}