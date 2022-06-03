package com.example.mapsapp.controllers

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mapsapp.PhotoActivity
import com.example.mapsapp.R
import com.example.mapsapp.data.User
import com.example.mapsapp.data.UserData

class UsersAdapter(val context: Context, private val userList: ArrayList<User>): RecyclerView.Adapter<UsersAdapter.UserHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.user_item, parent, false)
        return UserHolder(view)
    }

    override fun onBindViewHolder(holder: UserHolder, position: Int) {
        holder.name.text = userList[position].name
        if (userList[position].description != "null")
            holder.description.text = userList[position].description
        else holder.description.text = "No description"
        Glide.with(context).load(userList[position].photoUri).into(holder.photo)
        holder.body.setOnClickListener {
            context.startActivity(Intent(context, PhotoActivity::class.java).putExtra("photo", userList[position].photoUri.toString()))
        }
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    class UserHolder(view: View): RecyclerView.ViewHolder(view){
        val body = view.findViewById<RelativeLayout>(R.id.userItemBody)
        val name = view.findViewById<TextView>(R.id.person_name)
        val photo = view.findViewById<ImageView>(R.id.personImg)
        val description = view.findViewById<TextView>(R.id.description)
    }
}