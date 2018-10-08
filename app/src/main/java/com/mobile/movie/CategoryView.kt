package com.mobile.movie

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.v7.util.DiffUtil
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import com.mobile.adapters.BaseViewHolder
import com.mobile.adapters.BasicDiffCallback
import com.mobile.adapters.ItemSame
import com.mobile.featured.CategoryMoviePosterView
import com.mobile.featured.VerticalMoviePosterView
import com.mobile.listeners.BonusMovieClickListener
import com.mobile.model.Movie
import com.mobile.recycler.decorator.SpaceDecorator
import com.mobile.screening.MoviePosterClickListener
import com.mobile.utils.expandTouchArea
import com.mobile.utils.startCalendarIntent
import com.moviepass.R
import kotlinx.android.synthetic.main.layout_category.view.*

class CategoryView(context: Context?, attrs: AttributeSet? = null) : ConstraintLayout(context, attrs) {

    val adapter = CategoryAdapter()

    companion object {
        val DEFAULT_HEADERS = mutableMapOf(Pair("newReleases", "New Releases"),
                Pair("topBoxOffice", "Top Box Office"),
                Pair("comingSoon", "Coming Soon"),
                Pair("nowPlaying", "Now Playing")
        )
    }

    init {
        inflate(context, R.layout.layout_category, this)
        layoutParams = MarginLayoutParams(MATCH_PARENT,WRAP_CONTENT)
        recyclerView.itemAnimator = null
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(SpaceDecorator(
                start = resources.getDimension(R.dimen.margin_half).toInt()
        ))
        calendar.setOnClickListener {
            context.startCalendarIntent()
        }
        calendar.expandTouchArea()
    }

    fun bind(
            cat: Pair<String, List<Movie>>,
            position:Int=-1,
            moviePosterClickListener: MoviePosterClickListener? = null,
            bonusMovieClickListener: BonusMovieClickListener? = null
    ) {
        text.text = DEFAULT_HEADERS[cat.first] ?: cat.first
        val old = adapter.data?.list ?: emptyList()
        val newD = cat.second.map {
            CategoryPresentation(movie = it)
        }
        adapter.moviePosterClickListener = moviePosterClickListener
        adapter.bonusClickListener = bonusMovieClickListener
        adapter.data = CategoryData(newD, DiffUtil.calculateDiff(BasicDiffCallback(old, newD)))
        calendar.visibility = when(position) {
            0-> View.VISIBLE
            else->View.GONE
        }
    }
}

class CategoryAdapter() : RecyclerView.Adapter<BaseViewHolder>() {

    var data: CategoryData? = null
        set(value) {
            field = value
            value?.diffResult?.dispatchUpdatesTo(this)
        }

    var moviePosterClickListener: MoviePosterClickListener? = null
    var bonusClickListener:BonusMovieClickListener?=null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return BaseViewHolder(CategoryMoviePosterView(parent.context))
    }

    override fun getItemCount(): Int {
        return data?.list?.size ?: 0
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val view = holder.itemView
        val data = data!!.list[position]
        when (view) {
            is VerticalMoviePosterView -> {
                view.bind(data.movie, moviePosterClickListener, bonusClickListener)
            }
        }
    }

}

class CategoryData(val list: List<CategoryPresentation>, val diffResult: DiffUtil.DiffResult)

data class CategoryPresentation(val movie: Movie) : ItemSame<CategoryPresentation> {
    override fun sameAs(same: CategoryPresentation): Boolean {
        return equals(same)
    }

    override fun contentsSameAs(same: CategoryPresentation): Boolean {
        return hashCode() == same.hashCode()
    }

}