package com.example.mapsapp.controllers

import android.content.Context
import android.content.Intent
import com.example.mapsapp.MapsActivity
import com.example.mapsapp.ProfileActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

fun onBottomNavClick(bottomNavigationView: BottomNavigationView, context: Context, title: String= ""){
    bottomNavigationView.setOnItemSelectedListener {
        if (title == it.title)
            return@setOnItemSelectedListener true
        when(it.title){
            "Map" -> context.startActivity(Intent(context, MapsActivity::class.java))
            "Profile" -> context.startActivity(Intent(context, ProfileActivity::class.java))
        }

        true
    }
}
