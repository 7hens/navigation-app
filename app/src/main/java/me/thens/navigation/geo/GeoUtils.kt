package me.thens.navigation.geo

import android.location.Location
import com.google.android.gms.maps.model.LatLng
import com.google.maps.model.LatLng as MapsLatLng

fun Location.toLatLng() = LatLng(latitude, longitude)

fun LatLng.distanceTo(destination: LatLng): Double {
    val result = FloatArray(1)
    Location.distanceBetween(latitude, longitude, destination.latitude, destination.longitude, result)
    return result[0].toDouble()
}

fun LatLng.toMapsLatLng(): MapsLatLng {
    return MapsLatLng(latitude, longitude)
}

fun MapsLatLng.toLatLng(): LatLng {
    return LatLng(lat, lng)
}