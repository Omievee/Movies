package com.mobile.history

import android.content.Context
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mobile.fragments.MPFragment
import com.mobile.history.model.ReservationHistory
import com.moviepass.R
import dagger.android.support.AndroidSupportInjection
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fr_history.*
import javax.inject.Inject

/**
 * Created by omievee on 1/27/18.
 */

class PastReservationsFragment : MPFragment(), HistoryPosterClickListener {


    var historyAdapter: HistoryAdapter? = null


    @Inject
    lateinit var historyManager: HistoryManager

    internal var historySub: Disposable? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fr_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = activity ?: return

        val manager = GridLayoutManager(activity, 3, GridLayoutManager.VERTICAL, false)

        historyReycler?.layoutManager = manager
        historyAdapter = HistoryAdapter(this)
        historyReycler?.adapter = historyAdapter

        progress.visibility = View.VISIBLE
        loadData()
    }

    override fun onAttach(context: Context?) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    fun loadData() {
        progress.visibility = View.VISIBLE

        historySub?.dispose()

        historySub = historyManager.getHistory().doFinally { progress.visibility = View.GONE }?.subscribe({ res ->
            historyAdapter?.setData(res)

            when (res.size) {
                0 -> {
                    historyReycler?.visibility = View.GONE
                    NoMoives.visibility = View.VISIBLE
                }
                else -> {
                    historyReycler?.visibility = View.VISIBLE
                    NoMoives.visibility = View.GONE
                }
            }

        }, { error ->
        })
    }

    override fun onPosterClicked(pos: Int, historyposter: ReservationHistory, isFromRatingScreen: Boolean) {
        showFragment(HistoryDetailsFragment.newInstance(historyposter, isFromRatingScreen))
    }

    companion object {

        val TAG = PastReservationsFragment::class.java.simpleName

        fun newInstance(): PastReservationsFragment {

            val args = Bundle()

            val fragment = PastReservationsFragment()
            fragment.arguments = args

            return fragment
        }
    }
}



