package com.mobile.history

import com.mobile.network.Api
import com.mobile.network.ApiModule
import dagger.Module
import dagger.Provides
import io.realm.Realm
import io.realm.RealmConfiguration
import javax.inject.Singleton

@Module(includes = [ApiModule::class])
class HistoryModule {

    @Provides
    @Singleton
    @History
    fun provideRealmHistory(): Realm {
        return Realm.getInstance(RealmConfiguration.Builder()
                .name("History.Realm")
                .deleteRealmIfMigrationNeeded()
                .build())
    }

    @Provides
    @Singleton
    fun provideHistoryManager(@History realm: Realm, api: Api): HistoryManager {
        return HistoryManagerImpl(realm, api)
    }

}

