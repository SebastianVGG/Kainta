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

class DestacadoFragmentAdapter (
    val context: Context,
    val layoutResource: Int,
    var jsonArrayObjetos: JSONArray,
    var listener : OnItemClickListener

) : RecyclerView.Adapter<DestacadoFragmentAdapter.ServicioVH>() {

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



            itemView.setOnClickListener(View.OnClickListener {
                listener.onItemClick(jsonServicio)
            })

        }

    }
}


