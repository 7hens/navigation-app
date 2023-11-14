package me.thens.navigation

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineExceptionHandler
import me.thens.navigation.core.app.showToast
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideSharedPreferences(app: Application): SharedPreferences {
        return app.getSharedPreferences("main", Context.MODE_PRIVATE)
    }

    @Provides
    @Singleton
    fun providesCoroutineExceptionHandler(app: Application): CoroutineExceptionHandler {
        return CoroutineExceptionHandler { _, throwable ->
            Log.e("ERROR", "Coroutine Exception", throwable)
            app.showToast(throwable.message ?: "Unknown error")
        }
    }
}