package me.thens.navigation.map.compose

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.drawable.Drawable
import android.os.Message
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import com.baidu.mapapi.model.LatLng
import com.baidu.mapapi.walknavi.WalkNavigateHelper
import com.baidu.mapapi.walknavi.adapter.IWEngineInitListener
import com.baidu.mapapi.walknavi.adapter.IWRouteGuidanceListener
import com.baidu.mapapi.walknavi.adapter.IWRoutePlanListener
import com.baidu.mapapi.walknavi.model.IWRouteIconInfo
import com.baidu.mapapi.walknavi.model.RouteGuideKind
import com.baidu.mapapi.walknavi.model.WalkRoutePlanError
import com.baidu.mapapi.walknavi.params.WalkNaviLaunchParam
import com.baidu.mapapi.walknavi.params.WalkRouteNodeInfo
import me.thens.navigation.core.app.LifecycleEffect
import me.thens.navigation.core.app.getActivity

data class WalkNaviState(
    val startPoint: LatLng,
    val endPoint: LatLng,
    val onInitSuccess: () -> Unit = {},
    val onInitFailed: () -> Unit = {},
    val onNaviStart: () -> Unit = {},
    val onArriveDest: () -> Unit = {},
)

@SuppressLint("UnrememberedMutableState")
@Composable
fun WalkNavi(modifier: Modifier = Modifier, state: WalkNaviState) {
    val naviHelper = WalkNavigateHelper.getInstance()
    val context = LocalContext.current
    val activity = context.getActivity()!!
    WalkNaviInit(activity, naviHelper, state)
    AndroidView(factory = { naviHelper.onCreate(activity) }, modifier = modifier)
    WalkNaviLifecycle(naviHelper)
    WalkNaviStart(
        activity = activity,
        naviHelper = naviHelper,
        start = state.startPoint,
        end = state.endPoint,
        onNaviStart = state.onNaviStart,
    )
}

@Composable
private fun WalkNaviInit(activity: Activity, naviHelper: WalkNavigateHelper, state: WalkNaviState) {
    val TAG = "WalkNaviInit"
    val updatedState by rememberUpdatedState(state)
    LaunchedEffect(Unit) {
        Log.d(TAG, "WalkNaviInit: START")
        naviHelper.initNaviEngine(activity, object : IWEngineInitListener {
            override fun engineInitSuccess() {
                Log.d(TAG, "engineInitSuccess: ")
                updatedState.onInitSuccess()
            }

            override fun engineInitFail() {
                Log.d(TAG, "engineInitFail: ")
                updatedState.onInitFailed()
            }
        })
        naviHelper.setRouteGuidanceListener(activity, object : IWRouteGuidanceListener {
            override fun onRouteGuideIconInfoUpdate(p0: IWRouteIconInfo?) {
            }

            override fun onRouteGuideIconUpdate(p0: Drawable?) {
            }

            override fun onRouteGuideKind(p0: RouteGuideKind?) {
            }

            override fun onRoadGuideTextUpdate(p0: CharSequence?, p1: CharSequence?) {
            }

            override fun onRemainDistanceUpdate(p0: CharSequence?) {
            }

            override fun onRemainTimeUpdate(p0: CharSequence?) {
            }

            override fun onGpsStatusChange(p0: CharSequence?, p1: Drawable?) {
            }

            override fun onRouteFarAway(p0: CharSequence?, p1: Drawable?) {
            }

            override fun onRoutePlanYawing(p0: CharSequence?, p1: Drawable?) {
            }

            override fun onReRouteComplete() {
            }

            override fun onArriveDest() {
                updatedState.onArriveDest()
            }

            override fun onIndoorEnd(p0: Message?) {
            }

            override fun onFinalEnd(p0: Message?) {
            }

            override fun onVibrate() {
            }
        })
    }
}

@Composable
private fun WalkNaviLifecycle(naviHelper: WalkNavigateHelper) {
    LifecycleEffect(Unit) { event ->
        when (event) {
            Lifecycle.Event.ON_RESUME -> naviHelper.resume()
            Lifecycle.Event.ON_PAUSE -> naviHelper.pause()
            Lifecycle.Event.ON_DESTROY -> naviHelper.quit()
            else -> Unit
        }
    }
}

@Composable
private fun WalkNaviStart(
    activity: Activity,
    naviHelper: WalkNavigateHelper,
    start: LatLng,
    end: LatLng,
    onNaviStart: () -> Unit,
) {
    val TAG = "WalkNaviStart"
    LaunchedEffect(start, end, naviHelper.isInit) {
        if (!naviHelper.isInit) {
            return@LaunchedEffect
        }
        val params = WalkNaviLaunchParam().apply {
            startNodeInfo(WalkRouteNodeInfo().apply { location = start })
            endNodeInfo(WalkRouteNodeInfo().apply { location = end })
        }
        Log.d(TAG, "WalkNaviStart: START")
        naviHelper.routePlanWithRouteNode(params, object : IWRoutePlanListener {
            override fun onRoutePlanStart() {
                Log.i(TAG, "onRoutePlanStart: ")
            }

            override fun onRoutePlanSuccess() {
                Log.i(TAG, "onRoutePlanSuccess: ")
                naviHelper.startWalkNavi(activity)
                onNaviStart()
            }

            override fun onRoutePlanFail(p0: WalkRoutePlanError?) {
                Log.e(TAG, "onRoutePlanFail: $p0")
            }
        })
    }
}