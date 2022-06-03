package com.example.mapsapp.controllers

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.location.Location
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.bumptech.glide.Glide
import com.example.mapsapp.R
import com.example.mapsapp.data.Party
import com.example.mapsapp.data.UserData
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class Database() {
    private val database = FirebaseDatabase.getInstance("https://mapsapp-346920-default-rtdb.europe-west1.firebasedatabase.app/")
    val auth = Firebase.auth
    val users = database.getReference("users")
    private val storage = FirebaseStorage.getInstance("gs://mapsapp-346920.appspot.com")
    val usersImage = storage.getReference("users")
    val partiesImage = storage.getReference("parties")
    val parties = database.getReference("parties")


    fun getPhoto(){
        usersImage.child(auth.uid!!).downloadUrl.addOnCompleteListener {
            if (it.isSuccessful && it.result != null){
                UserData.photo = it.result
                UserData.fromDatabase = true
            }
        }
    }

    fun getName(){
        users.child(auth.uid!!).get().addOnSuccessListener {
            UserData.username = it.child("name").value.toString()
        }
    }

    @SuppressLint("SetTextI18n")
    fun getDescription(){
        users.child(auth.uid!!).get().addOnSuccessListener {
            if (it.child("description").value == null) UserData.description =  "No description"
            else UserData.description = it.child("description").value.toString()
        }
    }

    fun delParty(key: String){
        parties.child(key).removeValue()
    }

    @SuppressLint("NewApi")
    fun makeParty(pos: LatLng, text: String): String{
        users.child(auth.uid!!).child("party").get().addOnSuccessListener {partyRef->
            parties.child(partyRef.value.toString()).get().addOnSuccessListener {
                val party = it.getValue(Party::class.java)
                if (party != null){
                    party.listOfPeople?.remove(auth.uid!!)
                    if (party.listOfPeople?.size!! == 0) delParty(partyRef.value.toString())
                    else {
                        parties.child(partyRef.value.toString()).setValue(party)
                        if (party.organizer == auth.uid!!) party.organizer = party.listOfPeople[0]
                    }
                }
            }
        }

        val listOfPeople = ArrayList<String>()
        listOfPeople.add(auth.uid!!)
        val key = parties.push().key
        val pos = "${pos.latitude} ${pos.longitude}"
        val time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))  // оптимизируй для старых андроид
        parties.child(key!!).setValue(Party(text, pos, listOfPeople, time, auth.uid!!))
        users.child(auth.uid!!).child("party").setValue(key)
        return key
    }

    fun loadImage(context: Context, path: String, imageView: ImageView){
        val path2 = path.split("/")
        partiesImage.child(path2[2]).child("photos").child(path2[path2.size-1]).downloadUrl.addOnSuccessListener {
            Glide.with(context).load(it).into(imageView)
        }
    }

    fun takePartInParty(key: String, party: Party) {
        party.listOfPeople!!.add(auth.uid!!)
        parties.child(key).setValue(party)
    }

    fun leaveParty(key: String, party: Party){
        if (party.listOfPeople?.contains(auth.uid!!) == true) {
            users.child(auth.uid!!).child("party").removeValue()
            if (party.listOfPeople.size > 1) {
                party.listOfPeople.remove(auth.uid!!)
                if (party.organizer == auth.uid) {
                    party.organizer = party.listOfPeople[0]
                }
                parties.child(key).setValue(party)
            } else {
                parties.child(key).removeValue()
            }
        }
    }

    fun showParties(map: GoogleMap, mapsEdit: MapsEdit, lastLocation:Location? = null, filter: Int? = null){
        map.clear()
        parties.get().addOnCompleteListener {
            it.result.children.forEach {partySnap ->
                val party = partySnap.getValue(Party::class.java)!!
                val latLng = parseLatLng(party.latLng.toString())
                if (filter != null){
                    val length = getLength(LatLng(lastLocation!!.latitude, lastLocation.longitude), latLng)
                    if (length > filter){
                        return@forEach
                    }
                }
                when {
                    party.organizer == auth.uid -> {
                        mapsEdit.makeMarker(latLng, party.name.toString(), partySnap.key.toString(), BitmapDescriptorFactory.HUE_GREEN)
                    }
                    party.containsUser(auth.uid!!) -> {
                        mapsEdit.makeMarker(latLng, party.name.toString(), partySnap.key.toString(), BitmapDescriptorFactory.HUE_BLUE)
                    }
                    else -> {
                        mapsEdit.makeMarker(latLng, party.name.toString(), partySnap.key.toString(), BitmapDescriptorFactory.HUE_RED)
                    }
                }
            }
        }
    }

}