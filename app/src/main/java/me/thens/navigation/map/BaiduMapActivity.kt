package me.thens.navigation.map

import android.graphics.Color
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.baidu.mapapi.map.BaiduMap
import com.baidu.mapapi.map.BitmapDescriptorFactory
import com.baidu.mapapi.map.MapPoi
import com.baidu.mapapi.map.MapStatusUpdateFactory
import com.baidu.mapapi.map.MapView
import com.baidu.mapapi.map.MarkerOptions
import com.baidu.mapapi.map.MyLocationData
import com.baidu.mapapi.map.Overlay
import com.baidu.mapapi.map.PolylineOptions
import com.baidu.mapapi.model.LatLng
import dagger.hilt.android.AndroidEntryPoint
import me.thens.navigation.R
import me.thens.navigation.core.app.Permission
import me.thens.navigation.databinding.ActivityBaiduMapBinding

@AndroidEntryPoint
class BaiduMapActivity : AppCompatActivity() {
    private lateinit var b: ActivityBaiduMapBinding
    private val locationPermission = Permission.LOCATION.register(this)
    private val baiduMapVM by viewModels<BaiduMapVM>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        locationPermission.check()
        b = ActivityBaiduMapBinding.inflate(layoutInflater)
        setContentView(b.root)
        initBaiduMap(b.mapView)
        baiduMapVM.destination.observe(this) { updateDestination(it) }
        baiduMapVM.myLocation.observe(this) { updateMyLocation(it) }
        baiduMapVM.isNavigatable.observe(this) { updateNavigatable(it) }
        baiduMapVM.navigationPath.observe(this) { updateNavigationPath(it) }
    }

    private var destinationOverlay: Overlay? = null

    private fun updateDestination(destination: LatLng) {
        destinationOverlay?.remove()
        destinationOverlay = b.mapView.map.addOverlay(
            MarkerOptions()
                .position(destination)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_end))
        )
    }

    private var isFirstMyLocation = true

    private fun updateMyLocation(myLocation: MyLocationData) {
        b.mapView.map.setMyLocationData(myLocation)
        if (isFirstMyLocation) {
            b.mapView.map.run {
                animateMapStatus(MapStatusUpdateFactory.newLatLng(myLocation.toLatLng()))
                animateMapStatus(MapStatusUpdateFactory.zoomTo(15F))
            }
        }
        isFirstMyLocation = false
    }

    private fun updateNavigatable(isNavigatable: Boolean) {
        b.actionButton.run {
            isEnabled = isNavigatable
            text = when (baiduMapVM.destination.value) {
                null -> "Select Destination"
                else -> "Start navigation"
            }
        }
    }

    private var navigationPathOverlay: Overlay? = null

    private fun updateNavigationPath(navigationPath: List<LatLng>) {
        navigationPathOverlay?.remove()
        if (navigationPath.size < 2) {
            return
        }
        navigationPathOverlay = b.mapView.map.addOverlay(
            PolylineOptions()
                .points(navigationPath)
                .color(Color.BLUE)
        )
    }

    private fun initBaiduMap(mapView: MapView) {
        val map = mapView.map
        map.isMyLocationEnabled = locationPermission.isGranted()
        map.setOnMapClickListener(object : BaiduMap.OnMapClickListener {
            override fun onMapClick(p0: LatLng?) {
                baiduMapVM.onEvent(BaiduMapVM.Event.UpdateDestination(p0!!))
            }

            override fun onMapPoiClick(p0: MapPoi?) {
            }
        })
        map.setOnMapLoadedCallback {
            baiduMapVM.onEvent(BaiduMapVM.Event.OnMapLoaded)
        }
        b.relocationButton.setOnClickListener {
            val myLocation = baiduMapVM.myLocation.value ?: return@setOnClickListener
            map.animateMapStatus(MapStatusUpdateFactory.newLatLng(myLocation.toLatLng()))
        }
    }

    override fun onResume() {
        super.onResume()
        b.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        b.mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        b.mapView.onDestroy()
    }
}