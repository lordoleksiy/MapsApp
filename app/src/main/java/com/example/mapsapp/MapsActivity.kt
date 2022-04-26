package com.example.mapsapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import com.example.mapsapp.controllers.Database
import com.example.mapsapp.controllers.MapsEdit
import com.example.mapsapp.controllers.onBottomNavClick
import com.example.mapsapp.data.Party
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.example.mapsapp.databinding.ActivityMapsBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
    companion object {
        private const val  LOCATION_REQUEST_CODE = 1
    }
    private lateinit var mMap: GoogleMap
    private lateinit var mapsEdit: MapsEdit
    private lateinit var binding: ActivityMapsBinding
    private lateinit var lastLocation: Location
    private lateinit var fusedLocationClient: FusedLocationProviderClient


    private lateinit var database: Database
    lateinit var launcher: ActivityResultLauncher<Intent>
    private lateinit var name: String
    private lateinit var buttonlayout: RelativeLayout


    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        buttonlayout = findViewById(R.id.buttonLayout)
        buttonlayout.visibility = View.GONE

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        database = Database()
        launch()

        if (database.auth.currentUser == null){
            signIn()
        }
        onBottomNavClick(findViewById(R.id.bottomNav), this, "Map")
        findViewById<BottomNavigationView>(R.id.bottomNav).selectedItemId = R.id.mapButton
        database.getPhoto()
        database.getDescription()
        database.getName()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.upper_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    // при загрузке карты считуем нажатия
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mapsEdit = MapsEdit(googleMap)
        mMap.uiSettings.isZoomControlsEnabled = true
        setUpMap()
        showParties()
        mMap.setOnMarkerClickListener {
            startActivity(Intent(this, PartyActivity::class.java).putExtra("id", it.snippet))
            return@setOnMarkerClickListener true
        }
    }

    // нaстройка доступа + последняя локация
    private fun setUpMap(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_REQUEST_CODE)
            return
        }
        mMap.isMyLocationEnabled = true
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null){
                lastLocation = location
                val currentLatLong = LatLng(location.latitude, location.longitude)
                mMap.animateCamera(CameraUpdateFactory.newLatLng(currentLatLong))
            }
        }
    }

    // список доступных клиентов
    private fun getClient(): GoogleSignInClient{
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        return GoogleSignIn.getClient(this, gso)
    }

    private fun signIn(){
        val client = getClient()
        launcher.launch(client.signInIntent)
    }

    // авторизация гугла
    private fun authWithGoogle(idToken: String){
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        database.auth.signInWithCredential(credential).addOnSuccessListener {
            database.users.child(database.auth.uid!!).child("name").setValue(name)
//            setName()
            showParties()
        }
    }

    // лаунчр для регистрации
    private fun launch(){
        launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)
            try{
                val account = task.getResult(ApiException::class.java)
                if (account != null){
                    name = account.displayName!!
                    authWithGoogle(account.idToken!!)
                }
            }
            catch (e: ApiException){
                Toast.makeText(this, "Firstly, you must register to use this app", Toast.LENGTH_SHORT).show()
                Toast.makeText(this, "Make sure, that tour network connection is good", Toast.LENGTH_SHORT).show()
                signIn()
            }
        }
    }


    // Функция для создания маркера и переноса в базу данных пати

    fun makeParty(item: MenuItem? = null){
        if (this::lastLocation.isInitialized){
            val lastPos = LatLng(lastLocation.latitude, lastLocation.longitude)
            val marker = mapsEdit.makeMarker(lastPos, BitmapDescriptorFactory.HUE_BLUE)
            buttonlayout.visibility = View.VISIBLE
            findViewById<Button>(R.id.buttonOK).setOnClickListener {
                val text = findViewById<EditText>(R.id.EditPartyName).text.toString()
                if (text.isNotEmpty()){
                    marker.remove()

                    val listOfPeople = ArrayList<String>()
                    listOfPeople.add(database.auth.uid!!)
                    val key =  database.parties.push().key
                    val pos = "${lastPos.latitude} ${lastPos.longitude}"
                    database.parties.child(key!!).setValue(Party(text, pos, listOfPeople, "time", database.auth.uid!!))
                    database.users.child(database.auth.uid!!).child("party").setValue(key)
                    mapsEdit.makeMarker(lastPos, text, key, BitmapDescriptorFactory.HUE_BLUE)
                    buttonlayout.visibility = View.GONE
                }
                else{
                    Toast.makeText(this, "Fill in name of party", Toast.LENGTH_SHORT).show()
                }
            }
            findViewById<Button>(R.id.buttonCancel).setOnClickListener {
                marker.remove()
                buttonlayout.visibility = View.GONE
            }
        }
    }

    // функция для отображения всех тусовок
    private fun showParties(){
        database.parties.get().addOnCompleteListener {
            it.result.children.forEach { party ->
                val latLngStr = party.child("latLng").value.toString().split(" ")
                val latLng = LatLng(latLngStr[0].toDouble(), latLngStr[1].toDouble())
                if (party.child("organizer").value == database.auth.uid){
                    mapsEdit.makeMarker(latLng, party.child("name").value as String, party.key.toString(), BitmapDescriptorFactory.HUE_BLUE)
                }
                else{
                    mapsEdit.makeMarker(latLng, party.child("name").value as String, party.key.toString(), BitmapDescriptorFactory.HUE_RED)
                }
            }
        }
    }


}