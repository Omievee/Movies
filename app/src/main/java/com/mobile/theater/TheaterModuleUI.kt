package com.mobile.theater

import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class TheaterModuleUI {

    @Provides
    @Singleton
    fun provideTheaterUIManager():TheaterUIManager {
        return TheaterUIManager()
    }

}