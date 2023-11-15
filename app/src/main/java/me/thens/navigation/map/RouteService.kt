package me.thens.navigation.map

import android.util.Log
import com.baidu.mapapi.model.LatLng
import com.baidu.mapapi.search.route.BikingRouteResult
import com.baidu.mapapi.search.route.DrivingRouteResult
import com.baidu.mapapi.search.route.IndoorRouteResult
import com.baidu.mapapi.search.route.MassTransitRouteResult
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener
import com.baidu.mapapi.search.route.PlanNode
import com.baidu.mapapi.search.route.RoutePlanSearch
import com.baidu.mapapi.search.route.TransitRouteResult
import com.baidu.mapapi.search.route.WalkingRoutePlanOption
import com.baidu.mapapi.search.route.WalkingRouteResult
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume

class RouteService @Inject constructor(
) {
    suspend fun walkingRoutes(origin: LatLng, destination: LatLng): WalkingRouteResult {
        return suspendCancellableCoroutine { continuation ->
            val routeSearch = RoutePlanSearch.newInstance()
            val listener = object : OnGetRoutePlanResultListener {
                override fun onGetWalkingRouteResult(p0: WalkingRouteResult?) {
                    val result = p0!!
                    Log.d(TAG, "onGetWalkingRouteResult: $result")
                    continuation.resume(result)
                }

                override fun onGetTransitRouteResult(p0: TransitRouteResult?) {
                }

                override fun onGetMassTransitRouteResult(p0: MassTransitRouteResult?) {
                }

                override fun onGetDrivingRouteResult(p0: DrivingRouteResult?) {
                }

                override fun onGetIndoorRouteResult(p0: IndoorRouteResult?) {
                }

                override fun onGetBikingRouteResult(p0: BikingRouteResult?) {
                }
            }
            routeSearch.setOnGetRoutePlanResultListener(listener)
            routeSearch.walkingSearch(
                WalkingRoutePlanOption()
                    .from(PlanNode.withLocation(origin))
                    .to(PlanNode.withLocation(destination))
            )
            Log.d(TAG, "walkingRoute: START")
            continuation.invokeOnCancellation {
                Log.d(TAG, "walkingRoute: END")
                routeSearch.destroy()
            }
        }
    }

    companion object {
        private const val TAG = "RouteService"
    }
}