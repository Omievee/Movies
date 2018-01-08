package com.moviepass.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
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
import com.moviepass.UserPreferences;
import com.moviepass.network.RestClient;
import com.moviepass.requests.AddressChangeRequest;
import com.moviepass.responses.UserInfoResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by anubis on 8/1/17.
 */

public class ProfileBillingAddressFragment extends Fragment {

    ArrayAdapter<CharSequence> statesAdapter;

    RelativeLayout relativeLayout;
    EditText name;
    EditText address;
    EditText address2;
    EditText city;
    Spinner state;
    EditText zip;
    Button update;
    View progress;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_profile_billing_address, container, false);

        final Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle("Billing Address");

        relativeLayout = rootView.findViewById(R.id.relative_layout);
        int padding_in_dp = 16;  // 6 dps
        final float scale = getResources().getDisplayMetrics().density;
        int padding_in_px = (int) (padding_in_dp * scale + 0.5f);
        relativeLayout.setPadding(padding_in_px, padding_in_px, padding_in_px, padding_in_px);

        address = rootView.findViewById(R.id.et_address);
        address2 = rootView.findViewById(R.id.et_address_two);
        city = rootView.findViewById(R.id.et_city);
        state = rootView.findViewById(R.id.state);
        zip = rootView.findViewById(R.id.et_zip);
        update = rootView.findViewById(R.id.button_update);
        progress = rootView.findViewById(R.id.progress);

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        statesAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.states_abbrev, R.layout.item_white_spinner);
        statesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        state.setAdapter(statesAdapter);

        loadUserInfo();

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                update.setEnabled(false);
                progress.setVisibility(View.VISIBLE);

                String finalAddress = address.getText().toString();
                String finalAddress2 = address2.getText().toString();
                String finalCity = city.getText().toString();
                String finalState = state.getSelectedItem().toString();
                String finalZip = zip.getText().toString();

                if (canContinue()) {
                    updateAddress(finalAddress, finalAddress2, finalCity, finalState, finalZip);
                } else {
                    update.setEnabled(true);
                    progress.setVisibility(View.VISIBLE);

                    if (!isAddressValid()) {
                        makeSnackbar(R.string.fragment_profile_billing_address_valid_address);
                    } else if (!isAddress2Valid()) {
                        makeSnackbar(R.string.fragment_profile_billing_address_valid_address_two);
                    } else if (!isCityValid()) {
                        makeSnackbar(R.string.fragment_profile_billing_address_valid_city);
                    } else if (!isStateValid()) {
                        makeSnackbar(R.string.fragment_profile_billing_address_valid_state);
                    } else {
                        makeSnackbar(R.string.fragment_profile_billing_address_valid_zip);
                    }
                }
            }
        });

        return rootView;
    }

    private void loadUserInfo() {
        int userId = UserPreferences.getUserId();

        RestClient.getAuthenticated().getUserData(userId).enqueue(new Callback<UserInfoResponse>() {
            @Override
            public void onResponse(Call<UserInfoResponse> call, Response<UserInfoResponse> response) {
                UserInfoResponse userInfoResponse = response.body();

                if (userInfoResponse != null && response.isSuccessful()) {
                    String addressLine1 = userInfoResponse.getBillingAddressLine1();
                    String addressLine2 = userInfoResponse.getBillingAddressLine2();

                    address.setText(addressLine1);

                    try {
                        String[] data = addressLine2.split(",");

                        city.setText(data[0].trim());
                        int statePosition = statesAdapter.getPosition(data[1].trim());
                        state.setSelection(statePosition);
                        zip.setText(data[2].trim());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<UserInfoResponse> call, Throwable t) {
            }

        });
    }

    public boolean canContinue() {
        if (isAddressValid() && isAddress2Valid() && isCityValid() && isStateValid() && isZipValid()) {
            return true;
        } else {
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

    public void updateAddress(String etAddress, String etAddress2, String etCity, String etState, String etZip) {

        String type = "signup2SameAddressSwitch";
        int userId = UserPreferences.getUserId();

        AddressChangeRequest request = new AddressChangeRequest(etAddress, etAddress2, etCity, etState, etZip, type);
        RestClient.getAuthenticated().updateAddress(userId, request).enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
                update.setEnabled(true);
                progress.setVisibility(View.GONE);

                loadUserInfo();

                final Snackbar snackbar = Snackbar.make(relativeLayout, R.string.fragment_profile_billing_address_updated, Snackbar.LENGTH_LONG);
                snackbar.show();
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                update.setEnabled(true);
                progress.setVisibility(View.GONE);

                Log.d("update BillAdd Error", t.toString());
            }
        });
    }
}