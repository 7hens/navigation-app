package me.thens.navigation.map

import android.annotation.SuppressLint
import android.util.Log
import com.baidu.location.BDAbstractLocationListener
import com.baidu.location.BDLocation
import com.baidu.location.LocationClient
import com.baidu.location.LocationClientOption
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class LocationService @Inject constructor(
    private val locationClient: LocationClient,
) {

    @SuppressLint("MissingPermission")
    fun monitorLocation(): Flow<BDLocation> = callbackFlow {
        val listener = object : BDAbstractLocationListener() {
            override fun onReceiveLocation(location: BDLocation) {
                try {
                    Log.d(TAG, "onLocationResult: $location")
                    trySend(location)
                } catch (e: Throwable) {
                    close(e)
                }
            }
        }
        locationClient.locOption = LocationClientOption().apply {
            openGps = true
            scanSpan = 1000
            locationMode = LocationClientOption.LocationMode.Hight_Accuracy
            coorType = "bd09ll"
        }
        locationClient.registerLocationListener(listener)
        locationClient.start()
        Log.d(TAG, "monitorLocationUpdate: START")
        awaitClose {
            Log.d(TAG, "monitorLocationUpdate: END")
            locationClient.unRegisterLocationListener(listener)
        }
    }

    @SuppressLint("MissingPermission")
    suspend fun getLocation(): BDLocation {
        return monitorLocation().first()
    }

    companion object {
        private const val TAG = "LocationService"
    }

}