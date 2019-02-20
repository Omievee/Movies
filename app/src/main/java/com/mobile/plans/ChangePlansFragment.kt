package com.mobile.plans

import android.content.Context
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mobile.fragments.MPFragment
import com.mobile.profile.ProfileCancellationFragment
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_change_plans.*
import javax.inject.Inject


class ChangePlansFragment : MPFragment(), ChangePlansInt, View.OnClickListener, PlansInterface {
    override fun onPlanClicked(planUUID: String) {
        newPlanUUID = planUUID
    }

    override fun planUpdateSuccess() {

    }


    @Inject
    lateinit var presenter: ChangePlansPresenter

    var plansAdapter: PlansAdapter? = null
    var newPlanUUID: String? = null

    override fun displayCancellationFragment() {
        showFragment(ProfileCancellationFragment())
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            com.moviepass.R.id.changePlansButton -> presenter.changePlan(newPlanUUID ?: "")
            com.moviepass.R.id.backButton -> activity?.onBackPressed()
            com.moviepass.R.id.cancelMembership -> presenter.cancelClicked()
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


    override fun updateAdapter(current: PlanObject, plans: Array<PlanObject>?) {
        if (plans.isNullOrEmpty()) {
            plansRecycler.visibility = View.GONE
            noPlans.visibility = View.VISIBLE
        }
        val newL = mutableListOf<PlanObject>()
        current.let {
            current.current = true
            newL.add(it)
        }

        if (!plans.isNullOrEmpty()) {
            plans.forEach {
                it.current = false
                newL.add(it)
            }
        }
        newL.sortBy {
            it.installmentAmount
        }
        plansAdapter?.data = PlansPresentation(newL)
        plansRecycler.adapter = plansAdapter

    }

    override fun displayError() {

    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.destroyEverything()
    }

}

interface PlansInterface {
    fun onPlanClicked(planUUID: String)
}

class PlansPresentation(val list: List<PlanObject>)