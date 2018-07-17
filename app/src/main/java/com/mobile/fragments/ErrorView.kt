package com.mobile.fragments

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.View
import com.mobile.ApiError
import com.moviepass.R
import kotlinx.android.synthetic.main.layout_error.view.*

class ErrorView(context: Context?, attrs: AttributeSet?=null) : ConstraintLayout(context, attrs) {

    init {
        View.inflate(context, R.layout.layout_error, this)
    }

    fun show(apiError: ApiError?=null) {
        textView.text = when(apiError?.error?.message) {
            null-> resources.getString(R.string.generic_error)
            else-> apiError.error.message
        }
        visibility = View.VISIBLE
    }

    fun hide() {
        visibility = View.GONE
    }
}