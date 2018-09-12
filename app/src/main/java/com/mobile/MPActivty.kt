package com.mobile

import android.os.Build
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import com.mobile.deeplinks.DeepLinksManagerImpl
import com.moviepass.BuildConfig
import android.util.Log
import com.mobile.deeplinks.DeepLinksManager
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import javax.inject.Inject

open class MPActivty : FragmentActivity(), HasSupportFragmentInjector {

    @Inject
    lateinit var fragmentInjector: DispatchingAndroidInjector<Fragment>

    @Inject
    lateinit var deepLinksManager: DeepLinksManager

    lateinit var uri: String

    override fun supportFragmentInjector(): AndroidInjector<Fragment> {
        receiveIntent()
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

    private fun receiveIntent() {
        Log.d(">>>>","RECEIVED INTENT" + intent.getStringExtra(Constants.APPBOY_DEEP_LINK_KEY))

        when (intent?.getStringExtra(Constants.APPBOY_DEEP_LINK_KEY)) {
            null -> {}
            else -> {
                uri = intent?.getStringExtra(Constants.APPBOY_DEEP_LINK_KEY).toString()
                Log.d(">>>", ">>> PASSING URI$uri")
                deepLinksManager.determineCategory(uri)
            }
        }
    }


}