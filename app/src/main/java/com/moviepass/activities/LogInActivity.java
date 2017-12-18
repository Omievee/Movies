package com.moviepass.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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
import com.moviepass.DeviceID;
import com.moviepass.R;
import com.moviepass.UserPreferences;
import com.moviepass.helpers.BottomNavigationViewHelper;
import com.moviepass.model.User;
import com.moviepass.network.RestClient;
import com.moviepass.requests.FacebookSignInRequest;
import com.moviepass.requests.LogInRequest;

import org.json.JSONObject;

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

    CallbackManager callbackManager;
    LoginButton loginButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        FacebookSdk.sdkInitialize(this);
        callbackManager = CallbackManager.Factory.create();

        mInputEmail = findViewById(R.id.input_email);
        mInputPassword = findViewById(R.id.input_password);
        progress = findViewById(R.id.progress);
        loginButton = findViewById(R.id.button_facebook_log_in);

        mButtonLogIn = findViewById(R.id.button_log_in);
        mButtonLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logIn();
            }
        });

        mSignUp = findViewById(R.id.sign_up);
        mSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LogInActivity.this, SignUpFirstOpenActivity.class);
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

        loginButton.setReadPermissions("public_profile", "email", "user_birthday");

        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {

                        Log.d("loginResult", loginResult.getAccessToken().getToken());

                        String device = UserPreferences.getDeviceUuid();

                        FacebookSignInRequest fbSigninRequest = new FacebookSignInRequest(loginResult.getAccessToken().getToken());

                        RestClient.getAuthenticated().loginWithFacebook(device, fbSigninRequest).enqueue(new Callback<User>() {
                            @Override
                            public void onResponse(Call<User> call, Response<User> response) {
                                if (response.body() != null && response.isSuccessful()) {
                                    moviePassLoginSucceeded(response.body());
                                } else if (response.errorBody() != null) {
                                    Toast.makeText(LogInActivity.this, "Please check your credentials and try again.", Toast.LENGTH_LONG).show();
                                    progress.setVisibility(View.GONE);
//                                    toggleControls(true);
                                }
                            }

                            @Override
                            public void onFailure(Call<User> call, Throwable t) {
                                progress.setVisibility(View.INVISIBLE);
//                                toggleControls(true);
                                Toast.makeText(LogInActivity.this, t.getMessage().toString(), Toast.LENGTH_LONG).show();
                            }
                        });
                    }

                    @Override
                    public void onCancel() {
                        // App code
                        Toast.makeText(LogInActivity.this, "cancel", Toast.LENGTH_SHORT).show();
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
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mInputEmail, InputMethodManager.SHOW_IMPLICIT);
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

//                            mProgress.setVisibility(View.GONE);
//                            toggleControls(true);
                            Toast.makeText(LogInActivity.this, jObjError.getString("message"), Toast.LENGTH_LONG).show();

                        } catch (Exception e) {
                            Toast.makeText(LogInActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                }

                @Override
                public void onFailure(Call<User> call, Throwable t) {
//                    mProgress.setVisibility(View.INVISIBLE);
//                    toggleControls(true);
                    Toast.makeText(LogInActivity.this, "No MoviePass User Found with this Facebook Account.", Toast.LENGTH_LONG).show();

                }


            });
        } else {
            Toast.makeText(LogInActivity.this, R.string.activity_sign_in_enter_valid_credentials, Toast.LENGTH_SHORT).show();
        }
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

            int userId = user.getId();
            String deviceUuid = user.getDeviceUuid();
            String authToken = user.getAuthToken();

            UserPreferences.setUserCredentials(userId, deviceUuid, authToken, user.getFirstName(), user.getEmail());

            Intent i = new Intent(LogInActivity.this, MoviesActivity.class);
            i.putExtra("launch", true);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);

            if (UserPreferences.getHasUserLoggedInBefore()) {
                UserPreferences.hasUserLoggedInBefore(true);
            }

//            mProgress.setVisibility(View.INVISIBLE);
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
}
