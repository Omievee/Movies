package com.mobile.plans

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.mobile.adapters.BaseViewHolder
import com.mobile.history.model.ReservationHistory

class PlansAdapter(


) : RecyclerView.Adapter<BaseViewHolder>() {

    var data: List<PlanObject>? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return BaseViewHolder(PlansView(parent.context))
    }

    override fun getItemCount(): Int {

        return data?.size ?: 0
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        (holder.itemView as PlansView).bind()
    }
}