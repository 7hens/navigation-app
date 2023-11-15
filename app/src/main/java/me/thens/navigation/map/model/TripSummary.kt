package me.thens.navigation.map.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TripSummary(
    val origin: LocPoint,
    val destination: LocPoint,
    val navigationPoints: List<LocPoint>,
    val traveledPoints: List<LocPoint>,
    val duration: Long,
    val distance: Double,
) : Parcelable {
}