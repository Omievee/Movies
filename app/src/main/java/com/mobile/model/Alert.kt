package com.mobile.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class Alert(val id: String? = null,
            val title: String? = null,
            val body: String? = null,
            val urlTitle: String? = null,
            val url: String? = null,
            val dismissible: Boolean = false
) : Parcelable
