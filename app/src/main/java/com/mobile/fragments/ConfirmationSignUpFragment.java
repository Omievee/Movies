package com.mobile.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.mobile.Constants;
import com.mobile.UserPreferences;
import com.mobile.helpers.LogUtils;
import com.mobile.model.ProspectUser;
import com.mobile.model.User;
import com.mobile.network.RestClient;
import com.mobile.requests.LogInRequest;
import com.moviepass.R;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConfirmationSignUpFragment extends Fragment {

    Context myContext;
    //    private OnFragmentInteractionListener mListener;
    View rootView;
    TextView confirmLogIn;
    User userRESPONSE;
    View progress;

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
        progress = view.findViewById(R.id.progress);
        LogUtils.newLog("CONFIMATION", "onViewCreated: ");
        confirmLogIn.setOnClickListener(v -> logIn());
    }


    private void logIn() {
        progress.setVisibility(View.VISIBLE);
        String email = ProspectUser.email;
        String password = ProspectUser.password;
        String deviceId = ProspectUser.androidID;
        String deviceType = Build.MODEL;
        String device = "ANDROID";


        LogInRequest request = new LogInRequest(email, password, deviceId, deviceType, device);
        LogUtils.newLog(Constants.TAG, "logIn: " + deviceId);
        String UUID = "flag";
        RestClient.getAuthenticated().login(UUID, request).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                userRESPONSE = response.body();
                LogUtils.newLog(Constants.TAG, "RESPONSE CODE??? : " + response.code());
                if (response.code() == 200) {
                    progress.setVisibility(View.GONE);
                    UserPreferences.INSTANCE.setHeaders(userRESPONSE.getAuthToken(), userRESPONSE.getId());
//                    verifyAndroidID(deviceType, deviceId, device, true);

                    RestClient.userId = userRESPONSE.getId();
                    RestClient.deviceAndroidID = userRESPONSE.getAndroidID();
                    RestClient.authToken = userRESPONSE.getAuthToken();
                    
                } else if (response.errorBody() != null) {
                    progress.setVisibility(View.GONE);
                    try {
                        JSONObject jObjError = new JSONObject(response.errorBody().string());
                        Toast.makeText(myContext, jObjError.getString("message"), Toast.LENGTH_LONG).show();
                        LogUtils.newLog(Constants.TAG, "onResponse: " + jObjError.getString("message"));

                    } catch (Exception e) {
                        Toast.makeText(myContext, e.getMessage(), Toast.LENGTH_LONG).show();
                        LogUtils.newLog(Constants.TAG, "onResponse: " + e.getMessage());
                    }
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                progress.setVisibility(View.GONE);
                Toast.makeText(myContext, t.getMessage(), Toast.LENGTH_LONG).show();
                LogUtils.newLog(Constants.TAG, "failure: " + t.getMessage());
            }
        });
    }




    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        myContext = context;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        myContext = activity;
    }
}
