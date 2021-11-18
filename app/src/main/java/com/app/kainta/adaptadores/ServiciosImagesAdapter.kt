package com.app.kainta.adaptadores

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.app.kainta.R

class ServiciosImagesAdapter (
    val context: Context,
    val layoutResource: Int,
    var listUri: ArrayList<Uri>,
    var listener : OnItemClickListener

) : RecyclerView.Adapter<ServiciosImagesAdapter.ServicioImageVH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServicioImageVH {
        val view = LayoutInflater.from(context).inflate(layoutResource, parent, false)
        return ServicioImageVH(view)
    }

    override fun onBindViewHolder(holder: ServicioImageVH, position: Int) {
        val uri = listUri[position]
        holder.bind(uri)
    }

    override fun getItemCount(): Int {
        return listUri.size
    }

    interface OnItemClickListener {
        fun onItemClick(uri : Uri)
    }

    inner class ServicioImageVH(itemView: View) : RecyclerView.ViewHolder(itemView) {

        //----------------BIND--------------------------
        fun bind(uri: Uri) {
            val image = itemView.findViewById<ImageView>(R.id.adapterImage)
            image.setImageURI(uri)
        }

    }
}