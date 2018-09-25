package com.mobile.utils

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.net.Uri
import android.provider.Settings
import android.support.transition.*
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.util.TypedValue
import android.view.Gravity
import android.view.TouchDelegate
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import com.mobile.BackFragment
import com.mobile.rx.Schedulers
import com.moviepass.R
import io.reactivex.Single

fun Context?.startIntentIfResolves(intent: Intent) {
    this?.packageManager?.let {
        intent.resolveActivity(it)?.let {
            startActivity(intent)
        }
    }
}

val View.highestElevation: Float
    get() {
        val vg = this as? ViewGroup ?: return this.elevation
        val max = (0 until vg.childCount).maxBy {
            val view = vg.getChildAt(it)
            view.elevation
        } ?: return this.elevation
        val view = vg.getChildAt(max)
        return view.elevation
    }

fun FragmentActivity.showFragment(fragment: Fragment) {
    val view = findViewById(R.id.mpActivityContainer) as? ViewGroup ?: return
    var vg = view.findViewById<ViewGroup>(R.id.activityFragmentContainer)
    vg = when (vg) {
        null -> {
            val fl = FrameLayout(view.context).apply {
            }
            view.addView(fl, ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT))
            fl
        }
        else -> vg
    }
    val set = TransitionSet()
    set.duration = 250
    val slide = Slide(Gravity.END);
    set.addTransition(slide);
    TransitionManager.endTransitions(vg)
    TransitionManager.beginDelayedTransition(vg, set)
    vg.visibility = View.VISIBLE
    supportFragmentManager.beginTransaction()
            .replace(R.id.activityFragmentContainer, fragment).commit()
}

fun FragmentActivity.onBackExtension(): Boolean {
    val fragment = supportFragmentManager.findFragmentById(R.id.activityFragmentContainer)
            ?: return false

    when (fragment) {
        is BackFragment -> {
            when (fragment.onBack()) {
                true -> return true
            }
        }
    }

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
    val view = findViewById(R.id.mpActivityContainer) as? ViewGroup ?: return false
    val fl = view.findViewById<ViewGroup>(R.id.activityFragmentContainer)
    set.addListener(listener)
    TransitionManager.beginDelayedTransition(fl, set)
    fl.visibility = View.INVISIBLE
    return true
}

fun View?.expandTouchArea(extraPaddingInDips: Int = -1) {
    this ?: return
    val extraPadding = when (extraPaddingInDips) {
        -1 -> resources.getDimension(R.dimen.margin_standard).toInt()
        else -> TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, extraPaddingInDips.toFloat(), resources.displayMetrics).toInt()
    }
    Single.just(this).compose(Schedulers.singleDefault())
            .subscribe({
                val view = it ?: return@subscribe
                val rect = Rect()
                val parent = (it.parent as? ViewGroup) ?: return@subscribe
                view.getHitRect(rect)
                rect.top -= extraPadding
                rect.left -= extraPadding
                rect.right += extraPadding
                rect.bottom += extraPadding
                view.touchDelegate = TouchDelegate(rect, parent)
            }, {})
}

fun Fragment?.startCameraIntent(requestCode: Int) {
    val context = this?.activity ?: return
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
    val uri = Uri.fromParts("package", context.packageName, null)
    intent.data = uri
    startActivityForResult(intent, requestCode)
}