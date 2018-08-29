package com.mobile.profile

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mobile.ApiError
import com.mobile.Primary
import com.mobile.UserPreferences
import com.mobile.activities.ActivatedCardTutorialActivity
import com.mobile.activities.LogInActivity
import com.mobile.analytics.AnalyticsManager
import com.mobile.fragments.AccountDetailsFragment
import com.mobile.fragments.MPFragment
import com.mobile.helpshift.HelpshiftHelper
import com.mobile.history.PastReservationsFragment
import com.mobile.loyalty.LoyaltyProgramFragment
import com.mobile.network.Api
import com.mobile.recycler.decorator.SpaceDecorator
import com.mobile.referafriend.ReferAFriendFragment
import com.mobile.reservation.ReservationActivity
import com.mobile.session.SessionManager
import com.mobile.utils.startIntentIfResolves
import com.moviepass.R
import dagger.android.support.AndroidSupportInjection
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_profile.*
import javax.inject.Inject

class ProfileFragment : MPFragment(), Primary {


    @Inject
    lateinit var analyticsManager: AnalyticsManager

    private val clickListener: ProfileClickListener = object : ProfileClickListener {
        override fun onClick(pres: ProfilePresentation) {
            when (pres.type) {
                Profile.ACCOUNT_DETAILS -> showFragment(AccountDetailsFragment())
                Profile.CURRENT_RESERVATION -> showCurrentReservation()
                Profile.HISTORY -> showFragment(PastReservationsFragment())
                Profile.REFER_A_FRIEND -> showFragment(ReferAFriendFragment())
                Profile.LOYALTY_PROGRAMS -> showFragment(LoyaltyProgramFragment.newInstance())
                Profile.HOW_TO_USE_MOVIEPASS -> navigateTo(ActivatedCardTutorialActivity.newIntent(activity))
                Profile.HELP -> onHelpClicked()
                Profile.LINK -> navigateTo(Intent(Intent.ACTION_VIEW, Uri.parse(pres.link)))
                Profile.SIGN_OUT -> onLogout()
                Profile.CLEAR_FLAGS -> UserPreferences.clearOutEverythingButUser()
                else -> {
                }
            }
        }
    }

    private val toggleListener: ProfileToggleListener = object : ProfileToggleListener {
        override fun onToggle(pres: ProfilePresentation) {
            when (pres.type) {

                Profile.NOTIFICATIONS -> {
                    UserPreferences.pushPermission = pres.toggled
                    analyticsManager.onUserChangedNotificationsSubscriptions(pres.toggled)
                }

                else -> {
                }
            }
            adapter.data = ProfileAdapter.createData(adapter.data, resources)
        }

    }

    @Inject
    lateinit var api: Api

    @Inject
    lateinit var sessionManager: SessionManager

    val adapter = ProfileAdapter(this, clickListener, toggleListener)

    val compositeSub = CompositeDisposable()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onAttach(context: Context?) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        compositeSub.dispose()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
        recyclerView.itemAnimator = null
        recyclerView.addItemDecoration(SpaceDecorator(lastBottom = resources.getDimension(R.dimen.bottom_navigation_height).toInt()))
        adapter.data = ProfileAdapter.createData(adapter.data, resources)
    }

    private fun onHelpClicked() {
        val activity = activity ?: return
        HelpshiftHelper.startHelpshiftConversation(activity)
    }

    private fun navigateTo(intent: Intent) {
        activity?.startIntentIfResolves(intent)
    }

    private fun showCurrentReservation() {
        compositeSub.clear()
        compositeSub.add(
                api.lastReservation()
                        .subscribe { r, e ->
                            val activity = activity ?: return@subscribe
                            when {
                                r != null -> startActivity(ReservationActivity.newInstance(
                                        activity, r, true
                                ))
                                e != null -> showSnackbar(e)
                            }
                        }
        )
    }

    override fun onPrimary() {
        UserPreferences.showPeakPassBadge = true
    }

    fun showSnackbar(e: Throwable) {
        val view = view ?: return
        val context = activity ?: return
        val snack = Snackbar.make(view, when {
            e is ApiError && e.httpErrorCode == 404 -> context.getString(R.string.reservation_not_found)
            e is ApiError -> e.error.message
            else -> context.getString(R.string.error)
        }, Snackbar.LENGTH_SHORT)
        snack.show()
    }

    private fun onLogout() {
        sessionManager.logout()
        val intent = Intent(activity, LogInActivity::class.java)
        startActivity(intent)
        activity?.finishAffinity()
    }
}