package com.mobile.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jaredrummler.materialspinner.MaterialSpinner;
import com.mobile.Constants;
import com.mobile.model.ProspectUser;
import com.mobile.network.RestClient;
import com.mobile.requests.CredentialsRequest;
import com.moviepass.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by anubis on 6/15/17.
 */

public class SignUpFirstOpenActivity extends AppCompatActivity {
    MaterialSpinner spinnerGender;
    RelativeLayout relativeLayout;
    View progress;
    static final int DATE_DIALOG_ID = 0;
    Button signupNowButton;
    TextView seeMap;
    EditText DOB;
    int month, year, day;
    Calendar myCalendar;
    EditText signupEmailInput, signupEmailConfirm, signupPasswordInput;
    TextInputLayout emailTextInputLayout, email2TextInputLayout, passwordTextInputLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_signup_first_open);

        relativeLayout = findViewById(R.id.relative_layout);
        signupEmailInput = findViewById(R.id.SIGNUP_EMAIL);
        signupPasswordInput = findViewById(R.id.SIGNUP_PASSSWORD);
        signupNowButton = findViewById(R.id.SIGNUP_BUTTON);
        progress = findViewById(R.id.progress);
        DOB = findViewById(R.id.DOB);
        signupEmailConfirm = findViewById(R.id.SIGNUP_EMAIL_confirm);
        spinnerGender = findViewById(R.id.SPINNER);

        signupEmailConfirm.clearFocus();
        signupEmailInput.clearFocus();
        signupPasswordInput.clearFocus();

        emailTextInputLayout = findViewById(R.id.emailTextInputLayout);
        email2TextInputLayout = findViewById(R.id.email2TextInputLayout);
        passwordTextInputLayout = findViewById(R.id.passwordTextInputLayout);

        spinnerGender.setItems("Gender", "Male", "Female", "Other");

        myCalendar = Calendar.getInstance();
        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, month);
                myCalendar.set(Calendar.DAY_OF_MONTH, day);
                updateLabel();
            }
        };

        DOB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(SignUpFirstOpenActivity.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        spinnerGender.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, Object item) {
                signupEmailConfirm.clearFocus();
                signupEmailInput.clearFocus();
                signupPasswordInput.clearFocus();
            }
        });


        signupNowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signupPasswordInput.clearFocus();
                signupEmailInput.clearFocus();
                signupEmailConfirm.clearFocus();
                Log.d(Constants.TAG, "onClick: " + DOB.getText().toString());
                if (!signupEmailConfirm.getText().toString().trim().isEmpty() && !signupEmailInput.getText().toString().trim().isEmpty() && !signupPasswordInput.getText().toString().trim().isEmpty()) {
                    if (!signupEmailInput.getText().toString().trim().equals(signupEmailConfirm.getText().toString().trim())) {
                        Toast.makeText(view.getContext(), "Emails do not match", Toast.LENGTH_SHORT).show();
                    } else if (DOB.getText().toString().equals("") || spinnerGender.getText().toString().equals("Gender")) {
                        Toast.makeText(SignUpFirstOpenActivity.this, "All fields are required", Toast.LENGTH_SHORT).show();
                    } else {
                        progress.setVisibility(View.VISIBLE);
                        final String email1 = signupEmailInput.getText().toString().trim();
                        final String password = signupPasswordInput.getText().toString().trim();
                        final String gender = spinnerGender.getText().toString().trim();
                        final String birthday = DOB.getText().toString().trim();
                        if (isValidEmail(email1) && isValidPassword(password)) {
                            final CredentialsRequest request = new CredentialsRequest(email1);
                            RestClient.getsAuthenticatedRegistrationAPI().registerCredentials(request).enqueue(new Callback<Object>() {
                                @Override
                                public void onResponse(Call<Object> call, Response<Object> response) {
                                    progress.setVisibility(View.GONE);
                                    if (response != null && response.isSuccessful()) {
                                        if (response.body().toString().contains(" userExists=1.0")) {
                                            Toast.makeText(SignUpFirstOpenActivity.this, "User already exists", Toast.LENGTH_SHORT).show();
                                        } else {
                                            ProspectUser.email = email1;
                                            ProspectUser.password = password;
                                            ProspectUser.gender = gender;
                                            ProspectUser.dateOfBirth = birthday;

                                            Intent intent = new Intent(SignUpFirstOpenActivity.this, SignUpActivity.class);
                                            intent.putExtra("email1", email1);
                                            intent.putExtra("password", password);
                                            intent.putExtra("gender", gender);
                                            intent.putExtra("dateOfBirth", birthday);
                                            startActivity(intent);
                                        }

                                    }
                                    else {
                                        Toast.makeText(SignUpFirstOpenActivity.this, "Server Error, Try again later.", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onFailure(Call<Object> call, Throwable t) {
                            /* TODO : Handle failure situation */
                                }
                            });
                        } else if (!isValidEmail(email1)) {
                            emailTextInputLayout.setError("Invalid Email Address");
                            signupEmailInput.clearFocus();
//                            Snackbar snackbar = Snackbar.make(relativeLayout, "Please enter a valid email1 address", Snackbar.LENGTH_INDEFINITE);
//                            snackbar.setAction("OK", new View.OnClickListener() {
//                                @Override
//                                public void onClick(View view) {
//                                    progress.setVisibility(View.GONE);
//                                }
//                            });
//
//                            // Changing message text color
//                            snackbar.setActionTextColor(ContextCompat.getColor(SignUpFirstOpenActivity.this, R.color.red));
//                            snackbar.show();
                        } else if (!isValidPassword(password)) {
                            passwordTextInputLayout.setError("Invalid password");
                            signupPasswordInput.clearFocus();
//                            if (password.length() < 4) {
//                                Snackbar snackbar = Snackbar.make(relativeLayout, "Please create a password longer than four characters", Snackbar.LENGTH_INDEFINITE);
//                                snackbar.setAction("OK", new View.OnClickListener() {
//                                    @Override
//                                    public void onClick(View view) {
//                                        progress.setVisibility(View.GONE);
//                                    }
//                                });
//                                // Changing message text color
//                                snackbar.setActionTextColor(ContextCompat.getColor(SignUpFirstOpenActivity.this, R.color.red));
//                                snackbar.show();
                            } else if (password.length() > 20) {
                                passwordTextInputLayout.setError("Invalid password");
                                signupPasswordInput.clearFocus();
//                                Snackbar snackbar = Snackbar.make(relativeLayout, "Please create password shorter than twenty characters", Snackbar.LENGTH_INDEFINITE);
//                                snackbar.setAction("OK", new View.OnClickListener() {
//                                    @Override
//                                    public void onClick(View view) {
//                                        progress.setVisibility(View.GONE);
//                                    }
//                                });
//                                // Changing message text color
//                                snackbar.setActionTextColor(ContextCompat.getColor(SignUpFirstOpenActivity.this, R.color.red));
//                                snackbar.show();
                            } else if (password.contains(" ")) {
                                passwordTextInputLayout.setError("Invalid password");
                                signupPasswordInput.clearFocus();
//                                Snackbar snackbar = Snackbar.make(relativeLayout, "Please create password without spaces", Snackbar.LENGTH_INDEFINITE);
//                                snackbar.setAction("OK", new View.OnClickListener() {
//                                    @Override
//                                    public void onClick(View view) {
//                                        progress.setVisibility(View.GONE);
//                                    }
//                                });
//                                // Changing message text color
//                                snackbar.setActionTextColor(ContextCompat.getColor(SignUpFirstOpenActivity.this, R.color.red));
//                                snackbar.show();
                            } else {
                                passwordTextInputLayout.setError("Invalid password");
                                signupPasswordInput.clearFocus();
//                                Snackbar snackbar = Snackbar.make(relativeLayout, "Please enter a valid password", Snackbar.LENGTH_INDEFINITE);
//                                snackbar.setAction("OK", new View.OnClickListener() {
//                                    @Override
//                                    public void onClick(View view) {
//                                        progress.setVisibility(View.GONE);
//                                    }
//                                });
//                                // Changing message text color
//                                snackbar.setActionTextColor(ContextCompat.getColor(SignUpFirstOpenActivity.this, R.color.red));
//                                snackbar.show();
                            }
                        }
                    } else {
                    if(signupPasswordInput.getText().toString().trim().isEmpty()){
                        passwordTextInputLayout.setError("Required");
                        signupPasswordInput.clearFocus();
                    }
                    if(signupEmailInput.getText().toString().trim().isEmpty()){
                        emailTextInputLayout.setError("Required");
                        signupEmailInput.clearFocus();
                    }
                    if(signupEmailConfirm.getText().toString().trim().isEmpty()){
                        email2TextInputLayout.setError("Required");
                        signupEmailConfirm.clearFocus();
                    }
                }
            }
        });
    }





        /* seeMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(SignUpFirstOpenActivity.this, ViewTheatersActivity.class);
                startActivity(intent);
            }
        }); */


    private void updateLabel() {
        String myFormat = "MM/dd/yy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        if (myCalendar.get(Calendar.YEAR) <= 2000) {
            DOB.setText(sdf.format(myCalendar.getTime()));
        } else {
            DOB.setText("");
            Toast.makeText(this, "Must be 18 years of age or older", Toast.LENGTH_SHORT).show();
        }
    }

    public static boolean isValidEmail(CharSequence target) {
        if (target == null) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }

    public static boolean isValidPassword(CharSequence target) {
        if (target == null || target.length() < 4 || target.length() > 20) {
            return false;
        } else {
            return true;
        }
    }

    /*
    @TargetApi(21)
    public void openAnimation() {
        if (Build.VERSION.SDK_INT >= 21) {
            mRedView = findViewById(R.id.red);

            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    // previously visible view
                    // get the center for the clipping circle
                    int cx = mRedView.getWidth() / 2;
                    int cy = mRedView.getHeight() / 2;

                    Log.d("cx", String.valueOf(cx));

                    // get the initial radius for the clipping circle
                    float initialRadius = (float) Math.hypot(cx, cy);

                    // create the animation (the final radius is zero)
                    Animator anim = ViewAnimationUtils.createCircularReveal(mRedView, cx, cy, initialRadius, 0);

                    // make the view invisible when the animation is done
                    anim.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            mRedView.setVisibility(View.INVISIBLE);
                        }
                    });

                    anim.start();
                }

            }, 100);
        }
    } */

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent i = new Intent(this, OnboardingActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        finish();
    }


}
