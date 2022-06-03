package com.example.mapsapp.data

data class Party(
    val name: String? = null,
    val latLng: String? = null,
    val listOfPeople: ArrayList<String>? = null,
    val time: String? = null,
    var organizer:String? = null
){
    fun containsUser(userID: String): Boolean{
        return listOfPeople!!.contains(userID)
    }
}
