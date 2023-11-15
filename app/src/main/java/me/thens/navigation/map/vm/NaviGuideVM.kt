package me.thens.navigation.map.vm

import androidx.lifecycle.MutableLiveData
import com.baidu.mapapi.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import me.thens.navigation.core.app.BaseViewModel
import me.thens.navigation.map.model.RouteLine
import me.thens.navigation.map.model.TripSummary
import me.thens.navigation.map.service.LocationService
import me.thens.navigation.map.util.distanceTo
import me.thens.navigation.map.util.toLatLng
import me.thens.navigation.map.util.toLocPoint
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class NaviGuideVM @Inject constructor(
    private val locationService: LocationService,
) : BaseViewModel() {
    private val startTime = MutableLiveData<Date>()
    private val routeLine = MutableLiveData<RouteLine>()
    val tripSummary = MutableLiveData<TripSummary>()
    private val traveledPoints = MutableLiveData<List<LatLng>>(emptyList())

    sealed interface Event {
        class StartNavi(val value: RouteLine) : Event
        object ArriveDestination : Event
        object QuitNavi : Event
    }

    fun onEvent(event: Event) {
        when (event) {
            is Event.StartNavi -> startNavi(event.value)
            Event.ArriveDestination, Event.QuitNavi -> finishNavi()
            else -> Unit
        }
    }

    private var naviJob: Job = Job()

    private fun startNavi(routeLineVal: RouteLine) {
        startTime.value = Date()
        routeLine.value = routeLineVal
        naviJob.cancel()
        naviJob = launch { monitorLocation() }
    }

    private fun finishNavi() {
        naviJob.cancel()
        val endTime = Date()
        val routeLineVal = routeLine.value!!
        tripSummary.value = TripSummary(
            origin = routeLineVal.origin,
            destination = routeLineVal.destination,
            navigationPoints = routeLineVal.points,
            traveledPoints = traveledPoints.value!!.map { it.toLocPoint() },
            duration = endTime.time - startTime.value!!.time,
            distance = computeDistance(traveledPoints.value!!)
        )
    }

    private suspend fun monitorLocation() {
        locationService.monitorLocation().collect { location ->
            traveledPoints.value = traveledPoints.value!! + location.toLatLng()
        }
    }

    private fun computeDistance(line: List<LatLng>): Double {
        return line.zipWithNext { a, b -> a.distanceTo(b) }.sum()
    }
}