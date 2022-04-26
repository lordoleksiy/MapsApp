package com.example.mapsapp.controllers

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

class MapsEdit(private val map: GoogleMap) {

    fun makeMarker(latLng: LatLng, bitmapColor: Float = BitmapDescriptorFactory.HUE_RED): Marker {
        val markerOptions = MarkerOptions().position(latLng)
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(bitmapColor))
        return map.addMarker(markerOptions)!!
    }
    fun makeMarker(latLng: LatLng, title: String, snippet: String, bitmapColor: Float = BitmapDescriptorFactory.HUE_RED): Marker {
        val markerOptions = MarkerOptions().position(latLng)
        markerOptions.title(title).snippet(snippet)
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(bitmapColor))
        return map.addMarker(markerOptions)!!
    }
}