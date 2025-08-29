package me.corv.pillenalarm.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import me.corv.pillenalarm.R
import java.util.function.Consumer

class GalleryGridAdapter(context: Context, gifsList: ArrayList<String>) :
    ArrayAdapter<String>(context, 0, gifsList) {

    private lateinit var onUnlikeConsumer: Consumer<String>
    private lateinit var onClickGifConsumer: Consumer<String>

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var galleryItem = convertView
        if (galleryItem == null) {
            // Layout Inflater inflates each item to be displayed in GridView.
            galleryItem =
                LayoutInflater.from(context).inflate(R.layout.gallery_item, parent, false)
        }
        val gif = getItem(position)
        galleryItem!!.findViewById<MaterialButton>(R.id.button_like).setOnClickListener {
            onUnlikeConsumer.accept(gif!!)
        }
        Glide.with(context).load(gif).into(galleryItem.findViewById(R.id.imageView))
        galleryItem.setOnClickListener {
            onClickGifConsumer.accept(gif!!)
        }
        return galleryItem
    }

    fun setOnUnlikeConsumer(consumer: Consumer<String>) {
        this.onUnlikeConsumer = consumer
    }

    fun setOnClickGifConsumer(consumer: Consumer<String>) {
        this.onClickGifConsumer = consumer
    }

}