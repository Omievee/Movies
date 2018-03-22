package com.mobile.activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mobile.Constants;
import com.mobile.UserPreferences;
import com.mobile.fragments.TicketVerificationDialog;
import com.mobile.model.Screening;
import com.mobile.network.RestClient;
import com.mobile.requests.CardActivationRequest;
import com.mobile.responses.CardActivationResponse;
import com.mobile.responses.RestrictionsResponse;
import com.moviepass.R;

import org.json.JSONObject;
import org.parceler.Parcels;

import io.card.payment.CardIOActivity;
import io.card.payment.CreditCard;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivateMoviePassCard extends AppCompatActivity {

    View progress;
    TextView activateInstructions, activateManualInput, activateSubmitButton;
    EditText activateDigits;
    ImageView activateScanCardIcon, activateXOut;
    String digits;
    Screening screeningObject;
    String selectedShowTime;
    private final static int REQUEST_CAMERA_CODE = 0;
    private static String CAMERA_PERMISSIONS[] = new String[]{
            Manifest.permission.CAMERA
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_activate_movie_pass_card);


        activateInstructions = findViewById(R.id.ACTIVATECARD_INTRUCTIONS);
        activateManualInput = findViewById(R.id.ACTIVATECARD_MANULINPUT);
        activateDigits = findViewById(R.id.ACTIVATE_DIGITS);
        activateSubmitButton = findViewById(R.id.ACTIVATE_BUTTON);
        activateScanCardIcon = findViewById(R.id.ACTIVATECARD_SCAN_ICON);
        activateXOut = findViewById(R.id.ACTIVATECARD_X_OUT);
        progress = findViewById(R.id.progress);
        activateScanCardIcon.setOnClickListener(v -> scanCard());

        Intent intent = getIntent();
        if (getIntent() != null) {
            screeningObject = Parcels.unwrap(intent.getParcelableExtra(MovieActivity.SCREENING));
            selectedShowTime = getIntent().getStringExtra(MovieActivity.SHOWTIME);
        }


        activateXOut.setOnClickListener(v -> {
            Intent closeIntent = new Intent(ActivateMoviePassCard.this, MoviesActivity.class);
            startActivity(closeIntent);
        });

        activateManualInput.setOnClickListener(v -> {
            fadeOut(activateScanCardIcon);
            activateScanCardIcon.setVisibility(View.GONE);
            fadeOut(activateManualInput);
            activateManualInput.setVisibility(View.GONE);


            fadeIn(activateSubmitButton);
            activateSubmitButton.setVisibility(View.VISIBLE);
            fadeIn(activateDigits);
            activateDigits.setVisibility(View.VISIBLE);
        });

//CODE FOR TEST
//        activateSubmitButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(ActivateMoviePassCard.this, ActivatedCard_TutorialActivity.class);
//                startActivity(intent);
//            }
//        });


        activateSubmitButton.setOnClickListener(v -> {
            progress.setVisibility(View.VISIBLE);
            digits = activateDigits.getText().toString().trim();
            final CardActivationRequest request = new CardActivationRequest(digits);
            RestClient.getAuthenticated().activateCard(request).enqueue(new Callback<CardActivationResponse>() {
                @Override
                public void onResponse(Call<CardActivationResponse> call, Response<CardActivationResponse> response) {
                    CardActivationResponse cardActivationResponse = response.body();
                    if (cardActivationResponse != null && response.isSuccessful()) {
                        progress.setVisibility(View.GONE);
                        Intent intent = new Intent(ActivateMoviePassCard.this, ActivatedCard_TutorialActivity.class);
                        intent.putExtra(MovieActivity.SCREENING, Parcels.wrap(screeningObject));
                        intent.putExtra(MovieActivity.SHOWTIME, selectedShowTime);
                        startActivity(intent);
                    } else {
                        progress.setVisibility(View.GONE);
                        Toast.makeText(ActivateMoviePassCard.this, response.message(), Toast.LENGTH_SHORT).show();

                    }
                }

                @Override
                public void onFailure(Call<CardActivationResponse> call, Throwable t) {
                    progress.setVisibility(View.GONE);
                    Toast.makeText(ActivateMoviePassCard.this, "Server Error. Try again later", Toast.LENGTH_SHORT).show();


                }
            });
        });


    }


    @TargetApi(Build.VERSION_CODES.M)
    public void scanCard() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(CAMERA_PERMISSIONS, REQUEST_CAMERA_CODE);

            Intent scanIntent = new Intent(ActivateMoviePassCard.this, CardIOActivity.class);
            scanIntent.putExtra(CardIOActivity.EXTRA_UNBLUR_DIGITS, 4);
            startActivityForResult(scanIntent, Constants.CARD_SCAN_REQUEST_CODE);
        } else {
            Intent scanIntent = new Intent(ActivateMoviePassCard.this, CardIOActivity.class);
            scanIntent.putExtra(CardIOActivity.EXTRA_UNBLUR_DIGITS, 4);
            startActivityForResult(scanIntent, Constants.CARD_SCAN_REQUEST_CODE);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.CARD_SCAN_REQUEST_CODE) {
            if (data != null && data.hasExtra(CardIOActivity.EXTRA_SCAN_RESULT)) {
                final CreditCard scanResult = data.getParcelableExtra(CardIOActivity.EXTRA_SCAN_RESULT);

                activateScanCardIcon.setVisibility(View.GONE);
                activateManualInput.setVisibility(View.GONE);
                activateSubmitButton.setVisibility(View.VISIBLE);
                activateDigits.setVisibility(View.VISIBLE);
                activateDigits.setText(scanResult.getLastFourDigitsOfCardNumber());


                activateSubmitButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        digits = activateDigits.getText().toString();

                        CardActivationRequest request = new CardActivationRequest(digits);
                        RestClient.getAuthenticated().activateCard(request).enqueue(new Callback<CardActivationResponse>() {
                            @Override
                            public void onResponse(Call<CardActivationResponse> call, Response<CardActivationResponse> response) {
                                CardActivationResponse cardActivationResponse = response.body();
                                if (cardActivationResponse != null && response.isSuccessful()) {
                                    Intent intent = new Intent(ActivateMoviePassCard.this, MoviesActivity.class);
                                    startActivity(intent);
                                } else {
                                    Snackbar.make(findViewById(R.id.ACTIVATE), "Incorrect card number", Snackbar.LENGTH_LONG);
                                }
                            }

                            @Override
                            public void onFailure(Call<CardActivationResponse> call, Throwable t) {
                                progress.setVisibility(View.GONE);
                                Snackbar.make(findViewById(R.id.ACTIVATE), "Server Error; Please try again. ", Snackbar.LENGTH_LONG);


                            }
                        });

                    }
                });
            }
        }
    }

    @Override
    public void onBackPressed() {
    }

    public void fadeIn(View view) {
        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new DecelerateInterpolator()); //add this
        fadeIn.setDuration(1000);

        AnimationSet animation = new AnimationSet(false); //change to false
        animation.addAnimation(fadeIn);
        view.setAnimation(animation);

    }

    public void fadeOut(View view) {
        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new DecelerateInterpolator()); //add this
        fadeOut.setDuration(1000);

        AnimationSet animation = new AnimationSet(false); //change to false
        animation.addAnimation(fadeOut);
        view.setAnimation(animation);
    }


}
