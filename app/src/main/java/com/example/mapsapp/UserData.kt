package com.example.mapsapp

import com.squareup.picasso.RequestCreator

object UserData {
    var username = ""
    var photo: RequestCreator? = null

    fun isName(): Boolean{
        return username.isNotEmpty()
    }

    fun isPhoto():Boolean{
        return photo != null
    }
}