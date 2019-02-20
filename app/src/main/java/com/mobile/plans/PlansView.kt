package com.mobile.plans

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.LinearLayoutManager
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import com.moviepass.R
import kotlinx.android.synthetic.main.list_item_plan_details.view.*
import kotlinx.android.synthetic.main.list_item_plans.view.*


class PlansView(context: Context?, attrs: AttributeSet? = null) : ConstraintLayout(context, attrs), View.OnClickListener {

    var plansInterface: PlansInterface? = null
    var plans: PlanObject? = null
    var adapter: PlansDetailAdapter? = null

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.planIcon -> {
                isSelected = true
            }
        }
    }

    init {
        View.inflate(context, com.moviepass.R.layout.list_item_plans, this)
        layoutParams = MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        planIcon.setOnClickListener(this)
    }

    private fun setUpFeaturesAdapter(plans: Array<String>) {
        plansDetails.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        adapter = plans.toList()?.let { PlansDetailAdapter(it) }
        plansDetails.adapter = adapter
    }


    fun bind(plans: PlanObject?) {
        this.plans = plans ?: return

        planTitle.text = plans.name
        val totalSpan = SpannableStringBuilder().apply {
            val span = SpannableString(plans.installmentAmount.toString()).apply {
                // setSpan(RelativeSizeSpan)
            }
            append("$")
            append(span)
            append(" /MO")
        }
        planPrice.text = totalSpan
        val currPlan = plans.current ?: false

        when (currPlan) {
            true -> {
                currentPlan.visibility = View.VISIBLE
                isSelected = true
            }
            else -> {
                currentPlan.visibility = View.GONE
                isSelected = false
            }
        }
        setUpFeaturesAdapter(plans?.features)
    }
}

class PlanDetailView(context: Context?, attrs: AttributeSet? = null) : ConstraintLayout(context, attrs) {
    init {
        View.inflate(context, com.moviepass.R.layout.list_item_plan_details, this)
        layoutParams = MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    fun bind(details: String) {
        detailText.text = details
    }
}