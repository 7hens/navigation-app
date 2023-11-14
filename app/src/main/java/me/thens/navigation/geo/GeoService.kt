package me.thens.navigation.geo

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.maps.DirectionsApiRequest
import com.google.maps.GeoApiContext
import com.google.maps.PendingResult
import com.google.maps.model.DirectionsResult
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class GeoService @Inject constructor(
    private val apiContext: GeoApiContext,
) {
    suspend fun calculateDirections(origin: LatLng, destination: LatLng): DirectionsResult {
        return suspendCallback {
            DirectionsApiRequest(apiContext)
                .alternatives(true)
                .origin(origin.toMapsLatLng())
                .destination(destination.toMapsLatLng())
        }
    }

    private suspend fun <T> suspendCallback(fn: () -> PendingResult<T>): T {
        return suspendCancellableCoroutine { continuation ->
            val request = fn()
            request.setCallback(object : PendingResult.Callback<T> {
                override fun onResult(result: T) {
                    Log.d(TAG, "onResult: $result")
                    continuation.resume(result)
                }

                override fun onFailure(e: Throwable) {
                    Log.e(TAG, "onFailure: ", e)
                    continuation.resumeWithException(e)
                }

            })
            continuation.invokeOnCancellation { request.cancel() }
        }
    }

    companion object {
        private const val TAG = "GeoService"
    }
}