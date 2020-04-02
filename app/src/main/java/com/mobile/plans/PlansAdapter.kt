package com.mobile.plans

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import com.mobile.adapters.BaseViewHolder
import kotlinx.android.synthetic.main.list_item_plan.view.*


class PlansAdapter(

        val plansInterface: PlansInterface

) : RecyclerView.Adapter<BaseViewHolder>() {

    var data: PlanData? = null
        set(value) {
            field = value
            value?.diffResult?.dispatchUpdatesTo(this) ?: notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return BaseViewHolder(PlanView(parent.context))
    }

    override fun getItemCount(): Int {
        return data?.pres?.size ?: 0
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val pres = data?.pres?.get(position)?:return
        (holder.itemView as PlanView).plansInterface = plansInterface

        holder.itemView.bind(pres)
    }
}