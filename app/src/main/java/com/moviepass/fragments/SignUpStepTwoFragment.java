package com.moviepass.fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.braintreepayments.api.BraintreeFragment;
import com.braintreepayments.api.PayPal;
import com.braintreepayments.api.exceptions.ErrorWithResponse;
import com.braintreepayments.api.exceptions.InvalidArgumentException;
import com.braintreepayments.api.interfaces.BraintreeCancelListener;
import com.braintreepayments.api.interfaces.BraintreeErrorListener;
import com.braintreepayments.api.interfaces.PaymentMethodNonceCreatedListener;
import com.braintreepayments.api.models.PaymentMethodNonce;
import com.moviepass.BuildConfig;
import com.moviepass.Constants;
import com.moviepass.DeviceID;
import com.moviepass.R;
import com.moviepass.UserPreferences;
import com.moviepass.activities.SignUpActivity;
import com.moviepass.activities.TheatersActivity;
import com.moviepass.model.ProspectUser;
import com.moviepass.model.User;
import com.moviepass.network.RestClient;
import com.moviepass.requests.LogInRequest;
import com.moviepass.requests.SignUpRequest;
import com.moviepass.responses.SignUpResponse;

import org.json.JSONObject;

import butterknife.ButterKnife;
import butterknife.OnClick;
import io.card.payment.CardIOActivity;
import io.card.payment.CreditCard;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by anubis on 7/11/17.
 */

public class SignUpStepTwoFragment extends Fragment implements PaymentMethodNonceCreatedListener,
        BraintreeCancelListener, BraintreeErrorListener {

    BraintreeFragment mBraintreeFragment;

    ArrayAdapter<CharSequence> statesAdapter;

    RelativeLayout relativeLayout;
    ImageButton buttonCreditCard;
    ImageButton buttonPaypal;
    ImageButton buttonAndroidPay;
    TextView price;
    TextView selectedCreditCardText;
    TextView selectedCreditCardMasked;
    EditText etAddress;
    EditText etAddress2;
    EditText etCity;
    Spinner state;
    EditText etZip;
    CheckBox terms;
    CheckBox billingAddress;
    LinearLayout fullBillingAddress;
    View progress;

    Button buttonFinish;

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
        View rootView = inflater.inflate(R.layout.fragment_sign_up_step_two, container, false);
        ButterKnife.bind(this, rootView);

        relativeLayout = rootView.findViewById(R.id.relative_layout);
        progress = rootView.findViewById(R.id.progress);
        buttonCreditCard = rootView.findViewById(R.id.button_credit_card);
//        buttonPaypal = rootView.findViewById(R.id.button_paypal);
//        buttonAndroidPay = rootView.findViewById(R.id.button_android_pay);
        selectedCreditCardText = rootView.findViewById(R.id.credit_card_number_copy);
        selectedCreditCardMasked = rootView.findViewById(R.id.credit_card_number);
        price = rootView.findViewById(R.id.price);
        etAddress = rootView.findViewById(R.id.et_address);
        etAddress2 = rootView.findViewById(R.id.et_address_two);
        etCity = rootView.findViewById(R.id.et_city);
        state = rootView.findViewById(R.id.state);
        etZip = rootView.findViewById(R.id.et_zip);

        terms = rootView.findViewById(R.id.checkbox_terms);
        billingAddress = rootView.findViewById(R.id.checkbox_address);
        fullBillingAddress = rootView.findViewById(R.id.full_billing_address);
        buttonFinish = rootView.findViewById(R.id.button_next);

        buttonCreditCard.setOnClickListener(new View.OnClickListener() {
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

        billingAddress.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on clicks, depending on whether it's now checked
                if (((CheckBox) v).isChecked()) {
                    fullBillingAddress.setVisibility(View.GONE);
                } else {
                    fullBillingAddress.setVisibility(View.VISIBLE);

                    statesAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.states_abbrev, R.layout.item_white_spinner);
                    statesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
                    state.setAdapter(statesAdapter);
                }
            }
        });

        return rootView;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String priceAmount = ((SignUpActivity) getActivity()).getPrice();
        if (priceAmount != null) {
            Log.d("priceAmount", priceAmount);
        }

        String formattedPriceText = "$" + priceAmount + "/month";

        price.setText(formattedPriceText);
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

    public void paypalClick() {
        progress.setVisibility(View.VISIBLE);
        buttonPaypal.setEnabled(false);
        try {
            if (BuildConfig.DEBUG) {
                String mAuthorization = getSandboxTokenizationKey();
                mBraintreeFragment = BraintreeFragment.newInstance(getActivity(), mAuthorization);
                progress.setVisibility(View.GONE);
                // mBraintreeFragment is ready to use!

                PayPal.authorizeAccount(mBraintreeFragment);
            } else {
                String mAuthorization = getProductionTokenizationKey();
                mBraintreeFragment = BraintreeFragment.newInstance(getActivity(), mAuthorization);
                progress.setVisibility(View.GONE);
                // mBraintreeFragment is ready to use!

                PayPal.authorizeAccount(mBraintreeFragment);
            }

        } catch (InvalidArgumentException e) {
            // There was an issue with your authorization string.
            buttonPaypal.setEnabled(true);
            Log.d("error", "error: " + e.getMessage());
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        buttonFinish.setEnabled(true);

        if (requestCode == Constants.CARD_SCAN_REQUEST_CODE) {
            if (data != null && data.hasExtra(CardIOActivity.EXTRA_SCAN_RESULT)) {
                final CreditCard scanResult = data.getParcelableExtra(CardIOActivity.EXTRA_SCAN_RESULT);

                selectedCreditCardText.setVisibility(View.VISIBLE);
                selectedCreditCardMasked.setVisibility(View.VISIBLE);
                selectedCreditCardMasked.setText(scanResult.getRedactedCardNumber());

                if (scanResult.isExpiryValid()) {
                    String month = String.valueOf(scanResult.expiryMonth);
                    String year = String.valueOf(scanResult.expiryYear);
                    if (year.length() > 4) {
                        year = year.substring(4);
                    }
                }

                buttonFinish.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        beginRegistration(scanResult.cardNumber, scanResult.expiryMonth, scanResult.expiryYear, scanResult.cvv);
                    }
                });
            }
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

    @OnClick(R.id.button_next)
    public void beginRegistration(String cardNumber, int cardExpMonth, int cardExpYear, String cardCvv) {
        progress.setVisibility(View.VISIBLE);

        String creditCardNumber = String.valueOf(cardNumber);
        String month = String.valueOf(cardExpMonth);
        String year = String.valueOf(cardExpYear);
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
        //String facebookToken = getIntent().getExtras().getString("fbToken");

        if (!billingAddress.isChecked()) {
            if (canContinue()) {
                bStreet = etAddress.getText().toString();
                bStreet2 = etAddress2.getText().toString();
                bCity = etCity.getText().toString();
                bState = state.getSelectedItem().toString();
                bZip = etZip.getText().toString();

                completeRegistration(creditCardNumber, month, year, cvv, sStreet, sStreet2, sCity, sState,
                        sZip, bStreet, bStreet2, bCity, bState, bZip, email, firstName, lastName, password, amc3dMarkup);
            } else {
                if (!isAddressValid()) {
                    progress.setVisibility(View.GONE);
                    makeSnackbar(getString(R.string.fragment_sign_up_step_one_valid_address));
                } else if (!isAddress2Valid()) {
                    progress.setVisibility(View.GONE);
                    makeSnackbar(getString(R.string.fragment_sign_up_step_one_valid_address_two));
                } else if (!isCityValid()) {
                    progress.setVisibility(View.GONE);
                    makeSnackbar(getString(R.string.fragment_sign_up_step_one_valid_city));
                } else if (!isStateValid()) {
                    progress.setVisibility(View.GONE);
                    makeSnackbar(getString(R.string.fragment_sign_up_step_one_valid_state));
                } else {
                    progress.setVisibility(View.GONE);
                    makeSnackbar(getString(R.string.fragment_sign_up_step_one_valid_zip));
                }
            }
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
        if (terms.isChecked()) {
            progress.setVisibility(View.VISIBLE);
            buttonFinish.setEnabled(false);

            final SignUpRequest request = new SignUpRequest(creditCardNumber, month, year, cvv,
                    sStreet, sStreet2, sCity, sState, sZip, bStreet, bStreet2, bCity, bState, bZip,
                    email, firstName, lastName, password, amc3dMarkup);

            RestClient.getUnauthenticated().signUp(ProspectUser.session, request).enqueue( new Callback<SignUpResponse>() {
                @Override
                public void onResponse(Call<SignUpResponse> call, Response<SignUpResponse> response) {

                    Log.d("isSuccessful", String.valueOf(response.isSuccessful()));

                    if (response.isSuccessful()) {

                        Log.d("subId", response.body().getSubId());
                        displaySuccess();
                    } else {
                        try {
                            JSONObject jObjError = new JSONObject(response.body().getGlobal());

                            //PENDING RESERVATION GO TO TicketConfirmationActivity or TicketVerificationActivity
                            makeSnackbar(jObjError.toString());
                            progress.setVisibility(View.GONE);
                            buttonFinish.setEnabled(true);

                        } catch (Exception e) {
                            makeSnackbar(e.getMessage());
                        }
                    }
                }

                @Override
                public void onFailure(Call<SignUpResponse> call, Throwable t) {
                    progress.setVisibility(View.GONE);
                    buttonFinish.setEnabled(true);
                    makeSnackbar(t.getMessage());
                }
            });
        } else {
            makeSnackbar(getString(R.string.fragment_sign_up_step_two_must_agree_to_terms));
            progress.setVisibility(View.GONE);
            buttonFinish.setEnabled(true);
        }
    }



    private void displaySuccess() {
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());

        View layout = View.inflate(getActivity(), R.layout.dialog_generic, null);

        alert.setView(layout);
        alert.setTitle(getString(R.string.fragment_sign_up_step_two_success_header));
        alert.setMessage(getString(R.string.fragment_sign_up_step_two_success_body));
        alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                login();
            }
        });

        AlertDialog dialog = alert.create();
        /*dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(email, InputMethodManager.SHOW_IMPLICIT);
            }
        }); */

        dialog.show();
        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
    }

    private void login() {
        String email = ProspectUser.email;
        String password = ProspectUser.password;

        LogInRequest request = new LogInRequest(email, password);
        String deviceId = DeviceID.getID(getActivity());

        RestClient.getUnauthenticated().login(deviceId, request).enqueue( new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                User user=response.body();
                if(user!=null){
                    RestClient.userId = user.getId();
                    RestClient.deviceUuid = user.getDeviceUuid();
                    RestClient.authToken = user.getAuthToken();

                    UserPreferences.setUserCredentials(RestClient.userId, RestClient.deviceUuid, RestClient.authToken, user.getFirstName(), user.getEmail());

                    Intent i = new Intent(getActivity(), TheatersActivity.class);
                    i.putExtra("launch", true);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    progress.setVisibility(View.GONE);
                    startActivity(i);
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                /* TODO : handle failure */
            }
        });
    }

    public void makeSnackbar(String message) {
        final Snackbar snackbar = Snackbar.make(relativeLayout, message, Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction("OK", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                snackbar.dismiss();
            }
        });
        snackbar.show();
    }

    @Override
    public void setMenuVisibility(final boolean visible) {
        super.setMenuVisibility(visible);
        if (getView() != null) {
            if (visible) {
                String priceAmount = ((SignUpActivity) getActivity()).getPrice();
                if (priceAmount != null) {
                    Log.d("priceAmount", priceAmount);
                }

                String formattedPriceText = "$" + priceAmount + "/month";

                price.setText(formattedPriceText);
            } else {
                isViewShown = false;
            }
        }
    }

    public boolean canContinue() {
        if (isAddressValid() && isAddress2Valid() && isCityValid() && isStateValid() && isZipValid()) {
            return true;
        } else {
            return false;
        }
    }


    public boolean isAddressValid() {
        if (etAddress.length() > 2 && etAddress.length() <= 26) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isAddress2Valid() {
        if ((etAddress2.length() > 0 && etAddress2.length() <= 26)
                || etAddress2.getText().toString().equals("")) {
            return true;
        } else {
            Log.d("mAddress2", etAddress2.getText().toString());
            return false;
        }
    }

    public boolean isCityValid() {
        if (etCity.length() > 2 && etCity.length() <= 26 && !etCity.getText().toString().matches(".*\\d+.*")) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isStateValid() {
        if (!state.getSelectedItem().toString().equals("State")) {
            Log.d("mStateValue: ", state.getSelectedItem().toString());
            return true;
        } else {
            return false;
        }
    }

    public boolean isZipValid() {
        if (etZip.length() == 5) {
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