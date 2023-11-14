package me.thens.navigation.geo

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.android.material.navigation.NavigationView
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapEffect
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import me.thens.navigation.core.app.Permission
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
    val isPermissionGranted by Permission.LOCATION.rememberGranted()
    val cameraPositionState = rememberCameraPositionState()
    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            onMapClick = { onEvent(NavigationVM.Event.UpdateDestination(it)) },
            properties = MapProperties(isMyLocationEnabled = isPermissionGranted),
        ) {
            MapEffect(isPermissionGranted) { map ->
                map.setOnMapLoadedCallback { onEvent(NavigationVM.Event.OnMapLoaded) }
            }
            state.destination?.let { destination ->
                Marker(
                    state = MarkerState(position = destination),
                    title = "Target",
                )
            }
            Polyline(points = state.navigationPath)
            Polyline(points = state.traveledPath)
        }
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
@Preview(showBackground = true)
fun NavigationViewPreview() {
    val state = NavigationVM.State()
    AppTheme {
        NavigationScreen(state) { }
    }
}