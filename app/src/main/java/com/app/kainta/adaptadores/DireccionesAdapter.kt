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

class DireccionesAdapter(
    val context: Context,
    val layoutResource: Int,
    var jsonArrayObjetos: JSONArray,
    var listener : DireccionesAdapter.OnItemClickListener

) : RecyclerView.Adapter<DireccionesAdapter.DireccionVH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DireccionVH {
        val view = LayoutInflater.from(context).inflate(layoutResource, parent, false)
        return DireccionVH(view)
    }

    override fun onBindViewHolder(holder: DireccionVH, position: Int) {
        val nuevoObjeto = jsonArrayObjetos.getJSONObject(position)
        holder.bind(nuevoObjeto)
    }

    override fun getItemCount(): Int {
        return jsonArrayObjetos.length()
    }

    interface OnItemClickListener {
        fun onItemClick(item : JSONObject?, editar : Boolean)
    }

    inner class DireccionVH(itemView: View) : RecyclerView.ViewHolder(itemView) {

        //----------------BIND--------------------------
        fun bind(jsonObjeto: JSONObject) {

            val nombre = itemView.findViewById<TextView>(R.id.cardNombre)
            val direccion = itemView.findViewById<TextView>(R.id.cardDireccion)
            val colonia = itemView.findViewById<TextView>(R.id.cardColonia)
            val cp = itemView.findViewById<TextView>(R.id.cardCP)
            val ciudad = itemView.findViewById<TextView>(R.id.cardCiudad)
            val telefono = itemView.findViewById<TextView>(R.id.cardTelefono)
            val btnEditar = itemView.findViewById<Button>(R.id.btnCardEditar)
            val btnEliminar = itemView.findViewById<Button>(R.id.btnCardEliminar)

            nombre.text = jsonObjeto.getString("nombre")
            direccion.text = jsonObjeto.getString("direccion")
            colonia.text = jsonObjeto.getString("colonia")
            cp.text = jsonObjeto.getString("cp")
            ciudad.text = jsonObjeto.getString("ciudad")
            telefono.text = jsonObjeto.getString("telefono")

            btnEditar.setOnClickListener {
                    listener.onItemClick(jsonObjeto, true)
            }

            btnEliminar.setOnClickListener {
                listener.onItemClick(jsonObjeto, false)
            }

        }

    }





}