package com.mobile.profile

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.util.DiffUtil
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mobile.ApiError
import com.mobile.Primary
import com.mobile.UserPreferences
import com.mobile.activities.ActivatedCardTutorialActivity
import com.mobile.activities.LogInActivity
import com.mobile.adapters.BaseViewHolder
import com.mobile.adapters.BasicDiffCallback
import com.mobile.adapters.ItemSame
import com.mobile.fragments.AccountDetailsFragment
import com.mobile.fragments.MPFragment
import com.mobile.fragments.ProfileAccountInformation
import com.mobile.helpshift.HelpshiftHelper
import com.mobile.referafriend.ReferAFriendFragment
import com.mobile.history.PastReservationsFragment
import com.mobile.loyalty.LoyaltyProgramFragment
import com.mobile.network.Api
import com.mobile.recycler.decorator.SpaceDecorator
import com.mobile.reservation.ReservationActivity
import com.mobile.session.SessionManager
import com.mobile.utils.startIntentIfResolves
import com.moviepass.BuildConfig
import com.moviepass.R
import dagger.android.support.AndroidSupportInjection
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_profile.*
import javax.inject.Inject

class ProfileFragmentV2 : MPFragment(), Primary {

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
                Profile.LINK-> navigateTo(Intent(Intent.ACTION_VIEW, Uri.parse(pres.link)))
                Profile.SIGN_OUT -> onLogout()
                Profile.CLEAR_FLAGS -> UserPreferences.clearOutEverythingButUser()
                else-> {}
            }
        }
    }

    private val toggleListener:ProfileToggleListener = object : ProfileToggleListener {
        override fun onToggle(pres: ProfilePresentation) {
            when(pres.type) {
                Profile.NOTIFICATIONS-> UserPreferences.pushPermission = pres.toggled
                else-> { }
            }
            adapter.data = ProfileAdapter.createData(adapter.data,resources)
        }

    }

    @Inject
    lateinit var api: Api

    @Inject
    lateinit var sessionManager: SessionManager

    val adapter = ProfileAdapter(clickListener, toggleListener)

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

class ProfileAdapter(val clickListener: ProfileClickListener, val toggleListener:ProfileToggleListener?=null) : RecyclerView.Adapter<BaseViewHolder>() {

    var data: ProfileData? = null
        set(value) {
            field = value
            field?.diffResult?.dispatchUpdatesTo(this)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return BaseViewHolder(when (viewType) {
            Profile.NOTIFICATIONS.ordinal -> ProfileCheckBox(context = parent.context)
            Profile.LINK.ordinal-> ProfileLinkView(context = parent.context)
            else -> ProfileView(context = parent.context)
        })
    }

    override fun getItemViewType(position: Int): Int {
        val pres = data!!.data[position]
        return when (pres.type) {
            Profile.NOTIFICATIONS -> Profile.NOTIFICATIONS.ordinal
            Profile.LINK,Profile.SIGN_OUT -> Profile.LINK.ordinal
            else -> 0
        }
    }

    override fun getItemCount(): Int {
        return data?.data?.size ?: 0
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val view = holder.itemView
        val pres = data!!.data[position]
        when (view) {
            is ProfileView -> view.bind(pres, clickListener)
            is ProfileCheckBox -> view.bind(pres, toggleListener)
            is ProfileLinkView->view.bind(pres, clickListener)
        }
    }

    companion object {
        fun createData(last: ProfileData?, r: Resources): ProfileData {
            val old = last?.data ?: emptyList()
            val debugs = when(BuildConfig.DEBUG) {
                true-> listOf(ProfilePresentation(
                        type = Profile.CLEAR_FLAGS,
                        title = "Clear App Data"))
                false-> emptyList()
            }
            val newData = mutableListOf(
                    ProfilePresentation(
                            type = Profile.ACCOUNT_DETAILS,
                            header = r.getString(R.string.profile),
                            subHeader = when(UserPreferences.hasNewPeakPass) {
                                true-> r.getString(R.string.peak_pass_added)
                                false-> null
                            },
                            title = r.getString(R.string.account_details)

                    ),
                    ProfilePresentation(
                            type = Profile.CURRENT_RESERVATION,
                            title = r.getString(R.string.current_reservation)
                    ),
                    ProfilePresentation(
                            type = Profile.HISTORY,
                            title = r.getString(R.string.history)
                    ),
                    ProfilePresentation(
                            type = Profile.REFER_A_FRIEND,
                            title = r.getString(R.string.refer_a_friend)
                    ),
                    ProfilePresentation(
                            type = Profile.LOYALTY_PROGRAMS,
                            title = r.getString(R.string.loyalty_programs)
                    ),
                    ProfilePresentation(
                            type = Profile.HOW_TO_USE_MOVIEPASS,
                            header = r.getString(R.string.help),
                            title = r.getString(R.string.how_to_use_moviepass)
                    ),
                    ProfilePresentation(
                            type = Profile.HELP,
                            title = r.getString(R.string.help)
                    ),
                    ProfilePresentation(
                            type = Profile.NOTIFICATIONS,
                            title = r.getString(R.string.notifications),
                            toggled = UserPreferences.pushPermission
                    ),
                    ProfilePresentation(
                            type = Profile.LINK,
                            title = r.getString(R.string.terms_of_service),
                            link = "https://www.moviepass.com/terms"

                    ),
                    ProfilePresentation(
                            type = Profile.LINK,
                            title = r.getString(R.string.privacy_policy),
                            link = "https://www.moviepass.com/privacy/"
                    ),
                    ProfilePresentation(
                            type = Profile.SIGN_OUT,
                            title = r.getString(R.string.sign_out)
                    ),
                    ProfilePresentation(
                            type = Profile.VERSION,
                            title = "App Version: ${BuildConfig.VERSION_NAME} Build: ${BuildConfig.VERSION_CODE}"
                    )
            ).apply {
                addAll(debugs)
            }
            return ProfileData(newData, DiffUtil.calculateDiff(BasicDiffCallback(old, newData)))
        }
    }
}

data class ProfilePresentation(
        val type: Profile,
        val header: String? = null,
        val title: String? = null,
        val subHeader: String? = null,
        var toggled: Boolean = false,
        val link:String? = null
) : ItemSame<ProfilePresentation> {
    override fun sameAs(same: ProfilePresentation): Boolean {
        return equals(same)
    }

    override fun contentsSameAs(same: ProfilePresentation): Boolean {
        return hashCode() == same.hashCode()
    }

}

interface ProfileClickListener {
    fun onClick(pres: ProfilePresentation)
}

interface ProfileToggleListener {
    fun onToggle(pres:ProfilePresentation)
}

class ProfileData(
        val data: List<ProfilePresentation>,
        val diffResult: DiffUtil.DiffResult
)

enum class Profile {
    ACCOUNT_DETAILS,
    CURRENT_RESERVATION,
    HISTORY,
    REFER_A_FRIEND,
    LOYALTY_PROGRAMS,
    HOW_TO_USE_MOVIEPASS,
    HELP,
    NOTIFICATIONS,
    LINK,
    SIGN_OUT,
    VERSION,
    CLEAR_FLAGS
}