package com.app.kainta.adaptadores

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.app.kainta.R
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

abstract class ServiciosImagesURLAdapter(
    val context: Context,
    val layoutResource: Int,
    var listURLS: ArrayList<String>,

) : RecyclerView.Adapter<ServiciosImagesURLAdapter.ServicioImageVH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServicioImageVH {
        val view = LayoutInflater.from(context).inflate(layoutResource, parent, false)
        return ServicioImageVH(view)
    }

    override fun onBindViewHolder(holder: ServicioImageVH, position: Int) {
        val url = listURLS[position]
        holder.bind(url)
    }

    override fun getItemCount(): Int {
        return listURLS.size
    }

    abstract fun finishedGlide()

    abstract fun onItemClick(uri : String, posicion : Int)


    inner class ServicioImageVH(itemView: View) : RecyclerView.ViewHolder(itemView) {

        //----------------BIND--------------------------

        fun bind(url: String) {

            val image = itemView.findViewById<ImageView>(R.id.adapterImage)


            if((adapterPosition+1) == listURLS.size){

                Glide.with(image.context)
                    .asBitmap()
                    .load(url)
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(image)

                finishedGlide()
            }else
            Glide.with(image.context)
                .asBitmap()
                .load(url)
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(image)

            image.setOnClickListener {
                onItemClick(url, adapterPosition)
            }


        }

    }
}