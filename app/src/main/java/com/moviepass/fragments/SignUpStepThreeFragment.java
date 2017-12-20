package com.moviepass.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.braintreepayments.api.exceptions.ErrorWithResponse;
import com.braintreepayments.api.interfaces.BraintreeCancelListener;
import com.braintreepayments.api.interfaces.BraintreeErrorListener;
import com.braintreepayments.api.interfaces.PaymentMethodNonceCreatedListener;
import com.braintreepayments.api.models.PaymentMethodNonce;
import com.moviepass.DeviceID;
import com.moviepass.R;
import com.moviepass.UserPreferences;
import com.moviepass.activities.MoviesActivity;
import com.moviepass.activities.SignUpActivity;
import com.moviepass.activities.TheatersActivity;
import com.moviepass.model.ProspectUser;
import com.moviepass.model.User;
import com.moviepass.network.RestClient;
import com.moviepass.requests.LogInRequest;
import com.moviepass.requests.SignUpRequest;
import com.moviepass.responses.SignUpResponse;

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
    CoordinatorLayout coordinatorLayout;

    public TextView confirmFullName, confirmFullAddress, confirmCityStateZip,
            confirmEditAddress, confirmEditBilling, confirmCCNum, confirmTermsText, confirmsPricacyText, confirmSubmit;
    public Switch confirmTermsAgreementSwitch;

    String num, month, year, ccv;
    View progress;


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

        confirmEditAddress = rootview.findViewById(R.id.CONFIRM_EDIT_ADDRESS);
        confirmEditBilling = rootview.findViewById(R.id.CONFIRM_EDIT_BILLING);

        confirmCCNum = rootview.findViewById(R.id.CONFIRM_NUMBER);
        confirmTermsText = rootview.findViewById(R.id.CONFIRM_ToS);
        confirmsPricacyText = rootview.findViewById(R.id.CONFIRM_PRIVACY);
        confirmTermsAgreementSwitch = rootview.findViewById(R.id.CONFIRM_SWITCH);
        confirmSubmit = rootview.findViewById(R.id.CONFIRM_SUBMIT);

        Log.d(TAG, "vc: " + confirmCCNum.getId());
        Log.d(TAG, "1: " + num);

        //ToS & Privacy Links
        confirmTermsText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = "https://www.moviepass.com/content/terms";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });

        confirmsPricacyText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://www.moviepass.com/content/privacy";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });


        confirmFullName.setText(ProspectUser.firstName + " " + ProspectUser.lastName);
        confirmFullAddress.setText(ProspectUser.address + " " + ProspectUser.address2);
        confirmCityStateZip.setText(ProspectUser.city + ", " + ProspectUser.state + " " + ProspectUser.zip);


        confirmEditBilling.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((SignUpActivity) getActivity()).mViewPager.setCurrentItem(1);
            }
        });

        confirmEditAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((SignUpActivity) getActivity()).mViewPager.setCurrentItem(0);
            }
        });

        return rootview;
    }

    @Override
    public void OnCreditCardEntered(String ccNum, int ccExMonth, int ccExYear, String ccCVV) {//        confirmCCNum.setText(num);
        Log.d(TAG, "OnCreditCardEntered: " + ccNum + " " + ccExMonth + " " + ccExYear + " " + ccCVV);

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


    public void beginRegistration(String cardNumber, int cardExpMonth, int cardExpYear, String cardCvv) {
        progress.setVisibility(View.VISIBLE);

        String creditCardNumber = String.valueOf(cardNumber);
        String month = String.valueOf(cardExpMonth);
        //TODO: UPDATE IN 2099 to avoid signup failures
        String year = "20" + String.valueOf(cardExpYear);
        Log.d(TAG, "beginRegistration: " + year);
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


        boolean amc3dMarkup = false;

        if (!confirmTermsAgreementSwitch.isChecked()) {
            makeSnackbar("You must agree to the Terms of Service");

        } else {
            bStreet = ProspectUser.address;
            bStreet2 = ProspectUser.address2;
            bCity = ProspectUser.city;
            bState = ProspectUser.state;
            bZip = ProspectUser.zip;

            completeRegistration(creditCardNumber, month, year, cvv, sStreet, sStreet2, sCity, sState,
                    sZip, bStreet, bStreet2, bCity, bState, bZip, email, firstName, lastName, password, amc3dMarkup);

        }


    }

    private void completeRegistration(String creditCardNumber, String month, String year, String cvv, String sStreet,
                                      String sStreet2, String sCity, String sState, String sZip, String bStreet,
                                      String bStreet2, String bCity, String bState, String bZip, String email,
                                      String firstName, String lastName, String password, boolean amc3dMarkup) {

        if (confirmTermsAgreementSwitch.isChecked()) {
            progress.setVisibility(View.VISIBLE);

            confirmSubmit.setEnabled(false);

            final SignUpRequest request = new SignUpRequest(creditCardNumber, month, year, cvv,
                    sStreet, sStreet2, sCity, sState, sZip, bStreet, bStreet2, bCity, bState, bZip,
                    email, firstName, lastName, password, amc3dMarkup);


            RestClient.getUnauthenticated().signUp(ProspectUser.session, request).enqueue(new Callback<SignUpResponse>() {
                @Override
                public void onResponse(Call<SignUpResponse> call, Response<SignUpResponse> response) {

                    Log.d("isSuccessful", String.valueOf(response.isSuccessful()));

                    if (response.isSuccessful()) {

                        Log.d("subId", response.body().getSubId());

                        //transition to final viewpager pag & show confirmation
                        ((SignUpActivity) getActivity()).setPage();

                    } else {
                        try {
                            JSONObject jObjError = new JSONObject(response.body().getGlobal());

                            //PENDING RESERVATION GO TO TicketConfirmationActivity or TicketVerificationActivity
                            makeSnackbar(jObjError.toString());
                            progress.setVisibility(View.GONE);
                            confirmSubmit.setEnabled(true);

                        } catch (Exception e) {
                            makeSnackbar("Internal Failure: Error 5");
                            Log.d(TAG, "try/catch try: " + e.getMessage());
                        }
                    }
                }

                @Override
                public void onFailure(Call<SignUpResponse> call, Throwable t) {
                    progress.setVisibility(View.GONE);
                    confirmSubmit.setEnabled(true);
                    makeSnackbar(t.getMessage());
                    Log.d(TAG, "failed: " + t.getMessage());

                }
            });
        } else {
            makeSnackbar(getString(R.string.fragment_sign_up_step_two_must_agree_to_terms));
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

}