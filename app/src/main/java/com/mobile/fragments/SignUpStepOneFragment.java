package com.mobile.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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
    public EditText signUpAddress1, signup1Address2, signup1City, signup1Zip, signup1State;
    public TextInputLayout firstNameTextInputLayout, lastNameTextInputLayout, address1TextInputLayout, cityTextInputLayout, stateTextInputLayout, zipTextInputLayout;
    TextView signup1NextButton;
    public boolean firstClick = true;
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

        firstNameTextInputLayout = rootView.findViewById(R.id.fistNameTextInputLayout);
        lastNameTextInputLayout = rootView.findViewById(R.id.lastNameTextInputLayout);
        address1TextInputLayout = rootView.findViewById(R.id.address1TextInputLayout);
        cityTextInputLayout = rootView.findViewById(R.id.cityTextInputLayout);
        stateTextInputLayout = rootView.findViewById(R.id.stateTextInputLayout);
        zipTextInputLayout = rootView.findViewById(R.id.zipTextInputLayout);

        signup1FirstName.clearFocus();
        signup1LastName.clearFocus();
        signUpAddress1.clearFocus();
        signup1Address2.clearFocus();
        signup1City.clearFocus();
        signup1State.clearFocus();
        signup1Zip.clearFocus();

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);


        if (!isViewShown) {
            setButtonActions();
        }

        signUpAddress1.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(firstClick){
                    callPlaceAutocompleteActivityIntent();
                    firstClick=false;
                    return true;
                }
                return false;
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
            clearFocusOnAllEditTexts();
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
                            processSignUpInfo();
                            ((SignUpActivity) getActivity()).setPage();
                            ((SignUpActivity) getActivity()).confirmSecondStep();
                        }
                    }
                });
            } else {
                isViewShown = false;
            }
        }
    }

    public boolean canContinue() {
        address1TextInputLayout.setError(null);
        firstNameTextInputLayout.setError(null);
        lastNameTextInputLayout.setError(null);
        stateTextInputLayout.setError(null);
        cityTextInputLayout.setError(null);
        zipTextInputLayout.setError(null);
        clearFocusOnAllEditTexts();
        if (isFirstNameValid() && isLastNameValid() && isAddressValid()) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isFirstNameValid() {
        if (signup1FirstName.length() > 1 && signup1FirstName.length() <= 26 && !signup1FirstName.getText().toString().matches(".*\\d+.*")) {
            return true;
        } else {
            pos = 0;
            firstNameTextInputLayout.setError(getResources().getString(R.string.fragment_sign_up_step_one_valid_first_name));
            return false;
        }
    }

    public boolean isLastNameValid() {
        if (signup1LastName.length() > 1 && signup1LastName.length() <= 26 && !signup1LastName.getText().toString().matches(".*\\d+.*")) {
            return true;
        } else {
            lastNameTextInputLayout.setError(getResources().getString(R.string.fragment_sign_up_step_one_valid_last_name));
            pos = 0;
            return false;
        }
    }

    private boolean isAddressValid() {
        address1TextInputLayout.setError(null);
        cityTextInputLayout.setError(null);
        stateTextInputLayout.setError(null);
        zipTextInputLayout.setError(null);

        int i = 0;
        if (!signUpAddress1.getText().toString().trim().isEmpty() && !signup1City.getText().toString().trim().isEmpty() && !signup1Zip.getText().toString().trim().isEmpty() && !signup1State.getText().toString().trim().isEmpty()) {

            //Validating Address
            String[] address1Array = signUpAddress1.getText().toString().split("\\W+");
            if (address1Array.length >= 2 && address1Array[0].trim().matches(".*\\d+.*")) {
                i++;
            }else {
                address1TextInputLayout.setError(getResources().getString(R.string.address_invalid_address));
                signUpAddress1.clearFocus();
                Log.d("ADDRESS", "isValidAddress: ");
            }

            //Validating City
            String[] cityArray = signup1City.getText().toString().split("\\W+");
            String cityWithNotWhiteSpaces = signup1City.getText().toString().replaceAll("\\s+","");
            //If city has less than 3 words
            if (cityArray.length <= 3 && cityWithNotWhiteSpaces.matches("^[a-zA-Z]+$")) {
                i++;
            } else {
                cityTextInputLayout.setError(getResources().getString(R.string.address_invalid_city));
                signup1City.clearFocus();
            }

            //Validating State
            if (signup1State.getText().toString().trim().length() == 2 && signup1State.getText().toString().trim().matches("^[a-zA-Z]+$")) {
                i++;
            } else {
                stateTextInputLayout.setError(getResources().getString(R.string.address_invalid_state));
                signup1State.clearFocus();
            }

            //Validating Zip Code
            if (signup1Zip.getText().toString().trim().matches("^[0-9]+$") && signup1Zip.getText().toString().trim().length()>=5) {
                i++;
            } else {
                zipTextInputLayout.setError(getResources().getString(R.string.address_invalid_zip));
                signup1Zip.clearFocus();
            }


        } else {
            if (signUpAddress1.getText().toString().trim().isEmpty()) {
                address1TextInputLayout.setError(getResources().getString(R.string.fragment_profile_shipping_address_valid_address));
                signUpAddress1.clearFocus();
            }
            if (signup1State.getText().toString().trim().isEmpty()) {
                stateTextInputLayout.setError(getResources().getString(R.string.fragment_profile_shipping_address_valid_state));
                signup1State.clearFocus();
            }
            if (signup1Zip.getText().toString().trim().isEmpty()) {
                zipTextInputLayout.setError(getResources().getString(R.string.fragment_profile_shipping_address_valid_zip));
                signup1Zip.clearFocus();
            }
            if (signup1City.getText().toString().trim().isEmpty()) {
                cityTextInputLayout.setError(getResources().getString(R.string.fragment_profile_shipping_address_valid_city));
                signup1City.clearFocus();
            }
        }
        if(i==4)
            return true;
        return false;
    }

    public void makeSnackbar(int message) {
        final Snackbar snackbar = Snackbar.make(signup1CoordMain, message, Snackbar.LENGTH_SHORT);
        snackbar.show();
    }

    public void clearFocusOnAllEditTexts(){
        signUpAddress1.clearFocus();
        signup1Address2.clearFocus();
        signup1City.clearFocus();
        signup1State.clearFocus();
        signup1Zip.clearFocus();
        signup1FirstName.clearFocus();
        signup1LastName.clearFocus();
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


        Log.d(TAG, "processSignUpInfo: " + ProspectUser.firstName + " " + ProspectUser.lastName);
    }

    private void setButtonActions() {
        signup1NextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeErrors();
                clearFocusOnAllEditTexts();
                if (canContinue()) {
                    processSignUpInfo();
                } else {
                    if (!isLastNameValid()) {
                        if(signup1LastName.getText().toString().trim().isEmpty())
                            lastNameTextInputLayout.setError("Required");
                         else
                             lastNameTextInputLayout.setError("Invalid Last Name");
                        signup1LastName.clearFocus();
                    }if (!isFirstNameValid()) {
                        if(signup1FirstName.getText().toString().trim().isEmpty())
                            firstNameTextInputLayout.setError("Required");
                        else
                            firstNameTextInputLayout.setError("Invalid First Name");
                        signup1FirstName.clearFocus();
                    }if (signUpAddress1.getText().toString().toString().trim().isEmpty()) {
                        address1TextInputLayout.setError("Required");
                        signUpAddress1.clearFocus();
                    }
                    if(signup1City.getText().toString().trim().isEmpty()){
                        cityTextInputLayout.setError("Required");
                        signup1City.clearFocus();
                    }
                    if(signup1State.getText().toString().trim().isEmpty()){
                        stateTextInputLayout.setError("Required");
                        signup1State.clearFocus();
                    }
                    if(signup1Zip.getText().toString().trim().isEmpty()){
                        zipTextInputLayout.setError("Required");
                        signup1Zip.clearFocus();
                    }
                }
            }
        });
    }

    public void removeErrors(){
        lastNameTextInputLayout.setError(null);
        firstNameTextInputLayout.setError(null);
        address1TextInputLayout.setError(null);
        cityTextInputLayout.setError(null);
        stateTextInputLayout.setError(null);
        zipTextInputLayout.setError(null);
    }


}

