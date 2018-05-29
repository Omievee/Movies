package com.mobile.screening

import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.mobile.adapters.BaseViewHolder
import com.mobile.adapters.BasicDiffCallback
import com.mobile.listeners.ShowtimeClickListener
import com.mobile.utils.isValidShowtime
import java.text.SimpleDateFormat
import java.util.*

class ShowtimeAdapter : RecyclerView.Adapter<BaseViewHolder>() {

    var screening: ScreeningPresentation? = null
    var showtimeClickListener: ShowtimeClickListener? = null

    var data: ShowtimeData? = null
        set(value) {
            field = value
            notifyItemRangeChanged(0, value?.data?.size ?: 0)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return BaseViewHolder(
                ShowtimeView(parent.context).apply {
                    setOnClickListener {
                        time?.let { time ->
                            screening?.screening?.let {
                                isSelected = true
                                showtimeClickListener?.onShowtimeClick(null, 0, it, time)
                            }

                        }

                    }
                }
        )

    }

    override fun getItemCount(): Int {
        return data?.data?.size ?: 0
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val view: View = holder.itemView
        val showTime = data?.data?.get(position)
        (view as? ShowtimeView)?.let { v ->
            screening?.let {
                v.bind(showTime?.showtime, it)
            }

        }
    }

    companion object {

        fun createData(data: ShowtimeData?, screening: ScreeningPresentation): ShowtimeData {
            val old = data?.data ?: emptyList()
            val newb = screening?.screening?.startTimes?.map {
                ShowtimePresentation(screening?.screening, it)
            }?.filter {
                isValidShowtime(it.showtime)
            }
            return ShowtimeData(
                    newb, DiffUtil.calculateDiff(BasicDiffCallback<ShowtimePresentation>(old, newb))
            )
        }
    }
}