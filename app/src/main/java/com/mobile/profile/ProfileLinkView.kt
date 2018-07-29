package com.mobile.profile

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.ViewGroup
import com.moviepass.R
import kotlinx.android.synthetic.main.layout_profile_view.view.*

class ProfileLinkView(context: Context?, attrs: AttributeSet?=null) : ConstraintLayout(context, attrs) {

    var presentation:ProfilePresentation?=null
    var listener:ProfileClickListener?=null

    init {
        inflate(context, R.layout.layout_profile_link, this)
        layoutParams = MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        name.setOnClickListener {
            presentation?.let {
                listener?.onClick(it)
            }
        }
    }

    fun bind(pres:ProfilePresentation, profileClickListener: ProfileClickListener) {
        presentation = pres
        listener = profileClickListener
        name.text = pres.title
    }

}