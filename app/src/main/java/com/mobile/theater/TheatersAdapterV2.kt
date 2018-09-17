package com.mobile.theater

import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.mobile.adapters.BaseViewHolder
import com.mobile.adapters.BasicDiffCallback
import com.mobile.adapters.ItemSame
import com.mobile.location.UserLocation
import com.mobile.model.AmcDmaMap
import com.mobile.model.Theater
import com.mobile.utils.text.toFixed
import com.mobile.utils.text.toMiles
import com.moviepass.R
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersAdapter

class TheatersAdapterV2(val onTheaterClicked:TheaterClickListener) : RecyclerView.Adapter<BaseViewHolder>(), StickyRecyclerHeadersAdapter<BaseViewHolder> {
    override fun getHeaderId(position: Int): Long {
        val element = data?.theaters?.get(position)?:return 0
        return when(element.theater.ticketTypeIsStandard()) {
            true-> TYPE_STANDARD
            else-> TYPE_E_TICKET
        }.toLong()
    }

    override fun onCreateHeaderViewHolder(parent: ViewGroup): BaseViewHolder {
        return BaseViewHolder(TicketHeader(parent.context))
    }

    override fun onBindHeaderViewHolder(holder: BaseViewHolder, position: Int) {
        val view = holder.itemView
        when(view) {
            is TicketHeader-> view.bind(when(getHeaderId(position).toInt()) {
                TYPE_STANDARD-> R.string.theaters
                else-> R.string.eticket
            })
        }
    }

    var data: TheatersData? = null
        set(value) {
            field = value
            value?.diffResult?.dispatchUpdatesTo(this)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return BaseViewHolder(TheaterItemView(parent.context))
    }

    override fun getItemCount(): Int {
        return data?.theaters?.size ?: 0
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val view = holder.itemView
        val data = data?.theaters?.get(position) ?: return
        when (view) {
            is TheaterItemView -> view.bind(data, onTheaterClicked)
        }
    }

    companion object {
        const val TYPE_E_TICKET = 0
        const val TYPE_STANDARD = 1
        fun createData(last: TheatersData?,
                       userLocation: UserLocation,
                       theaters: List<com.mobile.model.Theater>,
                       dataMap:AmcDmaMap): TheatersData {
            val old = last?.theaters ?: emptyList()
            val showDistance = !dataMap.hasOneMoveToBottom(theaters)
            val new = theaters.map {
                TheaterPresentation(it,
                        when(showDistance) {
                            true-> UserLocation.haversine(userLocation.lat, userLocation.lon, it.lat, it.lon).toMiles().toFixed(2)
                            false-> null
                        })
            }
            return TheatersData(new, DiffUtil.calculateDiff(BasicDiffCallback(old, new)))
        }
    }
}

class TheatersData(val theaters: List<TheaterPresentation>, val diffResult: DiffUtil.DiffResult)

data class TheaterPresentation(val theater: Theater, val distance: Double?) : ItemSame<TheaterPresentation> {
    override fun sameAs(same: TheaterPresentation): Boolean {
        return same.theater.id == theater.id
    }

    override fun contentsSameAs(same: TheaterPresentation): Boolean {
        return hashCode() == same.hashCode()
    }

}