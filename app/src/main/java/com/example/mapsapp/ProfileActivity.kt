package com.example.mapsapp

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.example.mapsapp.controllers.Database
import com.example.mapsapp.controllers.onBottomNavClick
import com.example.mapsapp.data.UserData
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso

class ProfileActivity : AppCompatActivity() {
    private lateinit var database: Database
    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        database = Database()
        supportActionBar?.title = "Edit Menu"
        onBottomNavClick(findViewById(R.id.bottomNav), this, "Profile")
        findViewById<BottomNavigationView>(R.id.bottomNav).selectedItemId = R.id.profileButton

        if (UserData.isPhoto()){
            findViewById<ImageView>(R.id.personImage).setImageURI(UserData.photo)
            if (UserData.fromDatabase)
                Picasso.with(this).load(UserData.photo).into(findViewById<ImageView>(R.id.personImage))
        }
        findViewById<TextView>(R.id.personName).text = UserData.username
        findViewById<TextView>(R.id.personDescription).text = UserData.description
    }

    fun editMenu(view: View){
        startActivity(Intent(this, ProfileEditActivity::class.java))
    }
}