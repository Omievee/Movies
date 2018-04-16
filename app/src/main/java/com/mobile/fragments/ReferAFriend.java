package com.mobile.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import com.moviepass.R;


public class ReferAFriend extends android.app.Fragment {
    // TODO: Rename parameter arguments, choose names that match

    // TODO: Rename and change types of parameters
    Activity myActivity;
    Context myContext;

    ImageView twitter, facebok;
    EditText firstName, lastName, email;

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
        firstName = view.findViewById(R.id.ReferName);
        lastName = view.findViewById(R.id.ReferLast);
        email = view.findViewById(R.id.ReferEmail);

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


    }
}
