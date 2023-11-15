package me.thens.navigation.map.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class RouteLine(
    val origin: LocPoint,
    val destination: LocPoint,
    val points: List<LocPoint>,
) : Parcelable {
}