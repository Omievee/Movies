package com.mobile.movie

import com.google.gson.Gson
import com.mobile.application.Application
import com.mobile.network.StaticApi
import dagger.Module
import dagger.Provides
import io.realm.Realm
import io.realm.RealmConfiguration
import javax.inject.Provider
import javax.inject.Singleton

@Module
class MoviesFragmentModule {

    @Provides
    @Singleton
    fun provideMovieManager(application:Application, gson:Gson, api: StaticApi): MoviesManager {
        return MoviesManagerImpl(application = application, gson = gson, api = api)
    }
}