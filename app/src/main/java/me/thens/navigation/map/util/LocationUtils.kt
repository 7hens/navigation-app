package me.thens.navigation.map.util

import android.location.Location
import com.baidu.location.BDLocation
import com.baidu.mapapi.map.MyLocationData
import com.baidu.mapapi.model.LatLng
import com.baidu.mapapi.search.route.WalkingRouteLine
import me.thens.navigation.map.model.LocPoint

fun BDLocation.toMyLocation(): MyLocationData {
    return MyLocationData.Builder()
        .accuracy(radius)
        .direction(direction)
        .latitude(latitude)
        .longitude(longitude)
        .build()
}

fun BDLocation.toLatLng() = LatLng(latitude, longitude)

fun MyLocationData.toLatLng() = LatLng(latitude, longitude)

val WalkingRouteLine.wayPoints: List<LatLng>
    get () = allStep?.flatMap { it.wayPoints } ?: emptyList()

fun Location.toLatLng() = LatLng(latitude, longitude)

fun LocPoint.toLatLng() = LatLng(lat, lng)

fun LatLng.toLocPoint() = LocPoint(latitude, longitude)

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