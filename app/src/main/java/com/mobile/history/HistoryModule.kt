package com.mobile.history

import com.mobile.db.AppDatabase
import com.mobile.db.ReservationDao
import com.mobile.network.Api
import com.mobile.network.ApiModule
import com.mobile.session.SessionManager
import com.mobile.theater.TheaterDao
import dagger.Module
import dagger.Provides
import javax.inject.Provider
import javax.inject.Singleton

@Module(includes = [ApiModule::class])
class HistoryModule {

    @Provides
    @Singleton
    fun provideTheaterDao(appDatabase: AppDatabase): ReservationDao {
        return appDatabase.reservationDao()
    }

    @Provides
    @Singleton
    fun provideHistoryManager(provider: Provider<ReservationDao>, api: Api, sessionManager: SessionManager): HistoryManager {
        return HistoryManagerImpl(
                dao = provider,
                api = api,
                sessionManager = sessionManager)
    }
}

