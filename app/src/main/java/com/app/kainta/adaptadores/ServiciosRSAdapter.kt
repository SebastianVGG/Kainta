package com.app.kainta.adaptadores

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.kainta.R
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

class ServiciosRSAdapter (
    val context: Context,
    val layoutResource: Int,
    var jsonArrayObjetos: JSONArray,
    var listener : OnItemClickListener

) : RecyclerView.Adapter<ServiciosRSAdapter.ServicioVH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServicioVH {
        val view = LayoutInflater.from(context).inflate(layoutResource, parent, false)
        return ServicioVH(view)
    }

    override fun onBindViewHolder(holder: ServicioVH, position: Int) {
        val nuevoObjeto = jsonArrayObjetos.getJSONObject(position)
        holder.bind(nuevoObjeto)
    }

    override fun getItemCount(): Int {
        return jsonArrayObjetos.length()
    }

    interface OnItemClickListener {
        fun onItemClick(item: JSONObject?)
    }

    inner class ServicioVH(itemView: View) : RecyclerView.ViewHolder(itemView) {

        //----------------BIND--------------------------
        fun bind(jsonServicio: JSONObject) {
            val servicio = itemView.findViewById<TextView>(R.id.txtServicio)
            val nombre = itemView.findViewById<TextView>(R.id.txtNombre)
            val ciudad = itemView.findViewById<TextView>(R.id.txtFecha)

            servicio.text = jsonServicio.getString("servicio").uppercase()
            nombre.text = jsonServicio.getString("nombre").replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(
                    Locale.getDefault()
                ) else it.toString()
            }

            itemView.setOnClickListener(View.OnClickListener {
                listener.onItemClick(jsonServicio)
            })

        }

    }

}