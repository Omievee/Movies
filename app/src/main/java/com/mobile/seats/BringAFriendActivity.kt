package com.mobile.seats

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.support.v4.app.FragmentActivity
import com.mobile.MPActivty
import com.mobile.model.*
import com.moviepass.R
import dagger.android.AndroidInjection
import kotlinx.android.parcel.Parcelize

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
        fun newIntent(context: Context, theater: Theater, screening: Screening, showtime: String): Intent {
            return Intent(context, BringAFriendActivity::class.java).apply {
                putExtra("data", BringAFriendPayload(
                        theater = from(theater),
                        screening = screening,
                        showtime = showtime
                ))
            }
        }
    }

    override fun onBackPressed() {
        bringAFriendFragment?.onBackPressed() ?: super.onBackPressed()
    }
}

@Parcelize
data class BringAFriendPayload(
        var theater: Theater2,
        var screening: Screening,
        var showtime: String
) : Parcelable