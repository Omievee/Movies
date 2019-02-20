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

    var lastCheckedRB: RadioButton? = null

    var data: PlansPresentation? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return BaseViewHolder(PlansView(parent.context))
    }

    override fun getItemCount(): Int {
        return data?.list?.size ?: 0
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        (holder.itemView as PlansView).plansInterface = plansInterface
        holder.itemView.bind((data?.list?.get(position)))

//        holder.itemView.radioGroup.setOnCheckedChangeListener { group, checkedId ->
//            val checked_rb = group.findViewById<View>(checkedId) as RadioButton
//            if (lastCheckedRB != null) {
//                lastCheckedRB?.isChecked = false
//            }
//            lastCheckedRB = checked_rb
//        }


    }

}