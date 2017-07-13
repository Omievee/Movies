package com.moviepass.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.moviepass.R;
import com.moviepass.activities.SignUpActivity;

/**
 * Created by anubis on 7/11/17.
 */

public class SignUpStepOneFragment extends Fragment {

    EditText mZip;
    Button mNext;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_sign_up_step_one, container, false);

        mZip = rootView.findViewById(R.id.et_zip);

        mNext = getActivity().findViewById(R.id.button_next);

        mNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String zip = mZip.getText().toString();

                if (zip.length() == 5) {
                    ((SignUpActivity) getActivity()).setZip(zip);
                }
            }
        });

        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    public static SignUpStepOneFragment newInstance(String text) {

        SignUpStepOneFragment f = new SignUpStepOneFragment();
        Bundle b = new Bundle();
        b.putString("msg", text);

        f.setArguments(b);

        return f;
    }

}
