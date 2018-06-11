package com.mobile.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import com.mobile.BackFragment
import com.mobile.utils.onBackExtension
import com.mobile.utils.showFragmentExtension
import com.moviepass.R

open class MPFragment : Fragment(), BackFragment {

    var fragmentContainer: FrameLayout? = null

    fun showFragment(fragment: Fragment) {
        showFragmentExtension(fragment)
    }

    override fun onBack(): Boolean {
        return onBackExtension()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val vg = view as? ViewGroup ?: return
        val none = (0 until vg.childCount).none {
            val view = vg.getChildAt(it)
            view.id == R.id.fragmentContainer
        }
        when (none) {
            true -> {
                fragmentContainer = FrameLayout(context).apply {
                    id = R.id.fragmentContainer
                }
                vg.addView(fragmentContainer, MATCH_PARENT, MATCH_PARENT)
            }
            false -> {
                fragmentContainer = vg.findViewById(R.id.fragmentContainer)
            }
        }
        fragmentContainer?.visibility = View.INVISIBLE
    }

    fun fadeIn(view: View) {
        val old = view.animation
        if (old != null) {
            old.setAnimationListener(null)
            old.cancel()
        }
        if (view.visibility == View.VISIBLE) {
            return
        }
        val fadeIn = AlphaAnimation(0f, 1f)
        fadeIn.interpolator = DecelerateInterpolator() //add this
        fadeIn.duration = 500

        val animation = AnimationSet(false) //change to false
        animation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {
                view.visibility = View.VISIBLE
            }

            override fun onAnimationEnd(animation: Animation) {
                view.visibility = View.VISIBLE
            }

            override fun onAnimationRepeat(animation: Animation) {

            }
        })
        animation.addAnimation(fadeIn)
        view.animation = animation

    }

    fun fadeOut(view: View) {
        val old = view.animation
        if (old != null) {
            old.setAnimationListener(null)
            old.cancel()
        }
        if (view.visibility != View.VISIBLE) {
            return
        }
        val fadeOut = AlphaAnimation(1f, 0f)
        fadeOut.interpolator = DecelerateInterpolator() //add this
        fadeOut.duration = 500
        val animation = AnimationSet(false) //change to false
        animation.addAnimation(fadeOut)
        animation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {
                view.visibility = View.VISIBLE
            }

            override fun onAnimationEnd(animation: Animation) {
                view.visibility = View.GONE
            }

            override fun onAnimationRepeat(animation: Animation) {

            }
        })
        view.animation = animation
    }

}