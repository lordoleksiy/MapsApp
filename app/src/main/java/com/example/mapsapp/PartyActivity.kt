package com.example.mapsapp

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.viewpager2.widget.ViewPager2
import com.example.mapsapp.controllers.Database
import com.example.mapsapp.controllers.ViewPageAdapter
import com.example.mapsapp.controllers.onBottomNavClick
import com.example.mapsapp.data.Party
import com.example.mapsapp.data.UserData
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.ByteArrayOutputStream
import java.lang.Exception
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class PartyActivity : AppCompatActivity() {
    private lateinit var party: Party
    private lateinit var database: Database
    private lateinit var key: String
    private lateinit var launcher: ActivityResultLauncher<Intent>
    private val tabTitles = listOf("users", "photos")
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
        key = UserData.party
        database.parties.child(key).addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.value != null){
                    party = snapshot.getValue(Party::class.java)!!
                    findViewById<TextView>(R.id.partyName).text = party.name
                    val listOfPeople = party.listOfPeople!!
                    when {
                        listOfPeople.isEmpty() -> database.delParty(key)
                        listOfPeople.size == 1 -> findViewById<TextView>(R.id.textPCount).text = "1 person"
                        else -> findViewById<TextView>(R.id.textPCount).text = "${listOfPeople.size} people"
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
        launch()
        findViewById<ViewPager2>(R.id.viewPager).adapter = ViewPageAdapter(this)
        TabLayoutMediator(findViewById(R.id.tabLayout), findViewById(R.id.viewPager)){
            tab, pos-> tab.text = tabTitles[pos]
        }.attach()
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

    fun takePart(item: MenuItem? = null){
        if (party.listOfPeople!!.contains(database.auth.uid!!)){
            Toast.makeText(this, "You have already joined this party!", Toast.LENGTH_SHORT).show()
        }
        else{
            Toast.makeText(this, "Warning! you can be a member only of one party in one time.", Toast.LENGTH_SHORT).show()
            database.takePartInParty(key, party)
        }
    }
    fun showOnMap(item:MenuItem? = null){
        startActivity(Intent(this, MapsActivity::class.java).putExtra("location", party.latLng.toString()))
    }

    fun leaveParty(item:MenuItem? = null){
        runBlocking {
            launch{
                database.leaveParty(key, party)
            }
            launch {
                startActivity(Intent(applicationContext, MapsActivity::class.java))
            }
        }
    }

    fun finishParty(item:MenuItem? = null){
        // еще предстоит придумать
    }

    @Suppress("DEPRECATION")
    @RequiresApi(Build.VERSION_CODES.P)
    fun pushPhoto(uri: Uri){
        val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 1, baos)
        val byteArray = baos.toByteArray()
        val time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        database.partiesImage.child(key).child("photos").child(time).putFile(uri).addOnCompleteListener {
            if (it.isSuccessful)
                Toast.makeText(this, "Photo successfully downloaded!", Toast.LENGTH_SHORT).show()
        }
        database.partiesImage.child(key).child("icons").child(time).putBytes(byteArray)
    }
}