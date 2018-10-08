package com.mobile.movie

import android.content.Context
import android.os.Build
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.LinearLayoutManager
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import com.mobile.screening.MoviePosterClickListener
import com.moviepass.R
import kotlinx.android.synthetic.main.layout_featured_container.view.*
import android.support.v7.widget.PagerSnapHelper
import android.support.v7.widget.RecyclerView
import com.mobile.Constants
import com.mobile.featured.FeaturedMovieAdapter
import com.mobile.featured.IndicatorsRecyclerViewItemDecoration
import com.mobile.model.Movie

class FeaturedView(context: Context?, attrs: AttributeSet? = null) : ConstraintLayout(context, attrs) {

    var adapter: FeaturedMovieAdapter? = null
    var currentView: View? = null
    var linearLayout: LinearLayoutManager? = null
    var snapHelper:  PagerSnapHelper ? = null

    init {
        View.inflate(context, R.layout.layout_featured_container, this)
        layoutParams = MarginLayoutParams(MATCH_PARENT, WRAP_CONTENT)
        linearLayout = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        trailerView.layoutManager = linearLayout
        snapHelper = PagerSnapHelper()
        snapHelper?.attachToRecyclerView(trailerView)
    }

    fun bind(presentation: MoviesPresentation, moviePosterClickListener: MoviePosterClickListener) {
        setUpAdapter(presentation, moviePosterClickListener)
        trailerView.adapter = adapter

        val endlessScrollSize = presentation.data.second.size
        when (endlessScrollSize > 1) {
            true -> {
                val scrollToPosition = endlessScrollSize * Constants.ENDLESS_HALF_START_POSITION
                trailerView.addItemDecoration(IndicatorsRecyclerViewItemDecoration(endlessScrollSize))
                setUpScrollListener()
                setOnScrollChangeListener(scrollToPosition)
                trailerView.scrollToPosition(scrollToPosition)
            }
            false -> {
                setOnScrollChangeListener(0)
                trailerView.scrollToPosition(0)
            }
        }
    }

    private fun setUpScrollListener() {
        trailerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (currentView != snapHelper?.findSnapView(linearLayout)) {
                        adapter?.noVisibleView(currentView)
                        currentView = snapHelper?.findSnapView(linearLayout)
                        onScrollFinished()
                    }
                }
            }
        })
    }

    private fun setOnScrollChangeListener(position: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            trailerView.setOnScrollChangeListener(
                    (OnScrollChangeListener { _, _, _, _, _ ->
                        currentView = linearLayout?.findViewByPosition(position)
                        onScrollFinished()
                        trailerView.setOnScrollChangeListener(null)
                    }))
        } else {
            currentView = linearLayout?.findViewByPosition(position)
            onScrollFinished()
        }
    }

    private fun onScrollFinished() {
        adapter?.onScrollFinished(currentView)
    }

    private fun setUpAdapter(presentation: MoviesPresentation, moviePosterClickListener: MoviePosterClickListener) {
        adapter = FeaturedMovieAdapter(presentation.data.second, moviePosterClickListener)

    }

}