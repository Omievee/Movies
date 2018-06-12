package com.mobile.location

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
    fun provideFusedLocation(application: Application): FusedLocationProviderClient {
        return LocationServices.getFusedLocationProviderClient(application)
    }

    @Provides
    @Singleton
    fun provideLocationManager(fused:FusedLocationProviderClient) : LocationManager {
        return LocationManagerImpl(fused)
    }
}