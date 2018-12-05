package com.mobile.fragments

import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.support.transition.Slide
import android.support.transition.TransitionManager
import android.support.transition.TransitionSet
import android.support.v4.app.Fragment
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.DecelerateInterpolator
import android.widget.RelativeLayout
import com.mobile.BackFragment
import com.mobile.utils.*
import com.moviepass.R

open class MPFragment : Fragment(), BackFragment {

    var fragmentContainer: ViewGroup? = null

    open fun showFragment(fragment: Fragment) {
        showFragment(R.id.fragmentContainer, fragment)
    }

    fun showFragment(id: Int, fragment: Fragment) {
        showFragmentExtension(id, fragment)
    }

    fun removeFragment(id: Int) {
        removeFragmentExtension(id)
    }

    fun slideFragmentIn(v: ViewGroup) {
        view as? ViewGroup ?: return
        val set = TransitionSet()
        set.duration = 250
        val slide = Slide(Gravity.END);
        set.addTransition(slide)
        TransitionManager.beginDelayedTransition(v, set)
        v.visibility = View.VISIBLE
    }

    fun slideFragmentOut(v: ViewGroup?) {
        val view = view as? ViewGroup ?: return
        v ?: return
        val set = TransitionSet()
        set.duration = 250
        val slide = Slide(Gravity.END);
        set.addTransition(slide);
        TransitionManager.beginDelayedTransition(view, set)
        v.visibility = View.INVISIBLE
    }

    fun preloadFragment(id: Int, fragment: Fragment) {
        preLoadFragmentExtension(id, fragment)
    }

    override fun onBack(): Boolean {
        return onBackExtension()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val vg = view as? ViewGroup ?: return
        val none = (0 until vg.childCount).none {
            val vv = vg.getChildAt(it)
            vv.id == R.id.fragmentContainer
        }
        when (none) {
            true -> {
                fragmentContainer = RelativeLayout(context).apply {
                    id = R.id.fragmentContainer
                    elevation = vg.highestElevation
                }
                vg.addView(fragmentContainer, MATCH_PARENT, MATCH_PARENT)
            }
            false -> {
                fragmentContainer = vg.findViewById(R.id.fragmentContainer)
            }
        }
        fragmentContainer?.visibility = when (childFragmentManager.findFragmentById(R.id.fragmentContainer)) {
            null -> View.INVISIBLE
            else -> View.VISIBLE
        }
    }

    fun isOnline(): Boolean {
        val cm = activity?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo = cm.activeNetworkInfo
        return netInfo != null && netInfo.isConnectedOrConnecting
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return super.onCreateView(inflater, container, savedInstanceState)
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