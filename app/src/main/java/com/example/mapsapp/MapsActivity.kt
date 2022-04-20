package com.example.mapsapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.icu.text.CaseMap
import android.location.Location
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.mapsapp.databinding.ActivityMapsBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso


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
    private lateinit var header: View
    private lateinit var buttonlayout: RelativeLayout

    private lateinit var navView: NavigationView


    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        buttonlayout = findViewById(R.id.buttonLayout)
        navView = findViewById(R.id.nav_view)
        header = navView.getHeaderView(0)
        buttonlayout.visibility = View.GONE

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        database = Database(Firebase.auth)
        launch()
        val photo = intent.getStringExtra("uri")
        // подгрузим фото и имя юзера:
        if (database.auth.currentUser == null){
            signIn()
        }
        else{
            if (photo != null){
                header.findViewById<ImageView>(R.id.personImage).setImageURI(Uri.parse(photo))
            }
            else{
                getPhoto()
            }
            setName()
            showParties()
        }
        header.findViewById<ImageButton>(R.id.imageButton).setOnClickListener{
            startActivity(Intent(this, MenuEdit::class.java)) }
    }

    // при загрузке карты считуем нажатия
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mapsEdit = MapsEdit(googleMap)
        mMap.uiSettings.isZoomControlsEnabled = true
        setUpMap()
        mMap.setOnMapClickListener {
            changeNavigationView()
        }
        mMap.setOnMarkerClickListener {
            onMarkerCkick()
            return@setOnMarkerClickListener true
        }
    }
    private fun onMarkerCkick(){
        navView.menu.clear()
        navView.inflateMenu(R.menu.nav_party_menu)
        findViewById<DrawerLayout>(R.id.DrawerLayout).openDrawer(GravityCompat.START)
    }

    // анстройка доступа + последняя локация
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

    // открыть закрыть навигатион вью
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home){
            changeNavigationView()
            val drawer = findViewById<DrawerLayout>(R.id.DrawerLayout)
            if (drawer.isDrawerOpen(GravityCompat.START)){
                drawer.closeDrawer(GravityCompat.START)
            }
            else{
                drawer.openDrawer(GravityCompat.START)
            }
        }
        return true
    }

    // просто меняем навигейшн вью на стандартный
    private fun changeNavigationView(){
        navView.menu.clear()
        navView.inflateMenu(R.menu.nav_menu)
    }

    // получить фото из базы
    private fun getPhoto(){
        database.usersImage.child(database.auth.uid!!).downloadUrl.addOnCompleteListener {
            if (it.isSuccessful){
                header.findViewById<ImageView>(R.id.personImage).setImageURI(it.result)
                Picasso.with(this).load(it.result).into(header.findViewById<ImageView>(R.id.personImage))
            }
        }
    }
    // установить имя пользователя
    private fun setName(){
        database.users.child(database.auth.uid!!).get().addOnSuccessListener {
            findViewById<TextView>(R.id.textName).text = it.child("name").value.toString()
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
            setName()
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
                    database.parties.child(key!!).setValue(Party(text, lastPos, listOfPeople, "time", database.auth.uid!!))
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
                val latitude = party.child("latLng").child("latitude").value.toString().toDouble()
                val longitude = party.child("latLng").child("longitude").value.toString().toDouble()
                val latLng = LatLng(latitude, longitude)
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