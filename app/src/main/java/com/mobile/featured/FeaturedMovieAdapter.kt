package com.mobile.featured

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.mobile.adapters.BaseViewHolder
import com.mobile.model.Movie
import com.mobile.screening.MoviePosterClickListener
import kotlinx.android.synthetic.main.list_item_featured_poster.view.*

class FeaturedMovieAdapter(private val featured: List<Movie>, val moviePosterClickListener: MoviePosterClickListener) : RecyclerView.Adapter<BaseViewHolder>() {

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
            else -> BaseViewHolder(LandscapePosterView(parent.context).apply {
                this.moviePosterClickListener = FeaturedMovieAdapter@this.moviePosterClickListener
            })
        }
    }

    override fun getItemViewType(position: Int): Int {
        val movie = featured.get(position)

        if (movie.teaserVideoUrl.isNullOrEmpty()) {
            return TYPE_POSTER
        } else {
            return TYPE_TRAILER
        }
    }

    override fun getItemCount(): Int {
        return featured.size
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val view = holder.itemView
        val movie = featured.get(position)


        if (view is MovieTrailerView) {
            view.bind(movie, enablePlayback)
        } else if (view is LandscapePosterView) {
            view.bind(movie)
        }
        view.videoTitle.text = movie.title

    }

    companion object {
        const val TYPE_TRAILER = 0;
        const val TYPE_POSTER = 1
    }
}