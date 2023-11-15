package me.thens.navigation.map.vm

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.baidu.mapapi.map.MyLocationData
import com.baidu.mapapi.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import me.thens.navigation.core.app.BaseViewModel
import me.thens.navigation.map.service.LocationService
import me.thens.navigation.map.service.RouteService
import me.thens.navigation.map.util.toLatLng
import me.thens.navigation.map.util.toMyLocation
import me.thens.navigation.map.util.wayPoints
import javax.inject.Inject

@HiltViewModel
class HomeVM @Inject constructor(
    private val locationService: LocationService,
    private val routeService: RouteService,
) : BaseViewModel() {
    val destination = MutableLiveData<LatLng>()
    val origin = MutableLiveData<LatLng>()
    val myLocation = MutableLiveData<MyLocationData>()
    val navigationPath = MutableLiveData(emptyList<LatLng>())
    val isNavigatable = navigationPath.map { it.size > 2 }

    sealed interface Event {
        class UpdateDestination(val value: LatLng) : Event
        object OnMapLoaded : Event
    }

    fun onEvent(event: Event) {
        when (event) {
            is Event.UpdateDestination -> updateDestination(event.value)
            is Event.OnMapLoaded -> onMapLoaded()
            else -> Unit
        }
    }

    private fun onMapLoaded() {
        launch { monitorLocation() }
    }

    private suspend fun monitorLocation() {
        locationService.monitorLocation().collect { location ->
            myLocation.value = location.toMyLocation()
        }
    }

    private var navigationJob: Job = Job()

    private fun updateDestination(value: LatLng) {
        val originVal = myLocation.value!!.toLatLng()
        origin.value = originVal
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

    companion object {
        private const val TAG = "NavigationVM"
    }
}