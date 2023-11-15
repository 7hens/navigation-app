package me.thens.navigation.map

import com.baidu.location.BDLocation
import com.baidu.mapapi.map.MyLocationData
import com.baidu.mapapi.model.LatLng
import com.baidu.mapapi.search.route.WalkingRouteLine

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