package com.mobile.adapters

import android.content.Context
import android.net.Uri
import android.support.v7.widget.LinearLayoutManager
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.TextAppearanceSpan
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.imagepipeline.common.ResizeOptions
import com.facebook.imagepipeline.request.ImageRequestBuilder
import com.mobile.listeners.ShowtimeClickListener
import com.mobile.recycler.decorator.SpaceDecorator
import com.mobile.screening.ScreeningPresentation
import com.mobile.screening.ShowtimeAdapter
import com.moviepass.R
import kotlinx.android.synthetic.main.horizontal_poster.view.*
import kotlinx.android.synthetic.main.list_item_cinemaposter.view.*

fun Int.runningTimeString(context: Context): SpannableStringBuilder {
    val hours = this / 60
    val minutes = this % 60
    return SpannableStringBuilder().apply {
        if (hours > 0) {
            val hoursString = "hr"
            append(SpannableString("${hours}").apply {
                setSpan(TextAppearanceSpan(context, R.style.RatingText), 0, length, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
            })
            append(' ')
            append(SpannableString(hoursString).apply {
                setSpan(TextAppearanceSpan(context, R.style.RatedText), 0, length, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
            })
        }
        if (minutes > 0) {
            if (hours > 0) {
                append(' ')
            }
            val minutesString = "min"
            append(SpannableString("${minutes}").apply {
                setSpan(TextAppearanceSpan(context, R.style.RatingText), 0, length, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
            })
            append(' ')
            append(SpannableString(minutesString).apply {
                setSpan(TextAppearanceSpan(context, R.style.RatedText), 0, length, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
            })
        }
    }
}

fun String?.toFormattedRating(context: Context): SpannableStringBuilder {
    val ratingStr = this
    return android.text.SpannableStringBuilder().apply {
        if (ratingStr.isNullOrEmpty()) {
            return@apply
        }
        val resources = context.resources
        val rated = android.text.SpannableString(resources.getString(com.moviepass.R.string.screening_rating)).apply {
            setSpan(android.text.style.TextAppearanceSpan(context, com.moviepass.R.style.RatedText), 0, length, android.text.SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        val rating = android.text.SpannableString(ratingStr).apply {
            setSpan(android.text.style.TextAppearanceSpan(context, com.moviepass.R.style.RatingText), 0, length, android.text.SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        append(rated)
        append("  ")
        append(rating)
    }
}

fun String?.toBold(context: Context): SpannableStringBuilder {
    val ratingStr = this
    return android.text.SpannableStringBuilder().apply {
        val resources = context.resources
        val bold = android.text.SpannableString(ratingStr).apply {
            setSpan(android.text.style.TextAppearanceSpan(context, com.moviepass.R.style.MPText_Bold), 0, length, android.text.SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        append(bold)
    }
}

class ScreeningView(context: Context) : FrameLayout(context) {

    val adapter: ShowtimeAdapter = ShowtimeAdapter()
    var screeningPresentation: ScreeningPresentation? = null
    var showtimeListener: ShowtimeClickListener? = null
    val layoutManager: LinearLayoutManager

    init {
        View.inflate(context, R.layout.list_item_cinemaposter, this)
        layoutManager = object : LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false) {
            override fun supportsPredictiveItemAnimations(): Boolean {
                return false
            }
        }
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter
        recyclerView.itemAnimator = null
        recyclerView.addItemDecoration(SpaceDecorator(
                firstStart = resources.getDimension(R.dimen.margin_quarter).toInt(),
                start = resources.getDimension(R.dimen.margin_quarter).toInt(),
                top = resources.getDimension(R.dimen.dp_1).toInt(),
                bottom = resources.getDimension(R.dimen.margin_half).toInt() + resources.getDimension(R.dimen.dp_1).toInt()
        ))
        layoutParams = MarginLayoutParams(MATCH_PARENT, WRAP_CONTENT)
    }

    fun bind(screening: ScreeningPresentation, showtimeClickListener: ShowtimeClickListener?) {
        this.screeningPresentation = screening
        this.showtimeListener = showtimeListener
        adapter.screening = screening
        adapter.showtimeClickListener = showtimeClickListener
        adapter.data = ShowtimeAdapter.createData(adapter.data, screening)
        synopsisIV.visibility = View.GONE
        val imgUrl = Uri.parse(screeningPresentation?.screening?.landscapeImageUrl)
        val request = ImageRequestBuilder.newBuilderWithSource(imgUrl)
                .setProgressiveRenderingEnabled(true)
                .setResizeOptions(ResizeOptions(1280, 720))
                .build()

        val controller = Fresco.newDraweeControllerBuilder()
                .setImageRequest(request).build()
        posterSPV.setImageURI(imgUrl)

        cinemaApprovedV.isEnabled = screening.enabled
        movieTitle.text = screening.screening?.title
        val disabledEx = screeningPresentation?.screening?.disabledExplanation ?: ""
        val approval = screeningPresentation?.screening?.approved ?: true


        when (screening.enabled) {
            false -> {
                notSupported.visibility = View.VISIBLE
                notSupported.text = when (screening.movie != null) {
                    true -> resources.getString(R.string.screening_already_seen)
                    false -> disabledEx
                }

                if (disabledEx.isEmpty() && !approval) {
                    notSupported.text = resources.getString(R.string.screening_premium)
                }
            }
            true -> {
                notSupported.visibility = View.GONE
            }
        }
        if (posterSPV.controller?.isSameImageRequest(controller) != true) {
            posterSPV.controller = controller
        }
        movieTime.text = screening.screening?.runningTime?.runningTimeString(context = context)
        movieTime.apply {
            visibility = when (text.isEmpty()) {
                true -> View.GONE
                false -> View.VISIBLE
            }
        }
        movieRating.text = screening.screening?.rating.toFormattedRating(context)
        movieRating.visibility = when (movieRating.text.isEmpty()) {
            true -> View.GONE
            else -> View.VISIBLE
        }
        spacer.visibility = movieRating.visibility
    }

}