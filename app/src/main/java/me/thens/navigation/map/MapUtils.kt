package me.thens.navigation.map

import android.location.Location
import com.baidu.mapapi.model.LatLng

fun Location.toLatLng() = LatLng(latitude, longitude)

fun LatLng.distanceTo(destination: LatLng): Double {
    val result = FloatArray(1)
    Location.distanceBetween(
        latitude,
        longitude,
        destination.latitude,
        destination.longitude,
        result
    )
    return result[0].toDouble()
}