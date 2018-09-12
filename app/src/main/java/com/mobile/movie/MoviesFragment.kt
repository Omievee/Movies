package com.mobile.movie

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Color.*
import android.os.Bundle
import android.support.constraint.ConstraintSet
import android.support.v4.content.ContextCompat
import android.support.v7.util.DiffUtil
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.mobile.Primary
import com.mobile.activities.ActivateMoviePassCard
import com.mobile.adapters.BasicDiffCallback
import com.mobile.adapters.ItemSame
import com.mobile.fragments.*
import com.mobile.model.Movie
import com.mobile.recycler.decorator.SpaceDecorator
import com.mobile.responses.CurrentMoviesResponse
import com.mobile.screening.MoviePosterClickListener
import com.moviepass.R
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_movies_v2.*
import javax.inject.Inject

class MoviesFragment : MPFragment(), MoviesView, Primary {
    override fun showDeepLinkMovie(movie: Movie) {
        showFragment(ScreeningsFragment.newInstance(ScreeningsData(
                movie = movie
        )))
    }

    @Inject
    lateinit var presenter: MoviesFragmentPresenter

    val movieClickListener: MoviePosterClickListener = object : MoviePosterClickListener {
        override fun onMoviePosterClick(movie: Movie) {
            showFragment(ScreeningsFragment.newInstance(ScreeningsData(
                    movie = movie
            )))
        }
    }

    var adapter: MoviesAdapter = MoviesAdapter(movieClickListener)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_movies_v2, container, false)
    }

    override fun onPrimary() {
        activity?.window?.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
    }

    override fun onAttach(context: Context?) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun hideProgress() {
        activity ?: return
        swipeRefresh.isRefreshing = false
    }

    override fun showProgress() {
        activity ?: return
        swipeRefresh.isRefreshing = true
    }

    override fun showSubscriptionActivationRequired() {
        activity ?: return
        activateMPCardView.visibility = View.VISIBLE
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.itemAnimator = null
        recyclerView.addItemDecoration(SpaceDecorator(
                lastBottom = resources.getDimension(R.dimen.bottom_navigation_height).toInt()
        ))
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            var scrolled: Float = 0f
            val constraintSet = ConstraintSet().apply {
                clone(toolbar)
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                scrolled += dy
                val height = 9 * resources.getDisplayMetrics().heightPixels / 16
                val color = ContextCompat.getColor(context ?: return, R.color.bottomNav)
                val percent = scrolled / height
                when {
                    scrolled > height -> {
                        toolbar.setBackgroundResource(R.color.bottomNav)
                        constraintSet.constrainPercentWidth(moviepassHeader.id, .333f)
                    }
                    scrolled < height -> {
                        toolbar.setBackgroundColor(Color.argb((Math.min(Color.alpha(color).toFloat(), percent * scrolled).toInt()), red(color), green(color), blue(color)))
                        constraintSet.constrainPercentWidth(moviepassHeader.id, Math.max(.333f, (1 - percent) * .45f))

                    }
                }
                constraintSet.applyTo(toolbar)
            }
        })
        recyclerView.adapter = adapter
        swipeRefresh.setOnRefreshListener {
            presenter.onRefresh()
        }
        searchIcon.setOnClickListener {
            showFragment(SearchFragment())
        }
        activateMPCardView.setOnClickListener {
            startActivity(Intent(context,ActivateMoviePassCard::class.java))
        }
        presenter.onViewCreated()
    }

    override fun updateAdapter(t1: CurrentMoviesResponse) {
        val old = adapter.data?.list ?: emptyList()
        val newD = mutableListOf<MoviesPresentation>()
        if (!t1.featured.isEmpty()) {
            newD.add(MoviesPresentation(type = MovieAdapterType.FEATURED, data = Pair("Featured", t1.featured)))
        }
        t1.categorizedMovies.forEach {
            newD.add(MoviesPresentation(type = MovieAdapterType.CATEGORY, data = it))
        }
        adapter.data = Data(newD, DiffUtil.calculateDiff(BasicDiffCallback(old, newD)))
    }

    override fun onResume() {
        super.onResume()
        presenter.onResume()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        presenter.onDestroy()
    }

}

class Data(val list: List<MoviesPresentation>, val diffResult: DiffUtil.DiffResult)

data class MoviesPresentation(val type: MovieAdapterType, val data: Pair<String, List<com.mobile.model.Movie>>) : ItemSame<MoviesPresentation> {
    override fun sameAs(same: MoviesPresentation): Boolean {
        return equals(same)
    }

    override fun contentsSameAs(same: MoviesPresentation): Boolean {
        return hashCode() == same.hashCode()
    }

}

enum class MovieAdapterType {
    FEATURED,
    CATEGORY
}