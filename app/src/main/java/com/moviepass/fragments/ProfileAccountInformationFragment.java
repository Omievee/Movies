package com.moviepass.fragments;

import android.Manifest;
import android.app.Fragment;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.moviepass.Constants;
import com.moviepass.R;
import com.moviepass.UserPreferences;
import com.moviepass.network.RestClient;
import com.moviepass.responses.UserInfoResponse;

import java.util.Arrays;
import java.util.List;

import io.card.payment.CardIOActivity;
import io.card.payment.CreditCard;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

/*
 * Created by anubis on 9/2/17.
 */

public class ProfileAccountInformationFragment extends Fragment {

    View rootView, progress;
    ImageView downArraow, backArrow, downArrow2;
    Switch billingSwitch;
    RelativeLayout userOldBilling;
    LinearLayout shippingDetails, bilingDetails, billing2, newBillingData, newBillingData2;
    TextView userName, userEmail, userAddress, userAddress2, userCity, userState, userZip, userBillingDate, userPlan, userPlanPrice, userPlanCancel, userBIllingCard, yesNo,
            userBillingChange, userNewAddress, userNewCity, userNewState, userNewZip, userEditShipping;
    EditText userNewAddress2, userNewBillingCC, userNewBillingCVV, userNewBillingExp;
    ImageButton userScanCard;

    private static String CAMERA_PERMISSIONS[] = new String[]{
            Manifest.permission.CAMERA
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.profile_account_details, container, false);

        progress = rootView.findViewById(R.id.progress);
        downArraow = rootView.findViewById(R.id.DOWN);
        shippingDetails = rootView.findViewById(R.id.ShippingDetails);
        backArrow = rootView.findViewById(R.id.accountback);
        downArrow2 = rootView.findViewById(R.id.DOWN2);
        bilingDetails = rootView.findViewById(R.id.billingdetails);
        billingSwitch = rootView.findViewById(R.id.SWITCH);
        billing2 = rootView.findViewById(R.id.Billing2);

        userName = rootView.findViewById(R.id.USER_NAME);
        userEmail = rootView.findViewById(R.id.USER_EMAIL);
        userAddress = rootView.findViewById(R.id.Address1);
        userCity = rootView.findViewById(R.id.city);
        userState = rootView.findViewById(R.id.State);
        userZip = rootView.findViewById(R.id.zip);
        userAddress2 = rootView.findViewById(R.id.Address2);

        userBillingDate = rootView.findViewById(R.id.BillingDate);
        userPlan = rootView.findViewById(R.id.Plan);
        userPlanPrice = rootView.findViewById(R.id.Plan_Data);
        userPlanCancel = rootView.findViewById(R.id.PLan_cancel);
        userBIllingCard = rootView.findViewById(R.id.USER_BILLING);
        userBillingChange = rootView.findViewById(R.id.Billing_Change);

        userNewAddress = rootView.findViewById(R.id.AddressShipping2);
        userNewAddress2 = rootView.findViewById(R.id.AddressShipping_EDIT);
        userNewCity = rootView.findViewById(R.id.city2);
        userNewState = rootView.findViewById(R.id.state2);
        userNewZip = rootView.findViewById(R.id.zip2);

        userEditShipping = rootView.findViewById(R.id.EDITSHIPPING);
        yesNo = rootView.findViewById(R.id.YesNo);

        userNewBillingCC = rootView.findViewById(R.id.profile_ccnum);
        userNewBillingCVV = rootView.findViewById(R.id.profile_cvv);
        userNewBillingExp = rootView.findViewById(R.id.profile_expiration);
        userScanCard = rootView.findViewById(R.id.profile_scanicon);

        userOldBilling = rootView.findViewById(R.id.old_billing);
        newBillingData = rootView.findViewById(R.id.profile_newBilling);
        newBillingData2 = rootView.findViewById(R.id.profile_newBilling2);
        return rootView;


    }

    @Override
    public void onResume() {
        super.onResume();

//
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progress.setVisibility(View.VISIBLE);
        loadUserInfo();

        downArraow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (shippingDetails.getVisibility() == View.GONE) {
                    shippingDetails.setVisibility(View.VISIBLE);
                } else {
                    shippingDetails.setVisibility(View.GONE);
                }
            }
        });

        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        downArrow2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bilingDetails.getVisibility() == View.GONE) {
                    bilingDetails.setVisibility(View.VISIBLE);
                } else {
                    bilingDetails.setVisibility(View.GONE);
                }
            }
        });

        billingSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (billing2.getVisibility() == View.GONE) {
                    yesNo.setText("NO");
                    billing2.setVisibility(View.VISIBLE);
                } else {
                    yesNo.setText("YES");
                    billing2.setVisibility(View.GONE);
                }
            }
        });

        userPlanCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });


        userNewAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callPlaceAutocompleteActivityIntent();
            }
        });

        userEditShipping.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                        .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS)
                        .build();

                try {
                    Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY).setFilter(typeFilter).build(getActivity());
                    startActivityForResult(intent, Constants.PLACE_AUTOCOMPLETE_REQUEST_CODE2);
                } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
                    // TODO: Handle the error.
                }
            }
        });

        userBillingChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                userOldBilling.setVisibility(View.GONE);
                newBillingData.setVisibility(View.VISIBLE);
                newBillingData2.setVisibility(View.VISIBLE);


                userScanCard.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            creditCardClick();
                        }
                    }
                });

            }
        });


        userNewBillingExp.addTextChangedListener(new TextWatcher() {
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
    }

    private void loadUserInfo() {
        int userId = UserPreferences.getUserId();
        RestClient.getAuthenticated().getUserData(userId).enqueue(new Callback<UserInfoResponse>() {
            @Override
            public void onResponse(Call<UserInfoResponse> call, Response<UserInfoResponse> response) {
                UserInfoResponse userInfoResponse = response.body();
                if (userInfoResponse != null) {

                    // Plan Info
                    String firstName = userInfoResponse.getUser().getFirstName();
                    String lastName = userInfoResponse.getUser().getLastName();
                    String email = userInfoResponse.getEmail();

                    userName.setText(firstName + " " + lastName);
                    userEmail.setText(email);
                    userAddress.setText(userInfoResponse.getShippingAddressLine1());

                    String address = userInfoResponse.getShippingAddressLine2();
                    List<String> addressList = Arrays.asList(address.split(",", -1));

                    for (int i = 0; i < addressList.size(); i++) {
                        Log.d(Constants.TAG, "onResponse: " + addressList.get(0));
                        userCity.setText(addressList.get(0));
                        userState.setText(addressList.get(1));
                        userZip.setText(addressList.get(2));


                    }
                    userBIllingCard.setText(userInfoResponse.getBillingCard());
                    userBillingDate.setText(userInfoResponse.getNextBillingDate());

                    Log.d(Constants.TAG, "onResponse: " + userInfoResponse.getPlan());

                    String plan = userInfoResponse.getPlan();
                    List<String> planList = Arrays.asList(plan.split(" ", -1));
                    for (int i = 0; i < planList.size(); i++) {
                        userPlan.setText(planList.get(0));
                        userPlanPrice.setText(planList.get(1));

                    }

                    progress.setVisibility(View.GONE);
                    Log.d(Constants.TAG, "onResponse: " + userInfoResponse.getShippingAddressLine1());
                    Log.d(Constants.TAG, "onResponse: " + userInfoResponse.getShippingAddressLine2());

                }
            }

            @Override
            public void onFailure(Call<UserInfoResponse> call, Throwable t) {

            }
        });
    }


    private void callPlaceAutocompleteActivityIntent() {
        AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS)
                .build();

        try {
            Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY).setFilter(typeFilter).build(getActivity());

            startActivityForResult(intent, Constants.PLACE_AUTOCOMPLETE_REQUEST_CODE);
        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
            // TODO: Handle the error.
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(getActivity(), data);

                String address = place.getAddress().toString();
                List<String> localList = Arrays.asList(address.split(",", -1));

                for (int i = 0; i < localList.size(); i++) {

                    userNewAddress.setText(localList.get(0));
                    userNewCity.setText(localList.get(1));
                    String State = localList.get(2).substring(0, 3);
                    String zip = localList.get(2).substring(4, 9);
                    userNewState.setText(State);
                    userNewZip.setText(zip);
                }


                Log.i(Constants.TAG, "Place:" + place.toString());
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(getActivity(), data);
                Log.i(Constants.TAG, status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {

            }
        } else if (requestCode == Constants.PLACE_AUTOCOMPLETE_REQUEST_CODE2) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(getActivity(), data);
                String address = place.getAddress().toString();
                List<String> localList = Arrays.asList(address.split(",", -1));

                for (int i = 0; i < localList.size(); i++) {
                    userAddress.setText(localList.get(0));
                    userCity.setText(localList.get(1));
                    String State = localList.get(2).substring(0, 3);
                    String zip = localList.get(2).substring(4, 9);
                    userState.setText(State);
                    userZip.setText(zip);
                }
            }
        } else if (requestCode == Constants.CARD_SCAN_REQUEST_CODE) {
            if (data != null && data.hasExtra(CardIOActivity.EXTRA_SCAN_RESULT)) {
                final CreditCard scanResult = data.getParcelableExtra(CardIOActivity.EXTRA_SCAN_RESULT);
                userNewBillingCC.setText(scanResult.getRedactedCardNumber());

                if (scanResult.isExpiryValid()) {
                    String month = String.valueOf(scanResult.expiryMonth);
                    String year = String.valueOf(scanResult.expiryYear);
                    if (year.length() > 4) {
                        year = year.substring(4);
                    }
                    userNewBillingExp.setText(month + " / " + year);
                    userNewBillingCVV.setText(scanResult.cvv);
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void creditCardClick() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(CAMERA_PERMISSIONS, Constants.REQUEST_CAMERA_CODE);
            scan_card();
        } else {
            scan_card();
        }
    }


    public void scan_card() {
        Intent scanIntent = new Intent(getActivity(), CardIOActivity.class);

        // customize these values to suit your needs.
        scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_EXPIRY, true); // default: false
        scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_CVV, true); // default: false
        scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_POSTAL_CODE, false); // default: false
        startActivityForResult(scanIntent, Constants.CARD_SCAN_REQUEST_CODE);
    }
}

