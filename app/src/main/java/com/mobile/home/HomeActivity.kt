package com.mobile.home

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.content.ContextCompat
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.app.AlertDialog
import android.view.ViewGroup
import android.widget.Toast
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.mobile.BackFragment
import com.mobile.DeviceID
import com.mobile.MPActivty
import com.mobile.Primary
import com.mobile.activities.AutoActivatedCard
import com.mobile.activities.LogInActivity
import com.mobile.alertscreen.AlertScreenFragment
import com.mobile.analytics.AnalyticsManager
import com.mobile.fragments.MoviesFragment
import com.mobile.fragments.TheatersFragmentV2
import com.mobile.fragments.TicketVerificationV2
import com.mobile.history.HistoryDetailsFragment
import com.mobile.history.model.ReservationHistory
import com.mobile.model.Alert
import com.mobile.model.LogoutInfo
import com.mobile.model.PopInfo
import com.mobile.profile.ProfileFragment
import com.mobile.reservation.CurrentReservationV2
import com.mobile.reservation.ReservationActivity
import com.mobile.seats.MPBottomSheetFragment
import com.mobile.seats.SheetData
import com.mobile.utils.onBackExtension
import com.mobile.utils.showFragment
import com.moviepass.R
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.activity_home.*
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject


class HomeActivity : MPActivty(), HomeActivityView {

    @Inject
    lateinit var analyticsManager: AnalyticsManager

    @Inject
    lateinit var presenter: HomeActivityPresenter

    companion object {
        const val POSITION: String = "position"

        fun newIntent(context: Context, position: Int): Intent {
            return Intent(context, HomeActivity::class.java).putExtra(POSITION, position)
        }
    }

    var adapter: HomeViewPager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        adapter = HomeViewPager(supportFragmentManager)
        val black = Color.argb(200, Color.red(0), Color.green(0), Color.blue(0))
        bottomSheetNav.setBackgroundColor(Color.BLACK)
        bottomSheetNav.defaultBackgroundColor = black
        bottomSheetNav.setNotificationTextColorResource(R.color.drawer_item)
        val tabs = arrayOf(
                AHBottomNavigationItem(getString(R.string.menu_bottom_navigation_main_movies), R.drawable.bottom_navigation_camera
                ),
                AHBottomNavigationItem(getString(R.string.menu_bottom_navigation_main_theaters), R.drawable.bottom_navigation_theaters
                ),
                AHBottomNavigationItem(getString(R.string.menu_bottom_navigation_main_profile), R.drawable.bottom_navigation_account
                )

        )
        bottomSheetNav.accentColor = ContextCompat.getColor(this, R.color.red)
        bottomSheetNav.inactiveColor = ContextCompat.getColor(this, R.color.white_ish)
        bottomSheetNav.setColoredModeColors(ResourcesCompat.getColor(resources,R.color.red,theme), ResourcesCompat.getColor(resources,R.color.white_ish,theme))
        bottomSheetNav.isForceTint = true
        bottomSheetNav.setNotificationBackgroundColorResource(R.color.red)
        tabs.forEach {
            bottomSheetNav.addItem(it)
        }
        bottomSheetNav.setOnTabSelectedListener { position, wasSelected ->
            when (wasSelected) {
                true -> {
                    val currItem = adapter?.currentItem ?: true
                    val backable
                            : BackFragment = currItem as? BackFragment
                            ?: return@setOnTabSelectedListener true
                    backable.onBack()
                }
                else -> {
                    viewPager.currentItem = position
                    true
                }
            }
        }
        viewPager.offscreenPageLimit = 2
        viewPager.adapter = adapter

        presenter.onCreate()
    }

    override fun onResume() {
        super.onResume()
        checkGooglePlayServices()
        presenter.onDeviceId(DeviceID.getID(this))
        presenter.onResume()
    }

    override fun showPeakPassBadge() {
        bottomSheetNav.setNotification(" ", 2)
    }

    override fun showOverSoftCap() {
        MPBottomSheetFragment.newInstance(SheetData(
                title = resources.getString(R.string.continue_seeing_movies_title),
                description = resources.getString(R.string.continue_seeing_movies_description)
        )).show(supportFragmentManager, "")
    }

    override fun hidePeakPassBadge() {
        bottomSheetNav.setNotification("", 2)
    }

    override fun onPause() {
        super.onPause()
        presenter.onPause()
    }

    private fun checkGooglePlayServices(): Boolean {
        val context = this
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val result = googleApiAvailability.isGooglePlayServicesAvailable(context)
        val LINK_TO_GOOGLE_PLAY_SERVICES = "play.google.com/store/apps/details?id=com.google.android.gms&hl=en"

        if (result != ConnectionResult.SUCCESS) {
            AlertDialog.Builder(context).setMessage("Google Play Services must either be enabled or updated in order to continue")
                    .setPositiveButton("OK") { _, _ ->
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://$LINK_TO_GOOGLE_PLAY_SERVICES")))
                        finish()
                    }.setCancelable(false).show()
            return false
        }
        return true
    }

    override fun showForceLogout(it: LogoutInfo) {
        AlertDialog.Builder(this)
                .setMessage(it.getMessage())
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    startActivity(Intent(this, LogInActivity::class.java));
                    finishAffinity();
                }.show()
    }

    override fun onBackPressed() {
        if (onBackExtension()) {
            return
        }

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
            else -> currentItem = viewPager.currentItem - 1
        }
    }

    override fun showAlert(it: Alert) {
        showFragment(AlertScreenFragment.newInstance(it))
    }


    override fun showTicketVerification(it: PopInfo) {
        showFragment(TicketVerificationV2.newInstance(it))
    }

    override fun showConfirmationScreen(it: CurrentReservationV2) {
        val context = this
        startActivity(ReservationActivity.newInstance(context, it))
    }

    override fun showActivatedCardScreen() {
        val activate = Intent(this@HomeActivity, AutoActivatedCard::class.java)
        startActivity(activate)

    }

    override fun showHistoryRateScreen(reservationHistory: ReservationHistory) {
        showFragment(HistoryDetailsFragment.newInstance(reservationHistory, true))
    }

    var currentItem: Int = 0
        set(value) {
            field = value
            viewPager.currentItem = value
            bottomSheetNav.currentItem = value

        }

    override fun logout() {
        val logUserOutIntent = Intent(this, LogInActivity::class.java)
        startActivity(logUserOutIntent)
        finishAffinity()
        Toast.makeText(this, R.string.no_longer_authorized, Toast.LENGTH_LONG).show()
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
        map.put(theaters, TheatersFragmentV2())
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
            fragment !== currentItem && (fragment is Fragment) && fragment.isAdded -> {
                currentItem = fragment
                (currentItem as? Primary)?.onPrimary()
            }
        }
    }

    override fun getCount(): Int {
        return frags.size
    }

}