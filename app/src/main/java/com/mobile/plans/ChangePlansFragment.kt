package com.mobile.plans

import android.content.Context
import android.os.Bundle
import android.support.design.widget.BottomSheetDialog
import android.support.v7.util.DiffUtil
import android.support.v7.util.DiffUtil.calculateDiff
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.mobile.adapters.BasicDiffCallback
import com.mobile.adapters.ItemSame
import com.mobile.fragments.MPFragment
import com.mobile.profile.ProfileCancellationFragment
import com.mobile.widgets.MPProgressButton
import com.moviepass.R
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_change_plans.*
import javax.inject.Inject


class ChangePlansFragment : MPFragment(), ChangePlansInt, View.OnClickListener, PlansInterface {

    @Inject
    lateinit var presenter: ChangePlansPresenter

    var plansAdapter: PlansAdapter? = null

    override fun displayBottomSheetFragment(selectedPlan: PlanObject) {
        val dialog = BottomSheetDialog(context ?: return)
        val sheetView = activity?.layoutInflater?.inflate(R.layout.fragment_change_plans_bottom_sheet, null)
        val planName = selectedPlan.name

        sheetView?.findViewById<TextView>(R.id.planText)?.text = """${getString(R.string.change_plan_to)} $planName """
        sheetView?.findViewById<TextView>(R.id.disclaimer)?.text = resources.getString(R.string.change_plan_upgrade, ((selectedPlan?.asDollars)))
        sheetView?.findViewById<MPProgressButton>(R.id.submit)?.setOnClickListener {
            presenter.changePlan()
        }

        dialog.setContentView(sheetView)
        dialog.show()
    }

    override fun onPlanSelected(selectedPlan: PlanObject) {
        presenter.onPlanSelected(selectedPlan)
    }


    override fun planUpdateSuccess(msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        activity?.onBackPressed()
    }

 
    override fun displayCancellationFragment() {
        showFragment(ProfileCancellationFragment())
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.changePlansButton -> presenter.onChangePlansClicked()
            R.id.backButton -> activity?.onBackPressed()
            R.id.cancelMembership -> presenter.cancelClicked()
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(com.moviepass.R.layout.fragment_change_plans, container, false)
    }


    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.onCreate()

        changePlansButton.setOnClickListener(this)
        cancelMembership.setOnClickListener(this)
        backButton.setOnClickListener(this)

        plansRecycler.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        plansAdapter = PlansAdapter(this)
    }

    override fun onResume() {
        super.onResume()
        presenter.onResume()
    }


    override fun updateAdapter(current: PlanObject, planToUse:PlanObject, plans: List<PlanObject>) {
        val old = plansAdapter?.data?.pres ?: emptyList()
        val newD = mutableListOf<PlansPresentation>()
        if (!plans.isNullOrEmpty()) {
            plans.forEach {
                newD.add(PlansPresentation(data = it, currentSelected = current,selected=it==planToUse))
            }
        } else {
            plansRecycler.visibility = View.GONE
            noPlans.visibility = View.VISIBLE
        }
        plansAdapter?.data = PlanData(pres = newD, diffResult = calculateDiff(BasicDiffCallback<PlansPresentation>(old, newD)))
        plansRecycler.adapter = plansAdapter
    }

    override fun displayError(err: String) {
        Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.onDestroy()
    }

}

class PlanData(val pres: List<PlansPresentation>, val diffResult: DiffUtil.DiffResult)

data class PlansPresentation(val data: PlanObject, val currentSelected: PlanObject, val selected:Boolean) : ItemSame<PlansPresentation> {
    override fun sameAs(same: PlansPresentation): Boolean {
        return equals(same)
    }

    override fun contentsSameAs(same: PlansPresentation): Boolean {
        return hashCode() == same.hashCode()
    }
}

interface PlansInterface {
    fun onPlanSelected(selectedPlan: PlanObject)
}