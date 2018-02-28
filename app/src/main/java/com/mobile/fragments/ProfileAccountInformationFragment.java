package com.mobile.fragments;

import android.Manifest;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
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
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.mobile.Constants;
import com.mobile.UserPreferences;
import com.mobile.network.RestClient;
import com.mobile.requests.AddressChangeRequest;
import com.mobile.requests.CreditCardChangeRequest;
import com.mobile.responses.UserInfoResponse;
import com.moviepass.R;

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

    ProfileCancellationFragment cancelSubscription;
    UserInfoResponse userInfoResponse;
    String addressSection, billingSection, creditCardSection;
    View rootView, progress;
    ImageView downArraow, backArrow, downArrow2;
    Switch billingSwitch;
    RelativeLayout userOldBilling, shippingClick, billingClick;
    LinearLayout shippingDetails, bilingDetails, billing2, newBillingData, newBillingData2;
    TextView userName, userEmail, userAddress, userAddress2, userCity, userState, userZip, userBillingDate, userPlan, userPlanPrice, userPlanCancel, userBIllingCard, yesNo,
            userBillingChange, userNewAddress, userNewCity, userNewState, userNewZip, userEditShipping, userMPCardNum, userMPExpirNum;

    Button userSave;
    EditText userNewAddress2, userNewBillingCC, userNewBillingCVV, userNewBillingExp;
    ImageButton userScanCard;
    String MONTH, YEAR;
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
        shippingDetails = rootView.findViewById(R.id.ShippingDetails);
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

        shippingClick = rootView.findViewById(R.id.MIDDLE);
        billingClick = rootView.findViewById(R.id.END);

        userNewBillingCC = rootView.findViewById(R.id.profile_ccnum);
        userNewBillingCVV = rootView.findViewById(R.id.profile_cvv);
        userNewBillingExp = rootView.findViewById(R.id.profile_expiration);
        userScanCard = rootView.findViewById(R.id.profile_scanicon);

        userOldBilling = rootView.findViewById(R.id.old_billing);
        newBillingData = rootView.findViewById(R.id.profile_newBilling);
        newBillingData2 = rootView.findViewById(R.id.profile_newBilling2);

        userSave = rootView.findViewById(R.id.saveChanges);
        cancelSubscription = new ProfileCancellationFragment();

        userMPCardNum = rootView.findViewById(R.id.MPCardNum);

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


        /* All click listeners => */

//
//        backArrow.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                getActivity().onBackPressed();
//            }
//        });

        shippingClick.setOnClickListener(v -> {
            if (shippingDetails.getVisibility() == View.GONE) {
                expand(shippingDetails);
            } else {
                collapse(shippingDetails);

            }
        });

        billingSwitch.setOnClickListener(v -> {
            if (billing2.getVisibility() == View.GONE) {
                yesNo.setText("NO");
                expand(billing2);
            } else {
                yesNo.setText("YES");
                collapse(billing2);
            }
        });

        userPlanCancel.setOnClickListener(v -> {

            FragmentManager manager = getActivity().getFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.setCustomAnimations(R.animator.enter_from_right, R.animator.exit_to_left, R.animator.enter_from_left, R.animator.exit_to_right);
            transaction.replace(R.id.profile_container, cancelSubscription);
            transaction.addToBackStack(null);
            transaction.commit();


        });


        userNewAddress.setOnClickListener(v -> callPlaceAutocompleteActivityIntent());

        userEditShipping.setOnClickListener(v -> {
            AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                    .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS)
                    .build();

            try {
                Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY).setFilter(typeFilter).build(getActivity());
                startActivityForResult(intent, Constants.PLACE_AUTOCOMPLETE_REQUEST_CODE2);
            } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
                // TODO: Handle the error.
            }
        });

        userBillingChange.setOnClickListener(v -> {

            userOldBilling.setVisibility(View.GONE);
            newBillingData.setVisibility(View.VISIBLE);
            newBillingData2.setVisibility(View.VISIBLE);

            userScanCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    creditCardClick();

                }
            });

        });

        billingClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bilingDetails.getVisibility() == View.GONE) {
                    expand(bilingDetails);
                    userBIllingCard.hasFocus();
                } else {
                    collapse(bilingDetails);
                }
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
                userInfoResponse = response.body();
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
                        userCity.setText(addressList.get(0));
                        userState.setText(addressList.get(1));
                        userZip.setText(addressList.get(2));


                    }
                    userBIllingCard.setText(userInfoResponse.getBillingCard());
                    if (userInfoResponse.getNextBillingDate().equals("")) {
                        userBillingDate.setText("Unknown");
                    } else {
                        userBillingDate.setText(userInfoResponse.getNextBillingDate());

                    }


                    userMPCardNum.setText(userInfoResponse.getMoviePassCardNumber());

                    String plan = userInfoResponse.getPlan();
                    List<String> planList = Arrays.asList(plan.split(" ", -1));
                    for (int i = 0; i < planList.size(); i++) {
                        userPlan.setText(planList.get(0));
                        userPlanPrice.setText(planList.get(1));

                    }


                    progress.setVisibility(View.GONE);

                }
            }

            @Override
            public void onFailure(Call<UserInfoResponse> call, Throwable t) {
                Toast.makeText(getActivity(), "Server Error; Please try again.", Toast.LENGTH_SHORT).show();
                Log.d(Constants.TAG, "onFailure: " + t.getMessage());
            }
        });
    }


    private void callPlaceAutocompleteActivityIntent() {
        AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS)
                .setCountry("US")
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
                    if (localList.get(2).trim().length() < 8) {
                        Toast.makeText(getActivity(), "Invalid", Toast.LENGTH_SHORT).show();
                    } else {
                        userNewAddress.setText(localList.get(0));
                        userNewCity.setText(localList.get(1));
                        String State = localList.get(2).substring(0, 3);
                        String zip = localList.get(2).substring(4, 9);
                        userNewState.setText(State);
                        userNewZip.setText(zip);
                    }
                }
                saveChanges();


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
                    if (localList.get(2).trim().length() < 8) {
                        Toast.makeText(getActivity(), "Invalid", Toast.LENGTH_SHORT).show();
                    } else {
                        userAddress.setText(localList.get(0));
                        userCity.setText(localList.get(1).trim());
                        String State = localList.get(2).substring(0, 3).trim();
                        String zip = localList.get(2).substring(4, 9);
                        userState.setText(State);
                        userZip.setText(zip);
                    }
                }
                saveChanges();

            }
        } else if (requestCode == Constants.CARD_SCAN_REQUEST_CODE)

        {
            if (data != null && data.hasExtra(CardIOActivity.EXTRA_SCAN_RESULT)) {
                final CreditCard scanResult = data.getParcelableExtra(CardIOActivity.EXTRA_SCAN_RESULT);

                String cardNumber = scanResult.getFormattedCardNumber();
                cardNumber = cardNumber.replace(" ", "");
                userNewBillingCC.setText(cardNumber);


                if (scanResult.isExpiryValid()) {
                    String month = String.valueOf(scanResult.expiryMonth);
                    String year = String.valueOf(scanResult.expiryYear);
                    if (month.length() < 2) {
                        MONTH = "0" + month;
                    } else {
                        MONTH = month;
                    }
                    YEAR = year.substring(2, 4);
                    userNewBillingExp.setText(MONTH + "/" + YEAR);
                    userNewBillingCVV.setText(scanResult.cvv);
                }
                saveChanges();

            }
        }


    }

    public void creditCardClick() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(CAMERA_PERMISSIONS, Constants.REQUEST_CAMERA_CODE);
            }
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

    public void saveChanges() {
        userSave.setTextColor(getResources().getColor(R.color.new_red));
        userSave.setOnClickListener(v -> {


            progress.setVisibility(View.VISIBLE);
//            updateShippingAddress();
                updateCCData();

        });
    }

    //TODO: save shipping & billing info when changed..
    public void updateShippingAddress() {
        int userId = UserPreferences.getUserId();
        if (userAddress.getText().toString() != userInfoResponse.getShippingAddressLine1()) {
            String newAddress = userAddress.getText().toString();
            String newAddress2 = userAddress2.getText().toString();
            String newCity = userCity.getText().toString();
            String newZip = userZip.getText().toString();
            String newState = userState.getText().toString();

            String type = "shippingAddress";

            AddressChangeRequest request = new AddressChangeRequest(newAddress, newAddress2, newCity, newState, newZip, type);
            RestClient.getAuthenticated().updateAddress(userId, request).enqueue(new Callback<Object>() {
                @Override
                public void onResponse(Call<Object> call, Response<Object> response) {
                    progress.setVisibility(View.GONE);

                    loadUserInfo();
                    Toast.makeText(getActivity(), "Shipping address has been updated", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(Call<Object> call, Throwable t) {
                    progress.setVisibility(View.GONE);

                    Toast.makeText(getActivity(), "Server Response Error", Toast.LENGTH_SHORT).show();
                }
            });
        }

    }

    public void updateCCData() {
        String newCC = userNewBillingCC.getText().toString();
        String ccExMonth = userNewBillingExp.getText().toString().substring(0, 2);
        String ccExYr = "20" + userNewBillingExp.getText().toString().substring(3, 5);
        String newExp = ccExMonth + "/" + ccExYr;
        String newCVV = userNewBillingCVV.getText().toString();

        updateCreditCard(newCC, newExp, newCVV);
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


                    userNewBillingCC.clearComposingText();
                    userNewBillingCVV.clearComposingText();
                    userNewBillingExp.clearComposingText();
                    userOldBilling.setVisibility(View.VISIBLE);
                    newBillingData.setVisibility(View.GONE);
                    newBillingData2.setVisibility(View.GONE);

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
}


