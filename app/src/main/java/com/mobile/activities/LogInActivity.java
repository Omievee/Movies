package com.mobile.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.gson.GsonBuilder;
import com.helpshift.support.Log;
import com.mobile.Constants;
import com.mobile.DeviceID;
import com.mobile.UserPreferences;
import com.mobile.fragments.ReactivateDialog;
import com.mobile.fragments.WebViewFragment;
import com.mobile.fragments.WebViewListener;
import com.mobile.helpers.LogUtils;
import com.mobile.home.HomeActivity;
import com.mobile.model.User;
import com.mobile.network.RestClient;
import com.mobile.requests.FacebookSignInRequest;
import com.mobile.requests.LogInRequest;
import com.mobile.responses.AndroidIDVerificationResponse;
import com.mobile.responses.MicroServiceRestrictionsResponse;
import com.mobile.responses.RestrictionsResponse;
import com.mobile.responses.SubscriptionStatus;
import com.moviepass.BuildConfig;
import com.moviepass.R;

import org.json.JSONObject;

import java.util.Arrays;

import butterknife.BindView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.mobile.UserPreferences.*;

/**
 * Created by anubis on 4/27/17.
 */

public class LogInActivity extends AppCompatActivity implements WebViewListener {

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
    MicroServiceRestrictionsResponse restriction;
    User userRESPONSE;
    private AndroidIDVerificationResponse androidId;

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
                //Changing - Removing Sign Up
//                Intent intent = new Intent(LogInActivity.this, OnboardingActivity.class);
//                startActivity(intent);

                FragmentManager manager = getSupportFragmentManager();
                FragmentTransaction transaction = manager.beginTransaction();
                transaction.setCustomAnimations(R.animator.enter_from_right, R.animator.exit_to_left, R.animator.enter_from_left, R.animator.exit_to_right);
                WebViewFragment web = new WebViewFragment();
                transaction.replace(R.id.fragmentContainer, web);
                transaction.addToBackStack("");
                transaction.commit();
            }
        });

//        mSignUp.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                UserPreferences.setOneDeviceId("");
//            }
//        });

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

        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                String device = INSTANCE.getDeviceUuid();
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
    public boolean onKeyDown(int keyCode, KeyEvent event) {

//        FragmentManager fragmentManager = getSupportFragmentManager();
//        LogUtils.newLog("COUNT: "+fragmentManager.getBackStackEntryCount());
//        WebViewFragment fragment = (WebViewFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
//        if (keyCode == KeyEvent.KEYCODE_BACK && fragmentManager.getBackStackEntryCount()>=1 && fragment.canGoBack()) {
//            fragment.goBack();
//            return true;
//        }
        return super.onKeyDown(keyCode, event);
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
        String deviceId = DeviceID.getID(this);
        String deviceType = Build.MODEL;
        String device = "ANDROID";

        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password) && isValidEmail(email)) {
            LogInRequest request = new LogInRequest(email, password, deviceId, deviceType, device);
            LogUtils.newLog(Constants.TAG, "logIn: " + deviceId);
            String UUID = "flag";
            RestClient.getAuthenticated().login(UUID, request).enqueue(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, Response<User> response) {
                    userRESPONSE = response.body();
                    if (response.code() == 200) {
                        moviePassLoginSucceeded(response.body());
                    } else if (response.code() == 207) {
                        AlertDialog.Builder alert = new AlertDialog.Builder(LogInActivity.this, R.style.CUSTOM_ALERT);
                        alert.setTitle("We’ve noticed you switched to a different device");
                        alert.setMessage("Switching to a new device will permanently lock you out from any other device for 30 days.");
                        alert.setCancelable(false);
                        alert.setPositiveButton("Switch to this device", (dialog, which) -> {
                            dialog.dismiss();
                            progress.setVisibility(View.GONE);
                            AlertDialog.Builder areYouSure = new AlertDialog.Builder(LogInActivity.this, R.style.CUSTOM_ALERT);

                            areYouSure.setTitle("Are you sure?");
                            areYouSure.setMessage("This cannot be undone.");
                            areYouSure.setPositiveButton("Switch to this device", (d, w) -> {
                                d.dismiss();
                                String userSwitchDeviceID = DeviceID.getID(getApplicationContext());
                                INSTANCE.setHeaders(userRESPONSE.getAuthToken(), userRESPONSE.getId());
                                verifyAndroidID(deviceType, userSwitchDeviceID, device, true);
                            });

                            areYouSure.setNegativeButton(android.R.string.cancel, (d, wi) -> {
                                d.dismiss();
                                d.cancel();
                                progress.setVisibility(View.GONE);
                            });
                            areYouSure.show();
                        });


                        alert.setNegativeButton(android.R.string.cancel, (dialog, which) -> {
                            dialog.cancel();
                            progress.setVisibility(View.GONE);

                        });

                        alert.show();

                    } else if (response.errorBody() != null) {
                        try {
                            JSONObject jObjError = new JSONObject(response.errorBody().string());
                            Toast.makeText(LogInActivity.this, jObjError.getString("message"), Toast.LENGTH_LONG).show();
                            LogUtils.newLog(Constants.TAG, "onResponse: " + jObjError.getString("message"));
                            progress.setVisibility(View.GONE);

                        } catch (Exception e) {
                            progress.setVisibility(View.GONE);

                            Toast.makeText(LogInActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                            LogUtils.newLog(Constants.TAG, "onResponse: " + e.getMessage());
                        }
                    }
                }

                @Override
                public void onFailure(Call<User> call, Throwable t) {
                    progress.setVisibility(View.GONE);
                    Toast.makeText(LogInActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
                    LogUtils.newLog(Constants.TAG, "failure: " + t.getMessage());
                }
            });
        } else {
            progress.setVisibility(View.GONE);
            Toast.makeText(LogInActivity.this, R.string.activity_sign_in_enter_valid_credentials, Toast.LENGTH_SHORT).show();
        }
    }

    private void verifyAndroidID(String deviceType, String deviceId, String device, boolean updateDevice) {

        AndroidIDVerificationResponse request = new AndroidIDVerificationResponse(device, deviceId, deviceType, updateDevice);
        String user_id = String.valueOf(userRESPONSE.getId());


        RestClient.getAuthenticated().verifyAndroidID(user_id, request).enqueue(new Callback<AndroidIDVerificationResponse>() {
            @Override
            public void onResponse(Call<AndroidIDVerificationResponse> call, Response<AndroidIDVerificationResponse> response) {
                if (response.code() == 200 || response.code() == 201) {
                    androidId = response.body();
                    moviePassLoginSucceeded(userRESPONSE);
                    INSTANCE.setOneDeviceId(androidId.getOneDeviceId());

                } else if (response.code() == 403) {
                    //TODO: ADD MESSAGE
                    progress.setVisibility(View.GONE);
                    Toast.makeText(LogInActivity.this, response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AndroidIDVerificationResponse> call, Throwable t) {
                progress.setVisibility(View.GONE);
                LogUtils.newLog(Constants.TAG, "onFailure: " + t.getMessage());
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

            LogUtils.newLog(Constants.TAG, "moviePassLoginSucceeded: ");
            int us = user.getId();
            String deviceUuid = user.getAndroidID();
            String authToken = user.getAuthToken();
            String ODID = user.getOneDeviceId();
            LogUtils.newLog(Constants.TAG, "moviePassLoginSucceeded: ONE DEVICE ID FROM LOG IN: "+ODID);

            INSTANCE.setUserCredentials(us, deviceUuid, authToken, user.getFirstName(), user.getEmail(), ODID);
            checkRestrictions(user);
        }
    }


    public void checkRestrictions(User user) {

        RestClient.getsAuthenticatedMicroServiceAPI().getInterstitialAlert(user.getId()).enqueue(new Callback<MicroServiceRestrictionsResponse>() {
            @Override
            public void onResponse(Call<MicroServiceRestrictionsResponse> call, Response<MicroServiceRestrictionsResponse> response) {
                if (response.body() != null && response.isSuccessful()) {
                    restriction = response.body();

                    INSTANCE.setRestrictions(restriction);

                    //Checking restriction
                    //If Missing - Account is cancelled, User can't log in
                    if (restriction.getSubscriptionStatus().equals(SubscriptionStatus.MISSING)
                            || restriction.getSubscriptionStatus().equals(SubscriptionStatus.CANCELLED) ||
                            restriction.getSubscriptionStatus().equals(SubscriptionStatus.CANCELLED_PAST_DUE) || restriction.getSubscriptionStatus().equals(SubscriptionStatus.ENDED_FREE_TRIAL)) {
                        progress.setVisibility(View.GONE);
                        hideKeyboard();
                        if(restriction.getCanReactivate().getCancelledWithinTimeframe()){
                            reactivationDialog();
                        } else {
                            INSTANCE.clearUserId();
                            Toast.makeText(LogInActivity.this, "You don't have an active subscription", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Crashlytics.setUserIdentifier(String.valueOf(INSTANCE.getUserId()));
                        if (!INSTANCE.getHasUserLoggedInBefore()) {
                            INSTANCE.hasUserLoggedInBefore(true);
                            Intent i = new Intent(LogInActivity.this, ActivatedCard_TutorialActivity.class);
                            startActivity(i);
                        } else {
                            Intent i = new Intent(LogInActivity.this, HomeActivity.class);
                            i.putExtra("launch", true);
                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(i);
                        }
//                        progress.setVisibility(View.GONE);
//                        finish();
                        progress.setVisibility(View.GONE);
                    }
                } else {
                    try {
                        progress.setVisibility(View.GONE);
                        JSONObject jObjError = new JSONObject(response.errorBody().string());
                        Toast.makeText(LogInActivity.this, jObjError.getString("message"), Toast.LENGTH_LONG).show();
                        LogUtils.newLog("LOG_IN RESTRICTIONS ", "onResponse: " + jObjError);
                        INSTANCE.clearUserId();
                    } catch (Exception e) {

                    }
                }
            }

            @Override
            public void onFailure(Call<MicroServiceRestrictionsResponse> call, Throwable t) {
                progress.setVisibility(View.GONE);
                t.printStackTrace();
            }
        });
    }

    public void reactivationDialog(){
        ReactivateDialog.newInstance("","").show(getSupportFragmentManager(),"reactivation");
    }

    public void openWebVIew(){
        progress.setVisibility(View.VISIBLE);
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.setCustomAnimations(R.animator.enter_from_right, R.animator.exit_to_left, R.animator.enter_from_left, R.animator.exit_to_right);
        WebViewFragment web = WebViewFragment.Companion.newInstance(BuildConfig.REACTIVATION_URL);
        transaction.replace(R.id.fragmentContainer, web);
        transaction.addToBackStack("");
        transaction.commit();

        progress.setVisibility(View.GONE);
    }

    public void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = this.getCurrentFocus();
        if (view == null) {
            view = new View(this);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        // do nothing. We want to force user to stay in this activity and not drop out.
        FragmentManager fragmentManager = getSupportFragmentManager();
        LogUtils.newLog("COUNT: "+fragmentManager.getBackStackEntryCount());
        WebViewFragment fragment = (WebViewFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
        if (fragmentManager.getBackStackEntryCount()>=1) {
            if(fragment.canGoBack())
                fragment.goBack();
            else
                fragmentManager.popBackStack();
        } else {
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

    @Override
    public void onDoneWithWebview() {
        Intent i = new Intent(LogInActivity.this, HomeActivity.class);
        i.putExtra("launch", true);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
    }
}
