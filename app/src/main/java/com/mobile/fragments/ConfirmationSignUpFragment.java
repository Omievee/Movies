package com.mobile.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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

import com.helpshift.support.Log;
import com.mobile.Constants;
import com.mobile.UserPreferences;
import com.mobile.activities.ActivatedCard_TutorialActivity;
import com.mobile.model.ProspectUser;
import com.mobile.model.User;
import com.mobile.network.RestClient;
import com.mobile.requests.LogInRequest;
import com.mobile.responses.AndroidIDVerificationResponse;
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
        Log.d("CONFIMATION", "onViewCreated: ");
        confirmLogIn.setOnClickListener(v -> logIn());
    }


    private void logIn() {
        String email = ProspectUser.email;
        String password = ProspectUser.password;
        String deviceId = ProspectUser.androidID;
        String deviceType = Build.MODEL;
        String device = "ANDROID";


        LogInRequest request = new LogInRequest(email, password, deviceId, deviceType, device);
        android.util.Log.d(Constants.TAG, "logIn: " + deviceId);
        String UUID = "flag";
        RestClient.getAuthenticated().login(UUID, request).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                userRESPONSE = response.body();
                android.util.Log.d(Constants.TAG, "RESPONSE CODE??? : " + response.code());
                if (response.code() == 200) {
                    UserPreferences.setHeaders(userRESPONSE.getAuthToken(), userRESPONSE.getId());
                    verifyAndroidID(deviceType, deviceId, device, true);

//                    } else if (response.code() == 207) {
//                        android.util.Log.d(Constants.TAG, "onResponse: ");
//                        AlertDialog.Builder alert = new AlertDialog.Builder(LogInActivity.this, R.style.CUSTOM_ALERT);
//                        alert.setView(R.layout.alertdialog_onedevice);
//                        alert.setCancelable(false);
//                        alert.setPositiveButton("Switch to this device", (dialog, which) -> {
//                            dialog.dismiss();
//                            progress.setVisibility(View.GONE);
//                            AlertDialog.Builder areYouSure = new AlertDialog.Builder(myContext, R.style.CUSTOM_ALERT);
//
//                            areYouSure.setView(R.layout.alertdialog_onedevice_commit);
//                            areYouSure.setPositiveButton("Switch to this device", (d, w) -> {
//                                d.dismiss();
//                                String userSwitchDeviceID = DeviceID.getID(myContext);
//                                UserPreferences.setHeaders(userRESPONSE.getAuthToken(), userRESPONSE.getId());
//                                verifyAndroidID(deviceType, userSwitchDeviceID, device, true);
//                            });
//
//                            areYouSure.setNegativeButton(android.R.string.cancel, (d, wi) -> {
//                                d.dismiss();
//                                d.cancel();
//                                progress.setVisibility(View.GONE);
//                            });
//                            areYouSure.show();
//                        });
//
//
//                        alert.setNegativeButton(android.R.string.cancel, (dialog, which) -> {
//                            dialog.cancel();
//                            progress.setVisibility(View.GONE);
//
//                        });
//
//                        alert.show();

                } else if (response.errorBody() != null) {
                    try {
                        JSONObject jObjError = new JSONObject(response.errorBody().string());
                        Toast.makeText(myContext, jObjError.getString("message"), Toast.LENGTH_LONG).show();
                        android.util.Log.d(Constants.TAG, "onResponse: " + jObjError.getString("message"));

                    } catch (Exception e) {
                        Toast.makeText(myContext, e.getMessage(), Toast.LENGTH_LONG).show();
                        Log.d(Constants.TAG, "onResponse: " + e.getMessage());
                    }
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(myContext, t.getMessage(), Toast.LENGTH_LONG).show();
                android.util.Log.d(Constants.TAG, "failure: " + t.getMessage());
            }
        });
    }

    private void verifyAndroidID(String deviceType, String deviceId, String device, boolean updateDevice) {

        AndroidIDVerificationResponse request = new AndroidIDVerificationResponse(device, deviceId, deviceType, updateDevice);
        String user_id = String.valueOf(userRESPONSE.getId());


        RestClient.getAuthenticated().verifyAndroidID(user_id, request).enqueue(new Callback<AndroidIDVerificationResponse>() {
            @Override
            public void onResponse(Call<AndroidIDVerificationResponse> call, Response<AndroidIDVerificationResponse> response) {
                android.util.Log.d(Constants.TAG, "onResponse: " + userRESPONSE.getAuthToken() + "   " + userRESPONSE.getId());
                android.util.Log.d(Constants.TAG, "onResponse: " + userRESPONSE.getOneDeviceId() + "   " + userRESPONSE.getId());

                if (response!=null && response.isSuccessful()) {
                    RestClient.userId = userRESPONSE.getId();
                    RestClient.deviceAndroidID = userRESPONSE.getAndroidID();
                    RestClient.authToken = userRESPONSE.getAuthToken();



                    UserPreferences.setUserCredentials(RestClient.userId, RestClient.deviceAndroidID, RestClient.authToken, ProspectUser.firstName, ProspectUser.email, userRESPONSE.getOneDeviceId());
                    UserPreferences.setOneDeviceId(response.body().getOneDeviceId());
                    Intent i = new Intent(myContext, ActivatedCard_TutorialActivity.class);
                    i.putExtra("launch", true);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                }

            }

            @Override
            public void onFailure(Call<AndroidIDVerificationResponse> call, Throwable t) {
                android.util.Log.d(Constants.TAG, "onFailure: " + t.getMessage());
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
