package com.mobile.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.mobile.Constants;
import com.mobile.UserPreferences;
import com.mobile.helpers.LogUtils;
import com.mobile.network.RestClient;
import com.mobile.responses.UserInfoResponse;
import com.moviepass.R;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileAccountInformation extends MPFragment {

    private View rootView, progress;
    private TextView userName,userEmail,moviePassCard;
    private UserInfoResponse userInfoResponse;
    private TextView changeEmail;

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
        progress.setVisibility(View.VISIBLE);
        changeEmail = rootView.findViewById(R.id.changeEmailTextView);
        loadUserInfo();

        changeEmail.setClickable(true);
        changeEmail.setOnClickListener(v -> {
            showFragment(new ProfileAccountChangeEmail());
        });


        return rootView;
    }

    private void loadUserInfo() {
        int userId = UserPreferences.INSTANCE.getUserId();
        RestClient.getAuthenticated().getUserData(userId).enqueue(new Callback<UserInfoResponse>() {
            @Override
            public void onResponse(Call<UserInfoResponse> call, Response<UserInfoResponse> response) {
                userInfoResponse = response.body();
                if (userInfoResponse != null) {

                    String firstName = userInfoResponse.getUser().getFirstName();
                    String lastName = userInfoResponse.getUser().getLastName();
                    String email = userInfoResponse.getUser().getEmail();

                    userName.setText(firstName + " " + lastName);
                    userEmail.setText(email);


                    moviePassCard.setText(userInfoResponse.getMoviePassCardNumber());

                    progress.setVisibility(View.GONE);

                }
            }

            @Override
            public void onFailure(Call<UserInfoResponse> call, Throwable t) {
                Activity activity = getActivity();
                if(activity==null) {
                    return;
                }
                Toast.makeText(activity, "Server Error; Please try again.", Toast.LENGTH_SHORT).show();
                LogUtils.newLog(Constants.TAG, "onFailure: " + t.getMessage());
            }
        });
    }

}
