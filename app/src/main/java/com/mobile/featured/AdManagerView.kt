package com.mobile.featured

import android.content.Context
import android.support.constraint.ConstraintLayout

import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.ads.formats.NativeCustomTemplateAd
import com.mobile.screening.MoviePosterClickListener
import com.moviepass.R
import kotlinx.android.synthetic.main.list_item_admanager.view.*

class AdManagerView(context: Context?, attrs: AttributeSet? = null) : ConstraintLayout(context, attrs), View.OnClickListener {
    var moviePosterClickListener: MoviePosterClickListener? = null
    var adID: String? = null
    var featuredAd: NativeCustomTemplateAd? = null

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.adManagerPoster -> {
                val adID = adID ?: return
                featuredAd?.performClick("")
                moviePosterClickListener?.onAdPosterClick(adID)
            }
        }
    }

    init {
        inflate(context, R.layout.list_item_admanager, this)
        layoutParams = MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        adSpace.maxHeight = 9 * resources.displayMetrics.heightPixels / 16
        adManagerPoster.setOnClickListener(this)
    }

    fun bind(featuredAd: NativeCustomTemplateAd) {
        this.featuredAd = featuredAd
        this.adID = featuredAd.getText(context.getString(R.string.ad_manager_movie_id)).toString()
        adTitle.text = featuredAd.getText(context.getString(R.string.ad_manager_movie_title)).toString()
        adManagerPoster.setImageURI(featuredAd.getImage(context.getString(R.string.ad_manager_poster))?.uri, context)
    }
}