package com.mobile.widgets

import android.content.Context
import android.util.AttributeSet

class BottomSoftNavigationPlaceholder(context:Context, attributeSet: AttributeSet?=null) : SoftNavigationPlaceholder(context, attributeSet) {

    init {
        showBottom=true
    }
}