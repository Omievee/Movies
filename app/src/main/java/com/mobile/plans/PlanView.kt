package com.mobile.plans

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.LinearLayoutManager
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.RelativeSizeSpan
import android.text.style.SuperscriptSpan
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.list_item_plan_details.view.*
import kotlinx.android.synthetic.main.list_item_plan.view.*


class PlanView(context: Context?, attrs: AttributeSet? = null) : ConstraintLayout(context, attrs), View.OnClickListener {
    override fun onClick(v: View?) {
        plansInterface?.onPlanSelected(plan ?: return)
    }
    var plansInterface: PlansInterface? = null
    var plan: PlanObject? = null
    var adapter: PlansDetailAdapter? = null

    init {
        View.inflate(context, com.moviepass.R.layout.list_item_plan, this)
        layoutParams = MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        setOnClickListener(this)
    }

    fun bind(plans: PlansPresentation) {
        val plan = plans.data ?: return
        this.plan = plan
        planTitle.text = "MoviePass ${plan.name}"

        setUpFeaturesAdapter(plan.features)

        val span = SpannableStringBuilder()
        span.append(plan.asDollars).setSpan(RelativeSizeSpan(1.1f), 0, plan.installmentAmount.toString().length + 1, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
        val s = SpannableStringBuilder()
        s.append("$ ".superscript).setSpan(RelativeSizeSpan(.6f), 0, 1, SpannableString.SPAN_INCLUSIVE_EXCLUSIVE)
        s.append(span)
        s.append(" /MO".rel)
        planPrice.text = s
        planIcon.isSelected = plans.selected
    }

    private fun setUpFeaturesAdapter(plans: Array<String>) {
        plansDetails.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        adapter = PlansDetailAdapter(plans.toList())
        plansDetails.adapter = adapter
    }

}

private val String.superscript: SpannableString
    get() {
        val spanstr = SpannableString(this)
        spanstr.setSpan(SuperscriptSpan(), 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        return spanstr
    }

private val String.rel: SpannableString
    get() {
        val spanstr = SpannableString(this)
        spanstr.setSpan(RelativeSizeSpan(.6f), 0, length, SpannableString.SPAN_INCLUSIVE_EXCLUSIVE)
        return spanstr
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
