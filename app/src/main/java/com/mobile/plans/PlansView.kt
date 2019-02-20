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
import com.moviepass.R
import kotlinx.android.synthetic.main.fragment_change_plans_bottom_sheet.view.*
import kotlinx.android.synthetic.main.list_item_plan_details.view.*
import kotlinx.android.synthetic.main.list_item_plans.view.*


class PlansView(context: Context?, attrs: AttributeSet? = null) : ConstraintLayout(context, attrs), View.OnClickListener {
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.planIcon -> plansInterface?.onPlanSelected(plans ?: return)
        }
    }
    var plansInterface: PlansInterface? = null
    var plans: PlanObject? = null
    var adapter: PlansDetailAdapter? = null

    init {
        View.inflate(context, com.moviepass.R.layout.list_item_plans, this)
        layoutParams = MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        planIcon.setOnClickListener(this)
    }


    fun bind(plans: PlanObject?) {
        this.plans = plans ?: return
        planTitle.text = plans.name

        setUpFeaturesAdapter(plans.features)

        val span = SpannableStringBuilder()
        span.append(plans.asDollars).setSpan(RelativeSizeSpan(1.4f), 0, plans.installmentAmount.toString().length + 1, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
        val s = SpannableStringBuilder()
        s.append("$".superscript).setSpan(RelativeSizeSpan(.8f), 0, 1, SpannableString.SPAN_INCLUSIVE_EXCLUSIVE)
        s.append(span)
        s.append(" /MO".rel)
        planPrice.text = s
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
        spanstr.setSpan(RelativeSizeSpan(.7f), 0, length, SpannableString.SPAN_INCLUSIVE_EXCLUSIVE)
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
