package com.moviepass.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.util.Log;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.moviepass.R;

/**
 * Created by anubis on 6/15/17.
 */

public class SignUpFirstOpenActivity extends AppCompatActivity {

    RelativeLayout mRelativeLayout;
    View mRedView;

    Button mButonSignUp;
    Button mButtonSignUpFacebook;
    TextView mNotReady;

    EditText mInputEmail;
    EditText mInputPassword;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_first_open);

        mRedView = findViewById(R.id.red);
        mRelativeLayout = findViewById(R.id.relative_layout);
        mInputEmail = findViewById(R.id.input_email);
        mInputPassword = findViewById(R.id.input_password);
        mButonSignUp = findViewById(R.id.button_sign_up);
        mNotReady = findViewById(R.id.not_ready);

        openAnimation();

        mButonSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = mInputEmail.getText().toString().trim();
                String password = mInputPassword.getText().toString().trim();

                if (isValidEmail(email) && isValidPassword(password)) {
                    /* TODO : animate this */
                    Intent intent = new Intent(SignUpFirstOpenActivity.this, SignUpActivity.class);
                    intent.putExtra("email", email);
                    intent.putExtra("password", password);
                    startActivity(intent);
                } else if (!isValidEmail(email)) {
                    Snackbar snackbar = Snackbar.make(mRelativeLayout, "Please enter a valid email address", Snackbar.LENGTH_INDEFINITE);
                    snackbar.setAction("OK", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                        }
                    });

                    // Changing message text color
                    snackbar.setActionTextColor(ContextCompat.getColor(SignUpFirstOpenActivity.this, R.color.red));
                    snackbar.show();
                } else if (!isValidPassword(password)) {
                    Snackbar snackbar = Snackbar.make(mRelativeLayout, "Please enter a valid password", Snackbar.LENGTH_INDEFINITE);
                    snackbar.setAction("OK", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                        }
                    });

                    // Changing message text color
                    snackbar.setActionTextColor(ContextCompat.getColor(SignUpFirstOpenActivity.this, R.color.red));
                    snackbar.show();
                }
            }
        });

        mNotReady.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignUpFirstOpenActivity.this, BrowseActivity.class);
                startActivity(intent);
            }
        });
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
            return  true;
        }
    }

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
    }
}
