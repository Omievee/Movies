package com.mobile.plans

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import com.mobile.adapters.BaseViewHolder
import kotlinx.android.synthetic.main.list_item_plans.view.*


class PlansAdapter(

        val plansInterface: PlansInterface

) : RecyclerView.Adapter<BaseViewHolder>() {
    private var lastCheckedRB: RadioButton? = null

    var data: PlanData? = null
        set(value) {
            field = value
            value?.diffResult?.dispatchUpdatesTo(this) ?: notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return BaseViewHolder(PlansView(parent.context))
    }

    override fun getItemCount(): Int {
        return data?.pres?.size ?: 0
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val pres = data?.pres?.get(position)
        (holder.itemView as PlansView).plansInterface = plansInterface

        holder.itemView.bind(plans = pres?.availableList)
        if (pres?.current?.id == pres?.availableList?.id) {
            holder.itemView.planIcon.isChecked = true
            holder.itemView.currentPlan.visibility = View.VISIBLE
            lastCheckedRB = holder.itemView.planIcon
        }

        holder.itemView.radioGroup.setOnCheckedChangeListener { group, checkedId ->
            val s = group.findViewById(checkedId) as RadioButton
            if (lastCheckedRB?.isChecked!!) {
                lastCheckedRB?.isChecked = false
                s.isChecked = true
            }
            lastCheckedRB = s
        }
    }
}