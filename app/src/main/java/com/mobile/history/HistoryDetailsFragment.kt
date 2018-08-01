package com.mobile.history

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Animatable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.support.v4.content.res.ResourcesCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.ScaleAnimation
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.controller.BaseControllerListener
import com.facebook.imagepipeline.image.ImageInfo
import com.facebook.imagepipeline.request.ImageRequestBuilder
import com.mobile.Constants
import com.mobile.fragments.MPFragment
import com.mobile.history.model.Rating
import com.mobile.history.model.ReservationHistory
import com.moviepass.R
import dagger.android.support.AndroidSupportInjection
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fr_historydetails.*
import java.text.SimpleDateFormat
import javax.inject.Inject

/**
 * Created by o_vicarra on 3/27/18.
 */

class HistoryDetailsFragment : MPFragment() {

    @Inject
    lateinit var historyManagerImpl: HistoryManager



    var historySub: Disposable? = null

    var fromRateScreen: Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fr_historydetails, container, false)
        root.setOnClickListener { onBack() }
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val historyItem = arguments?.getParcelable<ReservationHistory>(HISTORY_POSTER)
                ?: return@onViewCreated
        fromRateScreen = arguments?.getBoolean(Constants.IS_FROM_RATE_SCREEN)
                ?: return
        val black = Color.argb(200, 0, 0, 0)
        detailsBackground.setBackgroundColor(black)


        checkIfUserHasRatedFilm(historyItem)
        close.setOnClickListener { _ -> activity?.onBackPressed() }


        val imgUrl = Uri.parse(historyItem.imageUrl) ?: return@onViewCreated

        val request = ImageRequestBuilder.newBuilderWithSource(imgUrl)
                .setProgressiveRenderingEnabled(true)
                .setSource(imgUrl)
                .build()

        val controller = Fresco.newDraweeControllerBuilder()
                .setUri(imgUrl)
                .setImageRequest(request)
                .setControllerListener(object : BaseControllerListener<ImageInfo>() {
                    override fun onFinalImageSet(id: String?, imageInfo: ImageInfo?, animatable: Animatable?) {
                        super.onFinalImageSet(id, imageInfo, animatable)
                        enlargedImage.setImageURI(imgUrl)
                    }

                    override fun onFailure(id: String?, throwable: Throwable?) {
                        if (historyItem.imageUrl?.contains("https://s3.amazonaws.com/") == true) {
                            enlargedImage.setImageURI(imgUrl.toString() + "/original.jpg")
                        }
                    }
                })
                .build()


        if (fromRateScreen) {
            historyTitle.text = getString(R.string.history_rating_rate_last)
            historyLocal.visibility = View.INVISIBLE
            historyDate.visibility = View.INVISIBLE
        } else {
            val createdAt = historyItem.created
            createdAt?.let {
                val sdf = SimpleDateFormat("M/dd/yyyy")
                historyDate.text = sdf.format(createdAt)
            }
            historyLocal.text = historyItem.theaterName
            historyTitle.text = historyItem.title
        }

        enlargedImage.controller = controller
    }

    private fun checkIfUserHasRatedFilm(historyItem: ReservationHistory) {
        when (historyItem.rating) {
            Rating.GOOD -> {
                didYouLikeIt.text = getString(R.string.history_details_movie_like)
                like.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.thumbsupselect, null))
            }
            Rating.BAD -> {
                didYouLikeIt.text = getString(R.string.history_details_movie_dislike)
                dislike.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.thumbsdownselect, null))
            }
            Rating.UNKNOWN -> {
                like.setOnClickListener { _ ->
                    userClickedRating(historyItem, true)
                }
                dislike.setOnClickListener { _ ->
                    userClickedRating(historyItem, false)
                }
            }
        }
    }

    private fun userClickedRating(history: ReservationHistory, wasGood: Boolean) {
        historySub?.dispose()

        historySub = historyManagerImpl.submitRating(history, wasGood)
                .doAfterSuccess {
                    activity?: return@doAfterSuccess
                    if (fromRateScreen) {
                        historyTitle.text = getString(R.string.history_rating_thanks)
                    }
                }
                .subscribe({ res ->
                    activity?: return@subscribe
                    onHistorySaved(res)
                }, {

                })

    }

    private fun onHistorySaved(res: ReservationHistory?) {
        val wasGood = res?.rating == Rating.GOOD

        if (wasGood) {
            dislike.visibility = View.GONE
            fadeOut(dislike)
            animate(like)
        } else {
            like.visibility = View.GONE
            fadeOut(like)
            animate(dislike)
        }

        if (!fromRateScreen) {
            Handler().postDelayed({ activity?.onBackPressed() }, 1500)
        }
    }

    fun animate(view: View) {
        val expandAndShrink = AnimationSet(true)
        val expand = ScaleAnimation(
                1f, 1.5f,
                1f, 1.5f,
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, 0f)
        expand.duration = 500

        val shrink = ScaleAnimation(
                1.5f, 1f,
                1.5f, 1f,
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, 0f)
        shrink.startOffset = 500
        shrink.duration = 500

        expandAndShrink.addAnimation(expand)
        expandAndShrink.addAnimation(shrink)
        expandAndShrink.fillAfter = true
        expandAndShrink.interpolator = AccelerateInterpolator(1.0f)

        view.startAnimation(expandAndShrink)
    }

    companion object {
        private val HISTORY_POSTER = "poster"


        fun newInstance(moviePoster: ReservationHistory, isFromRatingScreen: Boolean): HistoryDetailsFragment {
            val fragment = HistoryDetailsFragment()

            val bundle = Bundle()
            bundle.putBoolean(Constants.IS_FROM_RATE_SCREEN, isFromRatingScreen)
            bundle.putParcelable(HISTORY_POSTER, moviePoster)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        historySub = null
    }


    override fun onAttach(context: Context?) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }
}
