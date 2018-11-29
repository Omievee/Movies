package com.mobile.history

import android.content.Context
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import com.mobile.adapters.BaseViewHolder
import com.mobile.featured.VerticalMoviePosterView
import com.mobile.fragments.MPFragment
import com.mobile.history.model.ReservationHistory
import com.mobile.home.HomeActivity
import com.mobile.model.Movie
import com.mobile.recycler.decorator.HistoryView
import com.mobile.screening.MoviePosterClickListener
import com.mobile.utils.highestElevation
import com.mobile.utils.showFragment
import com.mobile.widgets.SoftNavigationPlaceholder
import com.moviepass.R
import dagger.android.support.AndroidSupportInjection
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fr_historydetails.*
import kotlinx.android.synthetic.main.fragment_history.*
import javax.inject.Inject

/**
 * Created by omievee on 1/27/18.
 */

class HistoryFragment : MPFragment(), MoviePosterClickListener {
    override fun onMoviePosterClick(movie: Movie) {
        val context = context ?: return
        val history = historyAdapter.data?.find {
            movie.id == it.id
        } ?: return

        val position = historyAdapter.data?.indexOf(history)
        startActivity(HistoryDetailsActivity.newInstance(context, position?: 0, size = historyAdapter.data?.size))
    }

    var historyAdapter: ResevationAdapter = ResevationAdapter(this)

    @Inject
    lateinit var historyManager: HistoryManager

    internal var historySub: Disposable? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = activity ?: return
        val span = 3
        val manager = GridLayoutManager(activity, span, GridLayoutManager.VERTICAL, false)
        historyReycler.layoutManager = manager
        historyReycler.adapter = historyAdapter
        historyReycler.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                val height = historyReycler.measuredWidth
                historyAdapter.onMeasuredHeight(height, span)
                historyReycler.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }

        })
        historyReycler.elevation = headerBar.highestElevation
        progress.visibility = View.VISIBLE
        backButton.setOnClickListener { activity.onBackPressed() }
        loadData()
    }

    override fun onAttach(context: Context?) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    fun loadData() {
        activity ?: return
        progress.visibility = View.VISIBLE

        historySub?.dispose()

        historySub = historyManager.getHistory().doFinally {
            activity ?: return@doFinally
            progress.visibility = View.GONE
        }?.subscribe({ res ->
            activity ?: return@subscribe
            historyAdapter.data = res

            when (res.size) {
                0 -> {
                    historyReycler.visibility = View.GONE
                    NoMoives.visibility = View.VISIBLE
                }
                else -> {
                    historyReycler.visibility = View.VISIBLE
                    NoMoives.visibility = View.GONE
                }
            }

        }, { error ->
        })
    }

    companion object {

        val TAG = HistoryFragment::class.java.simpleName

        fun newInstance(): HistoryFragment {
            return HistoryFragment().apply {
                arguments = Bundle()
            }
        }
    }
}

class ResevationAdapter(val moviePosterClickListener: MoviePosterClickListener) : RecyclerView.Adapter<BaseViewHolder>() {

    var data: List<ReservationHistory>? = null
    set(value) {
        field = value
        notifyDataSetChanged()
    }

    var totalWidth: Int? = null
    var colSpan:Int?=null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return when (viewType == 0) {
            true -> return BaseViewHolder(HistoryView(parent.context).apply {
                val newWIdth = (totalWidth!!/colSpan!!.toFloat()).toInt()
                val height = newWIdth*3/2
                layoutParams = ViewGroup.MarginLayoutParams(newWIdth, height)
            })
            else -> return BaseViewHolder(SoftNavigationPlaceholder(parent.context).apply {
                showBottom = true
            })
        }
    }

    override fun getItemViewType(position: Int): Int {
        val size = data?.size ?: 0
        val count = when {
            position < size -> 0
            else -> 1
        }
        return count
    }

    override fun getItemCount(): Int {
        val size = data?.size
        return when {
            totalWidth == null -> 0
            size != null -> size + 3
            else -> 0
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val view = holder.itemView
        when (view) {
            is VerticalMoviePosterView -> view.bind(movie = data!![position].toMovie(), moviePosterClickListener = this.moviePosterClickListener)
            is SoftNavigationPlaceholder -> {
            }
        }
    }

    fun onMeasuredHeight(totalWidth: Int, colSpan:Int) {
        this.totalWidth = totalWidth
        this.colSpan = colSpan
        notifyDataSetChanged()
    }

}



