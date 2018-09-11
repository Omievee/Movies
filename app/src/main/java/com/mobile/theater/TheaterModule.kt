package com.mobile.theater

import com.google.gson.Gson
import com.mobile.application.Application
import com.mobile.db.AppDatabase
import com.mobile.location.LocationManager
import com.mobile.model.AmcDmaMap
import com.mobile.network.ApiModule
import com.mobile.network.StaticApi
import com.moviepass.R
import dagger.Module
import dagger.Provides
import java.io.InputStreamReader
import javax.inject.Provider
import javax.inject.Singleton

@Module(includes = [ApiModule::class])
class TheaterModule {

    @Provides
    @Singleton
    fun provideTheaterDao(appDatabase: AppDatabase):TheaterDao {
        return appDatabase.theaterDao()
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
    fun provideTheaterManager(dao: Provider<TheaterDao>, api: StaticApi, locationManager: LocationManager, amcDmaMap: AmcDmaMap): TheaterManager {
        return TheaterManagerImpl(
                api = api,
                theaterDao = dao,
                locationManager = locationManager,
                amcDmaMap = amcDmaMap
                )
    }

}

