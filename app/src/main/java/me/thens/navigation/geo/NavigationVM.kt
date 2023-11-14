package me.thens.navigation.geo

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import me.thens.navigation.core.app.BaseViewModel
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class NavigationVM @Inject constructor(
    private val locationService: LocationService,
    private val geoService: GeoService,
) : BaseViewModel() {

    data class State(
        val destination: LatLng? = null,
        val origin: LatLng = LatLng(1.35, 103.87),
        val cameraPosition: LatLng = origin,
        val traveledPath: List<LatLng> = emptyList(),
        val navigationPath: List<LatLng> = emptyList(),
        val isNavigating: Boolean = false,
        val isTripEnded: Boolean = false,
        val tripStartTime: Date = Date(),
        val tripEndTime: Date = Date(),
    )

    sealed interface Event {
        class UpdateDestination(val value: LatLng) : Event
        object OnMapLoaded : Event
        object StartNavigation : Event
        object StopNavigation : Event
    }

    var state by mutableStateOf(State())

    fun handle(event: Event) {
        when (event) {
            is Event.UpdateDestination -> state = state.copy(destination = event.value)
            is Event.StartNavigation -> startNavigation()
            is Event.StopNavigation -> stopNavigation()
            is Event.OnMapLoaded -> onMapLoaded()
            else -> Unit
        }
    }

    private fun onMapLoaded() {
        launch { monitorLocation() }
        launch {
            val location = locationService.getLocation()
            state = state.copy(origin = location.toLatLng())
        }
    }

    private suspend fun monitorLocation() {
        locationService.monitorLocation().collect { location ->
            val myLocation = location.toLatLng()
            if (state.isNavigating) {
                val destination = state.destination!!
                state = state.copy(traveledPath = state.traveledPath + myLocation)
                if (myLocation.distanceTo(destination) < 10) {
                    stopNavigation()
                }
            } else {
                state = state.copy(origin = myLocation)
            }
        }
    }

    private var navigationJob: Job = Job()

    private fun startNavigation() {
        val destination = state.destination!!
        Log.d(TAG, "startNavigation: $destination")
        state = state.copy(
            isNavigating = true,
            traveledPath = emptyList(),
            tripStartTime = Date(),
        )
        navigationJob.cancel()
        navigationJob = launch {
            val directions = geoService.calculateDirections(state.origin, destination)
            val route = directions.routes[0]
            val navigationPath = route.overviewPolyline.decodePath().map { it.toLatLng() }
            Log.d(TAG, "startNavigation: $navigationPath")
            state = state.copy(navigationPath = navigationPath)
        }
    }

    private fun stopNavigation() {
        state = state.copy(
            isNavigating = false,
            isTripEnded = true,
            tripEndTime = Date(),
        )
        navigationJob.cancel()
    }

    companion object {
        private const val TAG = "NavigationVM"
    }
}