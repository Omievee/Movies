package com.mobile.history

import com.mobile.network.Api
import com.mobile.network.ApiModule
import com.mobile.session.SessionManager
import dagger.Module
import dagger.Provides
import io.realm.Realm
import io.realm.RealmConfiguration
import javax.inject.Provider
import javax.inject.Singleton

@Module(includes = [ApiModule::class])
class HistoryModule {

    @Provides
    @History
    fun provideRealmHistory(): Realm {
        return Realm.getInstance(RealmConfiguration.Builder()
                .name("History.Realm")
                .deleteRealmIfMigrationNeeded()
                .build())
    }

    @Provides
    @Singleton
    fun provideHistoryManager(@History realm: Provider<Realm>, api: Api, sessionManager: SessionManager): HistoryManager {
        return HistoryManagerImpl(
                realmHistory = realm,
                api = api,
                sessionManager = sessionManager)
    }
}

