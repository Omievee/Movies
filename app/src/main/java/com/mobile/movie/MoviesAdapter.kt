package com.mobile.movie

import android.os.Build
import android.support.annotation.RequiresApi
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.mobile.adapters.BaseViewHolder
import com.mobile.listeners.BonusMovieClickListener
import com.mobile.screening.MoviePosterClickListener

class MoviesAdapter(
        val movieCLickListener: MoviePosterClickListener,
        val bonusClickListener: BonusMovieClickListener?=null
        ) : RecyclerView.Adapter<BaseViewHolder>() {

    var data: Data? = null
        set(value) {
            field = value
            value?.diffResult?.dispatchUpdatesTo(this)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return when (viewType) {
            MovieAdapterType.FEATURED.ordinal -> BaseViewHolder(FeaturedView(parent.context))
            else -> BaseViewHolder(CategoryView(parent.context))
        }
    }

    override fun getItemViewType(position: Int): Int {
        val pres = data!!.list[position]
        return pres.type.ordinal
    }

    override fun getItemCount(): Int {
        return data?.list?.size ?: 0
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val view = holder.itemView
        val pres = data!!.list[position]
        when (view) {
            is FeaturedView -> view.bind(pres, movieCLickListener)
            is CategoryView -> view.bind(position = data!!.positionForCateogry(position), cat = pres.data, moviePosterClickListener = movieCLickListener, bonusMovieClickListener = bonusClickListener)
        }
    }

}