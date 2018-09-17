package com.mobile

import android.content.Intent
import android.os.Build
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import com.mobile.deeplinks.DeepLinksManager
import com.moviepass.BuildConfig
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import javax.inject.Inject

open class MPActivty : FragmentActivity(), HasSupportFragmentInjector {

    @Inject
    lateinit var fragmentInjector: DispatchingAndroidInjector<Fragment>

    @Inject
    lateinit var deepLinksManager: DeepLinksManager


    override fun supportFragmentInjector(): AndroidInjector<Fragment> {
        intent?.let {
            receiveIntent(it)
        }
        return fragmentInjector
    }

    companion object {
        val isEmulator: Boolean by lazy {
            BuildConfig.DEBUG && (Build.FINGERPRINT.startsWith("generic")
                    || Build.FINGERPRINT.startsWith("unknown")
                    || Build.MODEL.contains("google_sdk")
                    || Build.MODEL.contains("Emulator")
                    || Build.MODEL.contains("Android SDK built for x86")
                    || Build.MANUFACTURER.contains("Genymotion")
                    || Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")
                    || "google_sdk" == Build.PRODUCT)
        }
    }

    public fun receiveIntent(intent: Intent) {
        val url = intent.getStringExtra(Constants.APPBOY_DEEP_LINK_KEY) ?: return
        deepLinksManager.handleCategory(url)
    }
}