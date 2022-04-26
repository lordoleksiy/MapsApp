package com.example.mapsapp.data

data class Party(
    val name: String? = null,
    val latLng: String? = null,
    val listOfPeople: ArrayList<String>? = null,
    val time: String? = null,
    val organizer:String? = null
)
