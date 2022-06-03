package com.example.mapsapp

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.example.mapsapp.controllers.Database
import com.example.mapsapp.data.UserData

class PhotoActivity : AppCompatActivity() {
    val database = Database()
    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo)
        val photo = intent.getStringExtra("photo")
        val name = intent.getStringExtra("name")
        if (photo != null)
            Glide.with(this).load(Uri.parse(photo)).into(findViewById(R.id.fullImage))
        else
            database.loadImage(this, name!!, findViewById(R.id.fullImage))

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toggleBar()
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        findViewById<ConstraintLayout>(R.id.LayoutBack).setOnClickListener { toggleBar() }

    }
    @SuppressLint("RestrictedApi")
    private fun toggleBar(){
        if (supportActionBar?.isShowing!!){
            supportActionBar?.hide()
        }
        else{
            supportActionBar?.show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home){
            startActivity(Intent(this, PartyActivity::class.java))
        }
        return true
    }
}