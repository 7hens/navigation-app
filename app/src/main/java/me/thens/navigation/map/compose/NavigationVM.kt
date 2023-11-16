package me.thens.navigation.map.compose

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.baidu.mapapi.map.MyLocationData
import com.baidu.mapapi.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import me.thens.navigation.core.app.BaseViewModel
import me.thens.navigation.map.service.LocationService
import me.thens.navigation.map.service.RouteService
import me.thens.navigation.map.util.distanceTo
import me.thens.navigation.map.util.toLatLng
import me.thens.navigation.map.util.toMyLocation
import me.thens.navigation.map.util.wayPoints
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class NavigationVM @Inject constructor(
    private val locationService: LocationService,
    private val routeService: RouteService,
) : BaseViewModel() {

    data class State(
        val destination: LatLng? = null,
        val origin: LatLng? = null,
        val myLocation: MyLocationData? = null,
        val cameraPosition: LatLng? = null,
        val traveledPath: List<LatLng> = emptyList(),
        val navigationPath: List<LatLng> = emptyList(),
        val isNavigating: Boolean = false,
        val isTripEnded: Boolean = false,
        val tripStartTime: Date = Date(),
        val tripEndTime: Date = Date(),
        val animatesToMyLocation: Boolean = true,
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
            is Event.UpdateDestination -> updateDestination(event.value)
            is Event.StartNavigation -> startNavigation()
            is Event.StopNavigation -> stopNavigation()
            is Event.OnMapLoaded -> onMapLoaded()
            else -> Unit
        }
    }

    private fun onMapLoaded() {
        launch { monitorLocation() }
    }

    private suspend fun monitorLocation() {
        locationService.monitorLocation().collect { location ->
            if (state.isNavigating) {
                val destination = state.destination!!
                state = state.copy(traveledPath = state.traveledPath + location.toLatLng())
                if (location.toLatLng().distanceTo(destination) < 10) {
                    stopNavigation()
                }
            } else {
                state = state.copy(
                    myLocation = location.toMyLocation(),
                    animatesToMyLocation = state.myLocation == null,
                )
            }
        }
    }

    private var routePlanJob: Job = Job()

    private fun updateDestination(destination: LatLng) {
        val origin = state.myLocation!!.toLatLng()
        state = state.copy(
            destination = destination,
            origin = origin,
            navigationPath = emptyList(),
        )
        routePlanJob.cancel()
        routePlanJob = launch {
            val routes = routeService.walkingRoutes(origin, destination)
            require(routes.routeLines.isNotEmpty()) { "No available route line!" }
            val navigationPath = routes.routeLines.first().wayPoints
            Log.d(TAG, "navigationPath: $navigationPath")
            state = state.copy(navigationPath = navigationPath)
        }
    }

    private var navigationJob: Job = Job()

    private fun startNavigation() {
        val destination = state.destination!!
        val origin = state.myLocation!!.toLatLng()
        Log.d(TAG, "startNavigation: $destination")
        state = state.copy(
            isNavigating = true,
            traveledPath = emptyList(),
            tripStartTime = Date(),
            origin = origin,
        )
        navigationJob.cancel()
        navigationJob = launch {
            val routes = routeService.walkingRoutes(origin, destination)
            require(routes.routeLines.isNotEmpty()) { "No available route line!" }
            val navigationPath = routes.routeLines.first().wayPoints
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