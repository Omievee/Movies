package com.mobile.theater

import android.os.Environment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mobile.application.Application
import com.mobile.location.LocationManager
import com.mobile.model.AmcDmaMap
import com.mobile.network.ApiModule
import com.mobile.network.StaticApi
import com.moviepass.BuildConfig
import com.moviepass.R
import dagger.Module
import dagger.Provides
import io.realm.Realm
import io.realm.RealmConfiguration
import java.io.File
import java.io.InputStreamReader
import javax.inject.Provider
import javax.inject.Singleton

@Module(includes = [ApiModule::class])
class TheaterModule {

    @Provides
    @TheaterScope
    fun provideRealmTheater(application: Application): Realm {
        val builder = RealmConfiguration.Builder()
                .name("Theaters.Realm")
        if(BuildConfig.DEBUG) {
            builder.directory(application.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS))
        }
        builder.deleteRealmIfMigrationNeeded()
        return Realm.getInstance(builder
                .build())
    }

    @Provides
    @Singleton
    fun provideAmcDmaMap(application: Application, gson: Gson): AmcDmaMap {
        return try {
            return gson.fromJson<AmcDmaMap>(InputStreamReader(application.resources.openRawResource(R.raw.dmas)), AmcDmaMap::class.java)
        } catch (e:Exception) {
            AmcDmaMap()
        }
    }

    @Provides
    @Singleton
    fun provideTheaterManager(@TheaterScope realm: Provider<Realm>, api: StaticApi, locationManager: LocationManager, amcDmaMap: AmcDmaMap): TheaterManager {
        return TheaterManagerImpl(
                api = api,
                realm = realm,
                locationManager = locationManager,
                amcDmaMap = amcDmaMap
                )
    }

}

