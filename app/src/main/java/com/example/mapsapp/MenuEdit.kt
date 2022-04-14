package com.example.mapsapp

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.io.ByteArrayOutputStream
import java.lang.Exception


class MenuEdit : AppCompatActivity() {
    private lateinit var database: Database
    private lateinit var launcher: ActivityResultLauncher<Intent>
    private lateinit var uri: Uri
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu_edit)
        supportActionBar?.title = "Edit Menu"
        database = Database(Firebase.auth)
        database.users.child(database.auth.uid!!).get().addOnSuccessListener {
            if (it != null){
                findViewById<EditText>(R.id.editTextTextPersonName).setText(it.value.toString())
            }
        }
            launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.data?.data != null){
                    findViewById<ImageView>(R.id.imageView).setImageURI(it.data?.data)
                    uri = it.data?.data!!
                }
            }

    }

    fun back(view: View){
        startActivity(Intent(this, MapsActivity::class.java))
    }

    fun loadImage(view: View){
        try {
            val photoPickerIntent = Intent(Intent.ACTION_PICK)
            photoPickerIntent.type = "image/*"
            launcher.launch(photoPickerIntent)
        } catch (e: Exception){ Log.e("Error", "Палундра, очко в огне") }
    }

    fun save(view: View){
        if (this::uri.isInitialized){
            val bitmap = findViewById<ImageView>(R.id.imageView).drawable.toBitmap()
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos)
            val byteArray = baos.toByteArray()
            val text = findViewById<EditText>(R.id.editTextTextPersonName).text.toString()
            database.usersImage.child(database.auth.uid!!).putBytes(byteArray).addOnCompleteListener {
                if (it.isSuccessful){
                    Toast.makeText(this, "Photo successfully downloaded!", Toast.LENGTH_SHORT).show()
                }
            }
            if (text.isNotEmpty()){
                database.users.child(database.auth.uid!!).setValue(text)
            }
            startActivity(Intent(this, MapsActivity::class.java).putExtra("uri", uri.toString()))
        }

    }
}