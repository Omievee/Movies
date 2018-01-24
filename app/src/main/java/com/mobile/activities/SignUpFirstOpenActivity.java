package com.mobile.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mobile.model.ProspectUser;
import com.mobile.network.RestClient;
import com.mobile.requests.CredentialsRequest;
import com.moviepass.R;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by anubis on 6/15/17.
 */

public class SignUpFirstOpenActivity extends AppCompatActivity {

    RelativeLayout relativeLayout;
    View redView;
    View progress;

    Button signupNowButton;
    Button buttonSignUpFacebook;
    TextView seeMap;

    EditText signupEmailInput;
    android.support.design.widget.TextInputEditText signupPasswordInput;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_signup_first_open);

        relativeLayout = findViewById(R.id.relative_layout);
        signupEmailInput = findViewById(R.id.SIGNUP_EMAIL);
        signupPasswordInput = findViewById(R.id.SIGNUP_PASSSWORD);
        signupNowButton = findViewById(R.id.SIGNUP_BUTTON);
        progress = findViewById(R.id.progress);

    

        signupNowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progress.setVisibility(View.VISIBLE);

                final String email = signupEmailInput.getText().toString().trim();
                final String password = signupPasswordInput.getText().toString().trim();


                if (isValidEmail(email) && isValidPassword(password)) {
                    /* TODO : animate this */

                    CredentialsRequest request = new CredentialsRequest(email, password, password);
                    Log.d("request", email + " " + password);

                    RestClient.getUnauthenticated().registerCredentials(request).enqueue(new Callback<Object>() {
                        @Override
                        public void onResponse(Call<Object> call, Response<Object> response) {
                            progress.setVisibility(View.GONE);

                            if (response != null && response.isSuccessful()) {
                                ProspectUser.email = email;
                                ProspectUser.password = password;

                                Intent intent = new Intent(SignUpFirstOpenActivity.this, SignUpActivity.class);
                                intent.putExtra("email", email);
                                intent.putExtra("password", password);
                                startActivity(intent);
                            }
                        }

                        @Override
                        public void onFailure(Call<Object> call, Throwable t) {
                            /* TODO : Handle failure situation */
                        }
                    });
                } else if (!isValidEmail(email)) {
                    Snackbar snackbar = Snackbar.make(relativeLayout, "Please enter a valid email address", Snackbar.LENGTH_INDEFINITE);
                    snackbar.setAction("OK", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            progress.setVisibility(View.GONE);
                        }
                    });

                    // Changing message text color
                    snackbar.setActionTextColor(ContextCompat.getColor(SignUpFirstOpenActivity.this, R.color.red));
                    snackbar.show();
                } else if (!isValidPassword(password)) {
                    if (password.length() < 4) {
                        Snackbar snackbar = Snackbar.make(relativeLayout, "Please create a password longer than four characters", Snackbar.LENGTH_INDEFINITE);
                        snackbar.setAction("OK", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                progress.setVisibility(View.GONE);
                            }
                        });
                        // Changing message text color
                        snackbar.setActionTextColor(ContextCompat.getColor(SignUpFirstOpenActivity.this, R.color.red));
                        snackbar.show();
                    } else if (password.length() > 20) {
                        Snackbar snackbar = Snackbar.make(relativeLayout, "Please create password shorter than twenty characters", Snackbar.LENGTH_INDEFINITE);
                        snackbar.setAction("OK", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                progress.setVisibility(View.GONE);
                            }
                        });
                        // Changing message text color
                        snackbar.setActionTextColor(ContextCompat.getColor(SignUpFirstOpenActivity.this, R.color.red));
                        snackbar.show();
                    } else if (password.contains(" ")) {
                        Snackbar snackbar = Snackbar.make(relativeLayout, "Please create password without spaces", Snackbar.LENGTH_INDEFINITE);
                        snackbar.setAction("OK", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                progress.setVisibility(View.GONE);
                            }
                        });
                        // Changing message text color
                        snackbar.setActionTextColor(ContextCompat.getColor(SignUpFirstOpenActivity.this, R.color.red));
                        snackbar.show();
                    } else {
                        Snackbar snackbar = Snackbar.make(relativeLayout, "Please enter a valid password", Snackbar.LENGTH_INDEFINITE);
                        snackbar.setAction("OK", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                progress.setVisibility(View.GONE);
                            }
                        });
                        // Changing message text color
                        snackbar.setActionTextColor(ContextCompat.getColor(SignUpFirstOpenActivity.this, R.color.red));
                        snackbar.show();
                    }
                }
            }
        });

        /* seeMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(SignUpFirstOpenActivity.this, ViewTheatersActivity.class);
                startActivity(intent);
            }
        }); */
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