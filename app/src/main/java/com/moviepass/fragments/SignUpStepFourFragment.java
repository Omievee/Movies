package com.moviepass.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.braintreepayments.api.BraintreeFragment;
import com.braintreepayments.api.exceptions.ErrorWithResponse;
import com.braintreepayments.api.interfaces.BraintreeCancelListener;
import com.braintreepayments.api.interfaces.BraintreeErrorListener;
import com.braintreepayments.api.interfaces.PaymentMethodNonceCreatedListener;
import com.braintreepayments.api.models.PaymentMethodNonce;
import com.moviepass.Constants;
import com.moviepass.R;

import butterknife.OnClick;
import io.card.payment.CardIOActivity;

/**
 * Created by anubis on 7/11/17.
 */

public class SignUpStepFourFragment extends Fragment implements PaymentMethodNonceCreatedListener,
        BraintreeCancelListener, BraintreeErrorListener {

    BraintreeFragment mBraintreeFragment;

    static {
        System.loadLibrary("native-lib");
    }

    private native static String getSandboxTokenizationKey();
    private native static String getProductionTokenizationKey();

    static String sandboxString = String.valueOf(getSandboxTokenizationKey());
    static String productionString = String.valueOf(getProductionTokenizationKey());

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_sign_up_step_four, container, false);

        return rootView;
    }

    @OnClick(R.id.button_credit_card)
    public void onClickScan(View v) {
        /*
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(CAMERA_PERMISSIONS, REQUEST_CAMERA_CODE);
            } else {
                scan_card();
            }
        } else {
            scan_card();
        }
        */
    }

    @OnClick(R.id.button_paypal)
    public void paypalClick(View v) {

        /*
        mProgress.setVisibility(View.VISIBLE);
        mButtonPaypal.setEnabled(false);
        try {
            if (Constants.ENDPOINT.contains("android")) {
                String mAuthorization = Constants.PRODUCTION_TOKENIZATION_KEY;
                mBraintreeFragment = BraintreeFragment.newInstance(this, mAuthorization);
                mProgress.setVisibility(View.GONE);
                // mBraintreeFragment is ready to use!

                PayPal.authorizeAccount(mBraintreeFragment);
            } else {
                SANDBOX_TOKENIZATION_KEY =
                String mAuthorization = Constants.SANDBOX_TOKENIZATION_KEY;
                mBraintreeFragment = BraintreeFragment.newInstance(this, mAuthorization);
                mProgress.setVisibility(View.GONE);
                // mBraintreeFragment is ready to use!

                PayPal.authorizeAccount(mBraintreeFragment);
            }

        } catch (InvalidArgumentException e) {
            // There was an issue with your authorization string.
            mButtonPaypal.setEnabled(true);
            Log.d("error", "error: " + e.getMessage());
        }

        */
    }

    public void scan_card() {
        Intent scanIntent = new Intent(getActivity(), CardIOActivity.class);

        // customize these values to suit your needs.
        scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_EXPIRY, true); // default: false
        scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_CVV, true); // default: false
        scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_POSTAL_CODE, false); // default: false

        startActivityForResult(scanIntent, Constants.CARD_SCAN_REQUEST_CODE);
    }

    @Override
    public void onPaymentMethodNonceCreated(PaymentMethodNonce paymentMethodNonce) {
        // Send this nonce to your server
        String nonce = paymentMethodNonce.getNonce();

        final String bStreet;
        final String bStreet2;
        final String bCity;
        final String bState;
        final String bZip;

        /*

        String email = ProspectUser.email;
        String password = ProspectUser.password;
        String firstName = ProspectUser.firstName;
        String lastName = ProspectUser.lastName;
        String sStreet = ProspectUser.street;
        String sStreet2 = ProspectUser.street2;
        String sCity = ProspectUser.city;
        String sState = ProspectUser.state;
        String sZip = ProspectUser.zip;
        boolean amc3dMarkup = getIntent().getExtras().getBoolean("amc3dMarkup");
        String facebookToken = getIntent().getExtras().getString("fbToken");

        Log.d("facebookToken:", "" + facebookToken);

        bStreet = ProspectUser.street;
        bStreet2 = ProspectUser.street2;
        bCity = ProspectUser.city;
        bState = ProspectUser.state;
        bZip = ProspectUser.zip;

        if (facebookToken == null) {
            mProgress.setVisibility(View.VISIBLE);
            mButtonFinish.setEnabled(false);

            final SignUpRequest request = new SignUpRequest(nonce,
                    sStreet, sStreet2, sCity, sState, sZip, bStreet, bStreet2, bCity, bState, bZip,
                    email, firstName, lastName, password, amc3dMarkup);

            RestClient.getAuthenticated().signUp(ProspectUser.session, request).enqueue( new Callback<SignUpResponse>() {
                @Override
                public void onResponse(Call<SignUpResponse> call, Response<SignUpResponse> response) {

                    Log.d("isSuccessful", String.valueOf(response.isSuccessful()));

                    if (response.isSuccessful()) {

                        Log.d("subId", response.body().getSubId());
                        login();
                    } else {
                        try {
                            JSONObject jObjError = new JSONObject(response.body().getGlobal());

                            //PENDING RESERVATION GO TO TicketConfirmationActivity or TicketVerificationActivity

                            Toast.makeText(getActivity(), jObjError.toString(), Toast.LENGTH_LONG).show();
                            mProgress.setVisibility(View.GONE);
                            mButtonFinish.setEnabled(true);

                        } catch (Exception e) {
                            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                }

                @Override
                public void onFailure(Call<SignUpResponse> call, Throwable t) {
                    mProgress.setVisibility(View.GONE);
                    mButtonFinish.setEnabled(true);
                    Log.d("signUpRespnose: t", t.getMessage().toString());
                    Toast.makeText(getActivity(), t.getMessage(), Toast.LENGTH_SHORT).show();

                }
            });
        } else {
            mProgress.setVisibility(View.VISIBLE);
            final SignUpRequest request = new SignUpRequest(nonce,
                    sStreet, sStreet2, sCity, sState, sZip, bStreet, bStreet2, bCity, bState, bZip,
                    email, firstName, lastName, password, amc3dMarkup, facebookToken);

            RestClient.getAuthenticated().signUp(ProspectUser.session, request).enqueue(new Callback<SignUpResponse>() {
                @Override
                public void onResponse(Call<SignUpResponse> call, Response<SignUpResponse> response) {

                    if (response.isSuccessful()) {
                        Log.d("subId", response.body().getSubId());
                        login();
                    } else {
                        try {
                            JSONObject jObjError = new JSONObject(response.body().getGlobal());

                            //PENDING RESERVATION GO TO TicketConfirmationActivity or TicketVerificationActivity

                            Toast.makeText(getActivity(), jObjError.toString(), Toast.LENGTH_LONG).show();
                            mProgress.setVisibility(View.GONE);
                            mButtonFinish.setEnabled(true);

                        } catch (Exception e) {
                            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                }

                @Override
                public void onFailure(Call<SignUpResponse> call, Throwable t) {
                    mProgress.setVisibility(View.INVISIBLE);
                    mButtonFinish.setEnabled(true);
                    Log.d("error", t.getMessage().toString());

                    Toast.makeText(getActivity(), t.getMessage(), Toast.LENGTH_SHORT).show();

                }

            });
        }

        */
    }

    @Override
    public void onCancel(int requestCode) {
        // Use this to handle a canceled activity, if the given requestCode is important.
        // You may want to use this callback to hide loading indicators, and prepare your UI for input
    }

    @Override
    public void onError(Exception error) {
        if (error instanceof ErrorWithResponse) {
            Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public static SignUpStepOneFragment newInstance(String text) {

        SignUpStepOneFragment f = new SignUpStepOneFragment();
        Bundle b = new Bundle();
        b.putString("msg", text);

        f.setArguments(b);

        return f;
    }
}