package com.mobile.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
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
import com.mobile.Interfaces.ProfileActivityInterface;
import com.mobile.UserPreferences;
import com.mobile.helpers.LogUtils;
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


public class ProfileAccountChangePassword extends android.app.Fragment {


    private ProfileActivityInterface listener;
    private EditText oldPassword, newPassword1, newPassword2;
    private TextInputLayout oldPasswordTextInputLayout, newPassword1TextInputLayout, newPassword2TextInputLayout;
    private Button save, cancel;
    private View progress;
    private ChangePasswordResponse changePasswordResponse;
    private UserInfoResponse userInfoResponse;
    private boolean firstTime = true;
    private Activity myActivity;

    public ProfileAccountChangePassword() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    public void setUpView(View view) {
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

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (firstTime) {
                    enableSaveAndCancel();
                }
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
                if (firstTime) {
                    enableSaveAndCancel();
                    newPassword2.setEnabled(true);
                    firstTime = false;
                }
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

                if (newPassword1.getText().toString().trim().equalsIgnoreCase(newPassword2.getText().toString().trim()) && !oldPassword.getText().toString().trim().isEmpty()) {
                    if (newPassword1.getText().toString().length() >= 6) {
                        if (!newPassword1.getText().toString().trim().equalsIgnoreCase(oldPassword.getText().toString().trim())) {
                            progress.setVisibility(View.VISIBLE);
                            changePassword();
                        } else {
                            oldPasswordTextInputLayout.setError(getResources().getString(R.string.fragment_profile_account_information_new_password_same_as_old));
                        }
                    } else {
                        if (oldPassword.getText().toString().trim().isEmpty())
                            oldPasswordTextInputLayout.setError(getResources().getString(R.string.fragment_profile_account_information_old_password_empty));
                        if (newPassword1.getText().toString().trim().isEmpty())
                            newPassword1TextInputLayout.setError(getResources().getString(R.string.fragment_profile_account_information_password_empty));
                        else
                            newPassword1TextInputLayout.setError(getResources().getString(R.string.fragment_profile_account_information_password_more_than_6_characters));
                    }
                } else {
                    if (oldPassword.getText().toString().trim().isEmpty())
                        oldPasswordTextInputLayout.setError(getResources().getString(R.string.fragment_profile_account_information_old_password_empty));
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
        ChangePasswordRequest request = new ChangePasswordRequest(oldPassword.getText().toString().trim(), newPassword1.getText().toString().trim(), userId);
        RestClient.getAuthenticated().changePassword(request).enqueue(new Callback<ChangePasswordResponse>() {
            @Override
            public void onResponse(Call<ChangePasswordResponse> call, Response<ChangePasswordResponse> response) {
                if (response != null && response.isSuccessful()) {
                    changePasswordResponse = response.body();
                    logIn();
                } else {
                    oldPasswordTextInputLayout.setError("Wrong password");
                    progress.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<ChangePasswordResponse> call, Throwable t) {
                Toast.makeText(getActivity(), "Server Error; Please try again.", Toast.LENGTH_SHORT).show();
                LogUtils.newLog(Constants.TAG, "onFailure: " + t.getMessage());
                progress.setVisibility(View.GONE);
            }
        });
    }

    private void logIn() {
        String email = UserPreferences.getUserEmail().trim();
        String password = newPassword1.getText().toString().trim();
        String device_ID = DeviceID.getID(myActivity);
        String device_type = Build.DEVICE;
        String device = "android";

        LogInRequest request = new LogInRequest(email, password, device_ID, device_type, device);
        String UUID = "";
        LogUtils.newLog(TAG, "logIn: USER EMAIL " + email + " USER PASSWORD " + password);
        RestClient.getAuthenticated().login(UUID, request).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.body() != null && response.isSuccessful()) {
                    moviePassLoginSucceeded(response.body());
                    Toast.makeText(myActivity, "Password changed", Toast.LENGTH_LONG).show();
                    progress.setVisibility(View.GONE);
                    disableSaveAndCancel();
                    listener.closeFragment();
                } else {
                    progress.setVisibility(View.GONE);
                    LogUtils.newLog(TAG, "onResponse: FAILURE LOG IN " + response.toString());
                }
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
            String deviceUuid = user.getAndroidID();
            String authToken = user.getAuthToken();

           // UserPreferences.setUserCredentials(us, deviceUuid, authToken, user.getFirstName(), user.getEmail());
        }
    }

    public void enableSaveAndCancel() {
        save.setClickable(true);
        cancel.setClickable(true);
        cancel.setTextColor(ContextCompat.getColor(myActivity, R.color.almost_white));
        save.setTextColor(ContextCompat.getColor(myActivity, R.color.new_red));
    }

    public void disableSaveAndCancel() {
        firstTime = true;
        save.setClickable(false);
        cancel.setClickable(false);
        cancel.setTextColor(ContextCompat.getColor(myActivity, R.color.gray_icon));
        save.setTextColor(ContextCompat.getColor(myActivity, R.color.gray_icon));
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
        firstTime = true;
        final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ProfileActivityInterface) {
            listener = (ProfileActivityInterface) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement ProfileActivityInterface");
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        myActivity = activity;
        if (myActivity instanceof ProfileActivityInterface) {
            listener = (ProfileActivityInterface) myActivity;
        } else {
            throw new RuntimeException(myActivity.toString()
                    + " must implement ProfileActivityInterface");
        }
    }


    @Override
    public void onDetach() {
        super.onDetach();
    }

}
