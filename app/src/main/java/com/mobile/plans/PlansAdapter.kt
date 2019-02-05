package com.mobile.plans

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.mobile.adapters.BaseViewHolder

class PlansAdapter(


) : RecyclerView.Adapter<BaseViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return BaseViewHolder(PlansView(parent.context))
    }

    override fun getItemCount(): Int {
        return 0
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        (holder.itemView as PlansView).bind()
    }
}