package com.moviepass.activities;

import android.content.Intent;
import android.graphics.Paint;
import android.support.design.widget.Snackbar;
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

import com.moviepass.Constants;
import com.moviepass.R;
import com.moviepass.network.RestClient;
import com.moviepass.requests.CardActivationRequest;
import com.moviepass.responses.CardActivationResponse;

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
        activateScanCardIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanCard();
            }
        });

        activateXOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ActivateMoviePassCard.this, MoviesActivity.class);
                startActivity(intent);
            }
        });

        activateManualInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fadeOut(activateScanCardIcon);
                activateScanCardIcon.setVisibility(View.GONE);
                fadeOut(activateManualInput);
                activateManualInput.setVisibility(View.GONE);


                fadeIn(activateSubmitButton);
                activateSubmitButton.setVisibility(View.VISIBLE);
                fadeIn(activateDigits);
                activateDigits.setVisibility(View.VISIBLE);
            }
        });
        Log.d(Constants.TAG, "onCreate: ");


        activateSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                progress.setVisibility(View.VISIBLE);
                digits = activateDigits.getText().toString();

                final CardActivationRequest request = new CardActivationRequest(digits);
                Log.d(Constants.TAG, "onClick: " + digits);
                RestClient.getAuthenticated().activateCard(request).enqueue(new Callback<CardActivationResponse>() {
                    @Override
                    public void onResponse(Call<CardActivationResponse> call, Response<CardActivationResponse> response) {
                        CardActivationResponse cardActivationResponse = response.body();

                        Log.d(Constants.TAG, "onResponse: " + request.toString());
                        if (cardActivationResponse != null && response.isSuccessful()) {
                            progress.setVisibility(View.GONE);

                            Intent intent = new Intent(ActivateMoviePassCard.this, ActivatedCard_TutorialActivity.class);
                            startActivity(intent);


                        } else {
                            progress.setVisibility(View.GONE);
                            Log.d(Constants.TAG, "fail: ");
                            Toast.makeText(ActivateMoviePassCard.this, "Incorrect card number", Toast.LENGTH_SHORT).show();

                        }
                    }

                    @Override
                    public void onFailure(Call<CardActivationResponse> call, Throwable t) {
                        progress.setVisibility(View.GONE);
                        Toast.makeText(ActivateMoviePassCard.this, "Server Error. Try again later", Toast.LENGTH_SHORT).show();


                    }
                });
            }
        });


    }


    public void scanCard() {
        Intent scanIntent = new Intent(ActivateMoviePassCard.this, CardIOActivity.class);

        scanIntent.putExtra(CardIOActivity.EXTRA_UNBLUR_DIGITS, 4);

        startActivityForResult(scanIntent, Constants.CARD_SCAN_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.CARD_SCAN_REQUEST_CODE) {
            Log.d(Constants.TAG, "this did: ");

            if (data != null && data.hasExtra(CardIOActivity.EXTRA_SCAN_RESULT)) {
                final CreditCard scanResult = data.getParcelableExtra(CardIOActivity.EXTRA_SCAN_RESULT);

                Log.d(Constants.TAG, "made it: ");

                activateScanCardIcon.setVisibility(View.GONE);
                activateManualInput.setVisibility(View.GONE);
                activateSubmitButton.setVisibility(View.VISIBLE);
                activateDigits.setVisibility(View.VISIBLE);
                activateDigits.setText(scanResult.getLastFourDigitsOfCardNumber());


                activateSubmitButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        CardActivationRequest request = new CardActivationRequest(digits);
                        RestClient.getAuthenticated().activateCard(request).enqueue(new Callback<CardActivationResponse>() {
                            @Override
                            public void onResponse(Call<CardActivationResponse> call, Response<CardActivationResponse> response) {
                                CardActivationResponse cardActivationResponse = response.body();

                                if (cardActivationResponse != null && response.isSuccessful()) {

                                    Intent intent = new Intent(ActivateMoviePassCard.this, MoviesActivity.class);
                                    Toast.makeText(ActivateMoviePassCard.this, "Card Activated!", Toast.LENGTH_SHORT).show();

                                    startActivity(intent);
                                } else {
                                    Snackbar.make(findViewById(R.id.ACTIVATE), "Incorrect card number", Snackbar.LENGTH_LONG);
                                }
                            }

                            @Override
                            public void onFailure(Call<CardActivationResponse> call, Throwable t) {
                                progress.setVisibility(View.GONE);
                                Snackbar.make(findViewById(R.id.ACTIVATE), "Incorrect card number", Snackbar.LENGTH_LONG);


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
