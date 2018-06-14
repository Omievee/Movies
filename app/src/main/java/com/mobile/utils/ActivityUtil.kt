package com.mobile.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.View
import android.view.TouchDelegate
import android.graphics.Rect
import android.support.transition.*
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.view.Gravity
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import com.mobile.rx.Schedulers
import com.moviepass.R
import io.reactivex.Single
import kotlinx.android.synthetic.main.fragment_profile_account_plan_and_billing.*

fun Context?.startIntentIfResolves(intent: Intent) {
    this?.packageManager?.let {
        intent.resolveActivity(it)?.let {
            startActivity(intent)
        }
    }
}

fun FragmentActivity.showFragment(fragment: Fragment) {
    val view = view as? ViewGroup ?: return
    var vg = view.findViewById<ViewGroup>(R.id.fragmentContainer)
    vg = when (vg) {
        null -> {
            val fl = FrameLayout(view.context)
            view.addView(fl, ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT))
            fl
        }
        else -> vg
    }
    val set = TransitionSet()
    set.duration = 250
    val slide = Slide(Gravity.END);
    set.addTransition(slide);
    TransitionManager.beginDelayedTransition(vg, set)
    vg.visibility = View.VISIBLE
    supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.animator.enter_from_right, R.animator.exit_to_left, R.animator.enter_from_left, R.animator.exit_to_right)
            .replace(R.id.fragmentContainer, fragment).commit()
}

fun FragmentActivity.onBackExtension(): Boolean {
    val fragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer)?:return false

    val listener = object : TransitionListenerAdapter() {
        override fun onTransitionEnd(transition: Transition) {
            fragment.let {
                supportFragmentManager
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
    val view = view as? ViewGroup ?: return false
    val fl = view.findViewById<ViewGroup>(R.id.fragmentContainer)
    set.addListener(listener)
    TransitionManager.beginDelayedTransition(fl, set)
    fl.visibility = View.INVISIBLE
    return true
}

fun View?.expandTouchArea() {
    Single.just(this).compose(Schedulers.singleDefault())
            .subscribe({
                val view = it ?: return@subscribe
                val rect = Rect()
                val parent = (it.parent as? ViewGroup) ?: return@subscribe
                view.getHitRect(rect)
                val extraPadding = view.resources.getDimension(R.dimen.margin_standard).toInt()
                rect.top -= extraPadding
                rect.left -= extraPadding
                rect.right += extraPadding
                rect.bottom += extraPadding
                view.touchDelegate = TouchDelegate(rect, parent)
            }, {})
}