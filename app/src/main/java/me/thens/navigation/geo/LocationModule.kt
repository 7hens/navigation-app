package me.thens.navigation.geo

import android.content.Context
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.maps.GeoApiContext
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import me.thens.navigation.BuildConfig
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class LocationModule {
    @Provides
    @Singleton
    fun provideLocationClient(@ApplicationContext context: Context) =
        LocationServices.getFusedLocationProviderClient(context)

    @Provides
    @Singleton
    fun provideLocationService(@ApplicationContext context: Context, locationClient: FusedLocationProviderClient) =
        LocationService(context, locationClient)

    @Provides
    @Singleton
    fun provideGeoApiContext() =
        GeoApiContext.Builder()
            .apiKey(BuildConfig.GOOGLE_MAPS_API_KEY)
            .build()
}