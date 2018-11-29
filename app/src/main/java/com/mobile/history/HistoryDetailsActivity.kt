package com.mobile.history

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.PagerSnapHelper
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.mobile.MPActivty
import com.mobile.adapters.BaseViewHolder
import com.mobile.history.model.ReservationHistory
import com.moviepass.R
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.activity_history_details.*
import javax.inject.Inject

class HistoryDetailsActivity : MPActivty(), HistoryDetailDismissListener {


    @Inject
    lateinit var presenter: HistoryDetailPresenter

    var data: List<ReservationHistory>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history_details)

        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        val historyItem = intent.getParcelableExtra<ReservationHistory>("historyItem")
        val position = intent.getIntExtra("position", 0)
        val size = intent.getIntExtra("size", 0)
        historyItem?.let { reservation ->
            recyclerView.adapter = HistoryDetailsAdapter(presenter, false).also {
                it.data = arrayListOf(reservation)
                it.dismissListener = this
                it.size = 1
            }

        } ?: if (size > 0) {
            recyclerView.adapter = HistoryDetailsAdapter(presenter, true).also {
                it.size = size
                it.dismissListener = this
            }
            recyclerView.scrollToPosition(intent.getIntExtra("position", 0))
            PagerSnapHelper().attachToRecyclerView(recyclerView)
        }

    }

    companion object {
        fun newInstance(context: Context, position: Int? = null, historyItem: ReservationHistory? = null, size: Int? = null): Intent {
            val intent = Intent(context, HistoryDetailsActivity::class.java)
            intent.putExtra("position", position)
            intent.putExtra("historyItem", historyItem)
            intent.putExtra("size", size)
            return intent
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        presenter.onDestroy()
    }

    override fun onDetailsFragmentDismissed() {
        finish()
    }
}

class HistoryDetailsAdapter(val presenter: HistoryDetailPresenter, val fromRateScreen: Boolean) : RecyclerView.Adapter<BaseViewHolder>() {

    var data: List<ReservationHistory>? = null
    var dismissListener: HistoryDetailDismissListener? = null
    var size: Int = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return BaseViewHolder(HistoryDetailsView(parent.context))
    }

    override fun getItemCount(): Int {
        return size
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val view = holder.itemView as? HistoryDetailViewListener
        presenter.onCreate(fromRateScreen, position, view, dismissListener)
    }
}
