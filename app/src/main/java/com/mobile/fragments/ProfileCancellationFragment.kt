package com.mobile.fragments

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.jaredrummler.materialspinner.MaterialSpinnerAdapter
import com.mobile.ApiError
import com.mobile.Constants
import com.mobile.UserPreferences
import com.mobile.helpers.LogUtils
import com.mobile.network.Api
import com.mobile.requests.CancellationRequest
import com.mobile.responses.CancellationResponse
import com.mobile.responses.UserInfoResponse
import com.mobile.widgets.MaterialSpinnerSpinnerView
import com.moviepass.R
import dagger.android.support.AndroidSupportInjection
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fr_profile_cancelation.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/**
 * Created by anubis on 9/1/17.
 */

class ProfileCancellationFragment : MPFragment() {


    @Inject
    lateinit var api: Api

    var profileCancellationDisposable: Disposable? = null

    internal var cancelReasons: String? = null
    internal var cancelSubscriptionReason: Long = 0
    internal var cancellationResponse: CancellationResponse? = null
    private var userInfoResponse: UserInfoResponse? = null
    private var billingDate: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fr_profile_cancelation, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cancelbutton.isEnabled = false
        spinnerCancelReason
                .setAdapter(object : MaterialSpinnerAdapter<String>(activity, Arrays.asList("Reason for Cancellation", "Price", "Theater selection", "Ease of use", "Lack of use", "Other")) {
                    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                        val view = MaterialSpinnerSpinnerView(parent.context)
                        view.bind(getItemText(position))
                        return view
                    }
                })
        loadUserInfo()

        spinnerCancelReason.setOnItemSelectedListener { view1, position, id, item ->
            cancelReasons = view1.getItems<Any>()[position] as String
            if (cancelReasons == "Reason for Cancellation") {
                cancelbutton.isEnabled = false
                Toast.makeText(activity, "Please make a selection", Toast.LENGTH_SHORT).show()
            } else {
                cancelbutton.isEnabled = true
            }
        }

        cancelBack.setOnClickListener { v -> activity?.onBackPressed() }
        cancelbutton.setOnClickListener { v -> showCancellationConfirmationDialog() }


    }

    fun showCancellationConfirmationDialog() {

        val builder = AlertDialog.Builder(spinnerCancelReason.context, R.style.CUSTOM_ALERT)
        var message: String?
        when (billingDate) {
            null -> message = getString(R.string.profile_cancel_are_you_sure)
            else -> message = getString(R.string.profile_cancel_remain_active) + billingDate + getString(R.string.profile_cancel_paid_through)
        }
        builder.setMessage(message)
                .setTitle(R.string.profile_cancel_cancel_membership)
                .setPositiveButton("Cancel Membership") { dialog, id ->
                    progress.visibility = View.VISIBLE
                    cancelFlow()
                }
                .setNegativeButton("Keep") { dialog, id ->

                }
        builder.create()
        builder.show()
    }


    fun cancelFlow() {
        val c = Calendar.getInstance()
        val df = SimpleDateFormat("yyyy-MM-dd")
        val requestDate = df.format(c.time)

        val cancelReason = spinnerCancelReason.text.toString()
        when (cancelReason) {
            "Price" -> cancelSubscriptionReason = 1
            "Theater selection" -> cancelSubscriptionReason = 2
            "Ease of use" -> cancelSubscriptionReason = 3
            "Lack of use" -> cancelSubscriptionReason = 4
            "Other" -> cancelSubscriptionReason = 7
            else -> cancelSubscriptionReason = 8
        }
        val angryComments = cancelComments.text.toString()
        val request = CancellationRequest(requestDate, cancelSubscriptionReason, angryComments)

        profileCancellationDisposable?.dispose()

        profileCancellationDisposable =
                api
                        .requestCancellation(request)
                        .subscribe({ r ->
                            progress.visibility = View.GONE
                            Toast.makeText(activity, "Cancellation successful", Toast.LENGTH_SHORT).show()
                            activity?.onBackPressed()
                        })
                        { error ->
                            progress.visibility = View.GONE
                            if (error is ApiError) {
                                Toast.makeText(context, error.error?.message, Toast.LENGTH_SHORT).show()
                            }
                        }
    }

    private fun loadUserInfo() {
        val userId = UserPreferences.userId
        api.getUserData(userId).enqueue(object : Callback<UserInfoResponse> {
            override fun onResponse(call: Call<UserInfoResponse>, response: Response<UserInfoResponse>) {
                userInfoResponse = response.body()
                if (userInfoResponse != null) {
                    if (userInfoResponse?.nextBillingDate == "") {
                    } else {
                        billingDate = userInfoResponse?.nextBillingDate
                    }
                }
            }

            override fun onFailure(call: Call<UserInfoResponse>, t: Throwable) {
                Toast.makeText(activity, "Server Error; Please try again.", Toast.LENGTH_SHORT).show()
                LogUtils.newLog(Constants.TAG, "onFailure: " + t.message)
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        profileCancellationDisposable?.dispose()
        profileCancellationDisposable = null
    }

    override fun onAttach(context: Context?) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }


}
