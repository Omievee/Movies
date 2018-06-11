package com.mobile.utils

import android.content.res.Configuration.ORIENTATION_PORTRAIT
import android.content.res.Resources
import android.support.annotation.Px

val Resources.navBarHeight: Int @Px get() {
    val id = getIdentifier("navigation_bar_height", "dimen", "android")
    return when {
        id > 0 -> getDimensionPixelSize(id)
        else -> 0
    }
}

val Resources.showsSoftwareNavBar: Boolean get() {
    val id = getIdentifier("config_showNavigationBar", "bool", "android")
    return id > 0 && getBoolean(id)
}

inline val Resources.isNavBarAtBottom: Boolean get() {
    // Navbar is always on the bottom of the screen in portrait mode, but rotates
    // with device in landscape orientations
    return this.configuration.orientation == ORIENTATION_PORTRAIT
}