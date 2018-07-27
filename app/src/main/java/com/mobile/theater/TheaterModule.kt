package com.mobile.theater

import android.os.Environment
import com.mobile.application.Application
import com.mobile.location.LocationManager
import com.mobile.network.ApiModule
import com.mobile.network.StaticApi
import com.moviepass.BuildConfig
import dagger.Module
import dagger.Provides
import io.realm.Realm
import io.realm.RealmConfiguration
import java.io.File
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
        return Realm.getInstance(builder
                .build())
    }

    @Provides
    @Singleton
    fun provideHistoryManager(@TheaterScope realm: Provider<Realm>, api: StaticApi, locationManager: LocationManager): TheaterManager {
        return TheaterManagerImpl(api, realm, locationManager)
    }

}

