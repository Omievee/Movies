package com.mobile.home

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v7.app.AlertDialog
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.mobile.*
import com.mobile.activities.LogInActivity
import com.mobile.fragments.*
import com.mobile.model.Alert
import com.mobile.model.LogoutInfo
import com.mobile.model.PopInfo
import com.mobile.reservation.CurrentReservationV2
import com.mobile.reservation.ReservationActivity
import com.mobile.utils.onBackExtension
import com.mobile.utils.showFragment
import com.moviepass.R
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.activity_home.*
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject


class HomeActivity : MPActivty(), HomeActivityView {


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
        bottomSheetNav.setOnNavigationItemSelectedListener {

            viewPager.currentItem = when (it.itemId) {
                R.id.action_movies -> adapter?.movies
                R.id.action_theaters -> adapter?.theaters
                else -> adapter?.profile
            } ?: 0
            true
        }
        val black = Color.argb(200, Color.red(0), Color.green(0), Color.blue(0))
        bottomSheetNav.setBackgroundColor(black)
        bottomSheetNav.setOnNavigationItemReselectedListener {
            val currItem = adapter?.currentItem ?: return@setOnNavigationItemReselectedListener
            val backable: BackFragment = currItem as? BackFragment
                    ?: return@setOnNavigationItemReselectedListener
            backable.onBack()
        }
        viewPager.offscreenPageLimit = 2
        viewPager.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        checkGooglePlayServices()
        presenter.onDeviceId(DeviceID.getID(this))
        presenter.onResume()


    }

    private fun checkGooglePlayServices(): Boolean {
        val context = this
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val result = googleApiAvailability.isGooglePlayServicesAvailable(context)
        val LINK_TO_GOOGLE_PLAY_SERVICES = "play.google.com/store/apps/details?id=com.google.android.gms&hl=en"

        if (result != ConnectionResult.SUCCESS) {
            AlertDialog.Builder(context).setMessage("Google Play Services must either be enabled or updated in order to continue")
                    .setPositiveButton("OK", { dialog, which ->
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://$LINK_TO_GOOGLE_PLAY_SERVICES")))
                        finish()
                    }).setCancelable(false).show()
            return false
        }
        return true
    }

    override fun showForceLogout(it: LogoutInfo) {
        AlertDialog.Builder(this)
                .setMessage(it.getMessage())
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok) { dialog, which ->
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
        TicketVerificationDialog
                .newInstance(it)
                .show(supportFragmentManager, "ticketVerification")
    }

    override fun showConfirmationScreen(it: CurrentReservationV2) {
        val context = this
        startActivity(ReservationActivity.newInstance(context, it))
    }

    override fun showActivatedCardScreen() {
        if (!UserPreferences.getHasUserSeenCardActivationScreen() ) {
            showFragment(AutoActivatedCardFragment.newInstance())
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

    override fun logout() {
        val logUserOutIntent = Intent(this, LogInActivity::class.java)
        startActivity(logUserOutIntent);
        finishAffinity();
        Toast.makeText(this, R.string.no_longer_authorized, Toast.LENGTH_LONG).show();
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