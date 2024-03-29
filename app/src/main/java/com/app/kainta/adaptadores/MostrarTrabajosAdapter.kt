package com.app.kainta.adaptadores

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.kainta.R
import org.json.JSONArray
import org.json.JSONObject

class MostrarTrabajosAdapter (
    val context: Context,
    val layoutResource: Int,
    var jsonArrayObjetos: JSONArray,
    var listener : OnItemClickListener

) : RecyclerView.Adapter<MostrarTrabajosAdapter.TrabajoVH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrabajoVH {
        val view = LayoutInflater.from(context).inflate(layoutResource, parent, false)
        return TrabajoVH(view)
    }

    override fun onBindViewHolder(holder: TrabajoVH, position: Int) {
        val nuevoObjeto = jsonArrayObjetos.getJSONObject(position)
        holder.bind(nuevoObjeto)
    }

    override fun getItemCount(): Int {
        return jsonArrayObjetos.length()
    }

    interface OnItemClickListener {
        fun onItemClick(item : JSONObject)
    }

    inner class TrabajoVH(itemView: View) : RecyclerView.ViewHolder(itemView) {

        //----------------BIND--------------------------
        fun bind(jsonTrabajo: JSONObject) {

            val btnMostrar = itemView.findViewById<Button>(R.id.btnMostrarTrabajo)
            val nombre = itemView.findViewById<TextView>(R.id.adapterNombre)
            val descripcion = itemView.findViewById<TextView>(R.id.adapterDescripcion)

            nombre.text = jsonTrabajo.getString("titulo")
            descripcion.text = jsonTrabajo.getString("descripcion")

            btnMostrar.setOnClickListener {
                listener.onItemClick(jsonTrabajo)
            }


        }

    }

}