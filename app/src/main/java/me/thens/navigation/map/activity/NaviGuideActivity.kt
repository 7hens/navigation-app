package me.thens.navigation.map.activity

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Message
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.baidu.mapapi.walknavi.WalkNavigateHelper
import com.baidu.mapapi.walknavi.adapter.IWNaviStatusListener
import com.baidu.mapapi.walknavi.adapter.IWRouteGuidanceListener
import com.baidu.mapapi.walknavi.model.IWRouteIconInfo
import com.baidu.mapapi.walknavi.model.RouteGuideKind
import com.baidu.platform.comapi.walknavi.WalkNaviModeSwitchListener
import dagger.hilt.android.AndroidEntryPoint
import me.thens.navigation.map.model.RouteLine
import me.thens.navigation.map.model.TripSummary
import me.thens.navigation.map.vm.NaviGuideVM

@AndroidEntryPoint
class NaviGuideActivity : AppCompatActivity() {
    private val naviHelper by lazy { WalkNavigateHelper.getInstance() }
    private val naviGuideVM by viewModels<NaviGuideVM>()
    private val data by lazy { getDataFromIntent() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startWalkNavi()
        naviGuideVM.tripSummary.observe(this) { showTripSummary(it) }
    }

    private fun startWalkNavi() {
        val view = naviHelper.onCreate(this)
        setContentView(view)
        naviHelper.setWalkNaviStatusListener(object : IWNaviStatusListener {
            override fun onWalkNaviModeChange(p0: Int, p1: WalkNaviModeSwitchListener?) {
                Log.d(TAG, "onWalkNaviModeChange: p0")
            }

            override fun onNaviExit() {
                Log.d(TAG, "onNaviExit: ")
                naviGuideVM.onEvent(NaviGuideVM.Event.QuitNavi)
            }
        })
        setRouteGuidanceListener()
        naviGuideVM.onEvent(NaviGuideVM.Event.StartNavi(data))
        naviHelper.startWalkNavi(this)
    }

    private fun setRouteGuidanceListener() {
        naviHelper.setRouteGuidanceListener(this, object : IWRouteGuidanceListener {
            override fun onRouteGuideIconInfoUpdate(p0: IWRouteIconInfo?) {
                Log.d(TAG, "onRouteGuideIconInfoUpdate: $p0")
            }

            override fun onRouteGuideIconUpdate(p0: Drawable?) {
                Log.d(TAG, "onRouteGuideIconUpdate: ")
            }

            override fun onRouteGuideKind(p0: RouteGuideKind?) {
                Log.d(TAG, "onRouteGuideKind: $p0")
            }

            override fun onRoadGuideTextUpdate(p0: CharSequence?, p1: CharSequence?) {
                Log.d(TAG, "onRoadGuideTextUpdate: $p0, $p1")
            }

            override fun onRemainDistanceUpdate(p0: CharSequence?) {
                Log.d(TAG, "onRemainDistanceUpdate: $p0")
            }

            override fun onRemainTimeUpdate(p0: CharSequence?) {
                Log.d(TAG, "onRemainTimeUpdate: $p0")
            }

            override fun onGpsStatusChange(p0: CharSequence?, p1: Drawable?) {
                Log.d(TAG, "onGpsStatusChange: $p0, $p1")
            }

            override fun onRouteFarAway(p0: CharSequence?, p1: Drawable?) {
                Log.d(TAG, "onRouteFarAway: $p0, $p1")
            }

            override fun onRoutePlanYawing(p0: CharSequence?, p1: Drawable?) {
                Log.d(TAG, "onRoutePlanYawing: $p0, $p1")
            }

            override fun onReRouteComplete() {
                Log.d(TAG, "onReRouteComplete: ")
            }

            override fun onArriveDest() {
                Log.d(TAG, "onArriveDest: ")
                naviGuideVM.onEvent(NaviGuideVM.Event.ArriveDestination)
            }

            override fun onIndoorEnd(p0: Message?) {
                Log.d(TAG, "onIndoorEnd: $p0")
            }

            override fun onFinalEnd(p0: Message?) {
                Log.d(TAG, "onFinalEnd: $p0")
            }

            override fun onVibrate() {
                Log.d(TAG, "onVibrate: ")
            }

        })
    }

    private fun showTripSummary(tripSummary: TripSummary) {
        finish()
        TripSummaryActivity.start(this, tripSummary)
    }

    override fun onResume() {
        super.onResume()
        naviHelper.resume()
    }

    override fun onPause() {
        super.onPause()
        naviHelper.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        naviHelper.quit()
    }

    private fun getDataFromIntent(): RouteLine {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(KEY_DATA, RouteLine::class.java)!!
        } else {
            intent.getParcelableExtra(KEY_DATA)!!
        }
    }

    companion object {
        private const val TAG = "NaviGuideActivity"
        private const val KEY_DATA = "data"

        fun start(context: Context, data: RouteLine) {
            val intent = Intent(context, NaviGuideActivity::class.java).apply {
                putExtra(KEY_DATA, data)
            }
            context.startActivity(intent)
        }
    }
}