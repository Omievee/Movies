package com.moviepass.listeners

import com.moviepass.model.Plan

/**
 * Created by anubis on 6/13/17.
 */

interface PlanClickListener {

    fun onPlanClick(pos: Int, plan: Plan)

}
