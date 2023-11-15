package me.thens.navigation.map.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class LocPoint(val lat: Double, val lng: Double) : Parcelable {
}