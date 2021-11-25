package com.app.kainta.adaptadores

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.kainta.R
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

class GeneralAdapter (
    val context: Context,
    val layoutResource: Int,
    var jsonArrayObjetos: JSONArray,
    var listener : OnItemClickListener

) : RecyclerView.Adapter<GeneralAdapter.SearchVH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchVH {
        val view = LayoutInflater.from(context).inflate(layoutResource, parent, false)
        return SearchVH(view)
    }

    override fun onBindViewHolder(holder: SearchVH, position: Int) {
        val nuevoObjeto = jsonArrayObjetos.getJSONObject(position)
        holder.bind(nuevoObjeto)
    }

    override fun getItemCount(): Int {
        return jsonArrayObjetos.length()
    }

    interface OnItemClickListener {
        fun onItemClick(item: JSONObject?)
    }

    inner class SearchVH(itemView: View) : RecyclerView.ViewHolder(itemView) {

        //----------------BIND--------------------------
        fun bind(jsonUsuario: JSONObject) {
            val servicio = itemView.findViewById<TextView>(R.id.cardServicio)
            val nombre = itemView.findViewById<TextView>(R.id.cardNombre)
            val ciudad = itemView.findViewById<TextView>(R.id.cardCiudad)
            val imagenPerfilURL = itemView.findViewById<ImageView>(R.id.imageviewPerfil)

            var imagenURL = ""

            if(jsonUsuario.has("url")){
                imagenURL = jsonUsuario.getString("url")
                context.let {
                    Glide.with(it)
                        .load(imagenURL)
                        .apply(
                            RequestOptions().override(
                                300,
                                300
                            )
                        )
                        .into(imagenPerfilURL)
                }
            }
            servicio.text = jsonUsuario.getString("servicio").uppercase()
            nombre.text = jsonUsuario.getString("nombre").replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(
                    Locale.getDefault()
                ) else it.toString()
            }
            ciudad.text = jsonUsuario.getString("ciudad").replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(
                    Locale.getDefault()
                ) else it.toString()
            }



            itemView.setOnClickListener(View.OnClickListener {
                listener.onItemClick(jsonUsuario)
            })

        }

    }





}