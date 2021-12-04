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
import com.smarteist.autoimageslider.SliderViewAdapter


class SliderAdapter(var images: ArrayList<String>, var isUrl : Boolean) : SliderViewAdapter<SliderAdapter.Holder>() {
    override fun onCreateViewHolder(parent: ViewGroup): Holder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.adapter_slide_images, parent, false)
        return Holder(view)
    }

    override fun onBindViewHolder(viewHolder: Holder, position: Int) {
        if(isUrl)
        Glide.with(viewHolder.imageView.context)
            .asBitmap()
            .load(images[position])
            .into(viewHolder.imageView)
        else
            Glide.with(viewHolder.imageView.context)
                .asBitmap()
                .load(images[position])
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(viewHolder.imageView)
    }

    override fun getCount(): Int {
        return images.size
    }

    inner class Holder(itemView: View) : ViewHolder(itemView) {

        var imageView: ImageView

        init {
            imageView = itemView.findViewById(R.id.adapterImage)
        }
    }
}