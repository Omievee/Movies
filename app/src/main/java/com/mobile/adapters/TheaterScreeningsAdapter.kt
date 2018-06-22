package com.mobile.adapters

import android.location.Location
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.mobile.history.model.ReservationHistory
import com.mobile.listeners.ShowtimeClickListener
import com.mobile.model.Screening
import com.mobile.model.Theater2
import com.mobile.model.TicketType
import com.mobile.responses.ScreeningsResponseV2
import com.mobile.screening.NoMoreScreenings
import com.mobile.screening.ScreeningPresentation

class TheaterScreeningsAdapter(
        var listener: ShowtimeClickListener? = null,
        var missingCheckinListener: MissingCheckinListener? = null) : RecyclerView.Adapter<BaseViewHolder>() {

    var data: ScreeningData? = null
        set(value) {
            field = value
            value?.diffResult?.dispatchUpdatesTo(this)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return when (viewType) {
            TYPE_SCREENING -> BaseViewHolder(ScreeningView(parent.context))
            TYPE_THEATER -> BaseViewHolder(MovieScreeningView(parent.context))
            TYPE_MISSING -> BaseViewHolder(MissingCheckinView(parent.context))
            else -> BaseViewHolder(NoMoreScreenings(parent.context))
        }
    }

    override fun getItemViewType(position: Int): Int {
        return data?.data?.get(position)?.type ?: 0
    }

    override fun getItemCount(): Int {
        return data?.data?.size ?: 0
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val view = holder.itemView
        val data = data?.data?.get(position)
        data?.let { pres ->
            when (view) {
                is MovieScreeningView -> view.bind(pres, listener)
                is ScreeningView -> view.bind(pres, listener)
                is MissingCheckinView -> view.bind(pres, missingCheckinListener)
            }
        }

    }

    companion object {
        const val TYPE_SCREENING = 0
        const val TYPE_MISSING = 1
        const val TYPE_NO_MORE_SCREENINGS = 2
        const val TYPE_THEATER = 3
        const val CHECK_IN_IF_MOVIE_MISSING = "Check In if Movie Missing"

        fun createData(data: ScreeningData?, response: android.util.Pair<List<ReservationHistory>, ScreeningsResponseV2>, location: Location?, selected: android.util.Pair<Screening, String?>?): ScreeningData {
            val screeningsResponse = response.second;
            val movies = response.first.associateBy { it.id }
            val old = data?.data ?: emptyList()
            val screenings = screeningsResponse.screenings
            val theaters = screeningsResponse.theaters?.associateBy {
                it.tribuneTheaterId
            } ?: emptyMap()
            val theaterLoc = location?.let { Location("") }
            val presentations = screenings?.map {

                val theater = theaters.get(it.tribuneTheaterId)?.let {
                    Theater2(id = it.id,
                            tribuneTheaterId = it.tribuneTheaterId,
                            name = it.name,
                            latitude = it.lat,
                            longitude = it.lon,
                            address = it.address,
                            city = it.city,
                            state = it.state,
                            zip = it.zip
                    )
                }
                theaterLoc?.latitude = theater?.latitude ?: 0.0
                theaterLoc?.longitude = theater?.longitude ?: 0.0
                ScreeningPresentation(
                        distance = location?.distanceTo(theaterLoc)?.toDouble(),
                        screening = it,
                        theater = theater,
                        selected = when (selected?.first == it) {
                            true -> selected
                            else -> null
                        },
                        movie = movies?.get(it.moviepassId),
                        type = when {
                            theater != null -> TYPE_THEATER
                            it.title == CHECK_IN_IF_MOVIE_MISSING -> TYPE_MISSING
                            else -> TYPE_SCREENING
                        }
                )
            }?.filter { it ->
                (it.type == TYPE_MISSING) || it.hasShowtimes
            }?.sortedWith(compareBy(
                    {
                        it.type == TYPE_MISSING
                    },
                    {
                        when (it.screening?.getTicketType()) {
                            TicketType.SELECT_SEATING, TicketType.E_TICKET -> true
                            else -> false
                        }
                    }, {
                it.distance ?: false
            },
                    {
                        !(it.screening?.approved ?: false)
                    },
                    {
                        it.movie != null
                    }
            ))?.toMutableList()
            val noMoreScreenings = presentations?.none { it.type == TYPE_SCREENING || it.type == TYPE_THEATER }
                    ?: false
            if (noMoreScreenings) {
                presentations?.add(ScreeningPresentation(type = TYPE_NO_MORE_SCREENINGS))
            }
            return ScreeningData(presentations, DiffUtil.calculateDiff(BasicDiffCallback(old, presentations)))
        }
    }
}

class ScreeningData(val data: List<ScreeningPresentation>?, val diffResult: DiffUtil.DiffResult) {
    fun findPosition(screening: Screening): Int {
        return data?.indexOfFirst {
            it.screening?.moviepassId == screening.moviepassId
        } ?: -1
    }
}