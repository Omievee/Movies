package com.mobile.seats

import android.content.Context
import android.os.Bundle
import android.support.constraint.ConstraintSet
import android.support.v4.app.Fragment
import android.text.*
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.LineHeightSpan
import android.text.style.TextAppearanceSpan
import android.transition.TransitionManager
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import com.mobile.ApiError
import com.mobile.model.Emails
import com.mobile.network.Api
import com.mobile.network.RestClient
import com.mobile.rx.Schedulers
import com.mobile.widgets.MPAlertDialog
import com.moviepass.R
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_guest_emails.*

class GuestEmailsFragment : Fragment() {

    var listener: BringAFriendListener? = null

    var state = GuestEmailState()

    val textWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }

        override fun afterTextChanged(s: Editable?) {
            eighteenOrOlder.isEnabled = hasEmails
            continueButton.isEnabled = hasEmails
        }
    }

    val hasEmails: Boolean
        get() {
            val emails = (0 until emailLL.childCount).map {
                val emailView = (emailLL.getChildAt(it) as? GuestEmailView)
                val emailStr = emailView?.emailText?.text.toString()
                emailStr
            }.filter {
                !it.isEmpty()
            }
            return !emails.isEmpty() && emails.all {
                Patterns.EMAIL_ADDRESS.matcher(it).matches()
            }
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_guest_emails, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        closeButton
                .setOnClickListener {
                    listener?.onClosePressed()
                }
        backButton.setOnClickListener {
            listener?.onBackPressed()
        }
        eighteenOrOlder.apply {
            setOnClickListener {
                if (hasEmails) {
                    toggle()
                }
            }
        }
        provideEmails.onCheckChangedListener = CompoundButton.OnCheckedChangeListener { _, isChecked ->
            if(!isChecked) {
                return@OnCheckedChangeListener
            }
            val viewG = view as ViewGroup
            val set = ConstraintSet()
            set.clone(provideEmailsCL)
            set.setVisibility(provideEmails.id, View.GONE)
            set.setVisibility(emailLL.id, View.VISIBLE)
            TransitionManager.beginDelayedTransition(viewG)
            set.applyTo(provideEmailsCL)
            val set2 = ConstraintSet()
            set2.clone(guestEmailsCL)
            set2.setVisibility(eighteenOrOlder.id, View.VISIBLE)
            set2.applyTo(guestEmailsCL)
            emailLL.isEnabled = isChecked
            (0 until emailLL.childCount).forEach {
                val emailField = emailLL.getChildAt(it)
                emailField.isEnabled = isChecked
            }
        }
        continueButton.setOnClickListener {
            val valid = (0 until emailLL.childCount).map {
                val emailView = (emailLL.getChildAt(it) as? GuestEmailView)
                val emailStr = emailView?.emailText?.text.toString()
                val vald = emailStr.isNullOrEmpty() || Patterns.EMAIL_ADDRESS.matcher(emailStr).matches()
                when (vald) {
                    true -> emailView?.error = null
                    false -> emailView?.error = resources.getString(R.string.invalid_email)
                }
                vald
            }
            when {
                valid.contains(false) -> return@setOnClickListener
            }
            val emails = (0 until emailLL.childCount).map {
                val email = (emailLL.getChildAt(it) as? GuestEmailView)?.email
                email
            }.filterNotNull().toMutableList()
            state.payload?.emails = emails
            eighteenOrOlder.isEnabled = hasEmails
            when (eighteenOrOlder.isEnabled && !eighteenOrOlder.isChecked) {
                true -> {
                    show18()
                    return@setOnClickListener
                }
            }
            checkEmails()
        }
        val descriptionSpan = SpannableStringBuilder(SpannableString(getString(R.string.guests_chance_go_free))
                .apply {
                    setSpan(TextAppearanceSpan(context, R.style.GoFree), 0,length,SpannedString.SPAN_EXCLUSIVE_EXCLUSIVE)
                })
                .append('\n')
                .append(SpannableString(getString(R.string.add_guest_emails_description))
                        .apply {
                            setSpan(TextAppearanceSpan(context, R.style.AddGuestEmailDescription), 0, length, SpannedString.SPAN_EXCLUSIVE_EXCLUSIVE)
                        })
                .apply {
                    append('\n')
                    val span = SpannableString(getString(R.string.restrictions_apply_see_details))
                            .apply {
                                setSpan(object : ClickableSpan() {
                                    override fun onClick(widget: View?) {
                                        showBottomFragment(SheetData(
                                                error = getString(R.string.free_guest_convenience),
                                                title = getString(R.string.free_guest_policy),
                                                description = getString(R.string.free_guest_policy_description)
                                        ))
                                    }

                                    override fun updateDrawState(ds: TextPaint) {
                                        ds.isUnderlineText = false
                                    }

                                }, 0, length, SpannableString.SPAN_INCLUSIVE_EXCLUSIVE)
                                setSpan(TextAppearanceSpan(context, R.style.SeeDetails), 0, length, SpannedString.SPAN_EXCLUSIVE_EXCLUSIVE)
                            }
                    append(span)
                }
        description.movementMethod = LinkMovementMethod.getInstance()
        description.text = descriptionSpan
        skipThisPromotion.setOnClickListener {
            state.payload?.emails?.clear()
            listener?.onEmailContinueClicked(payload = state.payload)
        }
        subscribe()
    }

    private fun showBottomFragment(sheetData: SheetData) {
        MPBottomSheetFragment.newInstance(sheetData).show(fragmentManager, "")
    }

    private fun show18() {
        val context = context ?: return
        MPAlertDialog(context).setMessage(R.string.eighteen_acknowledgement_error)
                .setPositiveButton(android.R.string.ok, null)
                .show()
    }

    private fun checkEmails() {
        state.emailCheckDisposable?.dispose()
        if (!hasEmails) {
            return listener?.onEmailContinueClicked(payload = state.payload) ?: Unit
        }
        showProgress()
        state.emailCheckDisposable = RestClient
                .getAuthenticated()
                .usersExist(Emails(emails = state.payload?.emails
                        ?.map { it.email }?.filterNotNull()?.toSet() ?: emptySet()))
                .doAfterTerminate { hideProgress() }
                .subscribe({ success ->
                    when (success.existingEmails.isEmpty()) {
                        true -> listener?.onEmailContinueClicked(payload = state.payload)
                        else -> showErrorDialog(success.existingEmails)
                    }
                }, { error ->
                    (error as? ApiError)?.let {
                        showErrorDialog(it)
                    }
                })
    }

    private fun showErrorDialog(error:ApiError) {
        val context = context ?: return
        MPAlertDialog(context).setMessage(error.message)
                .setPositiveButton(android.R.string.ok, { _, _ ->
                })
                .show()
    }

    private fun showErrorDialog(ema: Set<String>) {
        val context = context ?: return
        val emailstr = ema.joinToString("\n")
        val message = "${resources.getString(R.string.duplicate_emails)}${emailstr}"

        MPAlertDialog(context).setTitle(R.string.add_guest_emails)
                .setMessage(
                        message
                )
                .setPositiveButton(R.string.continue_button, { _, _ ->
                    listener?.onEmailContinueClicked(state.payload)
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show()
    }

    private fun hideProgress() {
        continueButton.progress = false
    }

    private fun showProgress() {
        continueButton.progress = true
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        listener = parentFragment as? BringAFriendListener
    }

    fun subscribe() {
        state.disposable?.dispose()
        state.disposable = listener?.payload()
                ?.subscribe({ payload ->
                    state.payload = payload
                    onPayload()
                }, { _ ->

                })
    }

    private fun onPayload() {
        val emails: List<GuestEmail> = state.payload?.emails ?: return
        emailLL.removeAllViews()
        emails.forEachIndexed { _, email ->
            emailLL.addView(GuestEmailView(context).apply {
                bind(email)
                isEnabled = provideEmails.isChecked
                emailText.addTextChangedListener(textWatcher)
            })
        }
        continueButton.isEnabled = hasEmails
    }

    override fun onDestroy() {
        super.onDestroy()
        state.onDestroy()
        listener = null
    }
}

class GuestEmailState(
        var disposable: Disposable? = null,
        var payload: SelectSeatPayload? = null,
        var emailCheckDisposable: Disposable? = null
) {
    fun onDestroy() {
        disposable?.dispose()
        emailCheckDisposable?.dispose()
    }
}
