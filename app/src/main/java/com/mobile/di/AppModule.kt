package com.mobile.di

import com.google.gson.Gson
import com.mobile.analytics.AnalyticsManager
import com.mobile.analytics.AnalyticsManagerImpl
import com.mobile.application.Application
import com.mobile.deeplinks.DeepLinksManager
import com.mobile.deeplinks.DeepLinksManagerImpl
import com.mobile.gowatchit.GoWatchItManager
import com.mobile.gowatchit.GoWatchItModule
import com.mobile.history.HistoryModule
import com.mobile.home.RestrictionsManager
import com.mobile.keyboard.KeyboardManager
import com.mobile.keyboard.KeyboardManagerImpl
import com.mobile.location.LocationModule
import com.mobile.movie.MoviesManager
import com.mobile.movie.MoviesManagerImpl
import com.mobile.network.Api
import com.mobile.network.BillingApi
import com.mobile.network.StaticApi
import com.mobile.network.StaticApiModule
import com.mobile.plans.PlansManager
import com.mobile.plans.PlansManagerImpl
import com.mobile.session.SessionManager
import com.mobile.session.SessionManagerImpl
import com.mobile.session.UserManager
import com.mobile.session.UserManagerImpl
import com.mobile.theater.TheaterManager
import com.mobile.theater.TheaterModule
import com.mobile.theater.TheaterModuleUI
import com.mobile.ticketverification.BarcodeDetectorManager
import com.mobile.ticketverification.DetectedTextManager
import com.mobile.upload.UploadModule
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module(includes = [StaticApiModule::class, TheaterModule::class, TheaterModuleUI::class, HistoryModule::class, LocationModule::class, TicketModule::class, GoWatchItModule::class, UploadModule::class, DatabaseModule::class])
class AppModule {

    @Provides
    @Singleton
    fun provideAnalyticsManager(context: Application, goWatchItManager: GoWatchItManager): AnalyticsManager {
        return AnalyticsManagerImpl(context, goWatchItManager)
    }

    @Provides
    @Singleton
    fun provideDeepLinkManager(context: Application, moviesManager: MoviesManager, theatersManager: TheaterManager): DeepLinksManager {
        return DeepLinksManagerImpl(context, moviesManager, theatersManager)
    }

    @Provides
    @Singleton
    fun providePlansManager(api:Api, billingApi: BillingApi): PlansManager {
        return PlansManagerImpl(api, billingApi)
    }


    @Provides
    @Singleton
    fun restrictionManager(): RestrictionsManager {
        return RestrictionsManager()
    }

    @Provides
    @Singleton
    fun sessionManager(): SessionManager {
        return SessionManagerImpl()
    }

    @Provides
    fun provideMoviesManager(api: StaticApi, gson: Gson, application: Application): MoviesManager {
        return MoviesManagerImpl(application, gson, api)
    }

    @Provides
    @Singleton
    fun userManager(api: Api, billingApi: BillingApi): UserManager {
        return UserManagerImpl(api = api, billingApi = billingApi)
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
    fun provideKeyboardManager(application: Application): KeyboardManager {
        return KeyboardManagerImpl(application)
    }

}