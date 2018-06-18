package com.mobile.history

import android.graphics.Color
import android.graphics.drawable.Animatable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.transition.TransitionInflater
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.ScaleAnimation
import android.widget.Toast
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.controller.BaseControllerListener
import com.facebook.imagepipeline.image.ImageInfo
import com.facebook.imagepipeline.request.ImageRequestBuilder
import com.mobile.Constants
import com.mobile.fragments.MPFragment
import com.mobile.helpers.LogUtils
import com.mobile.history.model.ReservationHistory
import com.mobile.network.RestClient
import com.mobile.responses.HistoryResponse
import com.moviepass.R
import kotlinx.android.synthetic.main.fr_historydetails.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat

/**
 * Created by o_vicarra on 3/27/18.
 */

class HistoryDetailsFragment : MPFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val activity = activity?:return
        activity.startPostponedEnterTransition()
        sharedElementEnterTransition = TransitionInflater.from(activity).inflateTransition(android.R.transition.explode).setDuration(2000)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fr_historydetails, container, false)
        root.setOnClickListener { onBack() }
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        close.setOnClickListener { v -> activity?.onBackPressed() }


        val historyItem = arguments!!.getParcelable<ReservationHistory>(HISTORY_POSTER)
        val transition = arguments!!.getString(EXTRA_TRANSITION_NAME)

        val black = Color.argb(200, 0, 0, 0)
        detailsBackground.setBackgroundColor(black)
        Log.d(Constants.TAG, "onViewCreated: " + historyItem!!.userRating)

        if (historyItem.userRating != null) {
            didYouLikeIt.visibility = View.GONE
            if (historyItem.userRating == "GOOD") {
                like.setImageDrawable(resources.getDrawable(R.drawable.thumbsupselect))
                dislike.visibility = View.GONE
            } else if (historyItem.userRating == "BAD") {
                dislike.setImageDrawable(resources.getDrawable(R.drawable.thumbsdownselect))
                like.visibility = View.GONE
            }

        } else {
            val id = historyItem.id?:return
            like.setOnClickListener { v -> rateMovie(id, "GOOD") }
            dislike.setOnClickListener { v -> rateMovie(id, "BAD") }
        }

        val imgUrl = Uri.parse(historyItem.imageUrl)
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


        val createdAt = historyItem.createdAt
        val sdf = SimpleDateFormat("M/dd/yyyy")
        historyDate.text = sdf.format(createdAt)

        historyLocal.text = historyItem.theaterName
        historyTitle.text = historyItem.title
        enlargedImage.transitionName = transition
        enlargedImage.controller = controller
    }


    private fun rateMovie(historyId: Int, userRating: String) {
        val rating = HistoryResponse(userRating)
        RestClient.getAuthenticated().submitRating(historyId, rating).enqueue(object : Callback<HistoryResponse> {
            override fun onResponse(call: Call<HistoryResponse>, response: Response<HistoryResponse>) {
                val h = Handler()
                if (response.isSuccessful) {
                    if (userRating == "GOOD") {
                        dislike.visibility = View.GONE
                        fadeOut(dislike)
                        animate(like)
                    } else if (userRating == "BAD") {
                        like.visibility = View.GONE
                        fadeOut(like)
                        animate(dislike)
                    }
                    h.postDelayed({ activity?.onBackPressed() }, 2000)
                }
            }

            override fun onFailure(call: Call<HistoryResponse>, t: Throwable) {
                val activity = activity?:return
                Toast.makeText(activity, t.message, Toast.LENGTH_SHORT).show()
                LogUtils.newLog(Constants.TAG, "onFailure: " + t.message)
            }
        })

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
        private val EXTRA_TRANSITION_NAME = "transition_name"


        fun newInstance(moviePoster: ReservationHistory, transitionName: String): HistoryDetailsFragment {
            val fragment = HistoryDetailsFragment()

            val bundle = Bundle()
            bundle.putParcelable(HISTORY_POSTER, moviePoster)
            bundle.putString(EXTRA_TRANSITION_NAME, transitionName)
            fragment.arguments = bundle
            return fragment
        }
    }

}
