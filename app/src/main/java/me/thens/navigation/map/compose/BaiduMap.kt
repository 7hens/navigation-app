package me.thens.navigation.map.compose

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import com.baidu.mapapi.map.BaiduMap
import com.baidu.mapapi.map.BaiduMap.OnMapClickListener
import com.baidu.mapapi.map.MapPoi
import com.baidu.mapapi.map.MapStatusUpdateFactory
import com.baidu.mapapi.map.MapView
import com.baidu.mapapi.map.MyLocationData
import com.baidu.mapapi.map.OverlayOptions
import com.baidu.mapapi.model.LatLng
import me.thens.navigation.core.app.LifecycleEffect
import me.thens.navigation.map.toLatLng

data class BaiduMapState(
    val isMyLocationEnabled: Boolean = false,
    val myLocation: MyLocationData? = null,
    val animatesToMyLocation: Boolean = false,
//    val mapStatus: MapStatus = MapStatus.Builder().build(),
    val overlays: List<OverlayOptions> = emptyList(),
    val onMapClick: (LatLng) -> Unit = {},
    val onMapPoiClick: (MapPoi) -> Unit = {},
    val onMapLoaded: () -> Unit = {},
)

@Composable
fun rememberBaiduMapState(): MutableState<BaiduMapState> {
    return remember { mutableStateOf(BaiduMapState()) }
}

@Composable
fun BaiduMap(
    modifier: Modifier = Modifier,
    state: BaiduMapState = BaiduMapState(),
) {
    val context = LocalContext.current
    val mapView = remember {
        MapView(context).apply {
            map.setMapStatus(MapStatusUpdateFactory.zoomTo(15F))
        }
    }
    AndroidView(
        modifier = modifier,
        factory = { mapView },
        onReset = { it.map.clear() },
    )
    MapLifecycle(mapView)
    MapOverlays(mapView, state.overlays)
    MapListeners(mapView, state)
    state.ApplyTo(mapView.map)
}

@Composable
private fun BaiduMapState.ApplyTo(map: BaiduMap) {
    val TAG = "BaiduMapApplyTo"
    LaunchedEffect(isMyLocationEnabled) { map.isMyLocationEnabled = isMyLocationEnabled }
    LaunchedEffect(myLocation) {
        if (animatesToMyLocation && myLocation != null) {
            Log.i(TAG, "ApplyTo: animateToMyLocation")
            map.animateMapStatus(MapStatusUpdateFactory.newLatLng(myLocation.toLatLng()))
        }
        map.setMyLocationData(myLocation)
    }
//    LaunchedEffect(mapStatus) {
//        map.animateMapStatus(MapStatusUpdateFactory.newMapStatus(mapStatus))
//    }
}

@Composable
private fun MapListeners(mapView: MapView, state: BaiduMapState) {
    val updatedState by rememberUpdatedState(state)
    LaunchedEffect(Unit) {
        val map = mapView.map
        map.setOnMapClickListener(object : OnMapClickListener {
            override fun onMapClick(p0: LatLng?) {
                updatedState.onMapClick(p0!!)
            }

            override fun onMapPoiClick(p0: MapPoi?) {
                updatedState.onMapPoiClick(p0!!)
            }
        })
        map.setOnMapLoadedCallback {
            updatedState.onMapLoaded()
        }
    }
}

@Composable
private fun MapOverlays(mapView: MapView, options: List<OverlayOptions>) {
    val TAG = "BaiduMapOverlay"
    DisposableEffect(options) {
        Log.d(TAG, "MapOverlays: $options")
        val overlays = mapView.map.addOverlays(options)
        onDispose {
            mapView.map.removeOverLays(overlays)
            mapView.overlay.clear()
        }
    }
}

@Composable
private fun MapLifecycle(mapView: MapView) {
    LifecycleEffect(mapView) { event ->
        when (event) {
            Lifecycle.Event.ON_RESUME -> mapView.onResume()
            Lifecycle.Event.ON_PAUSE -> mapView.onPause()
            Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
            else -> Unit
        }
    }
    DisposableEffect(mapView) {
        onDispose {
            mapView.onDestroy()
            mapView.removeAllViews()
        }
    }
}