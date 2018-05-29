package com.mobile.seats

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.view.ViewGroup
import com.mobile.model.Screening
import com.mobile.model.TicketType
import java.util.concurrent.atomic.AtomicInteger

class BringAFriendPagerAdapter(val screening: Screening, val showtime: String, fm: FragmentManager?) : FragmentPagerAdapter(fm) {

    var emailIndex: Int? = null
    var seatIndex: Int? = null

    var currentItem: Fragment? = null
        get() {
            return field?.isAdded?.let {
                when (it) {
                    true -> field
                    else -> null
                }
            }
        }

    override fun setPrimaryItem(container: ViewGroup, position: Int, `object`: Any) {
        super.setPrimaryItem(container, position, `object`)
        currentItem = `object` as? Fragment
    }

    private val frags by lazy {
        val availability = screening.getAvailability(showtime)
        availability?.let {
            val map = mutableMapOf<Int, Fragment>()
            var position = AtomicInteger(0)
            when (screening.maximumGuests > 0) {
                true -> {
                    map.put(position.getAndIncrement(), AddGuestsFragment())
                }
                else -> {

                }
            }
            when (availability.ticketType) {
                TicketType.SELECT_SEATING -> {
                    val seatPos = position.getAndIncrement()
                    seatIndex = seatPos
                    map.put(seatPos, SeatSelectionFragment())
                }
                else -> {

                }
            }
            when (screening.maximumGuests > 0) {
                true -> {
                    val index = position.getAndIncrement()
                    emailIndex = index
                    map.put(index, GuestEmailsFragment())
                }
                else -> {
                }
            }
            map.put(position.getAndIncrement(), ConfirmDetailsFragment())
            map
        } ?: emptyMap<String, Fragment>()

    }

    override fun getItem(position: Int): Fragment {
        return frags.get(position)!!
    }

    override fun getCount(): Int {
        return frags.size
    }
}