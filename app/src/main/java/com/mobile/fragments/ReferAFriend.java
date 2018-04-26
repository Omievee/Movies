package com.mobile.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.mobile.Constants;
import com.mobile.network.RestClient;
import com.mobile.responses.ReferAFriendResponse;
import com.moviepass.R;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ReferAFriend extends android.app.Fragment {
    // TODO: Rename parameter arguments, choose names that match

    // TODO: Rename and change types of parameters
    Activity myActivity;
    Context myContext;
    Button submitReferralButton;
    ImageView twitter, facebok;
    TextInputEditText firstName, lastName, email;
    String friendEmail;

    public ReferAFriend() {
        // Required empty public constructor
    }


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fr_refer_a_friend, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        twitter = view.findViewById(R.id.TwitterRefer);
        facebok = view.findViewById(R.id.FacebookRefer);
        firstName = view.findViewById(R.id.RF);
        lastName = view.findViewById(R.id.RL);
        email = view.findViewById(R.id.RE);
        submitReferralButton = view.findViewById(R.id.ReferSubmit);


        submitReferralButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!email.getText().toString().contains(".com") || firstName.getText().toString().isEmpty() || lastName.getText().toString().isEmpty()) {
                    Toast.makeText(myActivity, "Please fill out all fields", Toast.LENGTH_SHORT).show();
                } else {
                    friendEmail = email.getText().toString();
                    submitReferral();

                }

            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        myContext = context;
    }

    @Override
    public void onDetach() {
        super.onDetach();

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        myActivity = activity;
    }


    void submitReferral() {

        RestClient.getAuthenticated().referAFriend().enqueue(new Callback<ReferAFriendResponse>() {
            @Override
            public void onResponse(Call<ReferAFriendResponse> call, Response<ReferAFriendResponse> response) {
                ReferAFriendResponse referral = response.body();
                if (response.isSuccessful()) {

                    Log.d(Constants.TAG, "onResponse: " + friendEmail);
                    Intent emailIntent = new Intent(Intent.ACTION_SEND);
                    emailIntent.setData(Uri.parse("mailto:"));
                    emailIntent.setType("message/rfc822");
                    emailIntent.setType("text/plain");
                    emailIntent.putExtra(Intent.EXTRA_SUBJECT, referral.getEmailSubject());
                    emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{friendEmail});
                    emailIntent.putExtra(Intent.EXTRA_TEXT, "Hey " + firstName.getText().toString() + " " + lastName.getText().toString() + ", \n \n" + referral.getEmailMessage());
                    startActivity(emailIntent);
                }
            }

            @Override
            public void onFailure(Call<ReferAFriendResponse> call, Throwable t) {

            }
        });

    }
}
