package com.mobile.di

import com.mobile.application.Application
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import dagger.android.AndroidInjector
import dagger.android.DaggerApplication
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

@Singleton
@Component(modules = [
    AndroidInjectionModule::class,
    AppBindingModule::class,
    DebugBindingModule::class,
    AndroidSupportInjectionModule::class,
    AppModule::class])
interface DebugAppComponent : AndroidInjector<@JvmSuppressWildcards DaggerApplication> {

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(application: Application): Builder

        fun build(): DebugAppComponent
    }

    fun inject(app: Application)

}