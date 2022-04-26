package com.example.mapsapp

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.widget.Toolbar
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import com.example.mapsapp.controllers.Database
import com.example.mapsapp.controllers.onBottomNavClick
import com.example.mapsapp.data.Party
import com.example.mapsapp.data.UserData
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.io.ByteArrayOutputStream
import java.lang.Exception

class PartyActivity : AppCompatActivity() {
    private lateinit var party: Party
    private lateinit var database: Database
    private lateinit var key: String
    private lateinit var launcher: ActivityResultLauncher<Intent>
    @RequiresApi(Build.VERSION_CODES.P)
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_party)

        supportActionBar?.hide()
        onBottomNavClick(findViewById(R.id.bottomNav), this)
        findViewById<BottomNavigationView>(R.id.bottomNav).selectedItemId = R.id.tapeButton
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.customToolbar)
        supportActionBar?.customView = toolbar

        database = Database()
        key = intent.getStringExtra("id")!!
        database.parties.child(key).get().addOnSuccessListener {
            party = it.getValue(Party::class.java)!!
            findViewById<TextView>(R.id.partyName).text = party.name
            val listOfPeople = party.listOfPeople!!
            if (listOfPeople.size == 1) findViewById<TextView>(R.id.textPCount).text = "1 person"
            else findViewById<TextView>(R.id.textPCount).text = "${listOfPeople.size} people"
        }
        launch()
    }

    fun addPhoto(item: MenuItem? = null){
        try {
            val photoPickerIntent = Intent(Intent.ACTION_PICK)
            photoPickerIntent.type = "image/*"
            launcher.launch(photoPickerIntent)
        } catch (e: Exception){ Log.e("Error", "Палундра, очко в огне") }
    }
    @RequiresApi(Build.VERSION_CODES.P)
    fun launch(){
        launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.data?.data != null){
                pushPhoto(it.data?.data!!)
            }
        }
    }

    @Suppress("DEPRECATION")
    @RequiresApi(Build.VERSION_CODES.P)
    fun pushPhoto(uri: Uri){
        val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 2, baos)
        val byteArray = baos.toByteArray()
        database.partiesImage.child(key).child("photos").putFile(uri).addOnCompleteListener {
            if (it.isSuccessful)
                Toast.makeText(this, "Photo successfully downloaded!", Toast.LENGTH_SHORT).show()
        }
        database.partiesImage.child(key).child("icons").putBytes(byteArray)
    }
}