package com.mobile.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.mobile.Constants;
import com.mobile.UserPreferences;
import com.mobile.model.Alert;
import com.moviepass.R;

public class AlertScreenFragment extends Fragment {

    TextView alertTitle, alertBody, linkText;
    ImageView close;
    FrameLayout alertClickMessage;

    Context myContext;

    private Alert alertObject;

    public static AlertScreenFragment newInstance(Alert alert) {
        AlertScreenFragment fragment = new AlertScreenFragment();
        Bundle args = new Bundle();

        args.putParcelable("alert", alert);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        alertObject = getArguments().getParcelable("alert");
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fr_alert_screen, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        linkText = view.findViewById(R.id.LinkText);
        alertTitle = view.findViewById(R.id.alertTitle);
        alertBody = view.findViewById(R.id.alertMessage);
        close = view.findViewById(R.id.dismissAlert);
        alertClickMessage = view.findViewById(R.id.alertClickMessage);

        alertTitle.setText(alertObject.getTitle());
        alertBody.setText(alertObject.getBody());


        Log.d(Constants.TAG, "onViewCreated: " + alertObject.getDismissible());

        if (alertObject.getDismissible()) {
            close.setOnClickListener(v -> {
                UserPreferences.setAlertDisplayedId(alertObject.getId());
                getActivity().onBackPressed();
            });
        } else {
            close.setVisibility(View.INVISIBLE);
        }


        if (TextUtils.isEmpty(alertObject.getUrlTitle()) || TextUtils.isEmpty(alertObject.getUrl())) {
            alertClickMessage.setVisibility(View.INVISIBLE);
        } else {
            linkText.setText(alertObject.getUrlTitle());
            alertClickMessage.setOnClickListener(v -> {
                Intent alertIntentClick = new Intent(Intent.ACTION_VIEW, Uri.parse(alertObject.getUrl()));
                startActivity(alertIntentClick);
            });
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (alertObject.getDismissible()) {
            UserPreferences.setAlertDisplayedId(alertObject.getId());
        }
    }
}


//
//
//        if (urlTitle == null && url == null) {
//                alertClickMessage.setVisibility(View.INVISIBLE);
//                } else {
//                linkText.setText(urlTitle);
//                alertClickMessage.setOnClickListener(v -> {
//                if (Patterns.WEB_URL.matcher(url).matches()) {
//                Intent alertIntentClick = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
//                startActivity(alertIntentClick);
//                } else {
//                onButtonPressed(id);
//                }
//
//                });
//                }