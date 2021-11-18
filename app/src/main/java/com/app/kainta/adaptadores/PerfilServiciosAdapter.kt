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

class PerfilServiciosAdapter (
    val context: Context,
    val layoutResource: Int,
    var stringArray: ArrayList<String>,
    var listener : OnItemClickListener

) : RecyclerView.Adapter<PerfilServiciosAdapter.ServicioVH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServicioVH {
        val view = LayoutInflater.from(context).inflate(layoutResource, parent, false)
        return ServicioVH(view)
    }

    override fun onBindViewHolder(holder: ServicioVH, position: Int) {
        val nuevoString = stringArray[position]
        holder.bind(nuevoString)
    }

    override fun getItemCount(): Int {
        return stringArray.size
    }

    interface OnItemClickListener {
        fun onItemClick(item: String)
    }

    inner class ServicioVH(itemView: View) : RecyclerView.ViewHolder(itemView) {

        //----------------BIND--------------------------
        fun bind(servicioNombre: String) {
            val nombre = itemView.findViewById<TextView>(R.id.adapterNombre)

            nombre.text = servicioNombre

            itemView.setOnClickListener(View.OnClickListener {
                listener.onItemClick(servicioNombre)
            })

        }

    }





}