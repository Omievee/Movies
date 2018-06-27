package com.moviepass.debug

import com.mobile.application.Application
import com.mobile.di.DaggerDebugAppComponent

class DebugApplication : Application() {

    override fun inject() {
        DaggerDebugAppComponent
                .builder()
                .application(this)
                .build()
                .inject(this)
    }

}