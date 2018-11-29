package com.mobile.history

import android.content.Context
import android.net.Uri
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.controller.BaseControllerListener
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder
import com.facebook.imagepipeline.image.ImageInfo
import com.facebook.imagepipeline.request.ImageRequestBuilder
import com.mobile.history.StarsRating.Rating
import com.mobile.history.StarsRating.StarsRatingClickListener
import com.mobile.history.model.ReservationHistory
import com.moviepass.R
import kotlinx.android.synthetic.main.fr_historydetails.view.*
import java.text.SimpleDateFormat

class HistoryDetailsView(context: Context, attributeSet: AttributeSet? = null) : ConstraintLayout(context, attributeSet), HistoryDetailViewListener {

    var presenter: HistoryDetailPresenter? = null
    var data: ReservationHistory? = null

    override fun close() {
        dismissListener?.onDetailsFragmentDismissed()
    }

    private var dismissListener: HistoryDetailDismissListener? = null


    init {
        View.inflate(context, R.layout.fr_historydetails, this)
        layoutParams = MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun ratingFromRatingScreenSubmited() {
        val context = context ?: return
        historyTitle.text = context.getString(R.string.history_rating_thanks)
    }

    override fun setClickListeners() {
        stars.setOnClickListener(object : StarsRatingClickListener{
            override fun onStarClicked(rating: Rating) {
                presenter?.userClickedRating(data,rating)
            }
        })
    }

    override fun bindFromHistoryTap(res: ReservationHistory?, historyDetailsListener: HistoryDetailDismissListener?, presenter: HistoryDetailPresenter) {
        bind(res, historyDetailsListener, presenter)
        val createdAt = data?.created
        createdAt?.let {
            val sdf = SimpleDateFormat("MMMM dd, yyyy")
            historyDate.text = sdf.format(createdAt)
        }
        historyLocal.text = data?.theaterName
        historyTitle.text = data?.title
    }

    override fun bindRateLastMovie(res: ReservationHistory?, historyDetailsListener: HistoryDetailDismissListener?, presenter: HistoryDetailPresenter) {
        bind(res, historyDetailsListener, presenter)
        val context = context ?: return
        historyTitle.text = context.getString(R.string.history_rating_rate_last)
        historyLocal.visibility = View.INVISIBLE
        historyDate.visibility = View.INVISIBLE
    }

    fun bind(res: ReservationHistory?, dismissListener: HistoryDetailDismissListener?, presenter: HistoryDetailPresenter) {
        this.dismissListener = dismissListener
        this.data = res
        this.presenter = presenter

        close.setOnClickListener {
            close()
        }

        val imgUrl = Uri.parse(data?.imageUrl) ?: return

        val request = ImageRequestBuilder.newBuilderWithSource(imgUrl)
                .setProgressiveRenderingEnabled(true)
                .setSource(imgUrl)
                .build()
        val hier = GenericDraweeHierarchyBuilder(resources).setDesiredAspectRatio(1.5f)
        enlargedImage.hierarchy = hier.build()
        val controller = Fresco.newDraweeControllerBuilder()
                .setUri(imgUrl)
                .setImageRequest(request)
                .setControllerListener(object : BaseControllerListener<ImageInfo>() {
                    override fun onFailure(id: String?, throwable: Throwable?) {
                        when (data?.imageUrl?.contains("https://s3.amazonaws.com/") == true) {
                            true -> enlargedImage.setImageURI(imgUrl.toString() + "/original.jpg")
                        }
                    }
                })
                .build()

        enlargedImage.controller = controller
        fillUpStars(res?.rating)
    }

    override fun fillUpStars(rating: Rating?) {
        stars.fillStars(rating)
    }

    override fun emptyStars() {
        stars.emptyAllStars()
    }

}

