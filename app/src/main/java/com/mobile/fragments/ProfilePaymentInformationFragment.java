package com.mobile.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.app.Fragment;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.braintreepayments.api.BraintreeFragment;
import com.braintreepayments.api.PayPal;
import com.braintreepayments.api.exceptions.ErrorWithResponse;
import com.braintreepayments.api.exceptions.InvalidArgumentException;
import com.braintreepayments.api.interfaces.BraintreeCancelListener;
import com.braintreepayments.api.interfaces.BraintreeErrorListener;
import com.braintreepayments.api.interfaces.PaymentMethodNonceCreatedListener;
import com.braintreepayments.api.models.PaymentMethodNonce;
import com.mobile.Constants;
import com.mobile.UserPreferences;
import com.mobile.network.RestClient;
import com.mobile.requests.CreditCardChangeRequest;
import com.mobile.responses.UserInfoResponse;
import com.moviepass.R;

import java.util.Locale;

import butterknife.ButterKnife;
import io.card.payment.CardIOActivity;
import io.card.payment.CreditCard;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by anubis on 8/1/17.
 */

public class ProfilePaymentInformationFragment extends Fragment implements PaymentMethodNonceCreatedListener,
        BraintreeCancelListener, BraintreeErrorListener {

    BraintreeFragment mBraintreeFragment;

    RelativeLayout relativeLayout;
    ImageButton mButtonCreditCard;
    ImageButton mButtonPaypal;
    ImageButton mButtonAndroidPay;
    TextView selectedCreditCardText;
    TextView selectedCreditCardMasked;

    TextView cardNumber;
    View progress;
    Button update;

    static {
        System.loadLibrary("native-lib");
    }

    private static String CAMERA_PERMISSIONS[] = new String[]{
            Manifest.permission.CAMERA
    };

    private final static int REQUEST_CAMERA_CODE = 0;
//
//    private native static String getSandboxTokenizationKey();
//    private native static String getProductionTokenizationKey();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_profile_payment_information, container, false);
        ButterKnife.bind(getActivity());

        final Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle("Payment Information");

        relativeLayout = rootView.findViewById(R.id.relative_layout);
        progress = rootView.findViewById(R.id.progress);
        cardNumber = rootView.findViewById(R.id.card_number);
        mButtonCreditCard = rootView.findViewById(R.id.button_credit_card);
        selectedCreditCardText = rootView.findViewById(R.id.credit_card_number_copy);
        selectedCreditCardMasked = rootView.findViewById(R.id.credit_card_number);
//        mButtonPaypal = rootView.findViewById(R.id.button_paypal);
//        mButtonAndroidPay = rootView.findViewById(R.id.button_android_pay);

        update = rootView.findViewById(R.id.button_update);
        update.setVisibility(View.GONE);

        loadUserInfo();

        mButtonCreditCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                creditCardClick();
            }
        });

/*        mButtonPaypal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                paypalClick();
            }
        }); */

        return rootView;
    }

    private void loadUserInfo() {
        progress.setVisibility(View.VISIBLE);
        int userId = UserPreferences.getUserId();

        RestClient.getAuthenticated().getUserData(userId).enqueue(new Callback<UserInfoResponse>() {
            @Override
            public void onResponse(Call<UserInfoResponse> call, Response<UserInfoResponse> response) {
                progress.setVisibility(View.GONE);
                UserInfoResponse userInfoUpdateResponse = response.body();

                if (userInfoUpdateResponse != null && response.isSuccessful()) {
                    cardNumber.setText(userInfoUpdateResponse.getBillingCard());
                }
            }

            @Override
            public void onFailure(Call<UserInfoResponse> call, Throwable t) {
                progress.setVisibility(View.GONE);
            }

        });
    }

    public void creditCardClick() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(CAMERA_PERMISSIONS, REQUEST_CAMERA_CODE);
                scan_card();
            } else {
                scan_card();
            }
        } else {
            scan_card();
        }
    }

//    public void paypalClick() {
//        progress.setVisibility(View.VISIBLE);
//        mButtonPaypal.setEnabled(false);
//        try {
//            if (BuildConfig.DEBUG) {
//                String mAuthorization = getSandboxTokenizationKey();
//                mBraintreeFragment = BraintreeFragment.newInstance(getActivity(), mAuthorization);
//                progress.setVisibility(View.GONE);
//                // mBraintreeFragment is ready to use!
//
//                PayPal.authorizeAccount(mBraintreeFragment);
//            } else {
//                String mAuthorization = getProductionTokenizationKey();
//                mBraintreeFragment = BraintreeFragment.newInstance(getActivity(), mAuthorization);
//                progress.setVisibility(View.GONE);
//                // mBraintreeFragment is ready to use!
//
//                PayPal.authorizeAccount(mBraintreeFragment);
//            }
//
//        } catch (InvalidArgumentException e) {
//            // There was an issue with your authorization string.
//            mButtonPaypal.setEnabled(true);
//            Log.d("error", "error: " + e.getMessage());
//        }
//    }

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
        String sState = ProspectUser.signup1State;
        String sZip = ProspectUser.myZip;
        boolean amc3dMarkup = getIntent().getExtras().getBoolean("amc3dMarkup");
        String facebookToken = getIntent().getExtras().getString("fbToken");

        Log.d("facebookToken:", "" + facebookToken);

        bStreet = ProspectUser.street;
        bStreet2 = ProspectUser.street2;
        bCity = ProspectUser.city;
        bState = ProspectUser.signup1State;
        bZip = ProspectUser.myZip;

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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.CARD_SCAN_REQUEST_CODE) {
            if (data != null && data.hasExtra(CardIOActivity.EXTRA_SCAN_RESULT)) {
                update.setVisibility(View.VISIBLE);

                final CreditCard scanResult = data.getParcelableExtra(CardIOActivity.EXTRA_SCAN_RESULT);

                scanResult.getFormattedCardNumber();

                selectedCreditCardText.setVisibility(View.VISIBLE);
                selectedCreditCardMasked.setVisibility(View.VISIBLE);
                selectedCreditCardMasked.setText(scanResult.getRedactedCardNumber());

                if (scanResult.isExpiryValid() && scanResult.cvv != null) {
                    final String month = String.valueOf(scanResult.expiryMonth);
                    final String year = String.valueOf(scanResult.expiryYear);


                    update.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String expirationDate = String.format(Locale.getDefault(), "%s/%s", month, year);

                            updateCreditCard(scanResult.getFormattedCardNumber(), expirationDate, scanResult.cvv);
                        }
                    });
                }



            }
        }
    }

    public void updateCreditCard(String creditCardNumber, String expirationDate, String cvv) {
        progress.setVisibility(View.VISIBLE);

        int userId = UserPreferences.getUserId();
        CreditCardChangeRequest request = new CreditCardChangeRequest(creditCardNumber, expirationDate, cvv);

        RestClient.getAuthenticated().updateBillingCard(userId, request).enqueue(new Callback<UserInfoResponse>() {
            @Override
            public void onResponse(Call<UserInfoResponse> call, Response<UserInfoResponse> response) {
                progress.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    progress.setVisibility(View.GONE);
                    update.setVisibility(View.GONE);

                    final Snackbar snackbar = Snackbar.make(relativeLayout, R.string.fragment_profile_payment_information_updated, Snackbar.LENGTH_LONG);
                    snackbar.show();

                    loadUserInfo();
                }
            }

            @Override
            public void onFailure(Call<UserInfoResponse> call, Throwable t) {
                if (getActivity() != null) {
                    Toast.makeText(getActivity(), t.getMessage(), Toast.LENGTH_SHORT).show();
                    progress.setVisibility(View.GONE);
                }

            }
        });
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
}
