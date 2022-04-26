package com.example.mapsapp

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.service.autofill.UserData
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import com.example.mapsapp.controllers.Database
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.io.ByteArrayOutputStream
import java.lang.Exception


class ProfileEditActivity : AppCompatActivity() {
    private lateinit var database: Database
    private lateinit var launcher: ActivityResultLauncher<Intent>
    private lateinit var uri: Uri
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu_edit)
        supportActionBar?.title = "Edit Menu"
        database = Database()
        database.users.child(database.auth.uid!!).get().addOnSuccessListener {
            if (it != null){
                findViewById<EditText>(R.id.editTextTextPersonName).setText(it.child("name").value.toString())
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
        startActivity(Intent(this, ProfileActivity::class.java))
    }

    fun loadImage(view: View){
        try {
            val photoPickerIntent = Intent(Intent.ACTION_PICK)
            photoPickerIntent.type = "image/*"
            launcher.launch(photoPickerIntent)
        } catch (e: Exception){ Log.e("Error", "Палундра, очко в огне") }
    }

    // не забудь, что функция еще и uri картинки отправляет!
    fun save(view: View){
        val text = findViewById<EditText>(R.id.editTextTextPersonName).text.toString()
        val description = findViewById<EditText>(R.id.editTextTextMultiLine).text.toString()
        if (text.isNotEmpty()){
            database.users.child(database.auth.uid!!).child("name").setValue(text)
        }
        if (description.isNotEmpty()){
            database.users.child(database.auth.uid!!).child("description").setValue(description)
        }
        if (this::uri.isInitialized){
            com.example.mapsapp.data.UserData.photo = uri
            com.example.mapsapp.data.UserData.fromDatabase = false
            val bitmap = findViewById<ImageView>(R.id.imageView).drawable.toBitmap()
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 65, baos)
            val byteArray = baos.toByteArray()
            database.usersImage.child(database.auth.uid!!).putBytes(byteArray).addOnCompleteListener {
                if (it.isSuccessful) {
                    Toast.makeText(this, "Photo successfully downloaded!", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
        startActivity(Intent(this, ProfileActivity::class.java))
    }
}