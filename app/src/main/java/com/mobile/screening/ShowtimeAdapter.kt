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
                                showtimeClickListener?.onShowtimeClick(screening?.theater, it, time)
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
                val avail = showTime?.availability?:return
                val surge = showTime.surge?:return
                v.bind(avail, surge, it)
            }
        }
    }

    companion object {

        fun createData(data: ShowtimeData?, screening: ScreeningPresentation): ShowtimeData {
            val old = data?.data ?: emptyList()
            val newb = screening.screening?.availabilities?.map {
                ShowtimePresentation(screening.screening, it, screening.screening.getSurge(it.startTime, screening.userSegments))
            }?.filter {
                isValidShowtime(it.availability?.startTime)
            }
            return ShowtimeData(
                    newb, DiffUtil.calculateDiff(BasicDiffCallback<ShowtimePresentation>(old, newb))
            )
        }
    }
}