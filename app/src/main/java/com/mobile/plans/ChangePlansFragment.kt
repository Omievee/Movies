package com.mobile.plans

import android.content.Context
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.mobile.fragments.MPFragment
import com.mobile.profile.AccountDetailsFragment
import com.mobile.profile.ProfileCancellationFragment
import com.moviepass.R
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_change_plans.*
import javax.inject.Inject


class ChangePlansFragment : MPFragment(), ChangePlansInt, View.OnClickListener {


    @Inject
    lateinit var presenter: ChangePlansPresenter

    var plansAdapter: PlansAdapter? = null


    override fun displayCancellationFragment() {
        showFragment(ProfileCancellationFragment())
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.changePlansButton -> presenter.changePlan()
            R.id.backButton -> activity?.onBackPressed()
            R.id.cancelMembership -> presenter.cancelClicked()
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_change_plans, container, false)
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


        presenter.onCreate()
        plansRecycler.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        plansAdapter = PlansAdapter()
    }

    override fun onResume() {
        super.onResume()
        presenter.onCreate()
    }


    override fun updateAdapter(plans: Array<PlanObject>?) {
        val p = plans
        if (p.isNullOrEmpty()) {
            plansRecycler.visibility = View.GONE
            noPlans.visibility = View.VISIBLE
        }

        plansAdapter?.data = plans?.toList()
        plansRecycler.adapter = plansAdapter

    }

    override fun displayError() {

    }

}
