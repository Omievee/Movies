package com.mobile.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.helpshift.support.Log;
import com.mobile.Constants;
import com.mobile.UserPreferences;
import com.mobile.helpers.LogUtils;
import com.mobile.network.RestClient;
import com.mobile.requests.AddressChangeRequest;
import com.mobile.requests.CreditCardChangeRequest;
import com.mobile.responses.UserInfoResponse;
import com.moviepass.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import io.card.payment.CardIOActivity;
import io.card.payment.CreditCard;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

public class ProfileAccountPlanAndBilling extends MPFragment {


    private static int YES = 0, NO = 1;

    ProfileCancellationFragment cancelSubscription;
    private View rootView, billingAddressRoot, oldBilling, newBillingData, newBillingData2;
    private Button save, cancel;
    private TextView billingDate, plan, planPrice, planCancel, billingCard, billingChange, yesNo;
    private EditText address1, address2, city, state, zip;
    private EditText newBillingCC, newBillingCVV, newBillingExp;
    private ImageButton scanCard;
    private Switch billingSwitch;
    UserInfoResponse userInfoResponse;
    private View progress;
    private TextInputLayout zipTextInputLayout, cityTextInputLayout, stateTextInputLayout, address1TextInputLayout, address2TextInputLayout;
    private TextInputLayout ccNumTextInputLayout, cvvTextInputLayout, expTextInputLayout;
    private boolean updateBillingCard = false;
    private boolean updateBillingAddress = false;
    private boolean billingAddressSameAsShipping = false;
    Context myContext;
    private static String CAMERA_PERMISSIONS[] = new String[]{
            Manifest.permission.CAMERA
    };
    private boolean firstClick = true;
    String MONTH, YEAR;
    private Activity myActivity;

    public ProfileAccountPlanAndBilling() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_profile_account_plan_and_billing, container, false);


        save = rootView.findViewById(R.id.saveChanges);
        cancel = rootView.findViewById(R.id.cancelChanges);
        billingDate = rootView.findViewById(R.id.BillingDate);
        plan = rootView.findViewById(R.id.Plan);
        planPrice = rootView.findViewById(R.id.Plan_Data);
        planCancel = rootView.findViewById(R.id.PLan_cancel);
        billingCard = rootView.findViewById(R.id.USER_BILLING);
        billingChange = rootView.findViewById(R.id.Billing_Change);

        newBillingCC = rootView.findViewById(R.id.profile_ccnum);
        newBillingCVV = rootView.findViewById(R.id.profile_cvv);
        newBillingExp = rootView.findViewById(R.id.profile_expiration);
        scanCard = rootView.findViewById(R.id.profile_scanicon);

        yesNo = rootView.findViewById(R.id.YesNo);
        billingSwitch = rootView.findViewById(R.id.SWITCH);


        oldBilling = rootView.findViewById(R.id.old_billing);
        newBillingData = rootView.findViewById(R.id.profile_newBilling);
        newBillingData2 = rootView.findViewById(R.id.profile_newBilling2);

        address1 = rootView.findViewById(R.id.Address1);
        address2 = rootView.findViewById(R.id.Address2);
        city = rootView.findViewById(R.id.city);
        state = rootView.findViewById(R.id.state);
        zip = rootView.findViewById(R.id.zip);
        billingAddressRoot = rootView.findViewById(R.id.Billing2);
        zipTextInputLayout = rootView.findViewById(R.id.zipTextInputLayout);
        cityTextInputLayout = rootView.findViewById(R.id.cityTextInputLayout);
        stateTextInputLayout = rootView.findViewById(R.id.stateTextInputLayout);
        address1TextInputLayout = rootView.findViewById(R.id.address1TextInputLayout);
        address2TextInputLayout = rootView.findViewById(R.id.address2TextInputLayout);

        ccNumTextInputLayout = rootView.findViewById(R.id.ccNumTextInputLayout);
        cvvTextInputLayout = rootView.findViewById(R.id.cvvTextInputLayout);
        expTextInputLayout = rootView.findViewById(R.id.expTextInputLayout);


        progress = rootView.findViewById(R.id.progress);
        progress.setVisibility(View.VISIBLE);
        loadUserInfo();

        billingSwitch.setOnClickListener(v -> {
            if (billingAddressRoot.getVisibility() == View.GONE) {
                billingSwithChangeState(NO);
                expand(billingAddressRoot);
            } else {
                yesNo.setText("YES");
                collapse(billingAddressRoot);
                billingSwithChangeState(YES);
                yesNo.setTextColor(ContextCompat.getColor(v.getContext(), R.color.new_red));
                billingAddressSameAsShipping = true;
                saveChanges();
            }
        });

        planCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFragment(new ProfileCancellationFragment());
            }
        });

        billingChange.setOnClickListener(v -> {

            oldBilling.setVisibility(View.GONE);
            newBillingData.setVisibility(View.VISIBLE);
            newBillingData2.setVisibility(View.VISIBLE);

            newBillingCC.setText("");
            newBillingCVV.setText("");
            newBillingExp.setText("");

            scanCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    creditCardClick();

                }
            });

        });

        address1.addTextChangedListener(new CustomTextWatcher());
        address2.addTextChangedListener(new CustomTextWatcher());
        state.addTextChangedListener(new CustomTextWatcher());
        zip.addTextChangedListener(new CustomTextWatcher());
        city.addTextChangedListener(new CustomTextWatcher());

        newBillingCC.addTextChangedListener(new CustomTextWatcher());
        newBillingExp.addTextChangedListener(new CustomTextWatcher());
        newBillingCVV.addTextChangedListener(new CustomTextWatcher());

        address1.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (firstClick) {
                    firstClick = false;
                    AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                            .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS)
                            .build();

                    try {
                        Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY).setFilter(typeFilter).build(myActivity);
                        startActivityForResult(intent, Constants.PLACE_AUTOCOMPLETE_REQUEST_CODE);
                    } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
                        // TODO: Handle the error.
                    }
                    return true;
                } else {
                    return false;
                }
            }
        });

        newBillingExp.addTextChangedListener(new TextWatcher() {
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


        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        myContext = context;
        myActivity = getActivity();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                updateBillingAddress = true;
                Place place = PlaceAutocomplete.getPlace(myActivity, data);

                String address = place.getAddress().toString();
                List<String> localList = Arrays.asList(address.split(",", -1));

                for (int i = 0; i < localList.size(); i++) {
                    if (localList.get(2).trim().length() < 8) {
                        Toast.makeText(myActivity, "Invalid Address", Toast.LENGTH_SHORT).show();
                        firstClick = true;
                    } else {
                        address1.setText(localList.get(0));
                        city.setText(localList.get(1));
                        String State = localList.get(2).substring(0, 3);
                        String zip1 = localList.get(2).substring(4, 9);
                        state.setText(State);
                        zip.setText(zip1);
                    }
                }
                saveChanges();


            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(myActivity, data);
                Log.i(Constants.TAG, status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {

            }
        } else if (requestCode == Constants.CARD_SCAN_REQUEST_CODE) {
            if (data != null && data.hasExtra(CardIOActivity.EXTRA_SCAN_RESULT)) {
                final CreditCard scanResult = data.getParcelableExtra(CardIOActivity.EXTRA_SCAN_RESULT);
                updateBillingCard = true;
                String cardNumber = scanResult.getFormattedCardNumber();
                cardNumber = cardNumber.replace(" ", "");
                newBillingCC.setText(cardNumber);


                if (scanResult.isExpiryValid()) {
                    String month = String.valueOf(scanResult.expiryMonth);
                    String year = String.valueOf(scanResult.expiryYear);
                    if (month.length() < 2) {
                        MONTH = "0" + month;
                    } else {
                        MONTH = month;
                    }
                    YEAR = year.substring(2, 4);
                    newBillingExp.setText(MONTH + "/" + YEAR);
                    newBillingCVV.setText(scanResult.cvv);
                }
                saveChanges();

            }
        }
    }

    private void loadUserInfo() {
        int userId = UserPreferences.INSTANCE.getUserId();
        RestClient.getAuthenticated().getUserData(userId).enqueue(new Callback<UserInfoResponse>() {
            @Override
            public void onResponse(Call<UserInfoResponse> call, Response<UserInfoResponse> response) {
                userInfoResponse = response.body();
                if (userInfoResponse != null) {
                    UserPreferences.INSTANCE.saveBilling(userInfoResponse);
                    String address = userInfoResponse.getShippingAddressLine2();
                    List<String> addressList = Arrays.asList(address.split(",", -1));
                    String shippingCity = "", shippingState = "", shippingZip = "";

                    for (int i = 0; i < addressList.size(); i++) {
                        shippingCity = addressList.get(0);
                        shippingState = addressList.get(1);
                        shippingZip = addressList.get(2);
                    }


                    String billingAddress = userInfoResponse.getBillingAddressLine2();
                    List<String> billingAddressList = Arrays.asList(billingAddress.split(",", -1));
                    String billingCity = "", billingState = "", billingZip = "";

                    for (int i = 0; i < billingAddressList.size(); i++) {
                        billingCity = (billingAddressList.get(0));
                        billingState = (billingAddressList.get(1));
                        billingZip = (billingAddressList.get(2));

                    }

                    billingCity = billingCity.trim();
                    billingState = billingState.trim();
                    billingZip = billingZip.trim();

                    shippingCity = shippingCity.trim();
                    shippingState = shippingState.trim();
                    shippingZip = shippingZip.trim();

                    if (userInfoResponse.getBillingAddressLine1().equalsIgnoreCase(userInfoResponse.getShippingAddressLine1())) {
                        if (!shippingCity.equalsIgnoreCase(billingCity) || !shippingState.equalsIgnoreCase(billingState) ||
                                !shippingZip.equalsIgnoreCase(billingZip)) {

                            billingSwitch.setChecked(false);
                            billingSwithChangeState(NO);
                            billingAddressRoot.setVisibility(View.VISIBLE);

                            address1.setText(userInfoResponse.getBillingAddressLine1());
                            city.setText(billingCity);
                            state.setText(billingState);
                            zip.setText(billingZip);
                        }
                    } else {
                        billingSwitch.setChecked(false);
                        billingSwithChangeState(NO);
                        billingAddressRoot.setVisibility(View.VISIBLE);


                        address1.setText(userInfoResponse.getBillingAddressLine1());
                        city.setText(billingCity);
                        state.setText(billingState);
                        zip.setText(billingZip);

                    }

                    billingCard.setText(userInfoResponse.getBillingCard());
                    if (userInfoResponse.getNextBillingDate().equals("")) {
                        billingDate.setText("Unknown");
                    } else {
                        String currentDate = userInfoResponse.getNextBillingDate();
                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                        try {
                            Date currentBillingDateFormatted = format.parse(currentDate);
                            SimpleDateFormat SDFormat = new SimpleDateFormat("MMMM dd, yyyy");
                            billingDate.setText(SDFormat.format(currentBillingDateFormatted));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }

                    plan.setText(userInfoResponse.getPlan());
                    progress.setVisibility(View.GONE);

                }
            }

            @Override
            public void onFailure(Call<UserInfoResponse> call, Throwable t) {
                Toast.makeText(myActivity, "Server Error; Please try again.", Toast.LENGTH_SHORT).show();
                myActivity.onBackPressed();
                progress.setVisibility(View.GONE);
            }
        });
    }

    public void creditCardClick() {
        if (ContextCompat.checkSelfPermission(myActivity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(CAMERA_PERMISSIONS, Constants.REQUEST_CAMERA_CODE);
            }
            scan_card();
        } else {
            scan_card();
        }
    }


    public void scan_card() {
        Intent scanIntent = new Intent(myActivity, CardIOActivity.class);
        // customize these values to suit your needs.
        scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_EXPIRY, true); // default: false
        scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_CVV, true); // default: false
        scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_POSTAL_CODE, false); // default: false
        startActivityForResult(scanIntent, Constants.CARD_SCAN_REQUEST_CODE);
    }

    public void saveChanges() {
        save.setTextColor(getResources().getColor(R.color.new_red));
        cancel.setTextColor(getResources().getColor(R.color.white));
        save.setClickable(true);
        save.setOnClickListener(v -> {
            progress.setVisibility(View.VISIBLE);
            removeAllErrors();
            InputMethodManager imm = (InputMethodManager) myActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(Objects.requireNonNull(getView()).getWindowToken(), 0);
            }
            if (updateBillingCard) {
                updateCCData();
            }
            if (updateBillingAddress) {
                updateBillingAddress();
            }
            if (billingAddressSameAsShipping) {
                updateBillingAddressToShippingAddress();
            }
        });

        cancel.setOnClickListener(view -> myActivity.onBackPressed());
    }

    //LOGIC IS HERE
    //CANT USE RIGHT NOW BECAUSE OF THE WAY ADDRESS IS SET UP IN THE BACK END
    private void updateBillingAddressToShippingAddress() {
        int userId = UserPreferences.INSTANCE.getUserId();

        String address = userInfoResponse.getShippingAddressLine2();
        List<String> addressList = Arrays.asList(address.split(",", -1));
        String shippingCity = "", shippingState = "", shippingZip = "";
        String addres1 = userInfoResponse.getShippingAddressLine1().trim();

        for (int i = 0; i < addressList.size(); i++) {
            shippingCity = (addressList.get(0).trim());
            shippingState = (addressList.get(1).trim());
            shippingZip = (addressList.get(2).trim());
        }

        String type = "billingAddress";
        LogUtils.newLog("Billing address", "onResponse: Address: " + addres1 + " State " + shippingState + " City " + shippingCity + " Zip " + shippingZip);

        AddressChangeRequest request = new AddressChangeRequest(addres1, "", shippingCity, shippingState, shippingZip, type);
        String finalShippingState = shippingState;
        RestClient.getAuthenticated().updateAddress(userId, request).enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
                if (response.isSuccessful()) {
                    loadUserInfo();
                    Toast.makeText(myActivity, "Billing Information Updated", Toast.LENGTH_SHORT).show();
                    myActivity.onBackPressed();
                } else {
                    Toast.makeText(myActivity, "Invalid address. Please try another address.", Toast.LENGTH_SHORT).show();

                }
                progress.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                progress.setVisibility(View.GONE);
                Toast.makeText(myActivity, "Server Response Error", Toast.LENGTH_SHORT).show();
                myActivity.onBackPressed();
            }
        });
    }

    public void removeAllErrors() {
        ccNumTextInputLayout.setError(null);
        cvvTextInputLayout.setError(null);
        expTextInputLayout.setError(null);
        address1TextInputLayout.setError(null);
        cityTextInputLayout.setError(null);
        stateTextInputLayout.setError(null);
        zipTextInputLayout.setError(null);
    }

    public void updateCCData() {
        if (!newBillingCC.getText().toString().trim().isEmpty() && !newBillingExp.getText().toString().trim().isEmpty() && !newBillingCVV.getText().toString().trim().isEmpty()) {
            if (newBillingCC.getText().toString().length() >= 15) {
                ccNumTextInputLayout.setError(null);
                if (newBillingCVV.getText().toString().length() >= 3) {
                    cvvTextInputLayout.setError(null);
                    if (newBillingExp.getText().toString().length() >= 5) {
                        Calendar c = Calendar.getInstance();
                        int year = c.get(Calendar.YEAR);
                        int month = c.get(Calendar.MONTH);

                        int ccYear = Integer.valueOf(newBillingExp.getText().toString().charAt(3) + "" + newBillingExp.getText().toString().charAt(4));
                        int ccMonth = Integer.valueOf(newBillingExp.getText().toString().charAt(0) + "" + newBillingExp.getText().toString().charAt(1));
                        ccYear += 2000;

                        if ((year < ccYear) || (year == ccYear && month <= ccMonth)) {
                            expTextInputLayout.setError(null);
                            String newCC = newBillingCC.getText().toString();
                            String ccExMonth = newBillingExp.getText().toString().substring(0, 2);
                            String ccExYr = "20" + newBillingExp.getText().toString().substring(3, 5);
                            String newExp = ccExMonth + "/" + ccExYr;
                            String newCVV = newBillingCVV.getText().toString();
                            updateCreditCard(newCC, newExp, newCVV);
                        } else {
                            progress.setVisibility(View.GONE);
                            expTextInputLayout.setError(getResources().getString(R.string.invalid_exp));
                        }

                    } else {
                        progress.setVisibility(View.GONE);
                        expTextInputLayout.setError(getResources().getString(R.string.invalid_exp));
                    }
                } else {
                    progress.setVisibility(View.GONE);
                    cvvTextInputLayout.setError(getResources().getString(R.string.invalid_cvv));
                }
            } else {
                progress.setVisibility(View.GONE);
                ccNumTextInputLayout.setError(getResources().getString(R.string.credit_card_invalid_number));
            }
        } else {
            progress.setVisibility(View.GONE);
            if (newBillingCVV.getText().toString().trim().isEmpty())
                cvvTextInputLayout.setError(getResources().getString(R.string.invalid_cvv));
            if (newBillingExp.getText().toString().trim().isEmpty())
                expTextInputLayout.setError(getResources().getString(R.string.invalid_exp));
            if (newBillingCC.getText().toString().trim().isEmpty())
                ccNumTextInputLayout.setError(getResources().getString(R.string.credit_card_invalid_number));
        }
    }


    public void updateCreditCard(String creditCardNumber, String expirationDate, String cvv) {
        progress.setVisibility(View.VISIBLE);
        int userId = UserPreferences.INSTANCE.getUserId();
        CreditCardChangeRequest request = new CreditCardChangeRequest(creditCardNumber, expirationDate, cvv);
        RestClient.getAuthenticated().updateBillingCard(userId, request).enqueue(new Callback<UserInfoResponse>() {
            @Override
            public void onResponse(Call<UserInfoResponse> call, Response<UserInfoResponse> response) {

                if (response.isSuccessful() && response.body() != null) {
                    progress.setVisibility(View.GONE);
                    newBillingCC.clearComposingText();
                    newBillingCVV.clearComposingText();
                    newBillingExp.clearComposingText();
                    Toast.makeText(myContext, "Billing Information Updated", Toast.LENGTH_SHORT).show();
                    myActivity.onBackPressed();
                } else {
                    loadUserInfo();
                    Toast.makeText(myContext, "Error updating credit card information", Toast.LENGTH_SHORT).show();
                }
                progress.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<UserInfoResponse> call, Throwable t) {
                if (myActivity != null) {
                    Toast.makeText(myActivity, "Error updating credit card information", Toast.LENGTH_SHORT).show();
                    progress.setVisibility(View.GONE);
                    myActivity.onBackPressed();
                }

            }
        });
    }

    private boolean isValidAddress() {
        address1TextInputLayout.setError(null);
        cityTextInputLayout.setError(null);
        stateTextInputLayout.setError(null);
        zipTextInputLayout.setError(null);

        int i = 0;
        if (!address1.getText().toString().trim().isEmpty() && !city.getText().toString().trim().isEmpty() && !zip.getText().toString().trim().isEmpty() && !state.getText().toString().trim().isEmpty()) {

            //Validating Address
            String[] address1Array = address1.getText().toString().split("\\W+");
            if (address1Array.length >= 2 && address1Array[0].trim().matches(".*\\d+.*")) {
                i++;
            } else {
                address1TextInputLayout.setError(getResources().getString(R.string.address_invalid_address));
                address1.clearFocus();
                LogUtils.newLog("ADDRESS", "isValidAddress: ");
            }

            //Validating City
            String[] cityArray = city.getText().toString().split("\\W+");
            String cityWithNotWhiteSpaces = city.getText().toString().replaceAll("\\s+", "");
            //If city has less than 3 words
            if (cityArray.length <= 3 && cityWithNotWhiteSpaces.matches("^[a-zA-Z]+$")) {
                i++;
            } else {
                cityTextInputLayout.setError(getResources().getString(R.string.address_invalid_city));
                city.clearFocus();
            }

            //Validating State
            if (state.getText().toString().trim().length() == 2 && state.getText().toString().trim().matches("^[a-zA-Z]+$")) {
                i++;
            } else {
                stateTextInputLayout.setError(getResources().getString(R.string.address_invalid_state));
                state.clearFocus();
            }

            //Validating Zip Code
            if (zip.getText().toString().trim().matches("^[0-9]+$") && zip.getText().toString().trim().length() >= 5) {
                i++;
            } else {
                zipTextInputLayout.setError(getResources().getString(R.string.address_invalid_zip));
                zip.clearFocus();
            }


        } else {
            if (address1.getText().toString().trim().isEmpty()) {
                address1TextInputLayout.setError(getResources().getString(R.string.address_empty_shipping_address));
                address1.clearFocus();
            }
            if (state.getText().toString().trim().isEmpty()) {
                stateTextInputLayout.setError(getResources().getString(R.string.address_empty_state));
                state.clearFocus();
            }
            if (zip.getText().toString().trim().isEmpty()) {
                zipTextInputLayout.setError(getResources().getString(R.string.address_empty_zip));
                zip.clearFocus();
            }
            if (city.getText().toString().trim().isEmpty()) {
                cityTextInputLayout.setError(getResources().getString(R.string.address_empty_city));
                city.clearFocus();
            }
        }
        if (i == 4)
            return true;
        return false;
    }

    public void updateBillingAddress() {
        int userId = UserPreferences.INSTANCE.getUserId();
        if (address1.getText().toString() != userInfoResponse.getBillingAddressLine1()) {
            if (isValidAddress()) {
                String newAddress = address1.getText().toString();
                String newAddress2 = address2.getText().toString();
                String newCity = city.getText().toString();
                String newZip = zip.getText().toString();
                String newState = state.getText().toString().toUpperCase();

                String type = "billingAddress";

                AddressChangeRequest request = new AddressChangeRequest(newAddress, newAddress2, newCity, newState, newZip, type);
                RestClient.getAuthenticated().updateAddress(userId, request).enqueue(new Callback<Object>() {
                    @Override
                    public void onResponse(Call<Object> call, Response<Object> response) {
                        if (response != null && response.isSuccessful()) {
                            loadUserInfo();
                            Toast.makeText(myActivity, "Billing Information Updated", Toast.LENGTH_SHORT).show();
                            myActivity.onBackPressed();
                        } else {
                            Toast.makeText(myActivity, "Invalid address. Please try another address.", Toast.LENGTH_SHORT).show();
                        }
                        progress.setVisibility(View.GONE);
                    }

                    @Override
                    public void onFailure(Call<Object> call, Throwable t) {
                        progress.setVisibility(View.GONE);
                        Toast.makeText(myActivity, "Server Response Error", Toast.LENGTH_SHORT).show();
                        myActivity.onBackPressed();
                    }
                });
            } else {
                progress.setVisibility(View.GONE);
            }
        }

    }

    public void billingSwithChangeState(int option) {

        if (option == YES) {
            yesNo.setText("YES");
            yesNo.setTextColor(ContextCompat.getColor(myContext, R.color.new_red));
        } else {

            yesNo.setText("NO");
            yesNo.setTextColor(ContextCompat.getColor(myContext, R.color.white));
        }
    }

    /* ANIMATION FOR COLLAPSE & EXPAND VIEWS */
    public static void expand(final View v) {
        v.measure(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        final int targetHeight = v.getMeasuredHeight();

        v.getLayoutParams().height = 1;
        v.setVisibility(View.VISIBLE);

        Animation animate = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                v.getLayoutParams().height = interpolatedTime == 1 ? LinearLayout.LayoutParams.WRAP_CONTENT : (int) (targetHeight * interpolatedTime);
                v.requestLayout();

            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }


        };

        animate.setDuration((long) (targetHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(animate);
    }

    public static void collapse(final View v) {
        final int initialHeight = v.getMeasuredHeight();

        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if (interpolatedTime == 1) {
                    v.setVisibility(View.GONE);
                } else {
                    v.getLayoutParams().height = initialHeight - (int) (initialHeight * interpolatedTime);
                    v.requestLayout();
                }

            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        a.setDuration((long) (initialHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }


    @Override
    public void onDetach() {
        super.onDetach();
        myActivity = null;
    }

    public class CustomTextWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {

            if (address1.isFocused() || address2.isFocused() || city.isFocused() || state.isFocused() || zip.isFocused()) {
                updateBillingAddress = true;
                saveChanges();
            }
            if (newBillingCC.isFocused() || newBillingCVV.isFocused() || newBillingExp.isFocused()) {
                updateBillingCard = true;
                saveChanges();
            }
            if (address1.hasFocus())
                address1TextInputLayout.setError(null);
            if (state.hasFocus())
                stateTextInputLayout.setError(null);
            if (city.hasFocus())
                cityTextInputLayout.setError(null);
            if (zip.hasFocus())
                zip.setError(null);
            if (newBillingCC.hasFocus())
                ccNumTextInputLayout.setError(null);
            if (newBillingCVV.hasFocus())
                cvvTextInputLayout.setError(null);
            if (newBillingExp.hasFocus())
                expTextInputLayout.setError(null);

        }
    }
}
