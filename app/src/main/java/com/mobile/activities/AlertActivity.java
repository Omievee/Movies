package com.mobile.activities;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.mobile.Constants;
import com.mobile.UserPreferences;
import com.moviepass.R;

public class AlertActivity extends AppCompatActivity {

    String alertHeaderString, alertMessageString, alertURLTitle, alertURLlink, alertId;
    boolean dismissable;

    TextView alertTitle, alertBody, linkText;
    ImageView close;
    FrameLayout alertClickMessage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_alert);

//        Blurry.with(this).onto();

        Intent intent = getIntent();

        alertHeaderString = getIntent().getStringExtra("title");
        alertMessageString = getIntent().getStringExtra("body");
        alertURLTitle = getIntent().getStringExtra("urlTitle");
        alertURLlink = getIntent().getStringExtra("url");
        dismissable = getIntent().getBooleanExtra("dismissible", true);
        alertId = getIntent().getStringExtra("id");


        linkText = findViewById(R.id.LinkText);
        alertTitle = findViewById(R.id.alertTitle);
        alertBody = findViewById(R.id.alertMessage);
        close = findViewById(R.id.dismissAlert);
        alertClickMessage = findViewById(R.id.alertClickMessage);


        Log.d(Constants.TAG, "ALERT ID!!!!!--------------->: " + alertId);


        alertTitle.setText(alertHeaderString);
        alertBody.setText(alertMessageString);


        if (dismissable) {
            close.setOnClickListener(v -> finish());
            UserPreferences.setAlertDisplayedId(alertId);
        } else {
            close.setVisibility(View.INVISIBLE);
        }


        if (alertURLTitle == null || alertURLlink == null) {
            alertClickMessage.setVisibility(View.INVISIBLE);
        } else {
            linkText.setText(alertURLTitle);
            alertClickMessage.setOnClickListener(v -> {
                Intent alertIntentClick = new Intent(Intent.ACTION_VIEW, Uri.parse(alertURLlink));
                startActivity(alertIntentClick);
            });
        }
    }

    @Override
    public void onBackPressed() {
    }
}
