package com.mobile.featured

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.ads.formats.NativeCustomTemplateAd
import com.mobile.Constants
import com.mobile.adapters.BaseViewHolder
import com.mobile.model.Movie
import com.mobile.screening.MoviePosterClickListener
import kotlinx.android.synthetic.main.list_item_featured_poster.view.*

class FeaturedMovieAdapter(val featured: List<Movie>, private val featuredAd: NativeCustomTemplateAd? = null, val moviePosterClickListener: MoviePosterClickListener) : RecyclerView.Adapter<BaseViewHolder>() {

    var enablePlayback: Boolean = true
        set(value) {
            if (field != value) {
                field = value
                notifyDataSetChanged()
            }
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return when (viewType) {
            TYPE_TRAILER -> BaseViewHolder(MovieTrailerView(parent.context))
            TYPE_AD -> BaseViewHolder(AdManagerView(parent.context))
            else -> BaseViewHolder(LandscapePosterView(parent.context))
        }
    }

    override fun getItemViewType(position: Int): Int {
        val realPos = position % featured?.size
        val movie = featured[realPos]

        if (featuredAd != null) {
            return TYPE_AD
        } else if (!movie.teaserVideoUrl.isNullOrEmpty()) {
            return TYPE_TRAILER
        } else {
            return TYPE_POSTER
        }

    }

    override fun getItemCount(): Int {
        val size = featured.size
        return when (featured.size > 1) {
            true -> size * Constants.ENDLESS_SCROLL_SIZE
            false -> size

        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val view = holder.itemView
        when (featuredAd) {
            null -> {
                val size = featured.size
                val realPos = position % size
                val movie = featured[realPos]

                if (view is MovieTrailerView) {
                    view.bind(movie, enablePlayback)
                    view.moviePosterClickListener = moviePosterClickListener
                } else if (view is LandscapePosterView) {
                    view.bind(movie)
                    view.moviePosterClickListener = moviePosterClickListener
                    view.videoTitle.text = movie.title
                }
            }
            else -> {
                val b = view as AdManagerView
                b.bind(featuredAd)
                view.moviePosterClickListener = moviePosterClickListener
            }
        }

    }


    fun onScrollFinished(view: View?) {
        if (view is MovieTrailerView) {
            view.setUpVideo()
        }
    }

    fun noVisibleView(view: View?) {
        if (view is MovieTrailerView) {
            view.stopVideo()
        }
    }

    companion object {
        const val TYPE_TRAILER = 0
        const val TYPE_POSTER = 1
        const val TYPE_AD = 2
    }
}