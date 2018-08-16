package com.mobile.profile

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.v4.widget.TextViewCompat
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import com.moviepass.R
import kotlinx.android.synthetic.main.layout_profile_view.view.*

class ProfileView(context: Context?, attrs: AttributeSet? = null) : ConstraintLayout(context, attrs) {

    var clickListener: ProfileClickListener? = null
    var presentation: ProfilePresentation? = null

    init {
        inflate(context, R.layout.layout_profile_view, this)
        layoutParams = MarginLayoutParams(MATCH_PARENT, WRAP_CONTENT)
        name.setOnClickListener {
            presentation?.let {
                clickListener?.onClick(it)
            }
        }
    }

    fun bind(pres: ProfilePresentation, clickListener: ProfileClickListener? = null) {
        this.presentation = pres
        this.clickListener = clickListener
        if(pres.type==Profile.DIVIDER) {
            arrayOf(
                    name,header,subHeader
            ).forEach { it.visibility=View.GONE }
            divider.visibility=View.VISIBLE
            topGuide.visibility=View.VISIBLE
            return
        }
        name.text = pres.title
        header.text = pres.header
        header.visibility = when {
            header.text.isEmpty() -> View.GONE
            else -> View.VISIBLE
        }
        subHeader.text = pres.subHeader
        subHeader.visibility = when {
            subHeader.text.isEmpty()->View.GONE
            else->View.VISIBLE
        }
        TextViewCompat.setTextAppearance(name, when (pres.type) {
            Profile.VERSION -> R.style.ProfileVersion
            else -> R.style.ProfileTitle
        })
        divider.visibility = when {
            pres.type == Profile.VERSION -> View.GONE
            else -> View.VISIBLE
        }
        topGuide.visibility = divider.visibility

    }

}