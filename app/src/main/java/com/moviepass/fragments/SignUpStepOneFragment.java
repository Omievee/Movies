package com.moviepass.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import com.moviepass.R;
import com.moviepass.activities.SignUpActivity;
import com.moviepass.model.Plan;
import com.moviepass.network.RestClient;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by anubis on 7/11/17.
 */

public class SignUpStepOneFragment extends Fragment {

    ArrayAdapter<CharSequence> statesAdapter;

    RelativeLayout relativeLayout;
    EditText firstName;
    EditText lastName;
    EditText address;
    EditText address2;
    EditText city;
    Spinner state;
    EditText zip;
    Button next;

    String states[] = new String[]{
            "AK", "AL", "AR", "AZ", "CA", "CO", "CT", "DC", "DE", "FL",
            "GA", "HI", "IA", "ID", "IL", "IN", "KS", "KY", "LA", "MA",
            "MD", "ME", "MI", "MN", "MO", "MS", "MT", "NC", "ND", "NE",
            "NH", "NJ", "NM", "NV", "NY", "OH", "OK", "OR", "PA", "RI",
            "SC", "SD", "TN", "TX", "UT", "VA", "VT", "WA", "WI", "WV", "WY", "State"
    };

    int pos;
    private boolean isViewShown = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_sign_up_step_one, container, false);

        relativeLayout = rootView.findViewById(R.id.relative_layout);
        firstName = rootView.findViewById(R.id.et_first_name);
        lastName = rootView.findViewById(R.id.et_last_name);
        address = rootView.findViewById(R.id.et_address);
        address2 = rootView.findViewById(R.id.et_address_two);
        city = rootView.findViewById(R.id.et_city);
        state = rootView.findViewById(R.id.state);
        zip = rootView.findViewById(R.id.et_zip);
        next = getActivity().findViewById(R.id.button_next);

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        statesAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.states_abbrev, R.layout.item_white_spinner);
        statesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        state.setAdapter(statesAdapter);

        if (!isViewShown) {
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
                next.setOnClickListener(new View.OnClickListener() {
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
        if (firstName.length() > 1 && firstName.length() <= 26 && !firstName.getText().toString().matches(".*\\d+.*")) {
            return true;
        } else {
            pos = 0;
            return false;
        }
    }

    public boolean isLastNameValid() {
        if (lastName.length() > 1 && lastName.length() <= 26 && !lastName.getText().toString().matches(".*\\d+.*")) {
            return true;
        } else {
            pos = 0;
            return false;
        }
    }

    public boolean isAddressValid() {
        if (address.length() > 2 && address.length() <= 26) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isAddress2Valid() {
        if ((address2.length() > 0 && address2.length() <= 26)
                || address2.getText().toString().equals("")) {
            return true;
        } else {
            Log.d("mAddress2", address2.getText().toString());
            return false;
        }
    }

    public boolean isCityValid() {
        if (city.length() > 2 && city.length() <= 26 && !city.getText().toString().matches(".*\\d+.*")) {
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
        if (zip.length() == 5) {
            return true;
        } else {
            return false;
        }
    }

    public void makeSnackbar(int message) {
        final Snackbar snackbar = Snackbar.make(relativeLayout, message, Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction("OK", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                snackbar.dismiss();
            }
        });
        snackbar.show();
    }

    public void processSignUpInfo() {
        /* TODO : Set prospect user */

        ((SignUpActivity) getActivity()).setFirstName(firstName.getText().toString());
        ((SignUpActivity) getActivity()).setLastName(lastName.getText().toString());
        ((SignUpActivity) getActivity()).setAddress(address.getText().toString());
        ((SignUpActivity) getActivity()).setAddress2(address2.getText().toString());
        ((SignUpActivity) getActivity()).setCity(city.getText().toString());
        ((SignUpActivity) getActivity()).setState(state.getSelectedItem().toString());
        ((SignUpActivity) getActivity()).setAddressZip(zip.getText().toString());

        String email = ((SignUpActivity) getActivity()).getEmail();
        String password = ((SignUpActivity) getActivity()).getPassword();
    }

    private void setButtonActions() {
        next.setOnClickListener(new View.OnClickListener() {
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

}
