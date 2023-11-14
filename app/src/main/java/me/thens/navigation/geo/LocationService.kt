package me.thens.navigation.geo

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.google.maps.DirectionsApiRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import me.thens.navigation.core.app.Permission
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class LocationService @Inject constructor(
    private val context: Context,
    private val locationClient: FusedLocationProviderClient,
) {

    @SuppressLint("MissingPermission")
    fun monitorLocation(): Flow<Location> = callbackFlow {
        Permission.LOCATION.requireGranted(context)
        val request = LocationRequest.Builder(1000)
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .build()
        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation!!
                Log.d(TAG, "onLocationResult: $location")
                trySend(location)
            }
        }
        Log.d(TAG, "monitorLocationUpdate: START")
        locationClient.requestLocationUpdates(request, Dispatchers.IO.asExecutor(), callback)
        awaitClose {
            Log.d(TAG, "monitorLocationUpdate: END")
            locationClient.removeLocationUpdates(callback)
        }
    }

    @SuppressLint("MissingPermission")
    suspend fun getLocation(): Location {
        return suspendCancellableCoroutine { continuation ->
            Permission.LOCATION.requireGranted(context)
            val locationTask = locationClient.lastLocation
            locationTask.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    continuation.resume(task.result)
                } else {
                    continuation.resumeWithException(
                        RuntimeException(
                            "Get location failed",
                            task.exception
                        )
                    )
                }
            }
            continuation.invokeOnCancellation {
            }
        }
    }


    companion object {
        private const val TAG = "LocationService"
    }

}