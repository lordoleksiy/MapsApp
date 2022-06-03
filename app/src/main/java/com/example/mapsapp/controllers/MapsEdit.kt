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

fun parseLatLng(latLngStr: String): LatLng {
    val pos = latLngStr.split(" ")
    return LatLng(pos[0].toDouble(), pos[1].toDouble())
}

fun getLength(point1: LatLng, point2: LatLng): Double{
    val x = (point2.longitude - point1.longitude) * Math.cos(0.5 * (point1.latitude + point2.latitude))
    val y = point2.latitude - point1.latitude
    return 6371 * Math.sqrt(x * x + y * y)
}