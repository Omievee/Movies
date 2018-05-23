package com.mobile.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.mobile.Constants;
import com.mobile.Interfaces.ProfileActivityInterface;
import com.mobile.UserPreferences;
import com.mobile.helpers.LogUtils;
import com.mobile.network.RestClient;
import com.mobile.requests.ChangeEmailRequest;
import com.mobile.requests.ChangePasswordRequest;
import com.mobile.responses.ChangeEmailResponse;
import com.mobile.responses.ChangePasswordResponse;
import com.moviepass.R;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ProfileAccountChangeEmail extends Fragment implements View.OnClickListener {

    TextInputLayout newEmailTextInputLayout, currentPasswordTextInputLayout;
    EditText newEmail, currentPassword;
    TextView save, cancel;
    private ProfileActivityInterface listener;
    private Activity myActivity;

    public ProfileAccountChangeEmail() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUpViews(view);
    }

    public void setUpViews(View v){
        newEmailTextInputLayout = v.findViewById(R.id.newEmailTextInputLayout);
        newEmail = v.findViewById(R.id.newEmailEditText);
        currentPasswordTextInputLayout = v.findViewById(R.id.currentPasswordTextInputLayout);
        currentPassword = v.findViewById(R.id.currentPasswordEditText);
        save = v.findViewById(R.id.saveChanges);
        cancel = v.findViewById(R.id.cancelChanges);
        newEmail.addTextChangedListener(new CustomTextWatcher());
        currentPassword.addTextChangedListener(new CustomTextWatcher());
        save.setOnClickListener(this);
        cancel.setOnClickListener(this);
    }

    public void enableSave(){
        save.setVisibility(View.VISIBLE);
        cancel.setVisibility(View.VISIBLE);
    }

    public void disableSave(){
        save.setVisibility(View.INVISIBLE);
        cancel.setVisibility(View.INVISIBLE);

    }

    public void closeKeyboard(){
        final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile_account_change_email, container, false);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.saveChanges:
                if(valid()){
                    updateEmail();
                }
                break;

            case R.id.cancelChanges:
                disableSave();
                closeKeyboard();
                newEmailTextInputLayout.setError("");
                currentPasswordTextInputLayout.setError("");
                newEmail.getText().clear();
                currentPassword.getText().clear();
                newEmail.clearFocus();
                currentPassword.clearFocus();
                break;
        }
    }

    private void updateEmail() {
        int userId = UserPreferences.getUserId();
        ChangeEmailRequest request = new ChangeEmailRequest(newEmail.getText().toString().trim(),currentPassword.getText().toString().trim(), userId);
        RestClient.getAuthenticated().changeEmail(request).enqueue(new Callback<ChangeEmailResponse>() {
            @Override
            public void onResponse(Call<ChangeEmailResponse> call, Response<ChangeEmailResponse> response) {
                if (response != null && response.isSuccessful()) {
                    Log.d(Constants.TAG, "onResponse: "+response.toString());
                    Toast.makeText(myActivity, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                    UserPreferences.updateEmail(newEmail.getText().toString().trim());
                    listener.closeFragment();
                    listener.closeFragment();
                } else {
                    try {
                        JSONObject jObjError = new JSONObject(response.errorBody().string());
                        Toast.makeText(myActivity, jObjError.getString("error"), Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(myActivity, "Error updating email", Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ChangeEmailResponse> call, Throwable t) {
                Toast.makeText(getActivity(), "Server Error; Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public boolean valid(){
        newEmailTextInputLayout.setError("");
        currentPasswordTextInputLayout.setError("");
        boolean valid = true;
        if(newEmail.getText().toString().trim().isEmpty()) {
            valid = false;
            newEmailTextInputLayout.setError("Enter a valid email address");
        }
        if(currentPassword.getText().toString().trim().isEmpty() || (currentPassword.getText().toString().trim().length() < 6 || currentPassword.getText().toString().trim().length() > 20)) {
            valid = false;
            currentPasswordTextInputLayout.setError("Enter a valid password");
        }
      return  valid;
    }

    class CustomTextWatcher implements TextWatcher {


        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if(newEmail.getText().toString().trim().isEmpty() && currentPassword.getText().toString().trim().isEmpty()){
                disableSave();
            } else{
                enableSave();
            }
        }
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
}
