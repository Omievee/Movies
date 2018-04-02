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

    TextView alertTitle, alertBody;
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
        dismissable = getIntent().getBooleanExtra("dismissable", true);


        alertTitle = findViewById(R.id.alertTitle);
        alertBody = findViewById(R.id.alertMessage);
        close = findViewById(R.id.dismissAlert);
        alertClickMessage = findViewById(R.id.alertClickMessage);


//        if (dismissable) {
//            alertClickMessage.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    finish();
//                }
//            });
//        } else {
//            alertClickMessage.setVisibility(View.INVISIBLE);
//        }



        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


    }

    @Override
    public void onBackPressed() {
    }
}
