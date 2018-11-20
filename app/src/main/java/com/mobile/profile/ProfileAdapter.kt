package com.mobile.profile

import android.content.res.Resources
import android.support.v4.app.Fragment
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.mobile.UserPreferences
import com.mobile.adapters.BaseViewHolder
import com.mobile.adapters.BasicDiffCallback
import com.mobile.fragments.CappedPlanView
import com.mobile.fragments.PeakContainerView
import com.moviepass.BuildConfig
import com.moviepass.R

class ProfileAdapter(
        val fragment: Fragment,
        val clickListener: ProfileClickListener,
        val toggleListener: ProfileToggleListener? = null
) : RecyclerView.Adapter<BaseViewHolder>() {

    var data: ProfileData? = null
        set(value) {
            field = value
            field?.diffResult?.dispatchUpdatesTo(this)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return BaseViewHolder(when (viewType) {
            Profile.NOTIFICATIONS.ordinal -> ProfileCheckBox(context = parent.context)
            Profile.LINK.ordinal -> ProfileLinkView(context = parent.context)
            Profile.PEAK_PASS.ordinal -> PeakContainerView(context = parent.context)
            Profile.CAPPED_PLAN.ordinal -> CappedPlanView(context = parent.context)
            else -> ProfileView(context = parent.context)
        })
    }

    override fun getItemViewType(position: Int): Int {
        val pres = data!!.data[position]
        return when (pres.type) {
            Profile.NOTIFICATIONS -> Profile.NOTIFICATIONS.ordinal
            Profile.LINK, Profile.SIGN_OUT -> Profile.LINK.ordinal
            Profile.PEAK_PASS -> Profile.PEAK_PASS.ordinal
            Profile.CAPPED_PLAN -> Profile.CAPPED_PLAN.ordinal
            Profile.DIVIDER -> Profile.DIVIDER.ordinal
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
            is ProfileLinkView -> view.bind(pres, clickListener)
            is PeakContainerView -> view.bind(pres, fragment.childFragmentManager)
        }
    }

    companion object {
        fun createData(last: ProfileData?, r: Resources): ProfileData {
            val old = last?.data ?: emptyList()
            val debugs = when (BuildConfig.DEBUG) {
                true -> listOf(ProfilePresentation(
                        type = Profile.CLEAR_FLAGS,
                        title = "Clear App SubscriptionData"))
                false -> emptyList()
            }
            val newData = mutableListOf(
                    ProfilePresentation(
                            type = Profile.ACCOUNT_DETAILS,
                            header = r.getString(R.string.profile),
                            subHeader = when (UserPreferences.hasNewPeakPass) {
                                true -> r.getString(R.string.peak_pass_added)
                                false -> null
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