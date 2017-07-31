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
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.moviepass.DeviceID;
import com.moviepass.R;
import com.moviepass.UserPreferences;
import com.moviepass.helpers.BottomNavigationViewHelper;
import com.moviepass.model.User;
import com.moviepass.network.RestClient;
import com.moviepass.requests.LogInRequest;

import org.json.JSONObject;

import butterknife.BindView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by anubis on 4/27/17.
 */

public class LogInActivity extends BaseActivity {

    protected BottomNavigationView bottomNavigationView;

    @BindView(R.id.input_email)
    EditText mInputEmail;
    @BindView(R.id.input_password)
    EditText mInputPassword;
    @BindView(R.id.button_log_in)
    Button mButtonLogIn;
    @BindView(R.id.forgot_password)
    TextView mForgotPassword;
    @BindView(R.id.sign_up)
    TextView mSignUp;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        mInputEmail = findViewById(R.id.input_email);
        mInputPassword = findViewById(R.id.input_password);

        bottomNavigationView = findViewById(R.id.navigation);
        BottomNavigationViewHelper.disableShiftMode(bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);

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
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateNavigationBarState();
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
                            Toast.makeText(LogInActivity.this, "You will receive an email with instructions on how to reset your password.", Toast.LENGTH_SHORT).show();
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

            Intent i = new Intent(LogInActivity.this, BrowseActivity.class);
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

    int getContentViewId() {
        return R.layout.activity_profile;
    }

    int getNavigationMenuItemId() {
        return R.id.action_profile;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull final MenuItem item) {
        bottomNavigationView.postDelayed(new Runnable() {
            @Override
            public void run() {
                int itemId = item.getItemId();
                if (itemId == R.id.action_profile) {
                } else if (itemId == R.id.action_reservations) {
                    Toast.makeText(LogInActivity.this, "E-Ticket Activity", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(getApplicationContext(), ReservationsActivity.class));
                } else if (itemId == R.id.action_browse) {
                    startActivity(new Intent(getApplicationContext(), BrowseActivity.class));
                } else if (itemId == R.id.action_notifications) {
                    Toast.makeText(LogInActivity.this, "Notification Activity", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(getApplicationContext(), NotificationsActivity.class));
                } else if (itemId == R.id.action_settings) {
                    Toast.makeText(LogInActivity.this, "Settings Activity", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                }
                finish();
            }
        }, 300);
        return true;
    }

    private void updateNavigationBarState(){
        int actionId = getNavigationMenuItemId();
        selectBottomNavigationBarItem(actionId);
    }

    void selectBottomNavigationBarItem(int itemId) {
        Menu menu = bottomNavigationView.getMenu();
        for (int i = 0, size = menu.size(); i < size; i++) {
            MenuItem item = menu.getItem(i);
            boolean shouldBeChecked = item.getItemId() == itemId;
            if (shouldBeChecked) {
                item.setChecked(true);
                break;
            }
        }
    }
}
