package com.mobile.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.mobile.Constants;
import com.mobile.UserPreferences;
import com.mobile.activities.MoviesActivity;
import com.mobile.activities.ProfileActivity;
import com.moviepass.R;

import jp.wasabeef.blurry.Blurry;


public class AlertScreenFragment extends Fragment {

    TextView alertTitle, alertBody, linkText;
    ImageView close;
    FrameLayout alertClickMessage;

    Context myContext;
    Activity myActivity;
    private String id, title, body, url, urlTitle;
    private boolean dismissible;

    private onAlertClickListener mListener;

    public AlertScreenFragment() {
        // Required empty public constructor
    }


    // TODO: Rename and change types and number of parameters
    public static AlertScreenFragment newInstance(String alertId, String alertTitle, String alertBody, String alertUrl, String alertUrlTitle, boolean dismiss) {
        AlertScreenFragment fragment = new AlertScreenFragment();
        Bundle args = new Bundle();

        args.putString("id", alertId);
        args.putString("title", alertTitle);
        args.putString("body", alertBody);
        args.putString("url", alertUrl);
        args.putString("urlTitle", alertUrlTitle);
        args.putBoolean("dismissible", dismiss);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            id = getArguments().getString("id");
            title = getArguments().getString("title");
            body = getArguments().getString("body");
            url = getArguments().getString("url");
            urlTitle = getArguments().getString("urlTitle");
            dismissible = getArguments().getBoolean("dismissible");
        }
        myActivity.startPostponedEnterTransition();
        setSharedElementEnterTransition(TransitionInflater.from(myActivity).inflateTransition(android.R.transition.fade).setDuration(20000));
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fr_alert_screen, container, false);


        ((MoviesActivity) myActivity).CONTAIN = container;
        Blurry.with(myActivity).radius(35).sampling(5).animate().async().onto(((MoviesActivity) myActivity).CONTAIN);


        Log.d(Constants.TAG, "onCreateView: " + ((MoviesActivity) myActivity).getSupportFragmentManager().getBackStackEntryCount());


        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        linkText = view.findViewById(R.id.LinkText);
        alertTitle = view.findViewById(R.id.alertTitle);
        alertBody = view.findViewById(R.id.alertMessage);
        close = view.findViewById(R.id.dismissAlert);
        alertClickMessage = view.findViewById(R.id.alertClickMessage);

        alertTitle.setText(title);
        alertBody.setText(body);


        if (dismissible) {
            close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    onButtonPressed(id);
                }
            });
        } else {
            close.setVisibility(View.INVISIBLE);
        }


        if (urlTitle == null || url == null) {
            alertClickMessage.setVisibility(View.INVISIBLE);
        } else {
            linkText.setText(urlTitle);
            alertClickMessage.setOnClickListener(v -> {
                Intent alertIntentClick = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(alertIntentClick);
            });
        }


    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(String alertId) {
        if (mListener != null) {
            mListener.onAlertClickListener(alertId);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof onAlertClickListener) {
            mListener = (onAlertClickListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement onAlertClickListener");
        }
        myContext = context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    public interface onAlertClickListener {
        // TODO: Update argument type and name
        void onAlertClickListener(String alertId);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        myActivity = activity;
    }
}
