package com.mobile.adapters

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.mobile.model.Movie

class FeaturedAdapter(val featured: List<Movie>) : RecyclerView.Adapter<BaseViewHolder>() {

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
            else -> BaseViewHolder(FrameLayout(parent.context))
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
        }
    }

    companion object {
        const val TYPE_TRAILER = 0;
        const val TYPE_POSTER = 1
    }
}