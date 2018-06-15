package com.mobile.adapters

import android.content.Context
import android.net.Uri
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.TextAppearanceSpan
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.imagepipeline.request.ImageRequestBuilder
import com.mobile.listeners.ShowtimeClickListener
import com.mobile.screening.ScreeningPresentation
import com.mobile.screening.ShowtimeAdapter
import com.mobile.recycler.decorator.SpaceDecorator
import com.moviepass.R
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

class ScreeningView(context: Context) : FrameLayout(context) {

    val adapter: ShowtimeAdapter = ShowtimeAdapter()
    var screening: ScreeningPresentation? = null
    var showtimeListener: ShowtimeClickListener? = null

    init {
        View.inflate(context, R.layout.list_item_cinemaposter, this)
        recyclerView.layoutManager = object : LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false) {
            override fun supportsPredictiveItemAnimations(): Boolean {
                return false
            }
        }
        recyclerView.adapter = adapter
        val animator = DefaultItemAnimator()
        animator.supportsChangeAnimations = false
        recyclerView.itemAnimator = animator
        recyclerView.addItemDecoration(SpaceDecorator(resources.getDimension(R.dimen.card_button_margin_start).toInt()))
        layoutParams = MarginLayoutParams(MATCH_PARENT, WRAP_CONTENT)
    }

    fun bind(screening: ScreeningPresentation, showtimeClickListener: ShowtimeClickListener?) {
        this.screening = screening
        this.showtimeListener = showtimeListener
        adapter.screening = screening
        adapter.showtimeClickListener = showtimeClickListener
        adapter.data = ShowtimeAdapter.createData(adapter.data, screening)
        synopsis.visibility = View.GONE
        val imgUrl = Uri.parse(screening.screening?.landscapeImageUrl)
        val request = ImageRequestBuilder.newBuilderWithSource(imgUrl)
                .setProgressiveRenderingEnabled(true)
                .build()

        val controller = Fresco.newDraweeControllerBuilder()
                .setImageRequest(request).build()
        posterSPV.setImageURI(imgUrl)
        posterSPV.hierarchy.fadeDuration = 500
        cinemaApprovedV.isEnabled = screening.enabled
        movieTitle.text = screening.screening?.title
        when (screening.enabled) {
            false -> {
                notSupported.visibility = View.VISIBLE
                notSupported.text = when (screening.movie != null) {
                    true -> resources.getString(R.string.screening_already_seen)
                    false -> screening.screening?.disabledExplanation
                }
            }
            true -> {
                notSupported.visibility = View.GONE
            }
        }
        if(posterSPV.controller?.isSameImageRequest(controller)!=true) {
            posterSPV.controller = controller
        }
        movieTime.text = screening.screening?.runningTime?.runningTimeString(context = context)
        movieTime.apply {
            visibility = when (text.isEmpty()) {
                true -> View.GONE
                false -> View.VISIBLE
            }
        }
        val rating = SpannableStringBuilder().apply {
            if (screening.screening?.rating.isNullOrEmpty()) {
                return@apply
            }
            val rated = SpannableString(resources.getString(R.string.screening_rating)).apply {
                setSpan(TextAppearanceSpan(context, R.style.RatedText), 0, length, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            val rating = SpannableString(screening.screening?.rating).apply {
                setSpan(TextAppearanceSpan(context, R.style.RatingText), 0, length, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            append(rated)
            append("  ")
            append(rating)
        }
        movieRating.text = rating
        movieRating.visibility = when (movieRating.text.isEmpty()) {
            true -> View.GONE
            else -> View.VISIBLE
        }
        spacer.visibility = movieRating.visibility
    }

}