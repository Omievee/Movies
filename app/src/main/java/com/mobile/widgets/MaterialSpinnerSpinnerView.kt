package com.mobile.widgets

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import com.moviepass.R
import kotlinx.android.synthetic.main.layout_list_item_loyalty_spinner.view.*

open class MaterialSpinnerSpinnerView(context: Context) : ConstraintLayout(context) {

    init {
        View.inflate(context, R.layout.layout_list_item_loyalty_spinner, this)
        layoutParams = MarginLayoutParams(MATCH_PARENT, resources.getDimension(R.dimen.action_bar_size).toInt())
    }

    fun bind(text:String) {
        loyaltyChainName.text = text
    }
}