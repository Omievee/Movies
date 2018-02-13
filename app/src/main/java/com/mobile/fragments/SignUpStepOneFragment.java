package com.mobile.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.mobile.Constants;
import com.mobile.activities.SignUpActivity;
import com.mobile.model.ProspectUser;
import com.moviepass.R;

import java.util.Arrays;
import java.util.List;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

/**
 * Created by anubis on 7/11/17.
 */

public class SignUpStepOneFragment extends Fragment {

    public static final String TAG = "Found0";
    RelativeLayout signup1CoordMain;
    public EditText signup1FirstName, signup1LastName;
    public TextView signUpAddress1, signup1Address2, signup1City, signup1Zip, signup1State;
    TextView signup1NextButton;
    View progress;


    int pos;
    private boolean isViewShown = false;

    public SignUpStepOneFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fr_signup_stepone, container, false);

        signup1CoordMain = rootView.findViewById(R.id.relative_layout);
        signup1FirstName = rootView.findViewById(R.id.et_first_name);
        signup1LastName = rootView.findViewById(R.id.et_last_name);
        signUpAddress1 = rootView.findViewById(R.id.first_Addreess);
        signup1Address2 = rootView.findViewById(R.id.et_address_two);
        signup1City = rootView.findViewById(R.id.et_city);
        signup1State = rootView.findViewById(R.id.state);
        signup1Zip = rootView.findViewById(R.id.et_zip);
        signup1NextButton = rootView.findViewById(R.id.button_next);
        progress = getActivity().findViewById(R.id.progress);

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);


        if (!isViewShown) {
            setButtonActions();
        }


        signUpAddress1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callPlaceAutocompleteActivityIntent();
            }
        });


        return rootView;
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

                String local = place.getAddress().toString();
                List<String> localList = Arrays.asList(local.split(",", -1));

                for (int i = 0; i < localList.size(); i++) {
                    signUpAddress1.setText(localList.get(0));
                    Log.d(TAG, "onActivityResult: " + localList.get(i));
                    signup1City.setText(localList.get(1).trim());
                    String State = localList.get(2).substring(0, 3).trim();
                    String zip = localList.get(2).substring(4, 9).trim();
                    signup1State.setText(State);
                    signup1Zip.setText(zip);

                }


                Log.i(Constants.TAG, "Place:" + place.toString());
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(getActivity(), data);
                Log.i(Constants.TAG, status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {

            }
        }
    }

    public static SignUpStepOneFragment newInstance(String text) {
        SignUpStepOneFragment f = new SignUpStepOneFragment();
        Bundle b = new Bundle();
        b.putString("msg", text);

        f.setArguments(b);

        return f;
    }

    // This is for actions only available when SignUpTwoFrag is visible
    @Override
    public void setMenuVisibility(final boolean visible) {
        super.setMenuVisibility(visible);
        if (getView() != null) {
            if (visible) {
                signup1NextButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (canContinue()) {
//                            processSignUpInfo();
                            ((SignUpActivity) getActivity()).setPage();

                        } else {
                            if (!isFirstNameValid()) {
                                makeSnackbar(R.string.fragment_sign_up_step_one_valid_first_name);
                            } else if (!isLastNameValid()) {
                                makeSnackbar(R.string.fragment_sign_up_step_one_valid_last_name);
                            }
                        }
                    }
                });
            } else {
                isViewShown = false;
            }
        }
    }

    public boolean canContinue() {
        if (isFirstNameValid() && isLastNameValid() && isAddressValid()) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isFirstNameValid() {
        if (signup1FirstName.length() > 1 && signup1FirstName.length() <= 26 && !signup1FirstName.getText().toString().matches(".*\\d+.*")) {
            Log.d(TAG, "true: ");

            return true;
        } else {
            Log.d(TAG, "false: ");

            pos = 0;
            return false;
        }
    }

    public boolean isLastNameValid() {
        if (signup1LastName.length() > 1 && signup1LastName.length() <= 26 && !signup1LastName.getText().toString().matches(".*\\d+.*")) {
            return true;
        } else {
            pos = 0;
            return false;
        }
    }

    public boolean isAddressValid() {
        if (signUpAddress1.getText().toString().equals("")) {
            Toast.makeText(getActivity(), "Please enter a valid address", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }
    }

    public void makeSnackbar(int message) {
        final Snackbar snackbar = Snackbar.make(signup1CoordMain, message, Snackbar.LENGTH_SHORT);
        snackbar.show();
    }

    public void processSignUpInfo() {
        ((SignUpActivity) getActivity()).setFirstName(signup1FirstName.getText().toString());
        ((SignUpActivity) getActivity()).setLastName(signup1LastName.getText().toString());
        ((SignUpActivity) getActivity()).setAddress(signUpAddress1.getText().toString());
        ((SignUpActivity) getActivity()).setAddress2(signup1Address2.getText().toString());
        ((SignUpActivity) getActivity()).setCity(signup1City.getText().toString());
        ((SignUpActivity) getActivity()).setState(signup1State.getText().toString());
        ((SignUpActivity) getActivity()).setAddressZip(signup1Zip.getText().toString());
        ((SignUpActivity) getActivity()).setAddressZip(signup1Zip.getText().toString());

        String email = ((SignUpActivity) getActivity()).getEmail();
        String password = ((SignUpActivity) getActivity()).getPassword();

        ProspectUser.firstName = signup1FirstName.getText().toString();
        ProspectUser.lastName = signup1LastName.getText().toString();
        ProspectUser.address = signUpAddress1.getText().toString();
        ProspectUser.address2 = signup1Address2.getText().toString();
        ProspectUser.city = signup1City.getText().toString();
        ProspectUser.state = signup1State.getText().toString();
        ProspectUser.zip = signup1Zip.getText().toString();

        ((SignUpActivity) getActivity()).setPage();

        Log.d(TAG, "processSignUpInfo: " + ProspectUser.gender + " " + ProspectUser.dateOfBirth);


//        PersonalInfoRequest request = new PersonalInfoRequest(ProspectUser.email, ProspectUser.password,
//                ProspectUser.password, ProspectUser.firstName, ProspectUser.lastName, ProspectUser.address,
//                ProspectUser.address2, ProspectUser.city, ProspectUser.state, ProspectUser.zip);
//
//        RestClient.getUnauthenticated().registerPersonalInfo(request).enqueue(new Callback<PersonalInfoResponse>() {
//            @Override
//            public void onResponse(Call<PersonalInfoResponse> call, Response<PersonalInfoResponse> response) {
//                RestClient.getUnauthenticated().getPlans(ProspectUser.zip).enqueue(new RestCallback<RegistrationPlanResponse>() {
//                    @Override
//                    public void onResponse(Call<RegistrationPlanResponse> call, Response<RegistrationPlanResponse> response) {
//                        progress.setVisibility(View.GONE);
//                        RegistrationPlanResponse registrationPlanResponse = response.body();
//
//                        if (registrationPlanResponse != null) {
//                            String price = (registrationPlanResponse.getPrice());
//
//                            ((SignUpActivity) getActivity()).setPage();
//                            Log.d("SUSOFprice", price);
//                        } else if (response.errorBody() != null) {
//                            Toast.makeText(getActivity(), response.message(), Toast.LENGTH_SHORT).show();
//                        }
//                    }
//
//                    @Override
//                    public void failure(RestError restError) {
//                        Toast.makeText(getActivity(), "System Error; Please Try again", Toast.LENGTH_SHORT).show();
//                    }
//                });
//            }
//
//            @Override
//            public void onFailure(Call<PersonalInfoResponse> call, Throwable t) {
//                Toast.makeText(getActivity(), t.getMessage().toString(), Toast.LENGTH_SHORT).show();
//            }
//        });
    }

    private void setButtonActions() {
        signup1NextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (canContinue()) {
                    processSignUpInfo();
                } else {
                    if (!isLastNameValid()) {
                        makeSnackbar(R.string.fragment_sign_up_step_one_valid_first_name);
                    } else if (!isLastNameValid()) {
                        makeSnackbar(R.string.fragment_sign_up_step_one_valid_last_name);
                    } else if (signUpAddress1.getText().toString().equals("")) {
                        Toast.makeText(getActivity(), "Please enter a valid address", Toast.LENGTH_SHORT).show();

                    }
                }
            }
        });
    }


}

