package com.mobile.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.SpannableString;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.moviepass.R;

public class AlertActivity extends AppCompatActivity {

    String alertHeaderString, alertMessageString, alertURLTitle, alertURLlink;
    boolean dismissable;

    TextView alertTitle, alertBody, linkText;
    ImageView close;
    FrameLayout alertClickMessage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_alert);

        Intent intent = getIntent();

        alertHeaderString = getIntent().getStringExtra("title");
        alertMessageString = getIntent().getStringExtra("body");
        alertURLTitle = getIntent().getStringExtra("urlTitle");
        alertURLlink = getIntent().getStringExtra("url");
        dismissable = getIntent().getBooleanExtra("dismissible", true);

        linkText = findViewById(R.id.LinkText);
        alertTitle = findViewById(R.id.alertTitle);
        alertBody = findViewById(R.id.alertMessage);
        close = findViewById(R.id.dismissAlert);
        alertClickMessage = findViewById(R.id.alertClickMessage);


        if (dismissable) {
            close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        } else {
            close.setVisibility(View.INVISIBLE);
        }


        if(alertURLTitle == null || alertURLlink == null) {
            alertClickMessage.setVisibility(View.INVISIBLE);
        }

        alertTitle.setText(alertHeaderString);
        alertBody.setText(alertMessageString);
        linkText.setText(alertURLTitle);


    }

    @Override
    public void onBackPressed() {
    }
}
