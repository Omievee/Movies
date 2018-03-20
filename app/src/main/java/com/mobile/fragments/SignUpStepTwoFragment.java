package com.mobile.fragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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
import com.mobile.Constants;
import com.mobile.activities.SignUpActivity;
import com.moviepass.R;

import java.util.Calendar;

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

    View view;

    public static final String CREDITCARD_DATA = "card data";

    public static final String TAG = "foudnit";

    OnCreditCardEntered creditCardDataListener;

    CoordinatorLayout coordinatorLayout;
    ImageButton signup2ScanCardIcon;
    ImageButton buttonPaypal;
    ImageButton buttonAndroidPay;
    TextView selectedCreditCardText;
    TextView selectedCreditCardMasked;
    EditText signup2Address;
    EditText signup2Address2;
    TextView signupYesNo;
    EditText signup2City;
    Spinner signup2State;
    EditText signup2Zip;
    Switch signup2SameAddressSwitch;
    LinearLayout fullBillingAddress, fullBillingAddress2;
    TextInputLayout ccNumTextInputLayout, cvvTextInputLayout, expTextInputLayout;
    View progress;

    String MONTH, YEAR;
    EditText signup2CCNum, signup2CCName, signup2CCExp, signup2CC_CVV;

    TextView signup2NextButton;

    static {
        System.loadLibrary("native-lib");
    }

    private static String CAMERA_PERMISSIONS[] = new String[]{
            Manifest.permission.CAMERA
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fr_signup_steptwo, container, false);
        return view;
    }


    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);


        coordinatorLayout = view.findViewById(R.id.coord_main);
        progress = view.findViewById(R.id.progress);
        signup2ScanCardIcon = view.findViewById(R.id.SIGNUP2_SCANCARD_ICON);
        //  selectedCreditCardText = view.findViewById(R.id.credit_card_number_copy);
        // selectedCreditCardMasked = view.findViewById(R.id.credit_card_number);

        signup2Address = view.findViewById(R.id.SIGNUP2_ADDRESS);
        signup2Address2 = view.findViewById(R.id.SIGNUP2_ADDRESS2);
        signup2City = view.findViewById(R.id.SIGNUP2_CITY);
        signup2State = view.findViewById(R.id.signup2Spinner);
        signup2Zip = view.findViewById(R.id.SIGNUP2_ZIP);
        signup2SameAddressSwitch = view.findViewById(R.id.SIGNUP2_SWITCH);

        fullBillingAddress = view.findViewById(R.id.LAYOUT_6);
        fullBillingAddress2 = view.findViewById(R.id.LAYOUT_7);
        signup2NextButton = view.findViewById(R.id.button_next2);
        signupYesNo = view.findViewById(R.id.signup_yes_no);
        signup2CCNum = view.findViewById(R.id.SIGNUP2_CCNUM);
        signup2CC_CVV = view.findViewById(R.id.SIGNUP2_CVV);
        signup2CCExp = view.findViewById(R.id.SIGNUP2_EXPIRATION);

        ccNumTextInputLayout = view.findViewById(R.id.ccNumTextInputLayout);
        cvvTextInputLayout = view.findViewById(R.id.cvvTextInputLayout);
        expTextInputLayout = view.findViewById(R.id.expTextInputLayout);


        signup2ScanCardIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                creditCardClick();
            }
        });


        signup2SameAddressSwitch.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on clicks, depending on whether it's now checked
                if (((Switch) v).isChecked()) {
                    fullBillingAddress.setVisibility(View.GONE);
                    fullBillingAddress2.setVisibility(View.GONE);
                    signupYesNo.setText("YES");
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

        signup2CCExp.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0 && (s.length() % 3) == 0) {
                    final char c = s.charAt(s.length() - 1);
                    if ('/' == c) {
                        s.delete(s.length() - 1, s.length());
                    }
                }
                if (s.length() > 0 && (s.length() % 3) == 0) {
                    char c = s.charAt(s.length() - 1);
                    if (Character.isDigit(c) && TextUtils.split(s.toString(), String.valueOf("/")).length <= 2) {
                        s.insert(s.length() - 1, String.valueOf("/"));
                    }
                }
            }
        });


        signup2NextButton.setOnClickListener(view1 -> {
            ccNumTextInputLayout.setError(null);
            cvvTextInputLayout.setError(null);
            expTextInputLayout.setError(null);
            signup2CCNum.clearFocus();
            signup2CCExp.clearFocus();
            signup2CC_CVV.clearFocus();
            if (infoIsGood()) {
                Calendar c = Calendar.getInstance();
                int year = c.get(Calendar.YEAR);
                int month = c.get(Calendar.MONTH);

                int ccYear = Integer.valueOf(signup2CCExp.getText().toString().charAt(3)+""+signup2CCExp.getText().toString().charAt(4));
                int ccMonth = Integer.valueOf(signup2CCExp.getText().toString().charAt(0)+""+signup2CCExp.getText().toString().charAt(1));
                ccYear+=2000;

                if((year<ccYear)||(year==ccYear && month<=ccMonth)){
                    String ccNum = signup2CCNum.getText().toString();
                    String ccEx = signup2CCExp.getText().toString().substring(0, 2);
                    String ccEx2 = signup2CCExp.getText().toString().substring(3, 5);
                    String ccCVV = signup2CC_CVV.getText().toString();
                    creditCardDataListener.OnCreditCardEntered(ccNum, ccEx, ccEx2, ccCVV);
                    Log.d(TAG, "onViewCreated: " + ccNum + ccEx + ccEx2 + ccCVV);
                    if (((SignUpActivity) getActivity()) != null) {
                        ((SignUpActivity) getActivity()).setPage();
                    }
                } else {
                    progress.setVisibility(View.GONE);
                    expTextInputLayout.setError("Invalid Expiration Date");
                }

            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        signup2CCNum.setError(null);
        signup2CC_CVV.setError(null);
        signup2CCExp.setError(null);
    }

    public boolean infoIsGood() {
        if (signup2CCNum.length() == 16
                && !signup2CCExp.getText().toString().isEmpty()
                && signup2CCExp.getText().toString().length() == 5
                && !signup2CC_CVV.getText().toString().isEmpty()
                && signup2CC_CVV.getText().toString().length() <= 4 && signup2CC_CVV.length()>=3) {
            return true;


        }
        else{
            if(signup2CCNum.length()!=16) {
                if(signup2CCNum.getText().toString().trim().isEmpty())
                    ccNumTextInputLayout.setError("Required");
                else
                    ccNumTextInputLayout.setError("Invalid Credit Card Number");
                signup2CCNum.clearFocus();
            }
            if(signup2CC_CVV.length() != 4 && signup2CC_CVV.length()!=3){
                if(signup2CC_CVV.getText().toString().trim().isEmpty())
                    cvvTextInputLayout.setError("Required");
                else
                    cvvTextInputLayout.setError("Invalid CVV");
                signup2CC_CVV.clearFocus();
            }
            if(signup2CCExp.length()!=5) {
                if(signup2CCExp.getText().toString().trim().isEmpty())
                    expTextInputLayout.setError("Required");
                else
                    expTextInputLayout.setError("Invalid Expiration Date");
                signup2CCExp.clearFocus();
            }
        }
        return false;
    }

    public void creditCardClick() {
        if (ContextCompat.checkSelfPermission(view.getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(CAMERA_PERMISSIONS, Constants.REQUEST_CAMERA_CODE);
            scanCard();
        } else {

            scanCard();

        }
    }

    public void scanCard() {
        Intent scanIntent = new Intent(getActivity(), CardIOActivity.class);

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
                    if (month.length() < 2) {
                        MONTH = "0" + month;
                    } else {
                        MONTH = month;
                    }
                    YEAR = year.substring(2, 4);
                    signup2CCExp.setText(MONTH + "/" + YEAR);
                    signup2CC_CVV.setText(scanResult.cvv);
                    signup2NextButton.setOnClickListener(view -> {
                        if (signup2CCNum.getText().equals("") || signup2CC_CVV.equals("") || signup2CCExp.equals("")
                                || signup2CCNum.getText().length() < 16 || signup2CC_CVV.getText().length() < 3 || signup2CCExp.getText().length() < 5) {

                            infoIsGood();
                        } else {
                            signup2NextButton.setEnabled(true);
                            ((SignUpActivity) getActivity()).setPage();
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
        final Snackbar snackbar = Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_SHORT);
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

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnCreditCardEntered) {

            creditCardDataListener = (OnCreditCardEntered) context;
        } else {
            throw new ClassCastException(context.toString() + " must implement "
                    + OnCreditCardEntered.class.getCanonicalName());
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        creditCardDataListener = null;
    }

    public interface OnCreditCardEntered {
        void OnCreditCardEntered(String ccNum, String ccExMonth, String ccExYear, String ccCVV);
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


