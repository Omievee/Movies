package com.mobile.home

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v4.content.res.ResourcesCompat
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.doubleclick.PublisherAdRequest
import com.google.android.gms.ads.formats.NativeCustomTemplateAd
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.mobile.BackFragment
import com.mobile.DeviceID
import com.mobile.MPActivty
import com.mobile.UserPreferences
import com.mobile.activities.AutoActivatedCard
import com.mobile.activities.LogInActivity
import com.mobile.alertscreen.AlertScreenFragment
import com.mobile.fragments.TicketVerificationV2
import com.mobile.history.HistoryDetailsActivity
import com.mobile.history.model.ReservationHistory
import com.mobile.model.Alert
import com.mobile.model.LogoutInfo
import com.mobile.model.PopInfo
import com.mobile.reservation.CurrentReservationV2
import com.mobile.reservation.ReservationActivity
import com.mobile.seats.MPBottomSheetFragment
import com.mobile.seats.SheetData
import com.mobile.utils.children
import com.mobile.utils.forEachIndexed
import com.mobile.utils.onBackExtension
import com.mobile.utils.showFragment
import com.mobile.widgets.MPAlertDialog
import com.moviepass.R
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.activity_home.*
import javax.inject.Inject


class HomeActivity : MPActivty(), HomeActivityView {
    override fun showWhiteListMovie() {
        MPBottomSheetFragment.newInstance(SheetData(
                title = resources.getString(R.string.bonus_movies_title),
                description = resources.getString(R.string.bonus_movies_description)
        )).show(supportFragmentManager, "")
    }


    @Inject
    lateinit var presenter: HomeActivityPresenter

    companion object {
        const val POSITION: String = "position"

        fun newIntent(context: Context, position: Int): Intent {
            return Intent(context, HomeActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    .putExtra(POSITION, position)
        }

        fun newIntent(context: Context, uri: Uri?): Intent {
            return Intent(context, HomeActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP).apply {
                data = uri
            }
        }
    }

    var adapter: HomeViewPager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        //com.mobile.extensions.KeyboardUtil(this,window.findViewById(android.R.id.content))
        setContentView(R.layout.activity_home)
        bottomSpoof.visibility = View.VISIBLE
        bottomSheetNav.setDefaultBackgroundResource(R.color.bottomNav)

        MobileAds.initialize(this, getString(R.string.app_ad_id))
        adapter = HomeViewPager(supportFragmentManager)
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
        bottomSheetNav.setColoredModeColors(ResourcesCompat.getColor(resources, R.color.red, theme), ResourcesCompat.getColor(resources, R.color.white_ish, theme))
        bottomSheetNav.isForceTint = true
        bottomSheetNav.setNotificationBackgroundColorResource(R.color.red)
        val activity = this
        var idsSet = false
        bottomSheetNav.addOnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            if (idsSet) {
                return@addOnLayoutChangeListener
            }
            val group = bottomSheetNav.children { index, view ->
                view is LinearLayout
            }.firstOrNull() as? ViewGroup
            group?.forEachIndexed { index, view ->
                idsSet = true
                view.contentDescription = tabs[index].getTitle(activity)
                view.id = when (index) {
                    0 -> R.id.movies
                    1 -> R.id.theaters
                    else -> R.id.profile
                }
            }
        }
        bottomSheetNav.addItems(tabs.toList())

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
                description = resources.getString(R.string.continue_seeing_movies_description, UserPreferences.restrictions.cappedPlan?.used
                        ?: 3)
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
            MPAlertDialog(context)
                    .setMessage("Google Play Services must either be enabled or updated in order to continue")
                    .setPositiveButton("OK") { _, _ ->
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://$LINK_TO_GOOGLE_PLAY_SERVICES")))
                        finish()
                    }.setCancelable(false).show()
            return false
        }
        return true
    }

    override fun showForceLogout(it: LogoutInfo) {
        MPAlertDialog(this)
                .setMessage(it.getMessage())
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    startActivity(Intent(this, LogInActivity::class.java));
                    finishAffinity()
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
        startActivity(HistoryDetailsActivity.newInstance(this, historyItem = reservationHistory))
    }


    override fun updateBottomNavForMovies() {
        if (viewPager.currentItem != adapter?.movies) {
            adapter?.movies?.let { viewPager.setCurrentItem(it, true) }
            adapter?.movies?.let { bottomSheetNav.currentItem = it }
        }

    }

    override fun updateBottomNavForTheaters() {
        if (viewPager.currentItem != adapter?.theaters) {
            adapter?.theaters?.let { viewPager.setCurrentItem(it, true) }
            adapter?.theaters?.let { bottomSheetNav.currentItem = it }
        }
    }


    private var currentItem: Int = 0
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