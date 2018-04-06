package com.mobile.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.helpshift.support.Log;
import com.mobile.Constants;
import com.mobile.DeviceID;
import com.mobile.UserPreferences;
import com.mobile.model.User;
import com.mobile.network.RestClient;
import com.mobile.requests.ChangePasswordRequest;
import com.mobile.requests.LogInRequest;
import com.mobile.responses.ChangePasswordResponse;
import com.mobile.responses.UserInfoResponse;
import com.moviepass.R;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.mobile.fragments.PendingReservationFragment.TAG;


public class ProfileAccountChangePassword extends Fragment {


    private EditText oldPassword, newPassword1, newPassword2;
    private TextInputLayout oldPasswordTextInputLayout, newPassword1TextInputLayout, newPassword2TextInputLayout;
    private Button save, cancel;
    private View progress;
    private ChangePasswordResponse changePasswordResponse;
    private UserInfoResponse userInfoResponse;

    public ProfileAccountChangePassword() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
//        }
    }

    public void setUpView(View view){
        oldPassword = view.findViewById(R.id.oldPassword);
        oldPasswordTextInputLayout = view.findViewById(R.id.oldPasswordTextInputLayout);
        newPassword1 = view.findViewById(R.id.password1);
        newPassword1TextInputLayout = view.findViewById(R.id.password1TextInputLayout);
        newPassword2 = view.findViewById(R.id.password2);
        newPassword2TextInputLayout = view.findViewById(R.id.password2TextInputLayout);
        save = view.findViewById(R.id.saveChanges);
        cancel = view.findViewById(R.id.cancelChanges);
        progress = view.findViewById(R.id.progress);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile_account_change_password, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUpView(view);

        oldPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                newPassword1TextInputLayout.setVisibility(View.VISIBLE);
                newPassword2TextInputLayout.setVisibility(View.VISIBLE);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });



        newPassword1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        newPassword2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newPassword1TextInputLayout.setError(null);
                newPassword2TextInputLayout.setError(null);
                oldPasswordTextInputLayout.setError(null);
                newPassword1.clearFocus();
                newPassword2.clearFocus();
                oldPassword.clearFocus();
                final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);

                if(newPassword1.getText().toString().trim().equalsIgnoreCase(newPassword2.getText().toString().trim()) && !oldPassword.getText().toString().trim().isEmpty()){
                    if(newPassword1.getText().toString().length()>=6){
                        progress.setVisibility(View.VISIBLE);
                        changePassword();
                    } else{
                        if(oldPassword.getText().toString().trim().isEmpty())
                            oldPasswordTextInputLayout.setError(getResources().getString(R.string.fragment_profile_account_information_old_password_empty));
                        if(newPassword1.getText().toString().trim().isEmpty())
                            newPassword1TextInputLayout.setError(getResources().getString(R.string.fragment_profile_account_information_password_empty));
                        else
                            newPassword1TextInputLayout.setError(getResources().getString(R.string.fragment_profile_account_information_password_more_than_6_characters));
                    }
                } else {
                    newPassword2TextInputLayout.setError(getResources().getString(R.string.fragment_profile_account_information_password_match));
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disableSaveAndCancel();
            }
        });
    }

    private void changePassword() {
        int userId = UserPreferences.getUserId();
//        String oldPassword = UserPreferences.getP
        ChangePasswordRequest request = new ChangePasswordRequest(oldPassword.getText().toString().trim(),password1.getText().toString().trim(), userId);
        RestClient.getAuthenticated().changePassword(request).enqueue(new Callback<ChangePasswordResponse>() {
            @Override
            public void onResponse(Call<ChangePasswordResponse> call, Response<ChangePasswordResponse> response) {
                if(response!=null && response.isSuccessful()){
                    changePasswordResponse = response.body();
                    logIn();
                } else {
                    Toast.makeText(getContext(), "Wrong current password", Toast.LENGTH_SHORT).show();
                    progress.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<ChangePasswordResponse> call, Throwable t) {
                Toast.makeText(getActivity(), "Server Error; Please try again.", Toast.LENGTH_SHORT).show();
                Log.d(Constants.TAG, "onFailure: " + t.getMessage());
                progress.setVisibility(View.GONE);
            }
        });
    }

    private void logIn() {
        String email = UserPreferences.getUserEmail().trim();
        String password = newPassword1.getText().toString().trim();
        LogInRequest request = new LogInRequest(email, password);
        android.util.Log.d(TAG, "logIn: USER EMAIL "+email+" USER PASSWORD "+password);
        String deviceId = DeviceID.getID(getContext());
        RestClient.getAuthenticated().login(deviceId, request).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.body() != null && response.isSuccessful()) {
                    moviePassLoginSucceeded(response.body());
                    Toast.makeText(getContext(), "Password changed", Toast.LENGTH_LONG).show();
                    newPassword1.setText("");
                    newPassword2.setText("");
                    oldPassword.setText("");
                    progress.setVisibility(View.GONE);
                } else{
                    progress.setVisibility(View.GONE);
                    android.util.Log.d(TAG, "onResponse: FAILURE LOG IN "+response.toString());
                }
                disableSaveAndCancel();
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                progress.setVisibility(View.GONE);
//                   Toast.makeText(LogInActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void moviePassLoginSucceeded(User user) {
        if (user != null) {

            int us = user.getId();
            String deviceUuid = user.getDeviceUuid();
            String authToken = user.getAuthToken();

            UserPreferences.setUserCredentials(us, deviceUuid, authToken, user.getFirstName(), user.getEmail());
        }
    }

    public void enableSaveAndCancel(){
        save.setClickable(true);
        cancel.setClickable(true);
        cancel.setTextColor(ContextCompat.getColor(getContext(),R.color.almost_white));
        save.setTextColor(ContextCompat.getColor(getContext(),R.color.new_red));
    }

    public void disableSaveAndCancel(){
        save.setClickable(false);
        cancel.setClickable(false);
        cancel.setTextColor(ContextCompat.getColor(getContext(),R.color.gray_icon));
        save.setTextColor(ContextCompat.getColor(getContext(),R.color.gray_icon));
        newPassword1.setText("");
        newPassword2.setText("");
        oldPassword.setText("");
        newPassword2.setEnabled(false);
        newPassword1.clearFocus();
        newPassword2.clearFocus();
        oldPassword.clearFocus();
        newPassword2TextInputLayout.setError(null);
        newPassword1TextInputLayout.setError(null);
        oldPasswordTextInputLayout.setError(null);
        final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
    }
    



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

}
