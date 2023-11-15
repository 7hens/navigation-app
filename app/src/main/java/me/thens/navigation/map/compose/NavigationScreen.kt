package me.thens.navigation.map.compose

import android.graphics.Color
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.baidu.mapapi.map.BitmapDescriptorFactory
import com.baidu.mapapi.map.MarkerOptions
import com.baidu.mapapi.map.PolylineOptions
import com.baidu.mapapi.model.LatLng
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import me.thens.navigation.R
import me.thens.navigation.core.app.Permission
import me.thens.navigation.map.NavigationVM
import me.thens.navigation.ui.theme.AppTheme

@Composable
fun NavigationScreen() {
    hiltViewModel<NavigationVM>().run {
        NavigationScreen(state) { handle(it) }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun NavigationScreen(
    state: NavigationVM.State,
    onEvent: (NavigationVM.Event) -> Unit,
) {
    Permission.LOCATION.check()
//    Permission.STORAGE.check()
    Box(modifier = Modifier.fillMaxSize()) {
        BaiduNavigation(state = state, onEvent = onEvent)
        Box(modifier = Modifier.align(Alignment.BottomCenter)) {
            when {
                state.destination == null -> {
                    Button(onClick = { }, enabled = false) {
                        Text(text = "Select Destination")
                    }
                }

                state.isNavigating -> {
                    Button(onClick = { onEvent(NavigationVM.Event.StopNavigation) }) {
                        Text(text = "Stop Navigation")
                    }
                }

                else -> {
                    Button(onClick = { onEvent(NavigationVM.Event.StartNavigation) }) {
                        Text(text = "Start Navigation")
                    }
                }
            }
        }
    }
}

@Composable
private fun BaiduNavigation(state: NavigationVM.State, onEvent: (NavigationVM.Event) -> Unit) {
    val isPermissionGranted by Permission.LOCATION.rememberGranted()
    if (state.isNavigating) {
        WalkNavi(
            state = WalkNaviState(
                startPoint = state.origin!!,
                endPoint = state.destination!!,
            )
        )
    } else {
        var mapState by rememberBaiduMapState()
        val overlays = listOfNotNull(
            polylineOptions(state.navigationPath, Color.BLUE),
            polylineOptions(state.traveledPath, Color.GREEN),
            state.destination?.let {
                MarkerOptions()
                    .position(it)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_end))
            },
        )
        mapState = mapState.copy(
            isMyLocationEnabled = isPermissionGranted,
            myLocation = state.myLocation,
            onMapClick = { onEvent(NavigationVM.Event.UpdateDestination(it)) },
            overlays = overlays,
            onMapLoaded = { onEvent(NavigationVM.Event.OnMapLoaded) },
            animatesToMyLocation = state.animatesToMyLocation,
        )
        BaiduMap(
            modifier = Modifier.fillMaxSize(),
            state = mapState,
        )
    }
}

private fun polylineOptions(points: List<LatLng>, color: Int): PolylineOptions? {
    if (points.size < 2) {
        return null
    }
    return PolylineOptions().points(points).color(color)
}

@Composable
@Preview(showBackground = true)
fun NavigationViewPreview() {
    val state = NavigationVM.State()
    AppTheme {
        NavigationScreen(state) { }
    }
}