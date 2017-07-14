package com.moviepass.fragments;

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
import android.widget.TextView;

import com.moviepass.R;
import com.moviepass.activities.SignUpActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by anubis on 7/11/17.
 */

public class SignUpStepThreeFragment extends Fragment {

    ArrayAdapter<CharSequence> mStatesAdapter;

    RelativeLayout mRelativeLayout;
    EditText mName;
    EditText mAddress;
    EditText mAddress2;
    EditText mCity;
    Spinner mState;
    EditText mZip;
    Button mNext;

    String states[] = new String[]{
            "AK", "AL", "AR", "AZ", "CA", "CO", "CT", "DC", "DE", "FL",
            "GA", "HI", "IA", "ID", "IL", "IN", "KS", "KY", "LA", "MA",
            "MD", "ME", "MI", "MN", "MO", "MS", "MT", "NC", "ND", "NE",
            "NH", "NJ", "NM", "NV", "NY", "OH", "OK", "OR", "PA", "RI",
            "SC", "SD", "TN", "TX", "UT", "VA", "VT", "WA", "WI", "WV", "WY", "State"
    };

    int pos;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_sign_up_step_three, container, false);

        mRelativeLayout = rootView.findViewById(R.id.relative_layout);
        mName = rootView.findViewById(R.id.et_name);
        mAddress = rootView.findViewById(R.id.et_address);
        mAddress2 = rootView.findViewById(R.id.et_address_two);
        mCity = rootView.findViewById(R.id.et_city);
        mState = rootView.findViewById(R.id.state);
        mZip = rootView.findViewById(R.id.et_zip);
        mNext = getActivity().findViewById(R.id.button_next);

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        mStatesAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.states_abbrev, R.layout.item_white_spinner);
        mStatesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        mState.setAdapter(mStatesAdapter);

        return rootView;
    }

    public static SignUpStepThreeFragment newInstance(String text) {

        SignUpStepThreeFragment f = new SignUpStepThreeFragment();
        Bundle b = new Bundle();
        b.putString("msg", text);

        f.setArguments(b);

        return f;
    }

    // This is for actions only available when SignUpTwoFrag is visible
    @Override
    public void setMenuVisibility(final boolean visible) {
        super.setMenuVisibility(visible);
        if (visible) {
            mNext.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (canContinue()) {
                        processSignUpInfo();
                    } else {
                        if (!isNameValid()) {
                            makeSnackbar(R.string.fragment_sign_up_step_three_valid_name);
                        } else if (!isAddressValid()) {
                            makeSnackbar(R.string.fragment_sign_up_step_three_valid_address);
                        } else if (!isAddress2Valid()) {
                            makeSnackbar(R.string.fragment_sign_up_step_three_valid_address_two);
                        } else if (!isCityValid()) {
                            makeSnackbar(R.string.fragment_sign_up_step_three_valid_city);
                        } else if (!isStateValid()) {
                            makeSnackbar(R.string.fragment_sign_up_step_three_valid_state);
                        } else {
                            makeSnackbar(R.string.fragment_sign_up_step_three_valid_zip);
                        }
                    }
                }
            });
        }
    }

    public boolean canContinue() {
        if (isNameValid() && isAddressValid() && isAddress2Valid() && isCityValid() && isStateValid() && isZipValid()) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isNameValid() {
        if (mName.length() > 1 && mName.length() <= 26 && !mName.getText().toString().matches(".*\\d+.*")) {
            return true;
        } else {
            pos = 0;
            return false;
        }
    }

    public boolean isAddressValid() {
        if (mAddress.length() > 2 && mAddress.length() <= 26) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isAddress2Valid() {
        if ((mAddress2.length() > 0 && mAddress2.length() <= 26)
                || mAddress2.getText().toString().equals("")) {
            return true;
        } else {
            Log.d("mAddress2", mAddress2.getText().toString());
            return false;
        }
    }

    public boolean isCityValid() {
        if (mCity.length() > 2 && mCity.length() <= 26 && !mCity.getText().toString().matches(".*\\d+.*")) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isStateValid() {
        if (!mState.getSelectedItem().toString().equals("State")) {
            Log.d("mStateValue: ", mState.getSelectedItem().toString());
            return true;
        } else {
            return false;
        }
    }

    public boolean isZipValid() {
        if (mZip.length() == 5) {
            return true;
        } else {
            return false;
        }
    }

    public void makeSnackbar(int message) {
        final Snackbar snackbar = Snackbar.make(mRelativeLayout, message, Snackbar.LENGTH_INDEFINITE);
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

        ((SignUpActivity) getActivity()).setName(mName.getText().toString());
        ((SignUpActivity) getActivity()).setAddress(mAddress.getText().toString());
        ((SignUpActivity) getActivity()).setAddress2(mAddress2.getText().toString());
        ((SignUpActivity) getActivity()).setCity(mCity.getText().toString());
        ((SignUpActivity) getActivity()).setState(mState.getSelectedItem().toString());
        ((SignUpActivity) getActivity()).setAddressZip(mZip.getText().toString());

        String email = ((SignUpActivity) getActivity()).getEmail();
        String password = ((SignUpActivity) getActivity()).getPassword();
    }


}
