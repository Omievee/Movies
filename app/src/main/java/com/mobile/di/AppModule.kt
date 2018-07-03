package com.mobile.di

import com.mobile.analytics.AnalyticsManager
import com.mobile.analytics.AnalyticsManagerImpl
import com.mobile.history.HistoryModule
import com.mobile.home.RestrictionsManager
import com.mobile.location.LocationModule
import com.mobile.network.Api
import com.mobile.session.SessionManager
import com.mobile.session.SessionManagerImpl
import com.mobile.session.UserManager
import com.mobile.session.UserManagerImpl
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module(includes = [SharedPreferencesModule::class, HistoryModule::class, LocationModule::class, TicketModule::class])
class AppModule {

    @Provides
    @Singleton
    fun provideAnalyticsManager(): AnalyticsManager {
        return AnalyticsManagerImpl()
    }

    @Provides
    @Singleton
    fun restrictionManager() : RestrictionsManager {
        return RestrictionsManager()
    }

    @Provides
    @Singleton
    fun sessionManager(): SessionManager {
        return SessionManagerImpl()
    }

    @Provides
    @Singleton
    fun userManager(api:Api): UserManager {
        return UserManagerImpl(api)
    }

}