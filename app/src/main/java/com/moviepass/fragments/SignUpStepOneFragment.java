package com.moviepass.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.moviepass.R;
import com.moviepass.activities.SignUpActivity;
import com.moviepass.model.Plan;
import com.moviepass.network.RestClient;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by anubis on 7/11/17.
 */

public class SignUpStepOneFragment extends Fragment {

    RelativeLayout mRelativeLayout;
    EditText mZip;
    Button mNext;

    private boolean isViewShown = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_sign_up_step_one, container, false);

        mRelativeLayout = rootView.findViewById(R.id.relative_layout);
        mZip = rootView.findViewById(R.id.et_zip);
        mNext = getActivity().findViewById(R.id.button_next);

        if (!isViewShown) {
            setButtonActions();
        }

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

    public void checkZip(String zip) {
        final String finalZip = zip;

        RestClient.getAuthenticated().getPlans(zip).enqueue(new Callback<Plan>() {
            @Override
            public void onResponse(Call<Plan> call, Response<Plan> response) {
                if (response.isSuccessful()) {
                    ((SignUpActivity) getActivity()).setZip(finalZip);
                } else {
                    try {
                        JSONObject jObjError = new JSONObject(response.errorBody().string());
                        Log.d("jObjError", "jObjError: " + jObjError.getString("errors"));

                        final Snackbar snackbar = Snackbar.make(mRelativeLayout, jObjError.getString("errors"), Snackbar.LENGTH_INDEFINITE);
                        snackbar.setAction("OK", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                snackbar.dismiss();
                            }
                        });
                        snackbar.show();
                    } catch (Exception e) {
                        final Snackbar snackbar = Snackbar.make(mRelativeLayout, e.getMessage(), Snackbar.LENGTH_LONG);
                        snackbar.setAction("OK", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                snackbar.dismiss();
                            }
                        });
                        snackbar.show();
                    }
                }
            }

            @Override
            public void onFailure(Call<Plan> call, Throwable t) {

            }
        });
    }

    // This is for actions only available when SignUpIneFrag is visible
    @Override
    public void setMenuVisibility(final boolean visible) {
        super.setMenuVisibility(visible);

        if (getView() != null) {
            if (visible) {
                isViewShown = true;

                setButtonActions();
            } else {
                isViewShown = false;
            }
        }
    }

    private void setButtonActions() {
        mNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String zip = mZip.getText().toString();

                if (zip.length() == 5) {
                    checkZip(zip);
                } else {
                    final Snackbar snackbar = Snackbar.make(mRelativeLayout, R.string.fragment_sign_up_step_one_enter_valid_zip, Snackbar.LENGTH_INDEFINITE);
                    snackbar.setAction("OK", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            snackbar.dismiss();
                        }
                    });
                    snackbar.show();
                }
            }
        });
    }

}
