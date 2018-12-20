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
import com.google.android.gms.ads.formats.NativeCustomTemplateAd
import com.mobile.Primary
import com.mobile.activities.ActivateMoviePassCard
import com.mobile.adapters.BasicDiffCallback
import com.mobile.adapters.ItemSame
import com.mobile.fragments.*
import com.mobile.listeners.BonusMovieClickListener
import com.mobile.model.Movie
import com.mobile.model.Screening
import com.mobile.recycler.decorator.SpaceDecorator
import com.mobile.responses.CurrentMoviesResponse
import com.mobile.screening.MoviePosterClickListener
import com.mobile.seats.MPBottomSheetFragment
import com.mobile.seats.SheetData
import com.moviepass.R
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_movies.*
import javax.inject.Inject

class MoviesFragment : MPFragment(), MoviesView, Primary {

    @Inject
    lateinit var presenter: MoviesFragmentPresenter


    val movieClickListener = object : MoviePosterClickListener {
        override fun onAdPosterClick(adId: String) {
            presenter.onAdIdFound(adId = Integer.valueOf(adId))
        }

        override fun onMoviePosterClick(movie: Movie?, screening: Screening?) {
            showFragment(ScreeningsFragment.newInstance(ScreeningsData(
                    movie = movie
            )))
        }
    }

    val bonusClickListener = object : BonusMovieClickListener {
        override fun onBonusBannerClickListener() {
            MPBottomSheetFragment.newInstance(SheetData(
                    title = resources.getString(R.string.bonus_movies_title),
                    description = resources.getString(R.string.bonus_movies_description)
            )).show(childFragmentManager, "")
        }
    }

    var adapter: MoviesAdapter = MoviesAdapter(movieClickListener, bonusClickListener)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_movies, container, false)
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

    override fun hideSubscriptionActivationRequired() {
        activity ?: return
        activateMPCardView.visibility = View.GONE
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.itemAnimator = null
        recyclerView.addItemDecoration(SpaceDecorator(
                lastBottom =
                resources.getDimension(R.dimen.bottom_navigation_height).toInt()
                        +
                        resources.getDimension(R.dimen.margin_half).toInt()
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
            startActivity(Intent(context, ActivateMoviePassCard::class.java))
        }

        presenter.onViewCreated()
    }

    override fun updateAdapter(t1: CurrentMoviesResponse, native: NativeCustomTemplateAd?) {
        val old = adapter.data?.list ?: emptyList()
        val newD = mutableListOf<MoviesPresentation>()
        if (!t1.featured.isEmpty()) {
            when (native) {
                null -> newD.add(MoviesPresentation(type = MovieAdapterType.FEATURED, data = Pair("Featured", t1.featured)))
                else -> newD.add(MoviesPresentation(type = MovieAdapterType.FEATURED, data = Pair("Featured", t1.featured), ad = native))
            }
        }
        t1.categorizedMovies.forEach {
            newD.add(MoviesPresentation(type = MovieAdapterType.CATEGORY, data = it))
        }
        adapter.data = Data(newD, DiffUtil.calculateDiff(BasicDiffCallback(old, newD)))
    }

    override fun showDeepLinkMovie(movie: Movie?) {
        showFragment(ScreeningsFragment.newInstance(ScreeningsData(
                movie = movie
        )))
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

class Data(val list: List<MoviesPresentation>, val diffResult: DiffUtil.DiffResult) {
    val hasFeatured by lazy {
        list.firstOrNull()?.type == MovieAdapterType.FEATURED
    }

    fun positionForCateogry(currPosition: Int): Int {
        if (hasFeatured) {
            return currPosition - 1
        }
        return currPosition
    }
}

data class MoviesPresentation(val type: MovieAdapterType, val data: Pair<String, List<Movie>>, val ad: NativeCustomTemplateAd? = null) : ItemSame<MoviesPresentation> {
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