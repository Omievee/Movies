package com.mobile.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import com.helpshift.support.Log;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mobile.Constants;
import com.mobile.UserPreferences;
import com.mobile.home.HomeActivity;
import com.mobile.network.RestClient;
import com.mobile.requests.VerificationLostRequest;
import com.mobile.responses.VerificationLostResponse;
import com.mobile.widgets.MPAlertDialog;
import com.moviepass.R;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TicketVerification_NoStub extends AppCompatActivity {

    ImageView closeOut;
    TextView submit, counter;
    EditText noStubReason;
    int reservationID;
    View progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_ticket_verification__no_stub);

        closeOut = findViewById(R.id.closeOut);
        submit = findViewById(R.id.SubmitStub);
        counter = findViewById(R.id.charactersCounter);
        noStubReason = findViewById(R.id.noStubMessage);
        progress = findViewById(R.id.progress);
        Intent badExcuses = getIntent();
        submit.setOnClickListener(v -> {
            if (noStubReason.getText().toString().equals("")) {
                Toast.makeText(v.getContext(), "Please enter a reason", Toast.LENGTH_SHORT).show();
            } else {
                if (badExcuses.getExtras() != null) {
                    reservationID = badExcuses.getIntExtra(Constants.SCREENING, 0);
                    progress.setVisibility(View.VISIBLE);
                    submitNoTicket();
                }
            }
        });


        closeOut.setOnClickListener(v -> {
            finish();
        });

        noStubReason.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                counter.setText(noStubReason.getText().toString().length()+"/250");
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    public void submitNoTicket() {
        String badReason = noStubReason.getText().toString();
        VerificationLostRequest lostTicket = new VerificationLostRequest(badReason);

        RestClient.getAuthenticated().lostTicket(reservationID, lostTicket).enqueue(new Callback<VerificationLostResponse>() {
            @Override
            public void onResponse(Call<VerificationLostResponse> call, Response<VerificationLostResponse> response) {
                VerificationLostResponse lostResponse = response.body();
                if (lostResponse != null) {
                    progress.setVisibility(View.GONE);
                    displayWarning();
                    UserPreferences.INSTANCE.saveLastReservationPopInfo(reservationID);
                }
            }

            @Override
            public void onFailure(Call<VerificationLostResponse> call, Throwable t) {

            }
        });
    }


    public void displayWarning() {
        new MPAlertDialog(TicketVerification_NoStub.this)
        .setTitle(R.string.activity_verification_lost_ticket_title_post)
        .setMessage(R.string.activity_verification_lost_ticket_message_post)
        .setPositiveButton(android.R.string.ok, (dialog, which) -> {
            Intent intent = new Intent(TicketVerification_NoStub.this, HomeActivity.class);
            startActivity(intent);
        }).show();
    }
}
