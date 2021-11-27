package com.app.kainta.adaptadores

import android.annotation.SuppressLint
import android.content.Context
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.kainta.R
import org.json.JSONArray
import org.json.JSONObject
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.time.LocalDateTime
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
        @SuppressLint("SetTextI18n")
        fun bind(jsonServicio: JSONObject) {
            val servicio = itemView.findViewById<TextView>(R.id.txtServicio)
            val titulo = itemView.findViewById<TextView>(R.id.txtTitulo)
            val fecha = itemView.findViewById<TextView>(R.id.txtFecha)
            val estado = itemView.findViewById<TextView>(R.id.txtEstado)

            servicio.text = "Servicio: "+ jsonServicio.getString("servicio").uppercase()

            titulo.text = "Titulo: "+ jsonServicio.getString("titulo").replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(
                    Locale.getDefault()
                ) else it.toString()
            }
            jsonServicio.get("fecha").let {
                try {
                   val date = DateFormat.format("yyyy-MM-dd", it as Date)
                    fecha.text = "Fecha: $date"
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            estado.text = jsonServicio.getString("estado")

            itemView.setOnClickListener(View.OnClickListener {
                listener.onItemClick(jsonServicio)
            })

        }

    }

}