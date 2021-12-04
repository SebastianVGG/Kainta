package com.app.kainta.adaptadores

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.app.kainta.R
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

class ServiciosImagesAdapter (
    val context: Context,
    val layoutResource: Int,
    var mArrayUri: ArrayList<String>,
    var listener : OnItemClickListener

) : RecyclerView.Adapter<ServiciosImagesAdapter.ServicioImageVH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServicioImageVH {
        val view = LayoutInflater.from(context).inflate(layoutResource, parent, false)
        return ServicioImageVH(view)
    }

    override fun onBindViewHolder(holder: ServicioImageVH, position: Int) {
        val uri = mArrayUri[position]
        holder.bind(uri)
    }

    override fun getItemCount(): Int {
        return mArrayUri.size
    }

    interface OnItemClickListener {
        fun onItemClick(uri : String, posicion : Int)
    }

    inner class ServicioImageVH(itemView: View) : RecyclerView.ViewHolder(itemView) {

        //----------------BIND--------------------------
        fun bind(uri: String) {
            val image = itemView.findViewById<ImageView>(R.id.adapterImage)
            Glide.with(image.context)
                .asBitmap()
                .load(uri)
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(image)

            image.setOnClickListener {
                listener.onItemClick(uri, adapterPosition)
            }

        }

    }
}