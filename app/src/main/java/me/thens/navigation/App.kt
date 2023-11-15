package me.thens.navigation

import android.app.Application
import android.util.Log
import com.baidu.location.LocationClient
import com.baidu.mapapi.SDKInitializer
import com.baidu.mapapi.walknavi.WalkNavigateHelper
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate: ")
        initBaiduMapSDK()
    }

    private fun initBaiduMapSDK() {
        LocationClient.setAgreePrivacy(true)
        SDKInitializer.setAgreePrivacy(this, true)
        SDKInitializer.initialize(this)
    }

    companion object {
        private const val TAG = "App"
    }
}