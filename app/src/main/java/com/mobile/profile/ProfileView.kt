package com.mobile.profile

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.v4.content.res.ResourcesCompat
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
        clickSpace.setOnClickListener {
            presentation?.let {
                clickListener?.onClick(it)
            }
        }
    }

    fun bind(pres: ProfilePresentation, clickListener: ProfileClickListener? = null) {
        this.presentation = pres
        this.clickListener = clickListener
        if (pres.type == Profile.DIVIDER) {
            arrayOf(
                    name, header, arrowImage
            ).forEach { it.visibility = View.GONE }
            divider.visibility = View.VISIBLE
            topGuide.visibility = View.VISIBLE
            return
        }

        icon.visibility = when (pres.icon) {
            null -> {
                name.setPadding(55, 0, 0, 0)
                View.GONE
            }
            else -> {
                icon.background = ResourcesCompat.getDrawable(resources, pres.icon ?: return, null)
                View.VISIBLE
            }
        }


        name.contentDescription = pres.title.toString()
        name.text = pres.title.toString()
        header.text = pres.header
        header.visibility = when {
            header.text.isEmpty() -> View.GONE
            else -> View.VISIBLE
        }


        arrowImage.visibility = when {
            pres.title == resources.getString(R.string.how_to_use_moviepass) ||
                    pres.title == resources.getString(R.string.help) ->
                View.VISIBLE

            else -> View.GONE
        }
        TextViewCompat.setTextAppearance(name, when (pres.type) {
            Profile.VERSION -> R.style.ProfileVersion
            else -> R.style.ProfileTitle
        })
        divider.visibility = when {
            pres.type == Profile.VERSION || pres.type == Profile.HOW_TO_USE_MOVIEPASS || pres.type == Profile.HELP -> View.GONE
            else -> View.VISIBLE
        }
        topGuide.visibility = divider.visibility

    }

}