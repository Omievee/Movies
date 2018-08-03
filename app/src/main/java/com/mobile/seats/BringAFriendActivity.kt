package com.mobile.seats

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.mobile.MPActivty
import com.mobile.model.*
import com.mobile.reservation.Checkin
import com.moviepass.R
import dagger.android.AndroidInjection

class BringAFriendActivity : MPActivty() {

    var bringAFriendFragment: BringAFriendFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bring_a_friend)
        val bringAFriendPayload = intent?.getParcelableExtra<Checkin>("data")
        bringAFriendPayload?.let {
            when (savedInstanceState) {
                null -> {
                    bringAFriendFragment = BringAFriendFragment.newInstance(it)
                    supportFragmentManager
                            .beginTransaction()
                            .add(R.id.fragmentContainer, bringAFriendFragment)
                            .commit()
                }
                else -> {
                    bringAFriendFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer) as? BringAFriendFragment
                }

            }
        }
    }

    companion object {
        fun newIntent(context: Context, checkIn: Checkin): Intent {
            return Intent(context, BringAFriendActivity::class.java).apply {
                putExtra("data", checkIn)
            }
        }
    }

    override fun onBackPressed() {
        bringAFriendFragment?.onBackPressed() ?: super.onBackPressed()
    }
}