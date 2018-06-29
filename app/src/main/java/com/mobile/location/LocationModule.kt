package com.mobile.location

import android.content.Context
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.mobile.application.Application
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class LocationModule {

    @Provides
    @Singleton
    fun provideFusedLocation(application: Application): FusedLocationProviderClient? {
        try {
            return LocationServices.getFusedLocationProviderClient(application)
        } catch (e:Error) {
            e.printStackTrace()
            return null
        }
    }

    @Provides
    @Singleton
    fun provideSystemLocationManager(application: Application) : android.location.LocationManager {
        return application.getSystemService(Context.LOCATION_SERVICE) as android.location.LocationManager
    }

    @Provides
    @Singleton
    fun provideLocationManager(application: Application, systemLocationManager: android.location.LocationManager, fused:FusedLocationProviderClient?) : LocationManager {
        return LocationManagerImpl(application, systemLocationManager, fused)
    }
}