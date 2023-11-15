package me.thens.navigation.map.activity

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.baidu.mapapi.map.BitmapDescriptorFactory
import com.baidu.mapapi.map.MapStatusUpdateFactory
import com.baidu.mapapi.map.MapView
import com.baidu.mapapi.map.MarkerOptions
import com.baidu.mapapi.map.PolylineOptions
import com.baidu.mapapi.model.LatLngBounds
import me.thens.navigation.R
import me.thens.navigation.databinding.ActivityTripSummaryBinding
import me.thens.navigation.map.model.TripSummary
import me.thens.navigation.map.util.toLatLng

class TripSummaryActivity : AppCompatActivity() {
    private lateinit var v: ActivityTripSummaryBinding
    private val data by lazy { getDataFromIntent() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        v = ActivityTripSummaryBinding.inflate(layoutInflater)
        setContentView(v.root)
        initBaiduMap(v.mapView)
        v.distance.text = String.format("%.0fm", data.distance)
        v.duration.text = toDurationText(data.duration / 1000)
        v.back.setOnClickListener { finish() }
    }

    private fun toDurationText(seconds: Long): String {
        return String.format("%02d:%02d", seconds / 60, seconds % 60)
    }

    private fun initBaiduMap(mapView: MapView) {
        val map = mapView.map
        map.setOnMapLoadedCallback {
            showOverlays(mapView)
            updateMapStatus(mapView)
        }
    }

    private fun updateMapStatus(mapView: MapView) {
        val p = 400
        val bounds = LatLngBounds.Builder()
            .include(data.navigationPoints.map { it.toLatLng() })
            .build()
        val statusUpdate = MapStatusUpdateFactory.newLatLngZoom(bounds, p, p, p, p)
        mapView.map.animateMapStatus(statusUpdate)
    }

    private fun showOverlays(mapView: MapView) {
        mapView.map.addOverlay(
            MarkerOptions()
                .position(data.origin.toLatLng())
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_start))
        )
        mapView.map.addOverlay(
            MarkerOptions()
                .position(data.destination.toLatLng())
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_end))
        )
        mapView.map.addOverlay(
            PolylineOptions()
                .points(data.navigationPoints.map { it.toLatLng() })
                .width(20)
                .color(Color.BLUE)
        )
        mapView.map.addOverlay(
            PolylineOptions()
                .points(data.traveledPoints.map { it.toLatLng() })
                .width(10)
                .color(Color.GREEN)
        )
    }

    private fun getDataFromIntent(): TripSummary {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(KEY_DATA, TripSummary::class.java)!!
        } else {
            intent.getParcelableExtra(KEY_DATA)!!
        }
    }

    companion object {
        private const val TAG = "TripSummaryActivity"
        private const val KEY_DATA = "data"

        fun start(context: Context, data: TripSummary) {
            val intent = Intent(context, TripSummaryActivity::class.java).apply {
                putExtra(KEY_DATA, data)
            }
            context.startActivity(intent)
        }
    }
}