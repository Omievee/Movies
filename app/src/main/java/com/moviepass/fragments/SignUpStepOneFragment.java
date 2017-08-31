package com.moviepass.fragments;

import android.content.Context;
import android.content.Intent;
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
import android.widget.Toast;

import com.moviepass.R;
import com.moviepass.activities.SignUpActivity;
import com.moviepass.model.Plan;
import com.moviepass.model.ProspectUser;
import com.moviepass.network.RestCallback;
import com.moviepass.network.RestClient;
import com.moviepass.network.RestError;
import com.moviepass.requests.PersonalInfoRequest;
import com.moviepass.responses.PersonalInfoResponse;
import com.moviepass.responses.RegistrationPlanResponse;

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
    EditText etFirstName;
    EditText etLastName;
    EditText etAddress;
    EditText etAddress2;
    EditText etCity;
    Spinner state;
    EditText etZip;
    Button next;
    View progress;

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
        etFirstName = rootView.findViewById(R.id.et_first_name);
        etLastName = rootView.findViewById(R.id.et_last_name);
        etAddress = rootView.findViewById(R.id.et_address);
        etAddress2 = rootView.findViewById(R.id.et_address_two);
        etCity = rootView.findViewById(R.id.et_city);
        state = rootView.findViewById(R.id.state);
        etZip = rootView.findViewById(R.id.et_zip);
        next = rootView.findViewById(R.id.button_next);
        progress = getActivity().findViewById(R.id.progress);

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
        if (etFirstName.length() > 1 && etFirstName.length() <= 26 && !etFirstName.getText().toString().matches(".*\\d+.*")) {
            return true;
        } else {
            pos = 0;
            return false;
        }
    }

    public boolean isLastNameValid() {
        if (etLastName.length() > 1 && etLastName.length() <= 26 && !etLastName.getText().toString().matches(".*\\d+.*")) {
            return true;
        } else {
            pos = 0;
            return false;
        }
    }

    public boolean isAddressValid() {
        if (etAddress.length() > 2 && etAddress.length() <= 26) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isAddress2Valid() {
        if ((etAddress2.length() > 0 && etAddress2.length() <= 26)
                || etAddress2.getText().toString().equals("")) {
            return true;
        } else {
            Log.d("mAddress2", etAddress2.getText().toString());
            return false;
        }
    }

    public boolean isCityValid() {
        if (etCity.length() > 2 && etCity.length() <= 26 && !etCity.getText().toString().matches(".*\\d+.*")) {
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
        if (etZip.length() == 5) {
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
        ((SignUpActivity) getActivity()).setFirstName(etFirstName.getText().toString());
        ((SignUpActivity) getActivity()).setLastName(etLastName.getText().toString());
        ((SignUpActivity) getActivity()).setAddress(etAddress.getText().toString());
        ((SignUpActivity) getActivity()).setAddress2(etAddress2.getText().toString());
        ((SignUpActivity) getActivity()).setCity(etCity.getText().toString());
        ((SignUpActivity) getActivity()).setState(state.getSelectedItem().toString());
        ((SignUpActivity) getActivity()).setAddressZip(etZip.getText().toString());

        String email = ((SignUpActivity) getActivity()).getEmail();
        String password = ((SignUpActivity) getActivity()).getPassword();

        ProspectUser.firstName = etFirstName.getText().toString();
        ProspectUser.lastName = etLastName.getText().toString();
        ProspectUser.address = etAddress.getText().toString();
        ProspectUser.address2 = etAddress2.getText().toString();
        ProspectUser.city = etCity.getText().toString();
        ProspectUser.state = state.getSelectedItem().toString();
        ProspectUser.zip = etZip.getText().toString();

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

                            ((SignUpActivity) getActivity()).setPrice(price);
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
