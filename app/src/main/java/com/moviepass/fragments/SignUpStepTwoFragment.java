package com.moviepass.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.braintreepayments.api.BraintreeFragment;
import com.braintreepayments.api.exceptions.ErrorWithResponse;
import com.braintreepayments.api.interfaces.BraintreeCancelListener;
import com.braintreepayments.api.interfaces.BraintreeErrorListener;
import com.braintreepayments.api.interfaces.PaymentMethodNonceCreatedListener;
import com.braintreepayments.api.models.PaymentMethodNonce;
import com.moviepass.Constants;
import com.moviepass.R;
import com.moviepass.activities.SignUpActivity;
import com.moviepass.model.ProspectUser;

import butterknife.ButterKnife;
import io.card.payment.CardIOActivity;
import io.card.payment.CreditCard;

/**
 * Created by anubis on 7/11/17.
 */

public class SignUpStepTwoFragment extends Fragment implements PaymentMethodNonceCreatedListener,
        BraintreeCancelListener, BraintreeErrorListener {

    BraintreeFragment mBraintreeFragment;

    ArrayAdapter<CharSequence> statesAdapter;

    CoordinatorLayout coordinatorLayout;
    ImageButton signup2ScanCardIcon;
    ImageButton buttonPaypal;
    ImageButton buttonAndroidPay;
    TextView price;
    TextView selectedCreditCardText;
    TextView selectedCreditCardMasked;
    EditText signup2Address;
    EditText signup2Address2;
    TextView signupYesNo;
    EditText signup2City;
    Spinner signup2State;
    EditText signup2Zip;
    CheckBox terms;
    //    TextView termsLink;
    Switch signup2SameAddressSwitch;
    LinearLayout fullBillingAddress, fullBillingAddress2;
    View progress;

    EditText signup2CCNum, signup2CCName, signup2CCExp, signup2CC_CVV;

    TextView signup2NextButton;

    static {
        System.loadLibrary("native-lib");
    }

    private static String CAMERA_PERMISSIONS[] = new String[]{
            Manifest.permission.CAMERA
    };

    private final static int REQUEST_CAMERA_CODE = 0;

    private native static String getSandboxTokenizationKey();

    private native static String getProductionTokenizationKey();

    private boolean isViewShown = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fr_signup_steptwo, container, false);
        ButterKnife.bind(this, rootView);

        coordinatorLayout = rootView.findViewById(R.id.coord_main);
        progress = rootView.findViewById(R.id.progress);
        signup2ScanCardIcon = rootView.findViewById(R.id.SIGNUP2_SCANCARD_ICON);
        selectedCreditCardText = rootView.findViewById(R.id.credit_card_number_copy);
        selectedCreditCardMasked = rootView.findViewById(R.id.credit_card_number);
//        price = rootView.findViewById(R.id.price);
        signup2Address = rootView.findViewById(R.id.SIGNUP2_ADDRESS);
        signup2Address2 = rootView.findViewById(R.id.SIGNUP2_ADDRESS2);
        signup2City = rootView.findViewById(R.id.SIGNUP2_CITY);
        signup2State = rootView.findViewById(R.id.state);
        signup2Zip = rootView.findViewById(R.id.SIGNUP2_ZIP);

//        terms = rootView.findViewById(R.id.checkbox_terms);
//        termsLink = rootView.findViewById(R.id.terms_link);
        signup2SameAddressSwitch = rootView.findViewById(R.id.SIGNUP2_SWITCH);
        fullBillingAddress = rootView.findViewById(R.id.LAYOUT_6);
        fullBillingAddress2 = rootView.findViewById(R.id.LAYOUT_7);
        signup2NextButton = rootView.findViewById(R.id.button_next2);

        signupYesNo = rootView.findViewById(R.id.signup_yes_no);
        signup2CCName = rootView.findViewById(R.id.SIGNUP2_NAME);
        signup2CCNum = rootView.findViewById(R.id.SIGNUP2_CCNUM);
        signup2CC_CVV = rootView.findViewById(R.id.SIGNUP2_CVV);
        signup2CCExp = rootView.findViewById(R.id.SIGNUP2_EXPIRATION);


        signup2ScanCardIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                creditCardClick();
            }
        });

        /* buttonPaypal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                paypalClick();
            }
        }); */

        signup2SameAddressSwitch.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on clicks, depending on whether it's now checked
                if (((Switch) v).isChecked()) {
                    fullBillingAddress.setVisibility(View.GONE);
                    fullBillingAddress2.setVisibility(View.GONE);
                    signupYesNo.setText("Yes!");
                } else {
                    signupYesNo.setText("No");
                    fullBillingAddress.setVisibility(View.VISIBLE);
                    fullBillingAddress2.setVisibility(View.VISIBLE);
                    fullBillingAddress.requestFocus();
                    fullBillingAddress2.requestFocus();

                    statesAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.states_abbrev, R.layout.item_white_spinner);
                    statesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
                    signup2State.setAdapter(statesAdapter);
                }
            }
        });


        final String getTextNum = signup2CCNum.getText().toString();
        final String getTextExp = signup2CCExp.getText().toString();
        final String getTextCVV = signup2CC_CVV.getText().toString();
        final String getTextZip = signup2Zip.getText().toString();
        final String getTextCity = signup2City.getText().toString();
        final String getTextAddress = signup2Address.getText().toString();
        signup2NextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

//                if (TextUtils.isEmpty(getTextName) || TextUtils.isEmpty(getTextNum) || TextUtils.isEmpty(getTextExp) || TextUtils.isEmpty(getTextCVV)) {
//                    Toast.makeText(getActivity(), " Please fill out all required fields. ", Toast.LENGTH_SHORT).show();
//
//                }
                signup2NextButton.setEnabled(true);
                ((SignUpActivity) getActivity()).setPage();

            }

        });

        return rootView;

    }

    public boolean infoIsGood() {
        final String getTextName = signup2CCName.getText().toString();

        if (signup2CCNum.length() == 16 && !TextUtils.isEmpty(getTextName && signup2CCExp.) {
            return true;
        }
        return false;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//
//        String priceAmount = ((SignUpActivity) getActivity()).getPrice();
//        if (priceAmount != null) {
//            Log.d("priceAmount", priceAmount);
//        }
//
//        String formattedPriceText = "$" + priceAmount + "/month";
//
//        price.setText(formattedPriceText);
    }

    public void creditCardClick() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(CAMERA_PERMISSIONS, REQUEST_CAMERA_CODE);
                scanCard();
            } else {
                scanCard();
            }
        } else {
            scanCard();
        }
    }

    public void scanCard() {
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
        String sZip = ProspectUser.zip;
        boolean amc3dMarkup = getIntent().getExtras().getBoolean("amc3dMarkup");
        String facebookToken = getIntent().getExtras().getString("fbToken");

        Log.d("facebookToken:", "" + facebookToken);

        bStreet = ProspectUser.street;
        bStreet2 = ProspectUser.street2;
        bCity = ProspectUser.city;
        bState = ProspectUser.signup1State;
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        signup2NextButton.setEnabled(true);
        if (requestCode == Constants.CARD_SCAN_REQUEST_CODE) {
            if (data != null && data.hasExtra(CardIOActivity.EXTRA_SCAN_RESULT)) {
                final CreditCard scanResult = data.getParcelableExtra(CardIOActivity.EXTRA_SCAN_RESULT);

                signup2CCNum.setText(scanResult.getRedactedCardNumber());

                if (scanResult.isExpiryValid()) {
                    String month = String.valueOf(scanResult.expiryMonth);
                    String year = String.valueOf(scanResult.expiryYear);
                    if (year.length() > 4) {
                        year = year.substring(4);
                    }
                    signup2CCExp.setText(month + " / " + year);
                    signup2CC_CVV.setText(scanResult.cvv);


                    signup2NextButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (signup2CCNum.getText().equals("") || signup2CC_CVV.equals("") || signup2CCName.equals("") || signup2CCExp.equals("")) {
                                Toast.makeText(getActivity(), "Please fill out all required fields", Toast.LENGTH_SHORT).show();
                            } else {
                                signup2NextButton.setEnabled(true);
//                                ((SignUpActivity) getActivity()).setPage("b");
                            }

                        }
                    });
                }
            }
        }
    }

    @Override
    public void setMenuVisibility(final boolean visible) {
        super.setMenuVisibility(visible);
        if (getView() != null) {
            String TAG = "found";
        }

    }

    @Override
    public void onCancel(int requestCode) {
        // Use this to handle a canceled activity, if the given requestCode is important.
        // You may want to use this callback to hide loading indicators, and prepare your UI for input
    }

    @Override
    public void onError(Exception error) {
        if (error instanceof ErrorWithResponse) {
            makeSnackbar(error.getMessage());
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

//

    public boolean canContinue() {
        if (isAddressValid() && isAddress2Valid() && isCityValid() && isStateValid() && isZipValid()) {
            return true;
        } else {
            return false;
        }
    }


    public boolean isAddressValid() {
        if (signup2Address.length() > 2 && signup2Address.length() <= 26) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isAddress2Valid() {
        if ((signup2Address2.length() > 0 && signup2Address2.length() <= 26)
                || signup2Address2.getText().toString().equals("")) {
            return true;
        } else {
            Log.d("mAddress2", signup2Address2.getText().toString());
            return false;
        }
    }

    public boolean isCityValid() {
        if (signup2City.length() > 2 && signup2City.length() <= 26 && !signup2City.getText().toString().matches(".*\\d+.*")) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isStateValid() {
        if (!signup2State.getSelectedItem().toString().equals("State")) {
            Log.d("mStateValue: ", signup2State.getSelectedItem().toString());
            return true;
        } else {
            return false;
        }
    }

    public boolean isZipValid() {
        if (signup2Zip.length() == 5) {
            return true;
        } else {
            return false;
        }
    }

    public static SignUpStepTwoFragment newInstance(String text) {

        SignUpStepTwoFragment f = new SignUpStepTwoFragment();
        Bundle b = new Bundle();
        b.putString("msg", text);

        f.setArguments(b);

        return f;
    }


}


//TODO: CODE FOR PAYPAL

//    public void paypalClick() {
//        progress.setVisibility(View.VISIBLE);
//        buttonPaypal.setEnabled(false);
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
//            buttonPaypal.setEnabled(true);
//            Log.d("error", "error: " + e.getMessage());
//        }
//    }


//    @OnClick(R.id.button_next)
//    public void beginRegistration(String cardNumber, int cardExpMonth, int cardExpYear, String cardCvv) {
//        progress.setVisibility(View.VISIBLE);
//
//        String creditCardNumber = String.valueOf(cardNumber);
//        String month = String.valueOf(cardExpMonth);
//        String year = String.valueOf(cardExpYear);
//        String cvv = String.valueOf(cardCvv);
//
//        final String bStreet;
//        final String bStreet2;
//        final String bCity;
//        final String bState;
//        final String bZip;
//
//        String email = ProspectUser.email;
//        String password = ProspectUser.password;
//        String firstName = ProspectUser.firstName;
//        String lastName = ProspectUser.lastName;
//        String sStreet = ProspectUser.address;
//        String sStreet2 = ProspectUser.address2;
//        String sCity = ProspectUser.city;
//        String sState = ProspectUser.signup2State;
//        String sZip = ProspectUser.zip;
//        boolean amc3dMarkup = false;
//        //String facebookToken = getIntent().getExtras().getString("fbToken");
//
//        if (!signup2SameAddressSwitch.isChecked()) {
//            if (canContinue()) {
//                bStreet = signup2Address.getText().toString();
//                bStreet2 = signup2Address2.getText().toString();
//                bCity = signup2City.getText().toString();
//                bState = signup2State.getSelectedItem().toString();
//                bZip = signup2Zip.getText().toString();
//
//                completeRegistration(creditCardNumber, month, year, cvv, sStreet, sStreet2, sCity, sState,
//                        sZip, bStreet, bStreet2, bCity, bState, bZip, email, firstName, lastName, password, amc3dMarkup);
//            } else {
//                if (!isAddressValid()) {
//                    progress.setVisibility(View.GONE);
//                    makeSnackbar(getString(R.string.fragment_sign_up_step_one_valid_address));
//                } else if (!isAddress2Valid()) {
//                    progress.setVisibility(View.GONE);
//                    makeSnackbar(getString(R.string.fragment_sign_up_step_one_valid_address_two));
//                } else if (!isCityValid()) {
//                    progress.setVisibility(View.GONE);
//                    makeSnackbar(getString(R.string.fragment_sign_up_step_one_valid_city));
//                } else if (!isStateValid()) {
//                    progress.setVisibility(View.GONE);
//                    makeSnackbar(getString(R.string.fragment_sign_up_step_one_valid_state));
//                } else {
//                    progress.setVisibility(View.GONE);
//                    makeSnackbar(getString(R.string.fragment_sign_up_step_one_valid_zip));
//                }
//            }
//        } else {
//            bStreet = ProspectUser.address;
//            bStreet2 = ProspectUser.address2;
//            bCity = ProspectUser.city;
//            bState = ProspectUser.signup2State;
//            bZip = ProspectUser.zip;
//
//            completeRegistration(creditCardNumber, month, year, cvv, sStreet, sStreet2, sCity, sState,
//                    sZip, bStreet, bStreet2, bCity, bState, bZip, email, firstName, lastName, password, amc3dMarkup);
//
//        }
//
//
//    }

//    private void completeRegistration(String creditCardNumber, String month, String year, String cvv, String sStreet,
//                                      String sStreet2, String sCity, String sState, String sZip, String bStreet,
//                                      String bStreet2, String bCity, String bState, String bZip, String email,
//                                      String firstName, String lastName, String password, boolean amc3dMarkup) {
//        if (terms.isChecked()) {
//            progress.setVisibility(View.VISIBLE);
//            signup2NextButton.setEnabled(false);
//
//            final SignUpRequest request = new SignUpRequest(creditCardNumber, month, year, cvv,
//                    sStreet, sStreet2, sCity, sState, sZip, bStreet, bStreet2, bCity, bState, bZip,
//                    email, firstName, lastName, password, amc3dMarkup);
//
//            RestClient.getUnauthenticated().signUp(ProspectUser.session, request).enqueue(new Callback<SignUpResponse>() {
//                @Override
//                public void onResponse(Call<SignUpResponse> call, Response<SignUpResponse> response) {
//
//                    Log.d("isSuccessful", String.valueOf(response.isSuccessful()));
//
//                    if (response.isSuccessful()) {
//
//                        Log.d("subId", response.body().getSubId());
//                        displaySuccess();
//                    } else {
//                        try {
//                            JSONObject jObjError = new JSONObject(response.body().getGlobal());
//
//                            //PENDING RESERVATION GO TO TicketConfirmationActivity or TicketVerificationActivity
//                            makeSnackbar(jObjError.toString());
//                            progress.setVisibility(View.GONE);
//                            signup2NextButton.setEnabled(true);
//
//                        } catch (Exception e) {
//                            makeSnackbar(e.getMessage());
//                        }
//                    }
//                }
//
//                @Override
//                public void onFailure(Call<SignUpResponse> call, Throwable t) {
//                    progress.setVisibility(View.GONE);
//                    signup2NextButton.setEnabled(true);
//                    makeSnackbar(t.getMessage());
//                }
//            });
//        } else {
//            makeSnackbar(getString(R.string.fragment_sign_up_step_two_must_agree_to_terms));
//            progress.setVisibility(View.GONE);
//            signup2NextButton.setEnabled(true);
//        }
//    }


//    private void displaySuccess() {
//        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
//
//        View layout = View.inflate(getActivity(), R.layout.dialog_generic, null);
//
//        alert.setView(layout);
//        alert.setTitle(getString(R.string.fragment_sign_up_step_two_success_header));
//        alert.setMessage(getString(R.string.fragment_sign_up_step_two_success_body));
//        alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                login();
//            }
//        });
//
//        AlertDialog dialog = alert.create();
//        /*dialog.setOnShowListener(new DialogInterface.OnShowListener() {
//            @Override
//            public void onShow(DialogInterface dialog) {
//                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
//                imm.showSoftInput(email, InputMethodManager.SHOW_IMPLICIT);
//            }
//        }); */
//
//        dialog.show();
//        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
//    }
//
//    private void login() {
//        String email = ProspectUser.email;
//        String password = ProspectUser.password;
//
//        LogInRequest request = new LogInRequest(email, password);
//        String deviceId = DeviceID.getID(getActivity());
//
//        RestClient.getUnauthenticated().login(deviceId, request).enqueue(new Callback<User>() {
//            @Override
//            public void onResponse(Call<User> call, Response<User> response) {
//                User user = response.body();
//                if (user != null) {
//                    RestClient.userId = user.getId();
//                    RestClient.deviceUuid = user.getDeviceUuid();
//                    RestClient.authToken = user.getAuthToken();
//
//                    UserPreferences.setUserCredentials(RestClient.userId, RestClient.deviceUuid, RestClient.authToken, user.getFirstName(), user.getEmail());
//
//                    Intent i = new Intent(getActivity(), TheatersActivity.class);
//                    i.putExtra("launch", true);
//                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                    progress.setVisibility(View.GONE);
//                    startActivity(i);
//                }
//            }
//
//            @Override
//            public void onFailure(Call<User> call, Throwable t) {
//                /* TODO : handle failure */
//            }
//        });
//    }
