package com.mobile.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.app.Fragment;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mobile.Constants;
import com.mobile.Interfaces.ProfileActivityInterface;
import com.mobile.UserPreferences;
import com.mobile.network.RestClient;
import com.mobile.responses.UserInfoResponse;
import com.moviepass.R;

import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileAccountInformation extends Fragment {

    private ProfileActivityInterface mListener;
    private Context context;
    private View rootView, progress;
    private TextView userName,userEmail,moviePassCard;
    private EditText password1, password2;
    private TextInputLayout password1TextInputLayout, password2TextInputLayout;
    private UserInfoResponse userInfoResponse;
    private Button save, cancel;
    private ImageView clear1, clear2;
    private boolean firstTimePassword = true, firstTouchPassword2 = true;

    public ProfileAccountInformation() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView =  inflater.inflate(R.layout.fragment_profile_account_information, container, false);
        userName = rootView.findViewById(R.id.USER_NAME);
        userEmail = rootView.findViewById(R.id.USER_EMAIL);
        moviePassCard = rootView.findViewById(R.id.MPCardNum);
        progress = rootView.findViewById(R.id.progress);
        password1 = rootView.findViewById(R.id.password1);
        password2 = rootView.findViewById(R.id.password2);
        password1TextInputLayout = rootView.findViewById(R.id.password1TextInputLayout);
        password2TextInputLayout = rootView.findViewById(R.id.password2TextInputLayout);
        save = rootView.findViewById(R.id.saveChanges);
        cancel = rootView.findViewById(R.id.cancelChanges);
        progress.setVisibility(View.VISIBLE);
        clear1 = rootView.findViewById(R.id.clear1);
        clear2 = rootView.findViewById(R.id.clear2);
        loadUserInfo();
        save.setClickable(false);
        cancel.setClickable(false);
        password2.setEnabled(false);



        password1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (firstTimePassword) {
                    enableSaveAndCancel();
                    firstTimePassword = false;
                    password2.setEnabled(true);
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!firstTimePassword) {
                    if (s.length() > 0) {
                        clear1.setVisibility(View.VISIBLE);
                        if(s.length() >= 6)
                            password1TextInputLayout.setError("");
                    } else {
                        clear1.setVisibility(View.GONE);
                        if(firstTouchPassword2){
                            firstTouchPassword2=false;
                            password2.setText("");
                        }
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        password2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!firstTimePassword) {
                    if (s.length() > 0) {
                        clear2.setVisibility(View.VISIBLE);
                    } else {
                        clear2.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        clear1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                password1.setText("");
            }
        });
        clear2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                password2.setText("");
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                password1TextInputLayout.setError(null);
                password2TextInputLayout.setError(null);
                password1.clearFocus();
                password2.clearFocus();
                if(password1.getText().toString().trim().equalsIgnoreCase(password2.getText().toString().trim())){
                    if(password1.getText().toString().length()>=6){
                        Toast.makeText(context, "Changing password", Toast.LENGTH_SHORT).show();
                        disableSaveAndCancel();
                    } else{
                        password1TextInputLayout.setError(getResources().getString(R.string.activity_profile_password_more_than_6_characters));
                    }
                } else {
                    password2TextInputLayout.setError(getResources().getString(R.string.activity_profile_password_match));
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disableSaveAndCancel();
            }
        });

        return rootView;
    }

    public void enableSaveAndCancel(){
        save.setClickable(true);
        cancel.setClickable(true);
        cancel.setTextColor(ContextCompat.getColor(context,R.color.almost_white));
        save.setTextColor(ContextCompat.getColor(context,R.color.new_red));
    }

    public void disableSaveAndCancel(){
        save.setClickable(false);
        cancel.setClickable(false);
        cancel.setTextColor(ContextCompat.getColor(context,R.color.gray_icon));
        save.setTextColor(ContextCompat.getColor(context,R.color.gray_icon));
        password1.setText("password");
        password2.setText("password");
        password2.setEnabled(false);
        clear2.setVisibility(View.GONE);
        clear1.setVisibility(View.GONE);
        password1.clearFocus();
        password2.clearFocus();
        firstTouchPassword2 = true;
        firstTimePassword=true;
        password2TextInputLayout.setError(null);
        password1TextInputLayout.setError(null);
        final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
    }

    private void loadUserInfo() {
        int userId = UserPreferences.getUserId();
        RestClient.getAuthenticated().getUserData(userId).enqueue(new Callback<UserInfoResponse>() {
            @Override
            public void onResponse(Call<UserInfoResponse> call, Response<UserInfoResponse> response) {
                userInfoResponse = response.body();
                if (userInfoResponse != null) {

                    String firstName = userInfoResponse.getUser().getFirstName();
                    String lastName = userInfoResponse.getUser().getLastName();
                    String email = userInfoResponse.getEmail();

                    userName.setText(firstName + " " + lastName);
                    userEmail.setText(email);

                    moviePassCard.setText(userInfoResponse.getMoviePassCardNumber());

                    progress.setVisibility(View.GONE);

                }
            }

            @Override
            public void onFailure(Call<UserInfoResponse> call, Throwable t) {
                Toast.makeText(getActivity(), "Server Error; Please try again.", Toast.LENGTH_SHORT).show();
                mListener.closeFragment();
                Log.d(Constants.TAG, "onFailure: " + t.getMessage());
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;

        if (context instanceof ProfileActivityInterface) {
            mListener = (ProfileActivityInterface) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement ProfileActivityInterface");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        this.context = null;
    }

}
