package com.example.mapsapp.data

import android.net.Uri

object UserData {
    var username = ""
    var description = ""
    var photo: Uri? = null
    var fromDatabase: Boolean = false

    fun isName(): Boolean{
        return username.isNotEmpty()
    }

    fun isPhoto():Boolean{
        return photo != null
    }
    fun isDescription(): Boolean{
        return description.isNotEmpty()
    }
}