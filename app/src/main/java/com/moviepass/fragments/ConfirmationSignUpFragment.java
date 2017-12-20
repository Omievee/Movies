package com.moviepass.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.moviepass.DeviceID;
import com.moviepass.R;
import com.moviepass.UserPreferences;
import com.moviepass.activities.MoviesActivity;
import com.moviepass.model.ProspectUser;
import com.moviepass.model.User;
import com.moviepass.network.RestClient;
import com.moviepass.requests.LogInRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConfirmationSignUpFragment extends Fragment {

    //    private OnFragmentInteractionListener mListener;
    View rootView;

    ImageButton confirmLogIn;


    public ConfirmationSignUpFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fr_confirmation_sign_up, container, false);

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        confirmLogIn = view.findViewById(R.id.CONFIRM_GOTOLOGIN);
        confirmLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                login();
            }
        });

    }


    private void login() {
        String email = ProspectUser.email;
        String password = ProspectUser.password;

        LogInRequest request = new LogInRequest(email, password);
        String deviceId = DeviceID.getID(getActivity());

        RestClient.getUnauthenticated().login(deviceId, request).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                User user = response.body();
                if (user != null) {
                    RestClient.userId = user.getId();
                    RestClient.deviceUuid = user.getDeviceUuid();
                    RestClient.authToken = user.getAuthToken();

                    UserPreferences.setUserCredentials(RestClient.userId, RestClient.deviceUuid, RestClient.authToken, user.getFirstName(), user.getEmail());
                    Intent i = new Intent(getActivity(), MoviesActivity.class);
                    i.putExtra("launch", true);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                    progress.setVisibility(View.GONE);
                    startActivity(i);
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {

                /* TODO : handle failure */
            }
        });
    }
}
