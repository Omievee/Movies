package com.mobile.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import com.helpshift.support.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mobile.Constants;
import com.mobile.DeviceID;
import com.mobile.UserPreferences;
import com.mobile.model.User;
import com.mobile.network.RestClient;
import com.mobile.requests.FacebookSignInRequest;
import com.mobile.requests.LogInRequest;
import com.mobile.responses.RestrictionsResponse;
import com.moviepass.R;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.Objects;

import butterknife.BindView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by anubis on 4/27/17.
 */

public class LogInActivity extends AppCompatActivity {

    @BindView(R.id.input_email)
    EditText mInputEmail;
    @BindView(R.id.input_password)
    android.support.design.widget.TextInputEditText mInputPassword;
    @BindView(R.id.button_log_in)
    Button mButtonLogIn;
    @BindView(R.id.forgot_password)
    TextView mForgotPassword;
    @BindView(R.id.sign_up)
    TextView mSignUp;
    @BindView(R.id.progress)
    View progress;
    Button facebook;
    CallbackManager callbackManager;
    LoginButton facebookLogInButton;
    int offset = 3232323;
    int userId;
    public RestrictionsResponse restriction;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        FacebookSdk.sdkInitialize(this);
        callbackManager = CallbackManager.Factory.create();

        mInputEmail = findViewById(R.id.input_email);
        mInputPassword = findViewById(R.id.input_password);
        progress = findViewById(R.id.progress);
//        facebookLogInButton = findViewById(R.id.button_facebook_log_in);
        facebook = findViewById(R.id.button_facebook_log_in);
        mButtonLogIn = findViewById(R.id.button_log_in);
        mButtonLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                InputMethodManager inputMethodManager = (InputMethodManager)  getSystemService(Activity.INPUT_METHOD_SERVICE);
//                inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                progress.setVisibility(View.VISIBLE);
                logIn();
            }
        });

        mSignUp = findViewById(R.id.sign_up);
        mSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LogInActivity.this, OnboardingActivity.class);
                startActivity(intent);
            }
        });

        mForgotPassword = findViewById(R.id.forgot_password);
        mForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                forgotPassword();
            }
        });

        facebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginManager.getInstance().logInWithReadPermissions(LogInActivity.this, Arrays.asList("public_profile", "email", "user_birthday"));
            }
        });

//        facebookLogInButton.setReadPermissions("public_profile", "email", "user_birthday");
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                String device = UserPreferences.getDeviceUuid();
                FacebookSignInRequest fbSigninRequest = new FacebookSignInRequest(loginResult.getAccessToken().getToken());
                RestClient.getAuthenticated().loginWithFacebook(device, fbSigninRequest).enqueue(new Callback<User>() {
                    @Override
                    public void onResponse(Call<User> call, Response<User> response) {
                        if (response.body() != null && response.isSuccessful()) {
                            moviePassLoginSucceeded(response.body());
                        } else if (response.errorBody() != null) {
                            progress.setVisibility(View.GONE);
                            Toast.makeText(LogInActivity.this, "Please check your credentials and try again.", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<User> call, Throwable t) {
                        progress.setVisibility(View.INVISIBLE);
                        Toast.makeText(LogInActivity.this, t.getMessage().toString(), Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onCancel() {
            }

            @Override
            public void onError(FacebookException exception) {
                Toast.makeText(LogInActivity.this, exception.toString(), Toast.LENGTH_SHORT).show();
            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    // Remove inter-activity transition to avoid screen tossing on tapping bottom navigation items
    @Override
    public void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }

    private void logIn() {
        String email = mInputEmail.getText().toString().replace(" ", "");
        String password = mInputPassword.getText().toString();
        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password) && isValidEmail(email)) {
            LogInRequest request = new LogInRequest(email, password);
            String deviceId = DeviceID.getID(this);
            RestClient.getAuthenticated().login(deviceId, request).enqueue(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, Response<User> response) {
                    if (response.body() != null && response.isSuccessful()) {
                        moviePassLoginSucceeded(response.body());
                    } else if (response.errorBody() != null) {
                        try {
                            JSONObject jObjError = new JSONObject(response.errorBody().string());
                            Toast.makeText(LogInActivity.this, jObjError.getString("message"), Toast.LENGTH_LONG).show();
                            progress.setVisibility(View.GONE);

                        } catch (Exception e) {
                            progress.setVisibility(View.GONE);

                            Toast.makeText(LogInActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                }

                @Override
                public void onFailure(Call<User> call, Throwable t) {
                    progress.setVisibility(View.GONE);
                    Toast.makeText(LogInActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
                }


            });
        } else {
            progress.setVisibility(View.GONE);
            Toast.makeText(LogInActivity.this, R.string.activity_sign_in_enter_valid_credentials, Toast.LENGTH_SHORT).show();
        }
    }

    public void checkRestrictions(User user) {
        RestClient.getAuthenticated().getRestrictions(user.getId()).enqueue(new Callback<RestrictionsResponse>() {
            @Override
            public void onResponse(Call<RestrictionsResponse> call, Response<RestrictionsResponse> response) {
                if (response.body() != null && response.isSuccessful()) {
                    restriction = response.body();

                    String status = restriction.getSubscriptionStatus();
                    boolean fbPresent = restriction.getFacebookPresent();
                    boolean threeDEnabled = restriction.get3dEnabled();
                    boolean allFormatsEnabled = restriction.getAllFormatsEnabled();
                    boolean proofOfPurchaseRequired = restriction.getProofOfPurchaseRequired();
                    boolean hasActiveCard = restriction.getHasActiveCard();
                    boolean subscriptionActivationRequired = restriction.isSubscriptionActivationRequired();

                    //Setting User Preferences When User Logs In
                    if (!UserPreferences.getRestrictionSubscriptionStatus().equals(status) ||
                            UserPreferences.getRestrictionFacebookPresent() != fbPresent ||
                            UserPreferences.getRestrictionThreeDEnabled() != threeDEnabled ||
                            UserPreferences.getRestrictionAllFormatsEnabled() != allFormatsEnabled ||
                            UserPreferences.getProofOfPurchaseRequired() != proofOfPurchaseRequired ||
                            UserPreferences.getRestrictionHasActiveCard() != hasActiveCard ||
                            UserPreferences.getIsSubscriptionActivationRequired() != subscriptionActivationRequired) {

                        UserPreferences.setRestrictions(status, fbPresent, threeDEnabled, allFormatsEnabled, proofOfPurchaseRequired, hasActiveCard, subscriptionActivationRequired);
                    }

                    //Checking restriction
                    //If Missing - Account is cancelled, User can't log in
                    if(restriction.getSubscriptionStatus().equalsIgnoreCase(Constants.MISSING)||restriction.getSubscriptionStatus().equalsIgnoreCase(Constants.CANCELLED)||
                            restriction.getSubscriptionStatus().equalsIgnoreCase(Constants.CANCELLED_PAST_DUE) || restriction.getSubscriptionStatus().equalsIgnoreCase(Constants.ENDED_FREE_TRIAL)){
                        Toast.makeText(LogInActivity.this, "You don't have an active subscription", Toast.LENGTH_SHORT).show();
                        UserPreferences.clearUserId();
                        progress.setVisibility(View.GONE);
                    } else {
//                        moviePassLoginSucceeded(user);
                        if (!UserPreferences.getHasUserLoggedInBefore()) {
                            UserPreferences.hasUserLoggedInBefore(true);
                            Intent i = new Intent(LogInActivity.this, ActivatedCard_TutorialActivity.class);
                            startActivity(i);
                        } else {
                             Intent i = new Intent(LogInActivity.this, MoviesActivity.class);
                            i.putExtra("launch", true);
                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(i);
                        }
//                        progress.setVisibility(View.GONE);
//                        finish();
                    }
                } else {
                    try {
                        progress.setVisibility(View.GONE);
                        JSONObject jObjError = new JSONObject(response.errorBody().string());
                        Log.d("LOG_IN RESTRICTIONS ", "onResponse: "+jObjError);
                    } catch (Exception e) {

                    }
                }
            }

            @Override
            public void onFailure(Call<RestrictionsResponse> call, Throwable t) {

            }
        });
    }

    private void forgotPassword() {
        AlertDialog.Builder alert = new AlertDialog.Builder(LogInActivity.this);

        View layout = View.inflate(LogInActivity.this, R.layout.dialog_email, null);
        final EditText email = layout.findViewById(R.id.editEmail);
        email.getBackground().mutate().setColorFilter(ContextCompat.getColor(this, R.color.red), PorterDuff.Mode.SRC_ATOP);

        alert.setView(layout);
        alert.setTitle(getString(R.string.activity_sign_in_forgot_password_title));
        alert.setMessage(getString(R.string.activity_sign_in_forgot_password_body));
        alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                String emailAddress = email.getText().toString().replace(" ", "");

                if (isValidEmail(emailAddress)) {
                    RestClient.getAuthenticated().forgotPassword(emailAddress).enqueue(new Callback<Object>() {
                        @Override
                        public void onResponse(Call<Object> call, Response<Object> response) {
                            Toast.makeText(LogInActivity.this, "If the email is registered with MoviePass, you will receive an email with instructions about how to reset your password.", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(Call<Object> call, Throwable t) {
                            Toast.makeText(LogInActivity.this, "Something went wrong.", Toast.LENGTH_SHORT).show();
                        }
                    });
                    dialog.dismiss();
                } else {
                    Toast.makeText(LogInActivity.this, R.string.activity_sign_in_enter_valid_email, Toast.LENGTH_SHORT).show();
                }
            }
        });
        alert.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = alert.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(email, InputMethodManager.SHOW_IMPLICIT);
            }
        });

        dialog.show();
        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
    }

    public static boolean isValidEmail(CharSequence target) {
        if (target == null) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }

    private void moviePassLoginSucceeded(User user) {
        if (user != null) {

            int us = user.getId();
            String deviceUuid = user.getDeviceUuid();
            String authToken = user.getAuthToken();

            UserPreferences.setUserCredentials(us, deviceUuid, authToken, user.getFirstName(), user.getEmail());
            checkRestrictions(user);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        // do nothing. We want to force user to stay in this activity and not drop out.
        android.support.v7.app.AlertDialog alert;
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(LogInActivity.this, R.style.AlertDialogCustom);
        builder.setMessage("Do you want to quit MoviePass?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finishAffinity(); // finish activity

            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alert = builder.create();
        alert.show();
    }
}
