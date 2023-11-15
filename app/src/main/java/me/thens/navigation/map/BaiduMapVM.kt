package me.thens.navigation.map

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import com.baidu.ar.it
import com.baidu.mapapi.map.MyLocationData
import com.baidu.mapapi.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import me.thens.navigation.core.app.BaseViewModel
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class BaiduMapVM @Inject constructor(
    private val locationService: LocationService,
    private val routeService: RouteService,
) : BaseViewModel() {
    val destination = MutableLiveData<LatLng>()
    val origin = MutableLiveData<LatLng>()
    val myLocation = MutableLiveData<MyLocationData>()
    val traveledPath = MutableLiveData(emptyList<LatLng>())
    val navigationPath = MutableLiveData(emptyList<LatLng>())
    val isNavigatable = navigationPath.map { it.size > 2 }
    val isTripEnded = MutableLiveData(false)
    val tripStartTime = MutableLiveData<Date>()
    val tripEndTime = MutableLiveData<Date>()

    sealed interface Event {
        class UpdateDestination(val value: LatLng) : Event
        object OnMapLoaded : Event
        object StartNavigation : Event
        object StopNavigation : Event
    }

    fun onEvent(event: Event) {
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
            if (isNavigatable.value == true) {
                val destination = destination.value!!
                traveledPath.value = traveledPath.value!! + location.toLatLng()
                if (location.toLatLng().distanceTo(destination) < 10) {
                    stopNavigation()
                }
            } else {
                myLocation.value = location.toMyLocation()
            }
        }
    }

    private var navigationJob: Job = Job()

    private fun updateDestination(value: LatLng) {
        val originVal = myLocation.value!!.toLatLng()
        destination.value = value
        navigationPath.value = emptyList()
        navigationJob.cancel()
        navigationJob = launch {
            val routes = routeService.walkingRoutes(originVal, value)
            require(routes.routeLines.isNotEmpty()) { "No available route line!" }
            val navigationPoints = routes.routeLines.first().wayPoints
            Log.d(TAG, "startNavigation: $navigationPoints")
            navigationPath.value = navigationPoints
        }
    }

    private fun startNavigation() {
        val destinationVal = destination.value!!
        Log.d(TAG, "startNavigation: $destinationVal")
        traveledPath.value = emptyList()
        tripStartTime.value = Date()
        origin.value = myLocation.value!!.toLatLng()
    }

    private fun stopNavigation() {
        isTripEnded.value = true
        tripEndTime.value = Date()
        navigationJob.cancel()
    }

    companion object {
        private const val TAG = "NavigationVM"
    }
}