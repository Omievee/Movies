package com.mobile.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Movie(val id: Int = 0,
            val tribuneId: String? = null,
            val title: String? = null,
            val runningTime: Int = 0,
            val releaseDate: String? = null,
            val rating: String? = null,
            val synopsis: String? = null,
            val viewed: Boolean = false,
            val createdAt: Long = 0,
            val type: String? = null,
            val imageUrl: String? = null,
            val landscapeImageUrl: String? = null,
            val teaserVideoUrl:String?=null,
            val theaterName: String? = null
) : Parcelable