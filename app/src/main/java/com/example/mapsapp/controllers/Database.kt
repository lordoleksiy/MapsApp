package com.example.mapsapp.controllers

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.mapsapp.data.UserData
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream


class Database() {
    private val database = FirebaseDatabase.getInstance("https://mapsapp-346920-default-rtdb.europe-west1.firebasedatabase.app/")
    val auth = Firebase.auth
    val users = database.getReference("users")
    private val storage = FirebaseStorage.getInstance("gs://mapsapp-346920.appspot.com")
    val usersImage = storage.getReference("users")
    val partiesImage = storage.getReference("parties")
    val parties = database.getReference("parties")


    fun getPhoto(){
        usersImage.child(auth.uid!!).downloadUrl.addOnCompleteListener {
            if (it.isSuccessful){
                UserData.photo = it.result
                UserData.fromDatabase = true
            }
        }
    }

    fun getName(){
        users.child(auth.uid!!).get().addOnSuccessListener {
            UserData.username = it.child("name").value.toString()
        }
    }

    @SuppressLint("SetTextI18n")
    fun getDescription(){
        users.child(auth.uid!!).get().addOnSuccessListener {
            if (it.child("description").value == null) UserData.description =  "No description"
            else UserData.description = it.child("description").value.toString()
        }
    }
}