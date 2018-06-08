package com.mobile.utils

import android.content.Context
import android.content.Intent
import android.view.View
import android.view.TouchDelegate
import android.graphics.Rect
import android.view.ViewGroup
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