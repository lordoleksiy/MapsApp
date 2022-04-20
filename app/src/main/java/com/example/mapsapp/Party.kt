package com.example.mapsapp

import com.google.android.gms.maps.model.LatLng

data class Party(
    val name: String,
    val latLng: LatLng,
    val listOfPeople: ArrayList<String>,
    val time: String,
    val organizer:String
)
