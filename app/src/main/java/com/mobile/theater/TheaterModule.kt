package com.mobile.theater

import com.mobile.location.LocationManager
import com.mobile.network.ApiModule
import com.mobile.network.StaticApi
import dagger.Module
import dagger.Provides
import io.realm.Realm
import io.realm.RealmConfiguration
import javax.inject.Provider
import javax.inject.Singleton

@Module(includes = [ApiModule::class])
class TheaterModule {

    @Provides
    @TheaterScope
    fun provideRealmTheater(): Realm {
        return Realm.getInstance(RealmConfiguration.Builder()
                .name("Theater.Realm")
                .deleteRealmIfMigrationNeeded()
                .build())
    }

    @Provides
    @Singleton
    fun provideHistoryManager(@TheaterScope realm: Provider<Realm>, api: StaticApi, locationManager: LocationManager): TheaterManager {
        return TheaterManagerImpl(api, realm, locationManager)
    }

}

