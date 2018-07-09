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
        val bringAFriendPayload = intent?.getParcelableExtra<BringAFriendPayload>("data")
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
        fun newIntent(context: Context, theater: Theater, screening: Screening, availability: Availability): Intent {
            return Intent(context, BringAFriendActivity::class.java).apply {
                putExtra("data", BringAFriendPayload(
                        theater = theater,
                        screening = screening,
                        availability = availability
                ))
            }
        }

        fun newIntent(context: Context, checkIn:Checkin) : Intent {
            val theater = checkIn.theater
            val screening = checkIn.screening
            val availability = checkIn.availability
            return newIntent(context, theater, screening, availability)
        }
    }

    override fun onBackPressed() {
        bringAFriendFragment?.onBackPressed() ?: super.onBackPressed()
    }
}