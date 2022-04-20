package com.example.mapsapp

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class Database(auth: FirebaseAuth) {
    private val database = FirebaseDatabase.getInstance("https://mapsapp-346920-default-rtdb.europe-west1.firebasedatabase.app/")
    val auth = auth
    val users = database.getReference("users")
    val storage = FirebaseStorage.getInstance("gs://mapsapp-346920.appspot.com")
    val usersImage = storage.getReference("users")
    val parties = database.getReference("parties")

}