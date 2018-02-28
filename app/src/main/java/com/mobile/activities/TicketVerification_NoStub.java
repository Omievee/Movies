package com.mobile.activities;

import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.moviepass.R;

public class TicketVerification_NoStub extends AppCompatActivity {

    ImageView closeOut;
    Button submit;
    EditText noStubReason;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_ticket_verification__no_stub);

        closeOut = findViewById(R.id.closeOut);
        submit = findViewById(R.id.SubmitStub);
        noStubReason = findViewById(R.id.NoStubComments);


        closeOut.setOnClickListener(v -> {
            finish();
        });


    }
}
