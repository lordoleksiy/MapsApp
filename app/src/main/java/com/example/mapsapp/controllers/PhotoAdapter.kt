package com.example.mapsapp.controllers

import android.content.Context
import android.content.Intent
import android.media.Image
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mapsapp.PhotoActivity
import com.example.mapsapp.R

class PhotoAdapter(val context: Context, private val imageList: ArrayList<Uri>, private val nameList:ArrayList<String>): RecyclerView.Adapter<PhotoAdapter.ImageHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageHolder {
        return ImageHolder(LayoutInflater.from(parent.context).inflate(R.layout.photo_item, parent, false))
    }

    override fun onBindViewHolder(holder: ImageHolder, position: Int) {
        Glide.with(context).load(imageList[position]).into(holder.image)
        holder.image.setOnClickListener {
            context.startActivity(Intent(context, PhotoActivity::class.java).putExtra("name", nameList[position]))
        }
    }

    override fun getItemCount(): Int {
        return imageList.size
    }

    class ImageHolder(view: View): RecyclerView.ViewHolder(view){
        val image: ImageView = view.findViewById(R.id.galleryImage)
    }


}