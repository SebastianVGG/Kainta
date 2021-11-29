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

class ServicioVistaAdapter (
    val context: Context,
    val layoutResource: Int,
    var jsonServicios: JSONArray,
    var listener : OnItemClickListener

) : RecyclerView.Adapter<ServicioVistaAdapter.ServicioVH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServicioVH {
        val view = LayoutInflater.from(context).inflate(layoutResource, parent, false)
        return ServicioVH(view)
    }

    override fun onBindViewHolder(holder: ServicioVH, position: Int) {
        val jsonServicio = jsonServicios.getJSONObject(position)
        holder.bind(jsonServicio)
    }

    override fun getItemCount(): Int {
        return jsonServicios.length()
    }

    interface OnItemClickListener {
        fun onItemClick(jsonServicio : JSONObject)
    }

    inner class ServicioVH(itemView: View) : RecyclerView.ViewHolder(itemView) {

        //----------------BIND--------------------------
        fun bind(jsonServicio : JSONObject) {

            val image = itemView.findViewById<ImageView>(R.id.imageviewServicio)
            val servicio = itemView.findViewById<TextView>(R.id.cardServicio)
            val servicioDestacado = itemView.findViewById<TextView>(R.id.txtServicioDestacado)
            val buscado = itemView.findViewById<TextView>(R.id.cardCantidad)


            if(jsonServicio.has("url"))
            context.let {
                Glide.with(it)
                    .load(jsonServicio.getString("url"))
                    .apply(
                        RequestOptions().override(
                            300,
                            300
                        )
                    )
                    .into(image)
            }

            if(jsonServicio.has("search")){

                servicio.text = jsonServicio.getString("nombre").uppercase()
                servicioDestacado.text = "SERVICIO DISPONIBLE"
                buscado.visibility = View.GONE

            }else{
                servicio.text = jsonServicio.getString("nombre").uppercase()
                buscado.text = "Este servicio ha sido buscado más de " + jsonServicio.getString("buscado")+ " veces. "
            }
            itemView.setOnClickListener (View.OnClickListener {
                listener.onItemClick(jsonServicio)
            })


        }

    }
}