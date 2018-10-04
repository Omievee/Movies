package com.mobile.featured

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.mobile.Constants
import com.mobile.adapters.BaseViewHolder
import com.mobile.model.Movie
import com.mobile.screening.MoviePosterClickListener
import kotlinx.android.synthetic.main.list_item_featured_poster.view.*

class  FeaturedMovieAdapter(private val featured: List<Movie>, val moviePosterClickListener: MoviePosterClickListener) : RecyclerView.Adapter<BaseViewHolder>() {

    var enablePlayback: Boolean = true
        set(value) {
            if (field != value) {
                field = value
                notifyDataSetChanged()
            }
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return when (viewType == TYPE_TRAILER) {
            true -> BaseViewHolder(MovieTrailerView(parent.context))
            else -> BaseViewHolder(LandscapePosterView(parent.context))
        }
    }

    override fun getItemViewType(position: Int): Int {

        val realPos = position % featured.size
        val movie = featured[realPos]

        if (movie.teaserVideoUrl.isNullOrEmpty()) {
            return TYPE_POSTER
        } else {
            return TYPE_TRAILER
        }
    }

    override fun getItemCount(): Int {
        val size = featured.size
        return when (size > 1){
            true -> size * Constants.ENDLESS_SCROLL_SIZE
            false -> size
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val view = holder.itemView
        val size = featured.size
        val realPos = position % size
        val movie = featured[realPos]

        if (view is MovieTrailerView) {
            view.bind(movie, enablePlayback)
            view.moviePosterClickListener = moviePosterClickListener
        } else if (view is LandscapePosterView) {
            view.bind(movie)
            view.moviePosterClickListener = moviePosterClickListener
        }
        view.videoTitle.text = movie.title

    }

    fun onScrollFinished(view: View?){
        if(view is MovieTrailerView){
            view.setUpVideo()
        }
    }

    fun noVisibleView(view: View?){
        if(view is MovieTrailerView){
            view.stopVideo()
        }
    }

    companion object {
        const val TYPE_TRAILER = 0
        const val TYPE_POSTER = 1
    }
}