package com.mobile.seats

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.view.ViewGroup
import com.mobile.model.Availability
import com.mobile.model.Screening
import com.mobile.model.SurgeType
import com.mobile.model.TicketType
import com.mobile.reservation.Checkin
import com.mobile.surge.ConfirmSurgeFragment
import java.util.concurrent.atomic.AtomicInteger

class BringAFriendPagerAdapter(val checkin:Checkin?, userSegments:List<Int>, fm: FragmentManager?) : FragmentPagerAdapter(fm) {

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
        val checkin = checkin?: return@lazy emptyMap<Int,Fragment>()
        checkin.availability.let {
            val map = mutableMapOf<Int, Fragment>()
            var position = AtomicInteger(0)
            when (checkin.showGuestFlow && checkin.softCap==false) {
                true -> {
                    map[position.getAndIncrement()] = AddGuestsFragment()
                }
                else -> {

                }
            }
            when (checkin.availability.ticketType) {
                TicketType.SELECT_SEATING -> {
                    val seatPos = position.getAndIncrement()
                    seatIndex = seatPos
                    map[seatPos] = SeatSelectionFragment()
                }
                else -> {

                }
            }
            when (checkin.showGuestFlow) {
                true -> {
                    val index = position.getAndIncrement()
                    emailIndex = index
                    map[index] = GuestEmailsFragment()
                }
                else -> {
                }
            }
            val surge = checkin.screening.getSurge(checkin.availability.startTime, userSegments)
            when {
                checkin.availability.ticketType == TicketType.STANDARD && surge.level==SurgeType.SURGING-> map.put(position.getAndIncrement(), ConfirmSurgeFragment())
                else -> map[position.getAndIncrement()] = ConfirmDetailsFragment()
            }
            map
        }

    }

    override fun getItem(position: Int): Fragment {
        return frags.get(position)!!
    }

    override fun getCount(): Int {
        return frags.size
    }
}