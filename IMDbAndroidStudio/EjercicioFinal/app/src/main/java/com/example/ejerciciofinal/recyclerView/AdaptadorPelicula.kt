package com.example.ejerciciofinal.recyclerView

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ejerciciofinal.R
import com.example.ejerciciofinal.databinding.FragmentItemPeliculaBinding
import com.example.ejerciciofinal.modelo.Pelicula

/*
Tengo esta lista de películas.
Por cada película, crea una tarjeta visual.
Dentro de cada tarjeta, pon el nombre, director, año, crítica e imagen.
 */
class AdaptadorPelicula(
    /*Recibe una lista*/
    private val listaPeliculas: List<Pelicula>,
    private val onClickPelicula: (Pelicula) -> Unit
) : RecyclerView.Adapter<AdaptadorPelicula.ViewHolder>() {

    /*El ViewHolder guarda las vistas de cada item.
    tvNombrePelicula
    tvDirectorPelicula
    tvAnioPelicula
    ivPosterPelicula

     */
    inner class ViewHolder(
        val binding: FragmentItemPeliculaBinding
    ) : RecyclerView.ViewHolder(binding.root)


    /*crea tarjeta virtual usando item_pelicula.xml*/
    /*Coge el XML fragment_item_pelicula.xml y crea una tarjeta vacía.*/


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = FragmentItemPeliculaBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return ViewHolder(binding)
    }


    /*rellena la tarjeta con datos cogiendo la pelicula de la posicion actual y poner los datos en los TextView*/
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        /*position = 0 → Avatar
        position = 1 → Forrest Gump
        position = 2 → Pulp Fiction*/

        val pelicula = listaPeliculas[position]

        holder.binding.tvNombrePelicula.text = pelicula.nombre
        holder.binding.tvDirectorPelicula.text = "Director: ${pelicula.director}"
        holder.binding.tvAnioPelicula.text = "Año: ${pelicula.anio}"
        holder.binding.tvCriticaPelicula.text = pelicula.critica
      //  holder.binding.tvGeneroPelicula.text = pelicula.genero


        //En caso de no haber imagen se pone la imagen por defecto
        if (pelicula.imagen.isNotBlank()) {
            try {
                holder.binding.ivPosterPelicula.setImageURI(Uri.parse(pelicula.imagen))
            } catch (e: Exception) {
                holder.binding.ivPosterPelicula.setImageResource(R.drawable.no_foto)
            }
        } else {
            holder.binding.ivPosterPelicula.setImageResource(R.drawable.no_foto)
        }

        /*Cuando pulse esta película,
ejecuta la acción que me han pasado desde el Fragment.*/
        holder.binding.itemPelicula.setOnClickListener {
            onClickPelicula(pelicula)
        }
    }
    /*devuelve el tamaño de la lista asi se muestra la cantidad de peliculas que hay*/
    override fun getItemCount(): Int {
        return listaPeliculas.size
    }
}