package com.mobile.plans

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.mobile.adapters.BaseViewHolder

class PlansDetailAdapter(val details: List<String>)
    : RecyclerView.Adapter<BaseViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return BaseViewHolder(PlanDetailView(parent.context))
    }

    override fun getItemCount(): Int {
        return details.size
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        (holder.itemView as PlanDetailView).bind(details[position])
    }
}