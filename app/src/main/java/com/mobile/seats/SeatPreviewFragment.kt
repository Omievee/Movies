package com.mobile.seats

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mobile.model.*
import com.mobile.network.RestClient
import com.mobile.rx.Schedulers
import com.moviepass.R
import io.reactivex.disposables.Disposable
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.fragment_seat_preview.*
import kotlinx.android.synthetic.main.layout_seats.*

class SeatPreviewFragment : Fragment() {

    var listener: SeatPreviewListener? = null
    var disposable: Disposable? = null
    var payload: SeatPreviewPayload? = null
    var seatInfo: SeatingsInfo? = null

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        listener = parentFragment as? SeatPreviewListener
        when (listener == null) {
            true -> listener = activity as? SeatPreviewListener
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_seat_preview, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        payload = arguments?.getParcelable("payload")
        closeButton.setOnClickListener {
            listener?.onClose()
        }
        guide.noReserved()
    }

    override fun onResume() {
        super.onResume()
        when (seatInfo) {
            null -> fetchSeatInfo()
        }
    }

    private fun fetchSeatInfo() {
        val tribuneTheaterId = payload?.screening?.tribuneTheaterId ?: return
        val theaterId = payload?.theater?.id ?: return
        val performanceId = payload?.availability?.providerInfo?.performanceId
                ?: return
        disposable?.dispose()
        disposable = RestClient
                .getAuthenticated().getSeats(
                        tribuneTheaterId,
                        theaterId.toString(),
                        performanceId
                )
                .subscribe({
                    seatInfo = it.seatingInfo ?: return@subscribe
                    seatsView.bind(seatingsInfo = it.seatingInfo, seatsNeeded = 0)
                }, {

                })


    }

    override fun onDestroy() {
        super.onDestroy()
        disposable?.dispose()
    }

    companion object {
        fun newInstance(payload: SeatPreviewPayload): SeatPreviewFragment {
            return SeatPreviewFragment().apply {
                arguments = Bundle().apply {
                    putParcelable("payload", payload)
                }
            }
        }
    }
}

interface SeatPreviewListener {
    fun onClose()
}

@Parcelize
class SeatPreviewPayload(val screening: Screening? = null, val theater: Theater? = null, val availability: Availability? = null) : Parcelable