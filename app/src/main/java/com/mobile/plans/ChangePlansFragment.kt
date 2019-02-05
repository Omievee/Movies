package com.mobile.plans

import android.content.Context
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mobile.fragments.MPFragment
import com.mobile.profile.ProfileCancellationFragment
import com.moviepass.R
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_change_plans.*
import javax.inject.Inject


class ChangePlansFragment : MPFragment(), ChangePlansInt {


    @Inject
    lateinit var presenter: ChangePlansPresenter

    var plansAdapter:PlansAdapter?=null

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


        changePlansButton.setOnClickListener {
            presenter.changePlan()
        }

        backButton.setOnClickListener {
            activity?.onBackPressed()
        }
        cancelMembership.setOnClickListener {
            showFragment(ProfileCancellationFragment())
        }
      //  plansRecycler.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
    }

}
