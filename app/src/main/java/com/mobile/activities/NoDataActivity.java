package com.mobile.activities;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.moviepass.R;


/**
 * Created by anubis on 9/1/17.
 */

public class NoDataActivity extends AppCompatActivity {

    Button buttonReconnect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_no_data);

        buttonReconnect = findViewById(R.id.button_reconnect);

        buttonReconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isOnline()) {
                    finish();
                    /* Intent intent = new Intent(NoDataActivity.this, TheatersActivity.class);
                    startActivity(intent); */
                } else {
                    Toast.makeText(NoDataActivity.this, "Sorry, still unable to connect to the internet.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public boolean isOnline() {
        final ConnectivityManager connectivityManager = ((ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE));
        NetworkInfo nInfo = connectivityManager.getActiveNetworkInfo();
        if (nInfo!=null && nInfo.isConnectedOrConnecting()){
            return true;
        } else {
            return false;
        }
    }

}
