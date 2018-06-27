package com.mobile.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import com.helpshift.support.Log;
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
import com.moviepass.R;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TicketVerification_NoStub extends AppCompatActivity {

    ImageView closeOut;
    TextView submit;
    EditText noStubReason;
    int reservationID;
    View progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_ticket_verification__no_stub);

        closeOut = findViewById(R.id.closeOut);
        submit = findViewById(R.id.SubmitStub);
        noStubReason = findViewById(R.id.NoStubComments);
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
                    UserPreferences.saveLastReservationPopInfo(reservationID);
                }
            }

            @Override
            public void onFailure(Call<VerificationLostResponse> call, Throwable t) {

            }
        });
    }


    public void displayWarning() {
        AlertDialog.Builder alert = new AlertDialog.Builder(TicketVerification_NoStub.this, R.style.CUSTOM_ALERT);
        alert.setTitle(R.string.activity_verification_lost_ticket_title_post);
        alert.setMessage(R.string.activity_verification_lost_ticket_message_post);
        alert.setPositiveButton(android.R.string.ok, (dialog, which) -> {
            Intent intent = new Intent(TicketVerification_NoStub.this, HomeActivity.class);
            startActivity(intent);
        });
        alert.show();
    }
}
