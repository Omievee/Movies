package com.mobile.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import com.braintreepayments.api.exceptions.ErrorWithResponse;
import com.braintreepayments.api.interfaces.BraintreeCancelListener;
import com.braintreepayments.api.interfaces.BraintreeErrorListener;
import com.braintreepayments.api.interfaces.PaymentMethodNonceCreatedListener;
import com.braintreepayments.api.models.PaymentMethodNonce;
import com.helpshift.support.Log;
import com.mobile.activities.SignUpActivity;
import com.mobile.helpers.LogUtils;
import com.mobile.model.ProspectUser;
import com.mobile.network.RestClient;
import com.mobile.requests.SignUpRequest;
import com.mobile.responses.SignUpResponse;
import com.moviepass.R;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class SignUpStepThreeFragment extends Fragment implements PaymentMethodNonceCreatedListener,
        BraintreeCancelListener, BraintreeErrorListener, SignUpStepTwoFragment.OnCreditCardEntered {
    public static final String CCX = "ccx";
    public static final String CCCVV = "cccvv";
    public static final String CCNUM = "cc";
    View rootview;
    public static final String TAG = "foundit";
    View coordinatorLayout;
    Context myContext;
    Activity myActivity;
    public TextView confirmFullName, confirmFullAddress, confirmCityStateZip,
            confirmEditAddress, confirmEditBilling, confirmCCNum, confirmTermsText, confirmsPricacyText, confirmSubmit;
    public Switch confirmTermsAgreementSwitch;

    String num, month, year, ccv;
    View progress;
    TextView price, paymentDisclaimer, planDescription;


    public SignUpStepThreeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootview = inflater.inflate(R.layout.fr_signup_stepthree, container, false);

        coordinatorLayout = rootview.findViewById(R.id.CONFIRM_COORD);
        progress = rootview.findViewById(R.id.step3_progress);
        confirmFullName = rootview.findViewById(R.id.CONFIRM_FULL_NAME);
        confirmFullAddress = rootview.findViewById(R.id.CONFIRM_FULL_ADDRESS);
        confirmCityStateZip = rootview.findViewById(R.id.CONFIRM_CITYSTATEZIP);


        confirmCCNum = rootview.findViewById(R.id.CONFIRM_NUMBER);
        confirmTermsText = rootview.findViewById(R.id.CONFIRM_ToS);
        confirmsPricacyText = rootview.findViewById(R.id.CONFIRM_PRIVACY);
        confirmTermsAgreementSwitch = rootview.findViewById(R.id.CONFIRM_SWITCH);
        confirmSubmit = rootview.findViewById(R.id.CONFIRM_SUBMIT);

        price = rootview.findViewById(R.id.planPrice);
        paymentDisclaimer = rootview.findViewById(R.id.paymentDisclaimer);
        planDescription = rootview.findViewById(R.id.planDescription);

        if (ProspectUser.ccNum != null)
            confirmCCNum.setText(" - " + ProspectUser.ccNum.substring(12, 16));

        LogUtils.newLog(TAG, "vc: " + confirmCCNum.getId());
        LogUtils.newLog(TAG, "1: " + num);

        //ToS & Privacy Links
        confirmTermsText.setOnClickListener(view -> {
            String url = "https://www.moviepass.com/terms";
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        });

        confirmsPricacyText.setOnClickListener(v -> {
            String url = "https://www.moviepass.com/privacy/";
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        });


        confirmFullName.setText(ProspectUser.firstName + " " + ProspectUser.lastName);
        confirmFullAddress.setText(ProspectUser.address + " " + ProspectUser.address2);
        confirmCityStateZip.setText(ProspectUser.city + ", " + ProspectUser.state + " " + ProspectUser.zip);

        price.setText(ProspectUser.plan.getConfirmTotal());
        planDescription.setText(ProspectUser.plan.getConfirmPlanDescription());

        //CHANGING PAYMENT DISCLAIMER
//        paymentDisclaimer.setText(ProspectUser.plan.getPaymentDisclaimer());
        paymentDisclaimer.setText(getResources().getString(R.string.fragment_sign_up_step_three_bottom_disclaimer_part_one) + " " + ProspectUser.plan.getConfirmTotal() + " " +
                getResources().getString(R.string.fragment_sign_up_step_three_bottom_disclaimer_part_two) + " " + getResources().getString(R.string.fragment_sign_up_step_three_bottom_disclaimer_part_three));


        return rootview;
    }

    @Override
    public void OnCreditCardEntered(String ccNum, String ccExMonth, String ccExYear, String ccCVV) {

    }

    @Override
    public void onCancel(int requestCode) {

    }

    @Override
    public void onError(Exception error) {
        if (error instanceof ErrorWithResponse) {
            makeSnackbar(error.getMessage());
        }
    }

    @Override
    public void onPaymentMethodNonceCreated(PaymentMethodNonce paymentMethodNonce) {

    }


    public void beginRegistration(String cardNumber, String cardExpMonth, String cardExpYear, String cardCvv) {
        progress.setVisibility(View.VISIBLE);

        String creditCardNumber = String.valueOf(cardNumber);
        String month = String.valueOf(cardExpMonth);
        //TODO: UPDATE IN 2099 to avoid signup failures
        String year = "20" + cardExpYear;
//        int YEAR = Integer.valueOf(year);
        String cvv = String.valueOf(cardCvv);

        final String bStreet;
        final String bStreet2;
        final String bCity;
        final String bState;
        final String bZip;

        String email = ProspectUser.email;
        String password = ProspectUser.password;
        String firstName = ProspectUser.firstName;
        String lastName = ProspectUser.lastName;
        String sStreet = ProspectUser.address;
        String sStreet2 = ProspectUser.address2;
        String sCity = ProspectUser.city;
        String sState = ProspectUser.state;
        String sZip = ProspectUser.zip;
        String birthday = ProspectUser.dateOfBirth;
        String gender = ProspectUser.gender;
        String selectedPlanId;
        String androidID = ProspectUser.androidID;
        if (ProspectUser.plan == null) {
            selectedPlanId = null;
        } else {
            selectedPlanId = ProspectUser.plan.getId();
        }


        if (!confirmTermsAgreementSwitch.isChecked()) {
            makeSnackbar("You must agree to the Terms of Service");

        } else {
            bStreet = ProspectUser.address;
            bStreet2 = ProspectUser.address2;
            bCity = ProspectUser.city;
            bState = ProspectUser.state;
            bZip = ProspectUser.zip;

            completeRegistration(creditCardNumber, month, year, cvv, sStreet, sStreet2, sCity, sState,
                    sZip, bStreet, bStreet2, bCity, bState, bZip, email, firstName, lastName, password, birthday, gender, selectedPlanId, androidID);

        }


    }

    private void completeRegistration(String creditCardNumber, String month, String year, String cvv, String sStreet,
                                      String sStreet2, String sCity, String sState, String sZip, String bStreet,
                                      String bStreet2, String bCity, String bState, String bZip, String email,
                                      String firstName, String lastName, String password, String birthday, String gender, String selectedPlanId, String androidID) {

        if (confirmTermsAgreementSwitch.isChecked()) {
//            progress.setVisibility(View.VISIBLE);

            confirmSubmit.setEnabled(false);
            SignUpRequest request;

            if (selectedPlanId == null) {
                request = new SignUpRequest(creditCardNumber, month, year, cvv,
                        sStreet, sStreet2, sCity, sState, sZip, bStreet, bStreet2, bCity, bState, bZip,
                        email, firstName, lastName, password, birthday, gender, androidID);
            } else {
                request = new SignUpRequest(creditCardNumber, month, year, cvv,
                        sStreet, sStreet2, sCity, sState, sZip, bStreet, bStreet2, bCity, bState, bZip,
                        email, firstName, lastName, password, birthday, gender, Integer.valueOf(selectedPlanId), androidID);
            }

            LogUtils.newLog(TAG, "NAMES: " + firstName + "  " + lastName);

            LogUtils.newLog(TAG, "completeRegistration: " + ProspectUser.session);
            LogUtils.newLog(TAG, "completeRegistration: " + request);


            RestClient.getsAuthenticatedRegistrationAPI().signUp(ProspectUser.session, request).enqueue(new Callback<SignUpResponse>() {
                @Override
                public void onResponse(Call<SignUpResponse> call, Response<SignUpResponse> response) {
                    LogUtils.newLog("isSuccessful", String.valueOf(response.isSuccessful()));

                    if (response.isSuccessful()) {
                        //transition to final viewpager pag & show confirmation
                        progress.setVisibility(View.GONE);
                        ((SignUpActivity) myActivity).setPage();

                    } else {
                        try {
                            JSONObject jObjError = new JSONObject(response.body().getGlobal());
                            makeSnackbar(jObjError.toString());
                            progress.setVisibility(View.GONE);
                            confirmSubmit.setEnabled(true);
                        } catch (Exception e) {
                            progress.setVisibility(View.GONE);
                            makeSnackbar("Error processing payment");
                            confirmSubmit.setEnabled(true);
                        }
                    }
                }

                @Override
                public void onFailure(Call<SignUpResponse> call, Throwable t) {
                    progress.setVisibility(View.GONE);
                    confirmSubmit.setEnabled(true);
                    makeSnackbar(t.getMessage());
                    LogUtils.newLog(TAG, "failed: " + t.getMessage());

                }
            });
        } else {
            makeSnackbar(getString(R.string.fragment_sign_up_step_two_must_agree_to_terms));
            Log.d(TAG, "completeRegistration: NAMES");
            progress.setVisibility(View.GONE);
            confirmSubmit.setEnabled(true);
        }
    }


    public void makeSnackbar(String message) {
        final Snackbar snackbar = Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction("OK", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                snackbar.dismiss();
            }
        });
        snackbar.show();
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        myContext = context;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        myActivity = activity;
    }

}