package me.thens.navigation.map.activity

import android.graphics.Color
import android.os.Bundle
import android.util.Log
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
import com.baidu.mapapi.walknavi.WalkNavigateHelper
import com.baidu.mapapi.walknavi.adapter.IWEngineInitListener
import com.baidu.mapapi.walknavi.adapter.IWRoutePlanListener
import com.baidu.mapapi.walknavi.model.WalkRoutePlanError
import com.baidu.mapapi.walknavi.params.WalkNaviLaunchParam
import com.baidu.mapapi.walknavi.params.WalkRouteNodeInfo
import dagger.hilt.android.AndroidEntryPoint
import me.thens.navigation.R
import me.thens.navigation.core.app.Permission
import me.thens.navigation.databinding.ActivityHomeBinding
import me.thens.navigation.map.model.RouteLine
import me.thens.navigation.map.util.toLatLng
import me.thens.navigation.map.util.toLocPoint
import me.thens.navigation.map.vm.HomeVM

@AndroidEntryPoint
class HomeActivity : AppCompatActivity() {
    private lateinit var v: ActivityHomeBinding
    private val locationPermission = Permission.LOCATION.register(this)
    private val homeVM by viewModels<HomeVM>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        locationPermission.check()
        v = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(v.root)
        initBaiduMap(v.mapView)
        v.actionButton.setOnClickListener {
            initWalkNavi {
                startNavigation(homeVM.origin.value!!, homeVM.destination.value!!)
            }
        }
        homeVM.destination.observe(this) { updateDestination(it) }
        homeVM.myLocation.observe(this) { updateMyLocation(it) }
        homeVM.isNavigatable.observe(this) { updateNavigatable(it) }
        homeVM.navigationPath.observe(this) { updateNavigationPath(it) }
    }

    private var destinationOverlay: Overlay? = null

    private fun updateDestination(destination: LatLng) {
        destinationOverlay?.remove()
        destinationOverlay = v.mapView.map.addOverlay(
            MarkerOptions()
                .position(destination)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_end))
        )
    }

    private var isFirstMyLocation = true

    private fun updateMyLocation(myLocation: MyLocationData) {
        v.mapView.map.setMyLocationData(myLocation)
        if (isFirstMyLocation) {
            v.mapView.map.run {
                animateMapStatus(MapStatusUpdateFactory.newLatLng(myLocation.toLatLng()))
                animateMapStatus(MapStatusUpdateFactory.zoomTo(15F))
            }
        }
        isFirstMyLocation = false
    }

    private fun updateNavigatable(isNavigatable: Boolean) {
        v.actionButton.run {
            isEnabled = isNavigatable
            text = when (homeVM.destination.value) {
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
        navigationPathOverlay = v.mapView.map.addOverlay(
            PolylineOptions()
                .points(navigationPath)
                .width(10)
                .color(Color.BLUE)
        )
    }

    private fun initBaiduMap(mapView: MapView) {
        val map = mapView.map
        map.isMyLocationEnabled = locationPermission.isGranted()
        map.setOnMapClickListener(object : BaiduMap.OnMapClickListener {
            override fun onMapClick(p0: LatLng?) {
                homeVM.onEvent(HomeVM.Event.UpdateDestination(p0!!))
            }

            override fun onMapPoiClick(p0: MapPoi?) {
            }
        })
        map.setOnMapLoadedCallback {
            homeVM.onEvent(HomeVM.Event.OnMapLoaded)
        }
        v.relocationButton.setOnClickListener {
            val myLocation = homeVM.myLocation.value ?: return@setOnClickListener
            map.animateMapStatus(MapStatusUpdateFactory.newLatLng(myLocation.toLatLng()))
        }
    }

    private fun initWalkNavi(onInitSuccess: () -> Unit) {
        if (WalkNavigateHelper.getInstance().isInit) {
            onInitSuccess()
            return
        }
        WalkNavigateHelper.getInstance().initNaviEngine(this, object : IWEngineInitListener {
            override fun engineInitSuccess() {
                Log.d(TAG, "engineInitSuccess: ")
                onInitSuccess()
            }

            override fun engineInitFail() {
                Log.e(TAG, "engineInitFail: ")
                WalkNavigateHelper.getInstance().unInitNaviEngine()
            }
        })
    }

    private fun startNavigation(start: LatLng, end: LatLng) {
        val params = WalkNaviLaunchParam().apply {
            startNodeInfo(WalkRouteNodeInfo().apply { location = start })
            endNodeInfo(WalkRouteNodeInfo().apply { location = end })
        }
        WalkNavigateHelper.getInstance()
            .routePlanWithRouteNode(params, object : IWRoutePlanListener {
                override fun onRoutePlanStart() {
                }

                override fun onRoutePlanSuccess() {
                    NaviGuideActivity.start(
                        this@HomeActivity, RouteLine(
                            origin = start.toLocPoint(),
                            destination = end.toLocPoint(),
                            points = homeVM.navigationPath.value!!.map { it.toLocPoint() },
                        )
                    )
                }

                override fun onRoutePlanFail(p0: WalkRoutePlanError?) {
                }
            })

    }

    override fun onResume() {
        super.onResume()
        v.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        v.mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        v.mapView.onDestroy()
    }

    companion object {
        private const val TAG = "HomeActivity"
    }
}