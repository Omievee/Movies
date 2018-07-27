package com.mobile.di

import com.mobile.analytics.AnalyticsManager
import com.mobile.analytics.AnalyticsManagerImpl
import com.mobile.application.Application
import com.mobile.gowatchit.GoWatchItManager
import com.mobile.gowatchit.GoWatchItModule
import com.mobile.history.HistoryModule
import com.mobile.home.RestrictionsManager
import com.mobile.keyboard.KeyboardManager
import com.mobile.keyboard.KeyboardManagerImpl
import com.mobile.location.LocationModule
import com.mobile.network.Api
import com.mobile.network.StaticApiModule
import com.mobile.session.SessionManager
import com.mobile.session.SessionManagerImpl
import com.mobile.session.UserManager
import com.mobile.session.UserManagerImpl
import com.mobile.upload.UploadModule
import com.mobile.ticketverification.BarcodeDetectorManager
import com.mobile.ticketverification.DetectedTextManager
import com.mobile.theater.TheaterModule
import com.mobile.theater.TheaterModuleUI
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module(includes = [StaticApiModule::class, TheaterModule::class, TheaterModuleUI::class, HistoryModule::class, LocationModule::class, TicketModule::class, GoWatchItModule::class, UploadModule::class])
class AppModule {

    @Provides
    @Singleton
    fun provideAnalyticsManager(goWatchItManager: GoWatchItManager): AnalyticsManager {
        return AnalyticsManagerImpl(goWatchItManager)
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

    @Provides
    @Singleton
    fun provideDetectedText(): DetectedTextManager {
        return DetectedTextManager()
    }

    @Provides
    @Singleton
    fun provideBarcodeManager(): BarcodeDetectorManager {
        return BarcodeDetectorManager()
    }

    @Provides
    @Singleton
    fun provideKeyboardManager(application: Application):KeyboardManager {
        return KeyboardManagerImpl(application)
    }

}