package com.mobile.home

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.constraint.ConstraintSet
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import com.jaeger.library.StatusBarUtil
import com.mobile.BackFragment
import com.mobile.Primary
import com.mobile.fragments.MoviesFragment
import com.mobile.fragments.ProfileFragment
import com.mobile.fragments.TheatersFragment
import com.moviepass.R
import kotlinx.android.synthetic.main.activity_home.*
import java.util.concurrent.atomic.AtomicInteger

class HomeActivity : FragmentActivity() {

    companion object {
        const val POSITION:String = "position"

        fun newIntent(context: Context, position: Int):Intent {
            return Intent(context, HomeActivity::class.java).putExtra(POSITION, position)
        }
    }

    var adapter: HomeViewPager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        adapter = HomeViewPager(supportFragmentManager)
        bottomSheetNav.setOnNavigationItemSelectedListener {

            viewPager.currentItem = when (it.itemId) {
                R.id.action_movies -> adapter?.movies
                R.id.action_theaters -> adapter?.theaters
                else -> adapter?.profile
            } ?: 0
            true
        }
        viewPager.adapter = adapter
    }

    override fun onBackPressed() {
        val currItem = adapter?.currentItem ?: return back()
        val backable: BackFragment = currItem as? BackFragment ?: return back()
        when (backable.onBack()) {
            true -> return
            else -> back()
        }
    }

    private fun back() {
        when (viewPager.currentItem) {
            0 -> super.onBackPressed()
            else -> currentItem = viewPager.currentItem-1
        }
    }

    var currentItem: Int = 0
        set(value) {
            field = value
            viewPager.currentItem = value
            bottomSheetNav.selectedItemId = when (value) {
                adapter?.movies -> {
                    R.id.action_movies
                }
                adapter?.theaters -> {
                    R.id.action_theaters
                }
                adapter?.profile -> {
                    R.id.action_profile
                }
                else -> {
                    R.id.action_movies
                }
            }
        }
}

class HomeViewPager(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    var currentItem: Fragment? = null

    val frags by lazy {
        val map = mutableMapOf<Int, Fragment>()
        val position = AtomicInteger()
        movies = position.getAndIncrement()
        theaters = position.getAndIncrement()
        profile = position.getAndIncrement()
        map.put(movies, MoviesFragment())
        map.put(theaters, TheatersFragment())
        map.put(profile, ProfileFragment())
        map
    }

    var movies: Int = 0
    var theaters: Int = 0
    var profile: Int = 0

    override fun getItem(position: Int): Fragment {
        return frags.get(position)!!
    }

    override fun setPrimaryItem(container: ViewGroup, position: Int, fragment: Any) {
        super.setPrimaryItem(container, position, fragment)
        when {
            fragment !== currentItem -> {
                currentItem = fragment as? Fragment
                (currentItem as? Primary)?.onPrimary()
            }
        }
    }

    override fun getCount(): Int {
        return frags.size
    }

}