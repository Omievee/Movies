package com.mobile.keyboard

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.mobile.application.Application

class KeyboardManagerImpl(val application: Application) : KeyboardManager {

    val imm:InputMethodManager = application.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

    override fun hide() {
        val token = application.currentActivity?.window?.decorView?.windowToken?:return
        imm.hideSoftInputFromWindow(token, 0)
    }

    override fun show() {
        val token = application.currentActivity?.window?.decorView?.windowToken?:return
        imm.showSoftInputFromInputMethod(token,InputMethodManager.SHOW_IMPLICIT)
    }

}