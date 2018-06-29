package com.mobile.utils

import android.support.transition.*
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import com.moviepass.R

fun Fragment.showFragmentExtension(fragment: Fragment? = null) {
    val set = TransitionSet()
    set.duration = 250
    val slide = Slide(Gravity.END);
    set.addTransition(slide);
    val view = view as? ViewGroup ?: return
    val fl = view.findViewById<ViewGroup>(R.id.fragmentContainer)
    TransitionManager.endTransitions(fl)
    TransitionManager.beginDelayedTransition(fl, set)
    fl.visibility = View.VISIBLE
    childFragmentManager.beginTransaction()
            .setCustomAnimations(R.animator.enter_from_right, R.animator.exit_to_left, R.animator.enter_from_left, R.animator.exit_to_right)
            .replace(R.id.fragmentContainer, fragment).commit()
}

fun Fragment.replaceFragmentExtension(fragment: Fragment? = null) {
    childFragmentManager
            .beginTransaction()
            .replace(R.id.fragmentContainer, fragment).commit()
}

fun Fragment.onBackExtension(): Boolean {
    val fragment = findDeepestFragment(childFragmentManager) ?: return false
    val parent = fragment.parentFragment ?: return false

    val listener = object : TransitionListenerAdapter() {

        override fun onTransitionEnd(transition: Transition) {
            val frag = parent.childFragmentManager.findFragmentById(R.id.fragmentContainer)
            frag.let {
                childFragmentManager
                        .beginTransaction()
                        .remove(it)
                        .commit()
            }
        }
    }
    val set = TransitionSet()
    set.duration = 200
    val slide = Slide(Gravity.END);
    set.addTransition(slide);
    val view = parent.view as? ViewGroup ?: return false
    val fl = view.findViewById<ViewGroup>(R.id.fragmentContainer)
    set.addListener(listener)
    TransitionManager.endTransitions(fl)
    TransitionManager.beginDelayedTransition(fl, set)
    fl.visibility = View.INVISIBLE
    return true
}

fun findDeepestFragment(childFragmentManager: FragmentManager): Fragment? {
    var fragment = childFragmentManager.findFragmentById(R.id.fragmentContainer)
    while(fragment!=null && fragment.isAdded) {
        val frag = fragment.childFragmentManager.findFragmentById(R.id.fragmentContainer) ?: break
        fragment = frag
    }
    return fragment
}
