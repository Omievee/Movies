package com.moviepass.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Process;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.moviepass.R;
import com.moviepass.activities.SignUpActivity;
import com.moviepass.adapters.PlacesAutoCompleteAdapter;
import com.moviepass.model.ProspectUser;
import com.moviepass.network.RestCallback;
import com.moviepass.network.RestClient;
import com.moviepass.network.RestError;
import com.moviepass.requests.PersonalInfoRequest;
import com.moviepass.responses.PersonalInfoResponse;
import com.moviepass.responses.RegistrationPlanResponse;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by anubis on 7/11/17.
 */

public class SignUpStepOneFragment extends Fragment {

    private static String tag = SignUpStepOneFragment.class.getSimpleName();

    ArrayAdapter<CharSequence> statesAdapter;
    public PlacesAutoCompleteAdapter placesAdapter;
    HandlerThread handlerThread;
    Handler handler;


    public static final String TAG = "Found0";
    RelativeLayout signup1CoordMain;
    EditText signup1FirstName;
    EditText signup1LastName;
    public AutoCompleteTextView signUpAutoCompletePlace;
    EditText signup1Address2;
    EditText signup1City;
    Spinner signup1State;
    EditText signup1Zip;
    TextView signup1NextButton;
    View progress;
    ImageView indicator0, indicator1, indicator2;
    String states[] = new String[]{
            "AK", "AL", "AR", "AZ", "CA", "CO", "CT", "DC", "DE", "FL",
            "GA", "HI", "IA", "ID", "IL", "IN", "KS", "KY", "LA", "MA",
            "MD", "ME", "MI", "MN", "MO", "MS", "MT", "NC", "ND", "NE",
            "NH", "NJ", "NM", "NV", "NY", "OH", "OK", "OR", "PA", "RI",
            "SC", "SD", "TN", "TX", "UT", "VA", "VT", "WA", "WI", "WV", "WY", "State"
    };

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
        signUpAutoCompletePlace = rootView.findViewById(R.id.Autocomplete_TextView);
        signup1Address2 = rootView.findViewById(R.id.et_address_two);
        signup1City = rootView.findViewById(R.id.et_city);
        signup1State = rootView.findViewById(R.id.state);
        signup1Zip = rootView.findViewById(R.id.et_zip);
        signup1NextButton = rootView.findViewById(R.id.button_next);
        progress = getActivity().findViewById(R.id.progress);

        placesAdapter = new PlacesAutoCompleteAdapter(getActivity(), R.layout.list_item_autocomplete_places);
        signUpAutoCompletePlace.setAdapter(placesAdapter);


        if (handlerThread == null) {
            // Initialize and start the HandlerThread
            // which is basically a Thread with a Looper
            // attached (hence a MessageQueue)
            handlerThread = new HandlerThread("this", Process.THREAD_PRIORITY_BACKGROUND);
            handlerThread.start();

            // Initialize the Handler
            handler = new Handler(handlerThread.getLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    if (msg.what == 1) {
                        ArrayList<String> results = placesAdapter.placesResults;
                        Log.d(TAG, "handleMessage: " + placesAdapter.placesResults.size());
                        if (results != null && results.size() > 0) {
                            placesAdapter.notifyDataSetChanged();
                        } else {
                            placesAdapter.notifyDataSetInvalidated();
                        }
                    }
                }
            };
        }


        signUpAutoCompletePlace.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                final String value = s.toString();

                handler.removeCallbacksAndMessages(null);

                // Now add a new one
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        placesAdapter.placesResults = placesAdapter.api.autocomplete(value);
                        Log.d(TAG, "run: " + placesAdapter.api.autocomplete(value).toString());

                        handler.sendEmptyMessage(1);
                    }
                }, 300);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        getActivity().

                getWindow().

                setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        statesAdapter = ArrayAdapter.createFromResource(

                getActivity(), R.array.states_abbrev, R.layout.item_white_spinner);
        statesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        signup1State.setAdapter(statesAdapter);

        if (!isViewShown)

        {
            setButtonActions();
        }

        return rootView;
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
                        } else {
                            if (!isFirstNameValid()) {
                                makeSnackbar(R.string.fragment_sign_up_step_one_valid_first_name);
                            } else if (!isLastNameValid()) {
                                makeSnackbar(R.string.fragment_sign_up_step_one_valid_last_name);
                            } else if (!isAddressValid()) {
                                makeSnackbar(R.string.fragment_sign_up_step_one_valid_address);
                            } else if (!isAddress2Valid()) {
                                makeSnackbar(R.string.fragment_sign_up_step_one_valid_address_two);
                            } else if (!isCityValid()) {
                                makeSnackbar(R.string.fragment_sign_up_step_one_valid_city);
                            } else if (!isStateValid()) {
                                makeSnackbar(R.string.fragment_sign_up_step_one_valid_state);
                            } else {
                                makeSnackbar(R.string.fragment_sign_up_step_one_valid_zip);
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
        if (isFirstNameValid() && isLastNameValid() && isAddressValid() && isAddress2Valid() && isCityValid() && isStateValid() && isZipValid()) {
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
        if (signUpAutoCompletePlace.length() > 2 && signUpAutoCompletePlace.length() <= 26) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isAddress2Valid() {
        if ((signup1Address2.length() > 0 && signup1Address2.length() <= 26)
                || signup1Address2.getText().toString().equals("")) {
            return true;
        } else {
            Log.d("mAddress2", signup1Address2.getText().toString());
            return false;
        }
    }

    public boolean isCityValid() {
        if (signup1City.length() > 2 && signup1City.length() <= 26 && !signup1City.getText().toString().matches(".*\\d+.*")) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isStateValid() {
        if (!signup1State.getSelectedItem().toString().equals("State")) {
            Log.d("mStateValue: ", signup1State.getSelectedItem().toString());
            return true;
        } else {
            return false;
        }
    }

    public boolean isZipValid() {
        if (signup1Zip.length() == 5) {
            return true;
        } else {
            return false;
        }
    }

    public void makeSnackbar(int message) {
        final Snackbar snackbar = Snackbar.make(signup1CoordMain, message, Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction("OK", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                snackbar.dismiss();
            }
        });
        snackbar.show();
    }

    public void processSignUpInfo() {
        ((SignUpActivity) getActivity()).setFirstName(signup1FirstName.getText().toString());
        ((SignUpActivity) getActivity()).setLastName(signup1LastName.getText().toString());
        ((SignUpActivity) getActivity()).setAddress(signUpAutoCompletePlace.getText().toString());
        ((SignUpActivity) getActivity()).setAddress2(signup1Address2.getText().toString());
        ((SignUpActivity) getActivity()).setCity(signup1City.getText().toString());
        ((SignUpActivity) getActivity()).setState(signup1State.getSelectedItem().toString());
        ((SignUpActivity) getActivity()).setAddressZip(signup1Zip.getText().toString());

        String email = ((SignUpActivity) getActivity()).getEmail();
        String password = ((SignUpActivity) getActivity()).getPassword();

        ProspectUser.firstName = signup1FirstName.getText().toString();
        ProspectUser.lastName = signup1LastName.getText().toString();
        ProspectUser.address = signUpAutoCompletePlace.getText().toString();
        ProspectUser.address2 = signup1Address2.getText().toString();
        ProspectUser.city = signup1City.getText().toString();
        ProspectUser.state = signup1State.getSelectedItem().toString();
        ProspectUser.zip = signup1Zip.getText().toString();

        PersonalInfoRequest request = new PersonalInfoRequest(ProspectUser.email, ProspectUser.password,
                ProspectUser.password, ProspectUser.firstName, ProspectUser.lastName, ProspectUser.address,
                ProspectUser.address2, ProspectUser.city, ProspectUser.state, ProspectUser.zip);

        RestClient.getUnauthenticated().registerPersonalInfo(request).enqueue(new Callback<PersonalInfoResponse>() {
            @Override
            public void onResponse(Call<PersonalInfoResponse> call, Response<PersonalInfoResponse> response) {
                RestClient.getUnauthenticated().getPlans(ProspectUser.zip).enqueue(new RestCallback<RegistrationPlanResponse>() {
                    @Override
                    public void onResponse(Call<RegistrationPlanResponse> call, Response<RegistrationPlanResponse> response) {
                        progress.setVisibility(View.GONE);
                        RegistrationPlanResponse registrationPlanResponse = response.body();

                        if (registrationPlanResponse != null) {
                            String price = (registrationPlanResponse.getPrice());

                            ((SignUpActivity) getActivity()).setPage();
                            Log.d("SUSOFprice", price);
                        } else if (response.errorBody() != null) {
                            Toast.makeText(getActivity(), response.message(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void failure(RestError restError) {

                    }
                });
            }

            @Override
            public void onFailure(Call<PersonalInfoResponse> call, Throwable t) {
                Toast.makeText(getActivity(), t.getMessage().toString(), Toast.LENGTH_SHORT).show();
            }
        });
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
                    } else if (!isAddressValid()) {
                        makeSnackbar(R.string.fragment_sign_up_step_one_valid_address);
                    } else if (!isAddress2Valid()) {
                        makeSnackbar(R.string.fragment_sign_up_step_one_valid_address_two);
                    } else if (!isCityValid()) {
                        makeSnackbar(R.string.fragment_sign_up_step_one_valid_city);
                    } else if (!isStateValid()) {
                        makeSnackbar(R.string.fragment_sign_up_step_one_valid_state);
                    } else {
                        makeSnackbar(R.string.fragment_sign_up_step_one_valid_zip);
                    }
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
            handlerThread.quit();
        }
    }
}

