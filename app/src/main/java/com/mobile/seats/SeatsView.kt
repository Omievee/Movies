package com.mobile.seats

import android.content.Context
import android.support.annotation.StringRes
import android.support.constraint.ConstraintLayout
import android.support.constraint.ConstraintSet
import android.support.v7.app.AlertDialog
import android.support.v7.util.DiffUtil
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.mobile.adapters.BaseViewHolder
import com.mobile.adapters.BasicDiffCallback
import com.mobile.adapters.ItemSame
import com.mobile.extensions.SeatHelper
import com.mobile.model.SeatInfo
import com.mobile.model.SeatingsInfo
import com.mobile.recycler.layout.FixedGridLayoutManager
import com.moviepass.R
import kotlinx.android.synthetic.main.layout_seats.view.*
import kotlin.math.absoluteValue

class SeatsView(context: Context, attributeSet: AttributeSet? = null) : ConstraintLayout(context, attributeSet) {

    val layoutManager: FixedGridLayoutManager
    var adapter: SeatsAdapter? = null

    val selected: MutableSet<SeatInfo> = mutableSetOf()

    var seatingsInfo: SeatingsInfo? = null
    var seatsNeeded: Int = 1
    var seatsSelectedListener: SeatsSelectedListener? = null

    var seatsContainerWidth: Int? = null
    val seatWidth = context.resources.getDimension(R.dimen.seat_height)
    var scrolled = false

    val seatClickListener = object : SeatClickListener {
        override fun onClick(seat: SeatPresentation) {
            val seatInfo = seat.seatInfo ?: return
            when (selected.contains(seatInfo)) {
                true -> selected.remove(seatInfo)
                false -> addSeatIfAvailable(seatInfo)
            }
            seatingsInfo?.let {
                bind(it, seatsNeeded = seatsNeeded)
            }
            seatsSelectedListener?.onSeatsSelected(selected)
        }
    }

    init {
        inflate(context, R.layout.layout_seats, this)
        layoutManager = FixedGridLayoutManager()
        adapter = SeatsAdapter(seatClickListener)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter
        recyclerView.itemAnimator = null
    }

    private fun addSeatIfAvailable(seatInfo: SeatInfo) {
        when {
            selected.size + 1 > seatsNeeded -> {
                when (seatsNeeded) {
                    1 -> selected.apply {
                        clear()
                        add(seatInfo)
                    }
                    else -> {
                    }
                }
            }
            else -> {
                selected.add(seatInfo)
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w != seatsContainerWidth) {
            seatsContainerWidth = w
            val seatInfo = seatingsInfo ?: return
            bind(seatingsInfo = seatInfo,
                    seatsNeeded = seatsNeeded)
        }
    }

    fun error() {
        progressView.visibility = View.GONE
    }

    fun bind(seatingsInfo: SeatingsInfo, seatsNeeded: Int = 1, selectedSeats: Collection<SeatInfo>? = null) {
        progressView.visibility = View.GONE
        this.seatingsInfo = seatingsInfo
        this.seatsNeeded = seatsNeeded
        selectedSeats?.let {
            selected.apply {
                clear()
                addAll(it)
            }
        }
        val seatsContainerWidth = this.seatsContainerWidth ?: return
        removeItemDecorators()
        val data = SeatsAdapter.create(
                last = adapter?.data,
                seatingsInfo = seatingsInfo,
                selected = selected,
                widthAndHeight = seatsContainerWidth
        )
        val diffy = data.totalColumns * seatWidth
        val diff = (seatsContainerWidth - diffy) / 2
        layoutManager.setTotalColumnCount(data.totalColumns)
        adapter?.data = data
        when (!scrolled) {
            diffy < seatsContainerWidth -> {
                val set = ConstraintSet()
                set.clone(this)
                set.setMargin(recyclerView.id, ConstraintSet.START, diff.toInt())
                set.applyTo(this)
            }
            else -> {

            }
        }
        scrolled = true
    }

    private fun removeItemDecorators() {
        val count = recyclerView.itemDecorationCount
        (0 until count).forEach { recyclerView.removeItemDecorationAt(0) }
    }
}

class SeatsAdapter(var seatClickListener: SeatClickListener? = null) : RecyclerView.Adapter<BaseViewHolder>() {

    var data: Data? = null
        set(value) {
            field = value
            field?.diffResult?.dispatchUpdatesTo(this)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return when (viewType) {
            TYPE_SEAT -> BaseViewHolder(SeatView(parent.context))
            else -> BaseViewHolder(EmptySeat(parent.context))
        }
    }

    override fun getItemViewType(position: Int): Int {
        return data?.seats?.get(position)?.viewType ?: 0
    }

    override fun getItemCount(): Int {
        return data?.seats?.size ?: 0
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val item = data?.seats?.get(position)
        val view = holder.itemView
        if (view is SeatView) {
            item?.let {
                view.bind(it, seatClickListener)
            }
        }
    }

    companion object {
        const val TYPE_EMPTY = 0
        const val TYPE_SEAT = 1

        fun create(last: Data?, seatingsInfo: SeatingsInfo, widthAndHeight: Int, selected: Set<SeatInfo>): Data {
            val old = last?.seats ?: emptyList()
            val seats = seatingsInfo.seats ?: return Data(emptyList(),0, 0, DiffUtil.calculateDiff(BasicDiffCallback(old, emptyList())))
            val minRow = 0
            val maxRow = seatingsInfo.seats
                    .maxBy {
                        it.row
                    }?.row?:0 + 1
            val minCol = 0
            var maxCol = seatingsInfo.seats
                    .maxBy {
                        it.column
                    }?.column?:0 + 1
            if(maxCol==seatingsInfo.columns) {
                maxCol += 1
            }
            val data = mutableListOf<SeatInfo?>()
            for (i in minRow..maxRow) {
                for (j in minCol..maxCol) {
                    data.add(null)
                }
            }
            seatingsInfo
                    .seats
                    .forEach {
                        val row = it.row
                        val col = it.column
                        val index = (row * maxCol) + col
                        data[index] = it
                    }
            val dataPres = data.map {
                val type = when (it) {
                    null -> TYPE_EMPTY
                    else -> TYPE_SEAT
                }
                SeatPresentation(seatInfo = it,
                        viewType = type,
                        widthAndHeight = widthAndHeight,
                        selected = selected.contains(it))
            }
            return Data(
                    seats = dataPres,
                    totalRows = maxRow,
                    totalColumns = maxCol,
                    diffResult = DiffUtil.calculateDiff(BasicDiffCallback(old, dataPres))
            )
        }
    }
}

data class SeatPresentation(val seatInfo: SeatInfo? = null,
                            val viewType: Int,
                            val widthAndHeight: Int,
                            val selected: Boolean = false
) : ItemSame<SeatPresentation> {
    override fun contentsSameAs(same: SeatPresentation): Boolean {
        return hashCode() == same.hashCode()
    }

    override fun sameAs(same: SeatPresentation): Boolean {
        return seatInfo?.row == same.seatInfo?.row && seatInfo?.column == same.seatInfo?.column
    }

}

class EmptySeat(context: Context) : ImageView(context) {

    init {
        val spacing = resources.getDimension(R.dimen.seat_padding).toInt()
        val dp = resources.getDimension(R.dimen.seat_height).toInt()
        setPadding(spacing, spacing, spacing, spacing)
        layoutParams = ViewGroup.MarginLayoutParams(dp, dp)
    }

    fun bind() {

    }
}

class SeatView(context: Context) : ImageView(context) {

    var seat: SeatPresentation? = null
    var seatClickListener: SeatClickListener? = null

    init {
        val spacing = resources.getDimension(R.dimen.seat_padding).toInt()
        val dp = resources.getDimension(R.dimen.seat_height).toInt()
        setPadding(spacing, spacing, spacing, spacing)
        layoutParams = ViewGroup.MarginLayoutParams(dp, dp)
        setOnClickListener {
            seat?.let {
                if (it.seatInfo?.isWheelChairOrCompanion == true) {
                    showDialog(it)
                } else {
                    seatClickListener?.onClick(it)
                }
            }
        }
    }

    private fun showDialog(it: SeatPresentation) {
        @StringRes val message: Int
        @StringRes val title: Int
        when (it.seatInfo?.seatType) {
            SeatInfo.SeatType.SeatTypeWheelchair -> {
                title = R.string.dialog_select_seat_wheelchair_title
                message = R.string.dialog_select_seat_wheelchair_message
            }
            else -> {
                title = R.string.dialog_select_seat_companion_title
                message = R.string.dialog_select_seat_companion_message
            }
        }
        AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok) { _, _ -> seatClickListener?.onClick(it) }
                .setNegativeButton(R.string.cancel, null)
                .create()
                .show()
    }

    fun bind(seat: SeatPresentation, seatClickListener: SeatClickListener? = null) {
        this.seat = seat
        this.seatClickListener = seatClickListener
        seat.seatInfo?.let {
            setImageDrawable(SeatHelper.getDrawable(it, resources))
        }
        isEnabled = seat.seatInfo?.isAvailable ?: true
        isSelected = seat.selected
    }
}

interface SeatClickListener {
    fun onClick(seat: SeatPresentation)
}

interface SeatsSelectedListener {
    fun onSeatsSelected(seats: Set<SeatInfo>)
}

data class Data(
        val seats: List<SeatPresentation>,
        val totalRows: Int,
        val totalColumns: Int,
        val diffResult: DiffUtil.DiffResult
)