package com.mobile.plans

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.View
import com.moviepass.R

class PlansView(context: Context?, attrs: AttributeSet?=null) : ConstraintLayout(context, attrs)  {

    init {
        View.inflate(context, R.layout.list_item_plans, this)
    }


    fun bind(){

    }
}