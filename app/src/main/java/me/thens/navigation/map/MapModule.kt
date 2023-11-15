package me.thens.navigation.map

import android.content.Context
import android.util.Log
import com.baidu.location.LocationClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import me.thens.navigation.map.service.RouteService
import me.thens.navigation.map.service.LocationService
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MapModule {
    private const val TAG = "MapModule"

    @Provides
    @Singleton
    fun provideLocationClient(@ApplicationContext context: Context) = run {
        Log.d(TAG, "provideLocationClient: ")
        LocationClient(context)
    }

    @Provides
    @Singleton
    fun provideLocationService(locationClient: LocationClient) =
        LocationService(locationClient)

    @Provides
    @Singleton
    fun provideRouteService() = RouteService()
}