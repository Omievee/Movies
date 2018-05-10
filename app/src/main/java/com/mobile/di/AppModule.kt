package com.mobile.di

import com.mobile.analytics.AnalyticsManager
import com.mobile.analytics.AnalyticsManagerImpl
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AppModule {

    @Provides @Singleton fun provideAnalyticsManager() : AnalyticsManager {
        return AnalyticsManagerImpl()
    }

}