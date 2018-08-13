package com.mobile.fragments

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.mobile.extensions.DropDownFields
import com.mobile.extensions.CustomFlatDropDownClickListener
import com.mobile.profile.CancellationReason
import com.mobile.profile.ProfileCancellationPresenter
import com.mobile.profile.ProfileCancellationView
import com.moviepass.R
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.profile_cancellation_view_layout.*
import javax.inject.Inject



class ProfileCancellationFragment : MPFragment(), ProfileCancellationView, CustomFlatDropDownClickListener {

    var reason: String? = null

    var spinnerList: List<String>? = null
    @Inject
    lateinit var presenter: ProfileCancellationPresenter


    override fun bindView() {


        spinnerList = CancellationReason.getList()

        spinnerCancelReason.bind(CancellationReason.getTitle(), spinnerList ?: listOf(),this,true)

        cancelbutton.setOnClickListener {
            presenter.onSubmitCancellation(getReasonForCancellation(), cancelComments.getComments())
        }


        cancelBack.setOnClickListener {
            close()
        }

    }

    override fun onClick(reason: String) {
        this.reason = reason
        hideErrorMessage()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.profile_cancellation_view_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.onCreate()
    }

    private fun getReasonForCancellation(): String {
        return reason ?: DropDownFields.UNKNOWN.type
    }

    override fun showErrorMessage() {
        cancellationError.visibility = View.VISIBLE
    }

    override fun hideErrorMessage() {
        cancellationError.visibility = View.INVISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        presenter.onDestroy()
    }

    override fun onAttach(context: Context?) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onResume() {
        super.onResume()
        presenter.onResume()
    }

    fun close() {
        activity?.onBackPressed()
    }

    override fun hideProgress() {
        progress.visibility = View.GONE
    }

    override fun showProgress() {
        progress.visibility = View.VISIBLE
    }

    override fun showCancellationConfirmationDialog(reason: String, comment: String, billingDate: String?) {

        val builder = AlertDialog.Builder(context, R.style.CUSTOM_ALERT)


        var message: String = getString(R.string.profile_cancel_remain_active,billingDate)
        builder.setMessage(message)
                .setTitle(R.string.profile_cancel_are_you_sure)
                .setPositiveButton(getString(R.string.cancel_membership)) { _, _ ->
                    presenter.cancelFlow(reason, comment)
                }
                .setNegativeButton(getString(R.string.go_back)) { _, _ ->

                }
        builder.create()
        builder.show()
    }

    override fun successfullCancellation(billingDate: String?) {

        val builder = AlertDialog.Builder(context, R.style.CUSTOM_ALERT)
        var message: String? = when (billingDate) {
            null -> getString(R.string.profile_cancel_membership_message)
            else -> getString(R.string.profile_cancel_membership_message) + " " + billingDate
        }
        builder.setMessage(message)
                .setCancelable(false)
                .setTitle(R.string.profile_cancel_membership)
                .setPositiveButton(getString(R.string.ok)) { _, _ ->
                    activity?.onBackPressed()
                }

        builder.create()
        builder.show()
    }


    override fun unccessfullCancellation(error: String) {
        Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
    }

}
